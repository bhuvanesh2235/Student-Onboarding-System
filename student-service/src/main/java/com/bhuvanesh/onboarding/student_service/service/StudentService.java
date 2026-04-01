package com.bhuvanesh.onboarding.student_service.service;

import com.bhuvanesh.onboarding.student_service.dto.StudentDTO;
import com.bhuvanesh.onboarding.student_service.entity.Student;
import com.bhuvanesh.onboarding.student_service.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    // ─── Bulk Save (skip duplicates by email) ────────────────────────────────
    @Transactional
    public int saveBulkStudents(List<StudentDTO> dtos) {
        List<Student> toSave = new ArrayList<>();

        for (StudentDTO dto : dtos) {
            if (studentRepository.findByEmail(dto.getEmail()).isPresent()) {
                log.warn("Duplicate student skipped: {}", dto.getEmail());
                continue;
            }
            toSave.add(toEntity(dto));
        }

        if (!toSave.isEmpty()) {
            studentRepository.saveAll(toSave);
            log.info("Saved {} new student(s)", toSave.size());
        }

        return toSave.size();
    }

    // ─── Get All ─────────────────────────────────────────────────────────────
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // ─── Get All with Pagination and Sorting ──────────────────────────────────
    public Page<Student> getAllStudents(Pageable pageable) {
        return studentRepository.findAll(pageable);
    }

    // ─── Update ──────────────────────────────────────────────────────────────
    @Transactional
    public Student updateStudent(Long id, StudentDTO dto) {
        Student existing = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + id));

        // If email is changing, ensure the new email is not taken by another student
        if (!existing.getEmail().equalsIgnoreCase(dto.getEmail())) {
            boolean emailTaken = studentRepository.findByEmail(dto.getEmail())
                    .map(other -> !other.getId().equals(id))
                    .orElse(false);
            if (emailTaken) {
                throw new IllegalArgumentException("Email already in use: " + dto.getEmail());
            }
        }

        existing.setName(dto.getName());
        existing.setEmail(dto.getEmail());
        existing.setAge(dto.getAge());

        Student saved = studentRepository.save(existing);
        log.info("Updated student id={}", id);
        return saved;
    }

    // ─── Delete ──────────────────────────────────────────────────────────────
    @Transactional
    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new IllegalArgumentException("Student not found with id: " + id);
        }
        studentRepository.deleteById(id);
        log.info("Deleted student id={}", id);
    }

    // ─── Helper: DTO → Entity ────────────────────────────────────────────────
    private Student toEntity(StudentDTO dto) {
        return Student.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .age(dto.getAge())
                .build();
    }
}
