package com.example.studentmanagement.repository;

import com.example.studentmanagement.model.Student;

import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

@Repository
public class StudentRepository {

    private final JdbcTemplate jdbcTemplate;

    public StudentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Convert database row into Student object
    private final RowMapper<Student> studentMapper = (rs, rowNum) -> {
        Student s = new Student();

        s.setStudentId(rs.getInt("studentId"));
        s.setStudentName(rs.getString("studentName"));
        s.setEmail(rs.getString("email"));
        s.setCourseName(rs.getString("courseName"));
        s.setProgrammingScore(rs.getDouble("programmingScore"));
        s.setMathematicsScore(rs.getDouble("mathematicsScore"));

        return s;
    };

    // Check whether student ID already exists
    public boolean existsById(int id) {
        String sql = "SELECT COUNT(*) FROM Student WHERE studentId = ?";

        Integer count = jdbcTemplate.queryForObject(
            sql, Integer.class, id
        );

        return count != null && count > 0;
    }

    // Add a new student
    public void addStudent(Student s) {
        String sql = "INSERT INTO Student (studentId, studentName, email, courseName, programmingScore, mathematicsScore) VALUES (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(
            sql,
            s.getStudentId(),
            s.getStudentName(),
            s.getEmail(),
            s.getCourseName(),
            s.getProgrammingScore(),
            s.getMathematicsScore()
        );

        addSkills(s.getStudentId(), s.getTechnicalSkills());
    }

    // Add technical skills
    public void addSkills(int studentId, List<String> skills) {
        if (skills == null || skills.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO StudentSkill (studentId, skillName) VALUES (?, ?)";

        for (String skill : skills) {
            jdbcTemplate.update(sql, studentId, skill);
        }
    }

    // Get all students
    public List<Student> getAllStudents() {
        String sql = "SELECT * FROM Student";

        List<Student> students = jdbcTemplate.query(sql, studentMapper);

        for (Student s : students) {
            s.setTechnicalSkills(getSkills(s.getStudentId()));
        }

        return students;
    }

    // Get student by ID
    public Optional<Student> getStudentById(int id) {
        String sql = "SELECT * FROM Student WHERE studentId = ?";

        List<Student> result = jdbcTemplate.query(
            sql, studentMapper, id
        );

        if (result.isEmpty()) {
            return Optional.empty();
        }

        Student s = result.get(0);
        s.setTechnicalSkills(getSkills(id));

        return Optional.of(s);
    }

    // Get skills of a student
    public List<String> getSkills(int studentId) {
        String sql = "SELECT skillName FROM StudentSkill WHERE studentId = ?";

        return jdbcTemplate.queryForList(
            sql, String.class, studentId
        );
    }

    // Update student scores
    public void updateScores(int id, double programming, double mathematics) {
        String sql = "UPDATE Student SET programmingScore = ?, mathematicsScore = ? WHERE studentId = ?";

        jdbcTemplate.update(sql, programming, mathematics, id);
    }

    // Update student skills
    public void updateSkills(int id, List<String> skills) {
        String deleteSql = "DELETE FROM StudentSkill WHERE studentId = ?";

        jdbcTemplate.update(deleteSql, id);

        addSkills(id, skills);
    }

    // Delete student
    public void deleteStudent(int id) {
        jdbcTemplate.update(
            "DELETE FROM StudentSkill WHERE studentId = ?", id
        );

        jdbcTemplate.update(
            "DELETE FROM Student WHERE studentId = ?", id
        );
    }
}