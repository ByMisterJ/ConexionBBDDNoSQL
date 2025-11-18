package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import modelos.Equipo;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class OperacionesDynamoDB {

    // Clientes de DynamoDB para realizar operaciones
    // DynamoDBClient es para operaciones de bajo nivel (necesario para waiters)
    // DynamoDBEnhancedClient es para operaciones de alto nivel
    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;

    public OperacionesDynamoDB() {
        this.dynamoDbClient = DynamoDBManager.getDynamoDbClient();
        this.dynamoDbEnhancedClient = DynamoDBManager.getEnhancedClient();
    }

    public void crearTabla(String nombreTabla) {
        if (tablaExiste(nombreTabla)) {
            System.out.println("La tabla ya existe: " + nombreTabla);
            return;
        }

        // Crear la tabla usando la clase de esquema de la entidad Equipo
        dynamoDbEnhancedClient.table(nombreTabla, TableSchema.fromBean(Equipo.class)).createTable();
        System.out.println("Creando tabla: " + nombreTabla);

        // Esperar hasta que la tabla esté activa
        DynamoDbWaiter waiter = dynamoDbClient.waiter();
        waiter.waitUntilTableExists(DescribeTableRequest.builder().tableName(nombreTabla).build());
        System.out.println("Tabla '" + nombreTabla + "' creada y activa.");
    }

    private boolean tablaExiste (String nombreTabla) {
        try {
            // Intenta describir la tabla. Si no existe, se lanzará una excepción
            dynamoDbClient.describeTable(r -> r.tableName(nombreTabla));
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

    public void poblarEquiposDesdeJson(String fichero) {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        try (Reader reader = Files.newBufferedReader(Path.of(fichero))) {
            List<Equipo> equipos = objectMapper.readValue(reader, new TypeReference<>() {});
            equipos.forEach(this::insertarEquipo);
            System.out.println("Insertados equipos desde JSON");
        } catch (IOException e) {
            System.err.println("Error al leer el archivo JSON: " + e.getMessage());
        }
    }

    public void imprimirTodosLosEquipos() {
        DynamoDbTable<Equipo> tabla = dynamoDbEnhancedClient.table("EquiposF1", TableSchema.fromBean(Equipo.class));
        // Crear una solicitud de escaneo para obtener todos los elementos
        ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder().build();
        // Escanear la tabla y obtener un iterable de páginas de resultados
        PageIterable<Equipo> equipos = tabla.scan(scanRequest);
        // Iterar sobre cada página y luego sobre cada elemento de equipo
        equipos.items().forEach(System.out::println);
    }

    public void insertarEquipo(Equipo equipo) {
        // Obtener la tabla para realizar operaciones
        DynamoDbTable<Equipo> tabla = dynamoDbEnhancedClient.table("EquiposF1", TableSchema.fromBean(Equipo.class));
        // Insertar el equipo en la tabla
        tabla.putItem(equipo);
        System.out.println("Equipo insertado correctamente: " + equipo.getNombre());
    }

    public Equipo obtenerEquipo(String nombreEquipo) {
        DynamoDbTable<Equipo> tabla = dynamoDbEnhancedClient.table("EquiposF1", TableSchema.fromBean(Equipo.class));
        // Crear la clave de búsqueda
        Key key = Key.builder().partitionValue(nombreEquipo).build();
        // Obtener el equipo usando la clave
        Equipo equipo = tabla.getItem(r -> r.key(key));
        if (equipo != null) {
            System.out.println("Equipo encontrado: " + equipo.getNombre());
        } else {
            System.out.println("Equipo no encontrado");
        }
        return equipo;
    }

    public void actualizarEquipo(Equipo equipo) {
        // Primero, verificar si el equipo existe antes de actualizar
        if (obtenerEquipo(equipo.getNombre()) != null) {
            DynamoDbTable<Equipo> tabla = dynamoDbEnhancedClient.table("EquiposF1", TableSchema.fromBean(Equipo.class));
            // Actualizar el equipo en la tabla
            tabla.updateItem(equipo);
            System.out.println("Equipo actualizado: " + equipo.getNombre());
        } else {
            System.out.println("El equipo no existe y no se puede actualizar: " + equipo.getNombre());
        }
    }

    public void borrarEquipo(String nombre) {
        DynamoDbTable<Equipo> tabla = dynamoDbEnhancedClient.table("EquiposF1", TableSchema.fromBean(Equipo.class));
        // Crear la clave del equipo a borrar
        Key key = Key.builder().partitionValue(nombre).build();
        // Borrar el equipo de la tabla
        tabla.deleteItem(r -> r.key(key));
        System.out.println("Equipo borrado: " + nombre);
    }

    public void borrarTabla(String nombreTabla) {
        // Verificar si la tabla existe antes de intentar borrarla
        if (!tablaExiste(nombreTabla)) {
            System.out.println("La tabla no existe: " + nombreTabla);
            return;
        }

        // Solicitud para borrar la tabla
        dynamoDbClient.deleteTable(DeleteTableRequest.builder().tableName(nombreTabla).build());
        System.out.println("Eliminando tabla: " + nombreTabla);

        // Esperar hasta que la tabla ya no exista
        DynamoDbWaiter waiter = dynamoDbClient.waiter();
        waiter.waitUntilTableNotExists(DescribeTableRequest.builder().tableName(nombreTabla).build());
        System.out.println("Tabla '" + nombreTabla + "' eliminada.");
    }
}
