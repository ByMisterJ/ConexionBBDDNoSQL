package repository;

import modelos.hogwarts.Student;
import provider.DynamoDbClientProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of StudentRepository using DynamoDB Enhanced Client.
 */
public class StudentRepositoryImpl implements StudentRepository {
    
    private static final String TABLE_NAME = "Students";
    private static final String HOUSE_INDEX_NAME = "house-index";
    
    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<Student> studentTable;

    /**
     * Creates a new StudentRepositoryImpl instance.
     */
    public StudentRepositoryImpl() {
        this.dynamoDbClient = DynamoDbClientProvider.getDynamoDbClient();
        this.enhancedClient = DynamoDbClientProvider.getEnhancedClient();
        this.studentTable = enhancedClient.table(TABLE_NAME, TableSchema.fromBean(Student.class));
    }

    @Override
    public Student createStudent(Student student) {
        studentTable.putItem(student);
        System.out.println("Student created: " + student.getFirstName() + " " + student.getLastName());
        return student;
    }

    @Override
    public Optional<Student> getStudentById(String id) {
        Key key = Key.builder()
                .partitionValue(id)
                .build();
        
        Student student = studentTable.getItem(r -> r.key(key));
        if (student != null) {
            System.out.println("Student found: " + student.getFirstName() + " " + student.getLastName());
        } else {
            System.out.println("Student not found with id: " + id);
        }
        return Optional.ofNullable(student);
    }

    @Override
    public Student updateStudent(Student student) {
        // Verify student exists before updating
        if (getStudentById(student.getId()).isPresent()) {
            studentTable.updateItem(student);
            System.out.println("Student updated: " + student.getFirstName() + " " + student.getLastName());
            return student;
        } else {
            throw new IllegalArgumentException("Student does not exist with id: " + student.getId());
        }
    }

    @Override
    public void deleteStudent(String id) {
        Key key = Key.builder()
                .partitionValue(id)
                .build();
        
        studentTable.deleteItem(r -> r.key(key));
        System.out.println("Student deleted with id: " + id);
    }

    @Override
    public List<Student> listStudents() {
        List<Student> students = new ArrayList<>();
        ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder().build();
        PageIterable<Student> pages = studentTable.scan(scanRequest);
        
        pages.items().forEach(students::add);
        System.out.println("Found " + students.size() + " students");
        return students;
    }

    @Override
    public List<Student> findByHouse(String house) {
        List<Student> students = new ArrayList<>();
        
        try {
            // Query using the secondary index
            QueryConditional queryConditional = QueryConditional
                    .keyEqualTo(Key.builder().partitionValue(house).build());
            
            QueryEnhancedRequest queryRequest = QueryEnhancedRequest.builder()
                    .queryConditional(queryConditional)
                    .build();
            
            // Get the index and query it
            studentTable.index(HOUSE_INDEX_NAME).query(queryRequest)
                    .forEach(page -> page.items().forEach(students::add));
            
            System.out.println("Found " + students.size() + " students in house: " + house);
        } catch (Exception e) {
            System.err.println("Error querying by house (index may not exist): " + e.getMessage());
            // Fallback to scan if index doesn't exist
            return listStudents().stream()
                    .filter(s -> house.equals(s.getHouse()))
                    .toList();
        }
        
        return students;
    }

    @Override
    public void createTableIfNotExists() {
        if (tableExists()) {
            System.out.println("Table already exists: " + TABLE_NAME);
            return;
        }
        
        // Create table with GSI for house queries
        CreateTableEnhancedRequest createTableRequest = CreateTableEnhancedRequest.builder()
                .globalSecondaryIndices(
                        EnhancedGlobalSecondaryIndex.builder()
                                .indexName(HOUSE_INDEX_NAME)
                                .projection(Projection.builder()
                                        .projectionType(ProjectionType.ALL)
                                        .build())
                                .provisionedThroughput(ProvisionedThroughput.builder()
                                        .readCapacityUnits(5L)
                                        .writeCapacityUnits(5L)
                                        .build())
                                .build()
                )
                .provisionedThroughput(ProvisionedThroughput.builder()
                        .readCapacityUnits(5L)
                        .writeCapacityUnits(5L)
                        .build())
                .build();
        
        studentTable.createTable(createTableRequest);
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
