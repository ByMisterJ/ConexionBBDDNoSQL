package modelos.hogwarts;

import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

/**
 * Represents a house at Hogwarts School of Witchcraft and Wizardry.
 * This class is annotated for use with DynamoDB Enhanced Client.
 */
@Data
@NoArgsConstructor
@DynamoDbBean
public class House {
    private String id;
    private String name;
    private String founder;
    private String animal;

    /**
     * Gets the house ID (partition key).
     * @return the house ID
     */
    @DynamoDbPartitionKey
    @DynamoDbAttribute("id")
    public String getId() {
        return id;
    }

    /**
     * Gets the house name (e.g., Gryffindor, Slytherin, Hufflepuff, Ravenclaw).
     * @return the house name
     */
    @DynamoDbAttribute("name")
    public String getName() {
        return name;
    }

    /**
     * Gets the house founder's name.
     * @return the founder's name
     */
    @DynamoDbAttribute("founder")
    public String getFounder() {
        return founder;
    }

    /**
     * Gets the house animal/mascot.
     * @return the house animal
     */
    @DynamoDbAttribute("animal")
    public String getAnimal() {
        return animal;
    }
}
