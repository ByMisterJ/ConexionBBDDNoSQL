package modelos;

import lombok.Data;

import java.time.LocalDate;

import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.attribute.*;

@Data
@NoArgsConstructor
@DynamoDbBean
public class Piloto {
    private String nombre;
    private int numero;
    private String fechaNacimiento;
    private String nacionalidad;

    @DynamoDbAttribute("name")
    public String getNombre() {
        return nombre;
    }

    @DynamoDbAttribute("number")
    public int getNumero() {
        return numero;
    }

    @DynamoDbAttribute("nationality")
    public String getNacionalidad() {
        return nacionalidad;
    }

    @DynamoDbAttribute("birthDate")
    public String getFechaNacimiento() {
        return fechaNacimiento;
    }
}
