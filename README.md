# ConexionBBDDNoSQL - Hogwarts DynamoDB Integration

Este proyecto implementa una aplicación Java que utiliza Amazon DynamoDB como almacén de datos para gestionar información de Hogwarts (estudiantes y casas), siguiendo el patrón DAO/Repositorio.

## Características

- **Modelos de Datos**: Clases para estudiantes (`Student`) y casas (`House`) de Hogwarts
- **Patrón Repository**: Interfaces y implementaciones para operaciones CRUD
- **Cliente DynamoDB Configurable**: Soporte para modo local (DynamoDB Local) y modo AWS
- **Tests de Integración**: Tests completos con JUnit 5
- **Docker Compose**: Configuración para ejecutar DynamoDB Local
- **Ejemplos de Uso**: Clase de demostración con datos de Hogwarts

## Requisitos

- Java 17+
- Maven 3.6+
- Docker y Docker Compose (para desarrollo local)
- Credenciales AWS (para uso en producción)

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/
│   │   ├── modelos/
│   │   │   ├── hogwarts/
│   │   │   │   ├── Student.java      # Modelo de estudiante
│   │   │   │   └── House.java        # Modelo de casa
│   │   │   ├── Equipo.java          # (Existente) Modelo de equipo F1
│   │   │   └── Piloto.java          # (Existente) Modelo de piloto
│   │   ├── provider/
│   │   │   └── DynamoDbClientProvider.java  # Proveedor de clientes DynamoDB
│   │   ├── repository/
│   │   │   ├── StudentRepository.java       # Interfaz de repositorio de estudiantes
│   │   │   ├── StudentRepositoryImpl.java   # Implementación del repositorio
│   │   │   ├── HouseRepository.java         # Interfaz de repositorio de casas
│   │   │   └── HouseRepositoryImpl.java     # Implementación del repositorio
│   │   ├── sample/
│   │   │   └── HogwartsIntegrationSample.java  # Ejemplo de integración
│   │   └── utils/
│   │       ├── DynamoDBManager.java         # (Existente) Manager de DynamoDB
│   │       ├── OperacionesDynamoDB.java     # (Existente) Operaciones
│   │       └── App.java                     # (Existente) App de ejemplo F1
│   └── resources/
│       ├── application.properties           # Configuración de la aplicación
│       └── equipos.json                    # (Existente) Datos de equipos F1
└── test/
    └── java/
        └── repository/
            ├── StudentRepositoryIntegrationTest.java  # Tests de estudiantes
            └── HouseRepositoryIntegrationTest.java    # Tests de casas
```

## Configuración

### Modo Local (DynamoDB Local)

1. **Configurar variables de entorno:**

```bash
export DYNAMODB_ENDPOINT=http://localhost:8000
export AWS_REGION=us-east-1
export AWS_ACCESS_KEY_ID=dummy
export AWS_SECRET_ACCESS_KEY=dummy
```

2. **Iniciar DynamoDB Local con Docker Compose:**

```bash
docker-compose up -d
```

Esto iniciará DynamoDB Local en `localhost:8000`.

3. **Verificar que DynamoDB Local está corriendo:**

```bash
docker ps
```

Deberías ver un contenedor llamado `dynamodb-local`.

### Modo AWS (Producción)

1. **Configurar credenciales AWS:**

Opción A - Variables de entorno:
```bash
export AWS_ACCESS_KEY_ID=tu_access_key
export AWS_SECRET_ACCESS_KEY=tu_secret_key
export AWS_SESSION_TOKEN=tu_session_token  # Opcional
export AWS_REGION=us-east-1
```

Opción B - Archivo de propiedades (`src/main/resources/application.properties`):
```properties
aws.access.key.id=tu_access_key
aws.secret.access.key=tu_secret_key
aws.session.token=tu_session_token  # Opcional
aws.region=us-east-1
```

⚠️ **IMPORTANTE**: NO incluyas credenciales reales en el repositorio. Usa variables de entorno o AWS IAM roles.

## Compilación

```bash
mvn clean compile
```

## Ejecución de Ejemplos

### Ejemplo de Hogwarts

```bash
# Con DynamoDB Local
export DYNAMODB_ENDPOINT=http://localhost:8000
mvn exec:java -Dexec.mainClass="sample.HogwartsIntegrationSample"
```

Este ejemplo:
1. Crea las tablas `Students` y `Houses`
2. Inserta las 4 casas de Hogwarts
3. Inserta estudiantes de ejemplo (Harry, Hermione, Ron, etc.)
4. Realiza consultas por ID y por casa
5. Demuestra operaciones de actualización y eliminación

### Ejemplo de F1 (Existente)

```bash
mvn exec:java -Dexec.mainClass="utils.App"
```

## Tests

### Ejecutar Tests de Integración

Los tests requieren DynamoDB Local corriendo:

```bash
# 1. Iniciar DynamoDB Local
docker-compose up -d

