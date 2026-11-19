package com.example.studentmanagement.service;

import com.example.studentmanagement.model.Student;
import com.example.studentmanagement.repository.StudentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Comparator;

@Service
public class StudentService {

    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    // Validate student marks
    private void validateScores(double programming, double mathematics) {
        if (programming < 0 || programming > 100 ||
            mathematics < 0 || mathematics > 100) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Scores must be between 0 and 100"
            );
        }
    }

    // Add a new student
    @Transactional
    public Student addStudent(Student s) {

        validateScores(s.getProgrammingScore(), s.getMathematicsScore());

        if (repository.existsById(s.getStudentId())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Student ID already exists"
            );
        }

        repository.addStudent(s);

        return repository.getStudentById(s.getStudentId()).orElseThrow();
    }

    // Get all students
    public List<Student> getAllStudents() {
        return repository.getAllStudents();
    }

    // Get student by ID
    public Student getStudentById(int id) {

        return repository.getStudentById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Student not found"
            ));
    }

    // Update student scores and skills
    @Transactional
    public Student updateStudent(int id, Student updatedStudent) {

        getStudentById(id);

        validateScores(
            updatedStudent.getProgrammingScore(),
            updatedStudent.getMathematicsScore()
        );

        repository.updateScores(
            id,
            updatedStudent.getProgrammingScore(),
            updatedStudent.getMathematicsScore()
        );

        repository.updateSkills(
            id,
            updatedStudent.getTechnicalSkills()
        );

        return getStudentById(id);
    }

    // Delete student
    @Transactional
    public void deleteStudent(int id) {

        getStudentById(id);

        repository.deleteStudent(id);
    }

    // Generate merit list in descending order of average marks
    public List<Student> getMeritList() {

        List<Student> students = repository.getAllStudents();

        students.sort(
            Comparator.comparingDouble(Student::calculateAverage).reversed()
        );

        return students;
    }
}