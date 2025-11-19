package repository;

import modelos.hogwarts.Student;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for StudentRepository.
 * These tests require DynamoDB Local running on localhost:8000.
 * 
 * To run DynamoDB Local:
 * docker-compose up -d
 * 
 * Before running tests, set environment variable:
 * export DYNAMODB_ENDPOINT=http://localhost:8000
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StudentRepositoryIntegrationTest {

    private static StudentRepository studentRepository;
    private static String testStudentId;

    @BeforeAll
    static void setUp() {
        // Ensure DYNAMODB_ENDPOINT is set for local testing
        String endpoint = System.getenv("DYNAMODB_ENDPOINT");
        if (endpoint == null || endpoint.isEmpty()) {
            System.setProperty("DYNAMODB_ENDPOINT", "http://localhost:8000");
            System.out.println("Warning: DYNAMODB_ENDPOINT not set, using default: http://localhost:8000");
        }

        studentRepository = new StudentRepositoryImpl();
        studentRepository.createTableIfNotExists();
        
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
        if (studentRepository != null) {
            try {
                studentRepository.deleteTable();
            } catch (Exception e) {
                System.err.println("Error deleting table: " + e.getMessage());
            }
        }
    }

    @Test
    @Order(1)
    void testCreateStudent() {
        Student student = new Student();
        testStudentId = UUID.randomUUID().toString();
        student.setId(testStudentId);
        student.setFirstName("Harry");
        student.setLastName("Potter");
        student.setHouse("Gryffindor");
        student.setYear(1);
        student.setPatronus("Stag");

        Student created = studentRepository.createStudent(student);

        assertNotNull(created);
        assertEquals("Harry", created.getFirstName());
        assertEquals("Potter", created.getLastName());
        assertEquals("Gryffindor", created.getHouse());
        assertEquals(1, created.getYear());
        assertEquals("Stag", created.getPatronus());
    }

    @Test
    @Order(2)
    void testGetStudentById() {
        Optional<Student> result = studentRepository.getStudentById(testStudentId);

        assertTrue(result.isPresent());
        Student student = result.get();
        assertEquals("Harry", student.getFirstName());
        assertEquals("Potter", student.getLastName());
        assertEquals("Gryffindor", student.getHouse());
    }

    @Test
    @Order(3)
    void testUpdateStudent() {
        Optional<Student> result = studentRepository.getStudentById(testStudentId);
        assertTrue(result.isPresent());

        Student student = result.get();
        student.setYear(2);
        student.setPatronus("Phoenix");

        Student updated = studentRepository.updateStudent(student);

        assertEquals(2, updated.getYear());
        assertEquals("Phoenix", updated.getPatronus());

        // Verify the update persisted
        Optional<Student> verified = studentRepository.getStudentById(testStudentId);
        assertTrue(verified.isPresent());
        assertEquals(2, verified.get().getYear());
        assertEquals("Phoenix", verified.get().getPatronus());
    }

    @Test
    @Order(4)
    void testListStudents() {
        // Create additional students
        Student hermione = new Student();
        hermione.setId(UUID.randomUUID().toString());
        hermione.setFirstName("Hermione");
        hermione.setLastName("Granger");
        hermione.setHouse("Gryffindor");
        hermione.setYear(1);
        studentRepository.createStudent(hermione);

        Student draco = new Student();
        draco.setId(UUID.randomUUID().toString());
        draco.setFirstName("Draco");
        draco.setLastName("Malfoy");
        draco.setHouse("Slytherin");
        draco.setYear(1);
        studentRepository.createStudent(draco);

        List<Student> students = studentRepository.listStudents();

        assertNotNull(students);
        assertTrue(students.size() >= 3, "Should have at least 3 students");
    }

    @Test
    @Order(5)
    void testFindByHouse() {
        List<Student> gryffindorStudents = studentRepository.findByHouse("Gryffindor");

        assertNotNull(gryffindorStudents);
        assertTrue(gryffindorStudents.size() >= 2, "Should have at least 2 Gryffindor students");
        gryffindorStudents.forEach(s -> assertEquals("Gryffindor", s.getHouse()));
    }

    @Test
    @Order(6)
    void testDeleteStudent() {
        studentRepository.deleteStudent(testStudentId);

        Optional<Student> result = studentRepository.getStudentById(testStudentId);
        assertFalse(result.isPresent(), "Student should be deleted");
    }

    @Test
    @Order(7)
    void testGetNonExistentStudent() {
        Optional<Student> result = studentRepository.getStudentById("non-existent-id");
        assertFalse(result.isPresent());
    }

    @Test
    @Order(8)
    void testUpdateNonExistentStudent() {
        Student student = new Student();
        student.setId("non-existent-id");
        student.setFirstName("Test");
        student.setLastName("Student");

        assertThrows(IllegalArgumentException.class, () -> {
            studentRepository.updateStudent(student);
        });
    }
}
