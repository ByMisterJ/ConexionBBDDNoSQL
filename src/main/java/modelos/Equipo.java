package modelos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Data
@NoArgsConstructor
@DynamoDbBean
public class Equipo {
    private String nombre;
    private String nacionalidad;
    private List<Piloto> pilotos;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("name")
    public String getNombre() {
        return nombre;
    }

    @DynamoDbAttribute("nationality")
    public String getNacionalidad() {
        return nacionalidad;
    }

    @DynamoDbAttribute("drivers")
    public List<Piloto> getPilotos() {
        return pilotos;
    }
}
