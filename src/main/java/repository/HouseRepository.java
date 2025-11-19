package repository;

import modelos.hogwarts.House;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for House entities.
 * Provides CRUD operations for Hogwarts houses.
 */
public interface HouseRepository {
    
    /**
     * Creates a new house in the database.
     * @param house the house to create
     * @return the created house
     */
    House createHouse(House house);
    
    /**
     * Retrieves a house by its ID.
     * @param id the house ID
     * @return an Optional containing the house if found, empty otherwise
     */
    Optional<House> getHouseById(String id);
    
    /**
     * Updates an existing house.
     * @param house the house with updated information
     * @return the updated house
     */
    House updateHouse(House house);
    
    /**
     * Deletes a house by its ID.
     * @param id the house ID
     */
    void deleteHouse(String id);
    
    /**
     * Lists all houses in the database.
     * @return list of all houses
     */
    List<House> listHouses();
    
    /**
     * Creates the houses table if it doesn't exist.
     */
    void createTableIfNotExists();
    
    /**
     * Deletes the houses table.
     */
    void deleteTable();
}
