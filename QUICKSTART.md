# Quick Start Guide - Hogwarts DynamoDB Integration

## Prerequisites
- Java 17+
- Maven 3.6+
- Docker (for local testing)

## Quick Start (Local Development)

### 1. Start DynamoDB Local
```bash
docker-compose up -d
```

### 2. Set Environment Variables
```bash
export DYNAMODB_ENDPOINT=http://localhost:8000
export AWS_REGION=us-east-1
export AWS_ACCESS_KEY_ID=dummy
export AWS_SECRET_ACCESS_KEY=dummy
```

### 3. Build the Project
```bash
mvn clean package
```

### 4. Run the Hogwarts Integration Sample
```bash
mvn exec:java -Dexec.mainClass="sample.HogwartsIntegrationSample"
```

Expected output:
- Creates Students and Houses tables
- Inserts 4 Hogwarts houses
- Inserts 10 sample students
- Queries students by house (Gryffindor, Slytherin)
- Demonstrates CRUD operations

### 5. Run Integration Tests
```bash
mvn test
```

### 6. Stop DynamoDB Local (when done)
```bash
docker-compose down
```

## Using the Repositories in Your Code

```java
import repository.StudentRepository;
import repository.StudentRepositoryImpl;
import modelos.hogwarts.Student;

// Initialize repository
StudentRepository studentRepo = new StudentRepositoryImpl();

// Create table (first time only)
studentRepo.createTableIfNotExists();

// Create a student
Student harry = new Student();
harry.setId(UUID.randomUUID().toString());
harry.setFirstName("Harry");
harry.setLastName("Potter");
harry.setHouse("Gryffindor");
harry.setYear(1);
harry.setPatronus("Stag");
studentRepo.createStudent(harry);

// Query by house
List<Student> gryffindors = studentRepo.findByHouse("Gryffindor");

// Get by ID
Optional<Student> student = studentRepo.getStudentById(harry.getId());

// Update
harry.setYear(2);
studentRepo.updateStudent(harry);

// Delete
studentRepo.deleteStudent(harry.getId());
```

## Troubleshooting

**Error: Cannot connect to DynamoDB**
- Make sure Docker is running
- Verify DynamoDB Local is up: `docker ps`
- Check endpoint: `curl http://localhost:8000`

**Tests fail**
- Ensure DYNAMODB_ENDPOINT environment variable is set
- Wait a few seconds after starting DynamoDB Local
- Check Docker logs: `docker logs dynamodb-local`

**Build fails**
- Verify Java version: `java -version` (should be 17+)
- Clean Maven cache: `mvn clean`

## Next Steps

See full documentation in [README.md](README.md) for:
- AWS deployment configuration
- Advanced usage patterns
- Complete API reference
- Security best practices