# 2. Ejecutar tests
export DYNAMODB_ENDPOINT=http://localhost:8000
mvn test

# 3. Detener DynamoDB Local (opcional)
docker-compose down
```

### Ejecutar Tests con Maven Profile

```bash
docker-compose up -d
mvn test -Pdynamodb-local
docker-compose down
```

## Modelos de Datos

### Student (Estudiante)

```java
{
  "id": "uuid",
  "firstName": "Harry",
  "lastName": "Potter",
  "house": "Gryffindor",
  "year": 5,
  "patronus": "Stag"
}
```

**Tabla DynamoDB**: `Students`
- **Partition Key**: `id`
- **GSI**: `house-index` (permite consultas por casa)

### House (Casa)

```java
{
  "id": "gryffindor",
  "name": "Gryffindor",
  "founder": "Godric Gryffindor",
  "animal": "Lion"
}
```

**Tabla DynamoDB**: `Houses`
- **Partition Key**: `id`

## API de Repositorios

### StudentRepository

```java
StudentRepository studentRepo = new StudentRepositoryImpl();

// Crear tabla
studentRepo.createTableIfNotExists();

// CRUD
Student student = studentRepo.createStudent(newStudent);
Optional<Student> found = studentRepo.getStudentById("id");
Student updated = studentRepo.updateStudent(student);
studentRepo.deleteStudent("id");

// Consultas
List<Student> all = studentRepo.listStudents();
List<Student> gryffindors = studentRepo.findByHouse("Gryffindor");

// Limpiar
studentRepo.deleteTable();
```

### HouseRepository

```java
HouseRepository houseRepo = new HouseRepositoryImpl();

// Crear tabla
houseRepo.createTableIfNotExists();

// CRUD
House house = houseRepo.createHouse(newHouse);
Optional<House> found = houseRepo.getHouseById("id");
House updated = houseRepo.updateHouse(house);
houseRepo.deleteHouse("id");

// Listar
List<House> all = houseRepo.listHouses();

// Limpiar
houseRepo.deleteTable();
```

## Buenas Prácticas

1. **Gestión de Credenciales**: Nunca incluyas credenciales en el código fuente. Usa variables de entorno o AWS IAM roles.

2. **Tablas Temporales**: Para pruebas locales, las tablas se crean con provisioned throughput bajo (5 RCU/WCU). En producción, ajusta según necesidades.

3. **Índices Secundarios**: El índice `house-index` permite consultas eficientes por casa. Sin él, se usa scan (más costoso).

4. **Manejo de Errores**: Los repositorios lanzan excepciones para errores críticos (ej. actualizar estudiante inexistente).

5. **Cleanup**: Usa `deleteTable()` con cuidado en producción. Es útil para tests pero destructivo en entornos reales.

## Resolución de Problemas

### Error: "Unable to execute HTTP request"

Verifica que DynamoDB Local está corriendo:
```bash
docker ps
curl http://localhost:8000
```

### Error: "The security token included in the request is invalid"

Para DynamoDB Local, asegúrate de usar credenciales dummy:
```bash
export AWS_ACCESS_KEY_ID=dummy
export AWS_SECRET_ACCESS_KEY=dummy
```

### Tests fallan con "Table not found"

Espera unos segundos después de crear la tabla antes de ejecutar operaciones. Los tests incluyen sleeps para esto.

### Error: "Cannot do operations on a non-existent table"

Ejecuta `createTableIfNotExists()` antes de realizar operaciones en el repositorio.

## Dependencias Principales

- AWS SDK for Java v2 (DynamoDB y DynamoDB Enhanced)
- Lombok (reducir boilerplate)
- JUnit 5 (testing)
- Jackson (JSON serialization)
- SLF4J + Log4j2 (logging)

## Recursos Adicionales

- [AWS SDK for Java v2 Documentation](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/)
- [DynamoDB Enhanced Client Guide](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/dynamodb-enhanced-client.html)
- [DynamoDB Local Documentation](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.html)

## Licencia

Este proyecto es de código abierto y está disponible bajo la licencia MIT.

## Autor

ByMisterJ

## Contribuciones

Las contribuciones son bienvenidas. Por favor, abre un issue o pull request para sugerencias o mejoras.
