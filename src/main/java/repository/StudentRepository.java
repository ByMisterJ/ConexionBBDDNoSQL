package repository;

import modelos.hogwarts.Student;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Student entities.
 * Provides CRUD operations and custom queries for students.
 */
public interface StudentRepository {
    
    /**
     * Creates a new student in the database.
     * @param student the student to create
     * @return the created student
     */
    Student createStudent(Student student);
    
    /**
     * Retrieves a student by their ID.
     * @param id the student ID
     * @return an Optional containing the student if found, empty otherwise
     */
    Optional<Student> getStudentById(String id);
    
    /**
     * Updates an existing student.
     * @param student the student with updated information
     * @return the updated student
     */
    Student updateStudent(Student student);
    
    /**
     * Deletes a student by their ID.
     * @param id the student ID
     */
    void deleteStudent(String id);
    
    /**
     * Lists all students in the database.
     * @return list of all students
     */
    List<Student> listStudents();
    
    /**
     * Finds all students belonging to a specific house.
     * @param house the house name (e.g., "Gryffindor")
     * @return list of students in the house
     */
    List<Student> findByHouse(String house);
    
    /**
     * Creates the students table if it doesn't exist.
     */
    void createTableIfNotExists();
    
    /**
     * Deletes the students table.
     */
    void deleteTable();
}
