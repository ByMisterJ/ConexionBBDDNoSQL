package sample;

import modelos.hogwarts.House;
import modelos.hogwarts.Student;
import repository.HouseRepository;
import repository.HouseRepositoryImpl;
import repository.StudentRepository;
import repository.StudentRepositoryImpl;

import java.util.List;
import java.util.UUID;

/**
 * Integration sample demonstrating DynamoDB operations with Hogwarts data.
 * This class shows how to:
 * 1. Initialize DynamoDB clients
 * 2. Create tables
 * 3. Insert sample Hogwarts data
 * 4. Query data by ID and by house
 * 
 * Before running:
 * - Set environment variable DYNAMODB_ENDPOINT=http://localhost:8000 for local mode
 * - Or configure AWS credentials for AWS mode
 */
public class HogwartsIntegrationSample {

    public static void main(String[] args) {
        System.out.println("=== Hogwarts DynamoDB Integration Sample ===\n");

        // Initialize repositories
        StudentRepository studentRepo = new StudentRepositoryImpl();
        HouseRepository houseRepo = new HouseRepositoryImpl();

        try {
            // Create tables
            System.out.println("--- Creating Tables ---");
            houseRepo.createTableIfNotExists();
            studentRepo.createTableIfNotExists();
            System.out.println();

            // Insert Hogwarts houses
            System.out.println("--- Inserting Houses ---");
            insertHouses(houseRepo);
            System.out.println();

            // Insert sample students
            System.out.println("--- Inserting Students ---");
            insertSampleStudents(studentRepo);
            System.out.println();

            // List all houses
            System.out.println("--- Listing All Houses ---");
            List<House> houses = houseRepo.listHouses();
            houses.forEach(h -> System.out.println("  " + h.getName() + " - Founded by " + h.getFounder()));
            System.out.println();

            // List all students
            System.out.println("--- Listing All Students ---");
            List<Student> allStudents = studentRepo.listStudents();
            allStudents.forEach(s -> System.out.println("  " + s.getFirstName() + " " + s.getLastName() + 
                    " (House: " + s.getHouse() + ", Year: " + s.getYear() + ")"));
            System.out.println();

            // Query students by house
            System.out.println("--- Students in Gryffindor ---");
            List<Student> gryffindorStudents = studentRepo.findByHouse("Gryffindor");
            gryffindorStudents.forEach(s -> System.out.println("  " + s.getFirstName() + " " + s.getLastName() + 
                    " - Patronus: " + s.getPatronus()));
            System.out.println();

            System.out.println("--- Students in Slytherin ---");
            List<Student> slytherinStudents = studentRepo.findByHouse("Slytherin");
            slytherinStudents.forEach(s -> System.out.println("  " + s.getFirstName() + " " + s.getLastName()));
            System.out.println();

            // Get student by ID
            if (!allStudents.isEmpty()) {
                Student firstStudent = allStudents.get(0);
                System.out.println("--- Getting Student by ID ---");
                studentRepo.getStudentById(firstStudent.getId()).ifPresent(s -> 
                    System.out.println("  Found: " + s.getFirstName() + " " + s.getLastName()));
                System.out.println();
            }

            // Update a student
            if (!allStudents.isEmpty()) {
                Student studentToUpdate = allStudents.get(0);
                System.out.println("--- Updating Student ---");
                studentToUpdate.setYear(studentToUpdate.getYear() + 1);
                studentToUpdate.setPatronus("Phoenix");
                studentRepo.updateStudent(studentToUpdate);
                System.out.println();
            }

            // Delete a student
            if (allStudents.size() > 1) {
                Student studentToDelete = allStudents.get(1);
                System.out.println("--- Deleting Student ---");
                studentRepo.deleteStudent(studentToDelete.getId());
                System.out.println();
            }

            // Cleanup (optional - comment out to keep data)
            // System.out.println("--- Cleaning Up (Deleting Tables) ---");
            // studentRepo.deleteTable();
            // houseRepo.deleteTable();

            System.out.println("=== Sample Completed Successfully ===");

        } catch (Exception e) {
            System.err.println("Error during sample execution: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Inserts the four Hogwarts houses.
     */
    private static void insertHouses(HouseRepository houseRepo) {
        House gryffindor = new House();
        gryffindor.setId("gryffindor");
        gryffindor.setName("Gryffindor");
        gryffindor.setFounder("Godric Gryffindor");
        gryffindor.setAnimal("Lion");
        houseRepo.createHouse(gryffindor);

        House slytherin = new House();
        slytherin.setId("slytherin");
        slytherin.setName("Slytherin");
        slytherin.setFounder("Salazar Slytherin");
        slytherin.setAnimal("Serpent");
        houseRepo.createHouse(slytherin);

        House hufflepuff = new House();
        hufflepuff.setId("hufflepuff");
        hufflepuff.setName("Hufflepuff");
        hufflepuff.setFounder("Helga Hufflepuff");
        hufflepuff.setAnimal("Badger");
        houseRepo.createHouse(hufflepuff);

        House ravenclaw = new House();
        ravenclaw.setId("ravenclaw");
        ravenclaw.setName("Ravenclaw");
        ravenclaw.setFounder("Rowena Ravenclaw");
        ravenclaw.setAnimal("Eagle");
        houseRepo.createHouse(ravenclaw);
    }

    /**
     * Inserts sample students from various houses.
     */
    private static void insertSampleStudents(StudentRepository studentRepo) {
        // Gryffindor students
        createAndSaveStudent(studentRepo, "Harry", "Potter", "Gryffindor", 5, "Stag");
        createAndSaveStudent(studentRepo, "Hermione", "Granger", "Gryffindor", 5, "Otter");
        createAndSaveStudent(studentRepo, "Ron", "Weasley", "Gryffindor", 5, "Jack Russell Terrier");
        createAndSaveStudent(studentRepo, "Neville", "Longbottom", "Gryffindor", 5, "Non-corporeal");

        // Slytherin students
        createAndSaveStudent(studentRepo, "Draco", "Malfoy", "Slytherin", 5, "Unknown");
        createAndSaveStudent(studentRepo, "Pansy", "Parkinson", "Slytherin", 5, "Unknown");

        // Hufflepuff students
        createAndSaveStudent(studentRepo, "Cedric", "Diggory", "Hufflepuff", 6, "Unknown");
        createAndSaveStudent(studentRepo, "Hannah", "Abbott", "Hufflepuff", 5, "Unknown");

        // Ravenclaw students
        createAndSaveStudent(studentRepo, "Luna", "Lovegood", "Ravenclaw", 4, "Hare");
        createAndSaveStudent(studentRepo, "Cho", "Chang", "Ravenclaw", 6, "Swan");
    }

    /**
     * Helper method to create and save a student.
     */
    private static void createAndSaveStudent(StudentRepository repo, String firstName, String lastName,
                                            String house, int year, String patronus) {
        Student student = new Student();
        student.setId(UUID.randomUUID().toString());
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setHouse(house);
        student.setYear(year);
        student.setPatronus(patronus);
        repo.createStudent(student);
    }
}
