package repository;

import modelos.hogwarts.House;
import provider.DynamoDbClientProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of HouseRepository using DynamoDB Enhanced Client.
 */
public class HouseRepositoryImpl implements HouseRepository {
    
    private static final String TABLE_NAME = "Houses";
    
    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<House> houseTable;

    /**
     * Creates a new HouseRepositoryImpl instance.
     */
    public HouseRepositoryImpl() {
        this.dynamoDbClient = DynamoDbClientProvider.getDynamoDbClient();
        this.enhancedClient = DynamoDbClientProvider.getEnhancedClient();
        this.houseTable = enhancedClient.table(TABLE_NAME, TableSchema.fromBean(House.class));
    }

    @Override
    public House createHouse(House house) {
        houseTable.putItem(house);
        System.out.println("House created: " + house.getName());
        return house;
    }

    @Override
    public Optional<House> getHouseById(String id) {
        Key key = Key.builder()
                .partitionValue(id)
                .build();
        
        House house = houseTable.getItem(r -> r.key(key));
        if (house != null) {
            System.out.println("House found: " + house.getName());
        } else {
            System.out.println("House not found with id: " + id);
        }
        return Optional.ofNullable(house);
    }

    @Override
    public House updateHouse(House house) {
        // Verify house exists before updating
        if (getHouseById(house.getId()).isPresent()) {
            houseTable.updateItem(house);
            System.out.println("House updated: " + house.getName());
            return house;
        } else {
            throw new IllegalArgumentException("House does not exist with id: " + house.getId());
        }
    }

    @Override
    public void deleteHouse(String id) {
        Key key = Key.builder()
                .partitionValue(id)
                .build();
        
        houseTable.deleteItem(r -> r.key(key));
        System.out.println("House deleted with id: " + id);
    }

    @Override
    public List<House> listHouses() {
        List<House> houses = new ArrayList<>();
        ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder().build();
        PageIterable<House> pages = houseTable.scan(scanRequest);
        
        pages.items().forEach(houses::add);
        System.out.println("Found " + houses.size() + " houses");
        return houses;
    }

    @Override
    public void createTableIfNotExists() {
        if (tableExists()) {
            System.out.println("Table already exists: " + TABLE_NAME);
            return;
        }
        
        houseTable.createTable();
        System.out.println("Creating table: " + TABLE_NAME);
        
        // Wait for table to become active
        DynamoDbWaiter waiter = dynamoDbClient.waiter();
        waiter.waitUntilTableExists(DescribeTableRequest.builder()
                .tableName(TABLE_NAME)
                .build());
        
        System.out.println("Table '" + TABLE_NAME + "' created and active");
    }

    @Override
    public void deleteTable() {
        if (!tableExists()) {
            System.out.println("Table does not exist: " + TABLE_NAME);
            return;
        }
        
        dynamoDbClient.deleteTable(DeleteTableRequest.builder()
                .tableName(TABLE_NAME)
                .build());
        System.out.println("Deleting table: " + TABLE_NAME);
        
        // Wait for table to be deleted
        DynamoDbWaiter waiter = dynamoDbClient.waiter();
        waiter.waitUntilTableNotExists(DescribeTableRequest.builder()
                .tableName(TABLE_NAME)
                .build());
        
        System.out.println("Table '" + TABLE_NAME + "' deleted");
    }

    /**
     * Checks if the table exists.
     * @return true if table exists, false otherwise
     */
    private boolean tableExists() {
        try {
            dynamoDbClient.describeTable(DescribeTableRequest.builder()
                    .tableName(TABLE_NAME)
                    .build());
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }
}
