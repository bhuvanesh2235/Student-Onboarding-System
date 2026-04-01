package com.bhuvanesh.onboarding.student_service.controller;

import com.bhuvanesh.onboarding.student_service.dto.StudentDTO;
import com.bhuvanesh.onboarding.student_service.entity.Student;
import com.bhuvanesh.onboarding.student_service.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Validated                          // ← enables per-element validation on List parameters
@RestController
@RequiredArgsConstructor
@RequestMapping("/students")
@Tag(name = "Student Management", description = "APIs for managing student records")
public class StudentController {

    private final StudentService studentService;

    // ─── POST /students/bulk ──────────────────────────────────────────────────
    @PostMapping("/bulk")
    @Operation(summary = "Bulk create students", description = "Create multiple students at once. Duplicates are skipped based on email.")
    public ResponseEntity<Map<String, Object>> bulkSave(
            @RequestBody @Valid List<@Valid StudentDTO> dtos) {

        log.info("Received bulk request with {} record(s)", dtos.size());
        int saved = studentService.saveBulkStudents(dtos);

        // Use LinkedHashMap for a stable key order and safe mixed-type Map
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Bulk operation complete");
        response.put("received", dtos.size());
        response.put("saved", saved);
        response.put("duplicatesSkipped", dtos.size() - saved);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ─── GET /students (Paginated with Sorting) ───────────────────────────────
    @GetMapping
    @Operation(summary = "Get all students (paginated)", description = "Retrieve students with pagination and optional sorting. Default page size is 10.")
    public ResponseEntity<Page<Student>> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "id,asc") String sort) {

        // Validate pagination parameters
        if (page < 0) page = 0;
        if (size < 1) size = 10;
        if (size > 100) size = 100;  // Cap max size to prevent resource exhaustion

        // Parse sort parameter: format "field,direction" (e.g., "age,desc")
        String sortField = "id";
        Sort.Direction direction = Sort.Direction.ASC;
        
        if (sort != null && !sort.isEmpty()) {
            String[] sortParts = sort.split(",");
            // Validate sort field to prevent injection attacks
            String requestedField = sortParts[0].trim().toLowerCase();
            if (isValidSortField(requestedField)) {
                sortField = requestedField;
            }
            if (sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1].trim())) {
                direction = Sort.Direction.DESC;
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<Student> students = studentService.getAllStudents(pageable);

        return ResponseEntity.ok(students);
    }

    /**
     * Validate sort field to prevent injection attacks.
     * Only allow sorting by valid Student entity fields.
     */
    private boolean isValidSortField(String field) {
        return field.matches("^(id|name|email|age)$");
    }

    // ─── PUT /students/{id} ───────────────────────────────────────────────────
    @PutMapping("/{id}")
    @Operation(summary = "Update a student", description = "Update an existing student record by ID.")
    public ResponseEntity<Student> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentDTO dto) {

        Student updated = studentService.updateStudent(id, dto);
        return ResponseEntity.ok(updated);
    }

    // ─── DELETE /students/{id} ────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a student", description = "Delete a student record by ID.")
    public ResponseEntity<Map<String, String>> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", "Student with id " + id + " deleted successfully");
        return ResponseEntity.ok(response);
    }

    // ─── GET /students/health ─────────────────────────────────────────────────
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the service is running.")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("status", "Service is running");
        return ResponseEntity.ok(response);
    }
}
