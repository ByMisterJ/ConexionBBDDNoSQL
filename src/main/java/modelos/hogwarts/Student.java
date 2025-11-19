package modelos.hogwarts;

import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

/**
 * Represents a student at Hogwarts School of Witchcraft and Wizardry.
 * This class is annotated for use with DynamoDB Enhanced Client.
 */
@Data
@NoArgsConstructor
@DynamoDbBean
public class Student {
    private String id;
    private String firstName;
    private String lastName;
    private String house;
    private Integer year;
    private String patronus;

    /**
     * Gets the student ID (partition key).
     * @return the student ID
     */
    @DynamoDbPartitionKey
    @DynamoDbAttribute("id")
    public String getId() {
        return id;
    }

    /**
     * Gets the student's first name.
     * @return the first name
     */
    @DynamoDbAttribute("firstName")
    public String getFirstName() {
        return firstName;
    }

    /**
     * Gets the student's last name.
     * @return the last name
     */
    @DynamoDbAttribute("lastName")
    public String getLastName() {
        return lastName;
    }

    /**
     * Gets the student's house (e.g., Gryffindor, Slytherin, Hufflepuff, Ravenclaw).
     * @return the house name
     */
    @DynamoDbAttribute("house")
    @DynamoDbSecondaryPartitionKey(indexNames = "house-index")
    public String getHouse() {
        return house;
    }

    /**
     * Gets the student's year (1-7).
     * @return the year
     */
    @DynamoDbAttribute("year")
    public Integer getYear() {
        return year;
    }

    /**
     * Gets the student's patronus.
     * @return the patronus
     */
    @DynamoDbAttribute("patronus")
    public String getPatronus() {
        return patronus;
    }
}
