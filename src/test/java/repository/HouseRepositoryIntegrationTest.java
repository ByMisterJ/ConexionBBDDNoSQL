package repository;

import modelos.hogwarts.House;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for HouseRepository.
 * These tests require DynamoDB Local running on localhost:8000.
 * 
 * To run DynamoDB Local:
 * docker-compose up -d
 * 
 * Before running tests, set environment variable:
 * export DYNAMODB_ENDPOINT=http://localhost:8000
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HouseRepositoryIntegrationTest {

    private static HouseRepository houseRepository;
    private static String testHouseId = "gryffindor";

    @BeforeAll
    static void setUp() {
        // Ensure DYNAMODB_ENDPOINT is set for local testing
        String endpoint = System.getenv("DYNAMODB_ENDPOINT");
        if (endpoint == null || endpoint.isEmpty()) {
            System.setProperty("DYNAMODB_ENDPOINT", "http://localhost:8000");
            System.out.println("Warning: DYNAMODB_ENDPOINT not set, using default: http://localhost:8000");
        }

        houseRepository = new HouseRepositoryImpl();
        houseRepository.createTableIfNotExists();
        
        // Wait a bit for table to be ready
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterAll
    static void tearDown() {
        // Clean up - delete the table
        if (houseRepository != null) {
            try {
                houseRepository.deleteTable();
            } catch (Exception e) {
                System.err.println("Error deleting table: " + e.getMessage());
            }
        }
    }

    @Test
    @Order(1)
    void testCreateHouse() {
        House house = new House();
        house.setId(testHouseId);
        house.setName("Gryffindor");
        house.setFounder("Godric Gryffindor");
        house.setAnimal("Lion");

        House created = houseRepository.createHouse(house);

        assertNotNull(created);
        assertEquals("Gryffindor", created.getName());
        assertEquals("Godric Gryffindor", created.getFounder());
        assertEquals("Lion", created.getAnimal());
    }

    @Test
    @Order(2)
    void testGetHouseById() {
        Optional<House> result = houseRepository.getHouseById(testHouseId);

        assertTrue(result.isPresent());
        House house = result.get();
        assertEquals("Gryffindor", house.getName());
        assertEquals("Godric Gryffindor", house.getFounder());
        assertEquals("Lion", house.getAnimal());
    }

    @Test
    @Order(3)
    void testUpdateHouse() {
        Optional<House> result = houseRepository.getHouseById(testHouseId);
        assertTrue(result.isPresent());

        House house = result.get();
        house.setAnimal("Lion (Updated)");

        House updated = houseRepository.updateHouse(house);

        assertEquals("Lion (Updated)", updated.getAnimal());

        // Verify the update persisted
        Optional<House> verified = houseRepository.getHouseById(testHouseId);
        assertTrue(verified.isPresent());
        assertEquals("Lion (Updated)", verified.get().getAnimal());
    }

    @Test
    @Order(4)
    void testListHouses() {
        // Create additional houses
        House slytherin = new House();
        slytherin.setId("slytherin");
        slytherin.setName("Slytherin");
        slytherin.setFounder("Salazar Slytherin");
        slytherin.setAnimal("Serpent");
        houseRepository.createHouse(slytherin);

        House hufflepuff = new House();
        hufflepuff.setId("hufflepuff");
        hufflepuff.setName("Hufflepuff");
        hufflepuff.setFounder("Helga Hufflepuff");
        hufflepuff.setAnimal("Badger");
        houseRepository.createHouse(hufflepuff);

        House ravenclaw = new House();
        ravenclaw.setId("ravenclaw");
        ravenclaw.setName("Ravenclaw");
        ravenclaw.setFounder("Rowena Ravenclaw");
        ravenclaw.setAnimal("Eagle");
        houseRepository.createHouse(ravenclaw);

        List<House> houses = houseRepository.listHouses();

        assertNotNull(houses);
        assertEquals(4, houses.size(), "Should have all 4 Hogwarts houses");
    }

    @Test
    @Order(5)
    void testDeleteHouse() {
        houseRepository.deleteHouse(testHouseId);

        Optional<House> result = houseRepository.getHouseById(testHouseId);
        assertFalse(result.isPresent(), "House should be deleted");
    }

    @Test
    @Order(6)
    void testGetNonExistentHouse() {
        Optional<House> result = houseRepository.getHouseById("non-existent-id");
        assertFalse(result.isPresent());
    }

    @Test
    @Order(7)
    void testUpdateNonExistentHouse() {
        House house = new House();
        house.setId("non-existent-id");
        house.setName("Test House");

        assertThrows(IllegalArgumentException.class, () -> {
            houseRepository.updateHouse(house);
        });
    }
}
