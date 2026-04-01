import React from "react";

/**
 * StudentTable
 * ------------
 * Displays all students with Edit and Delete action buttons.
 *
 * Props:
 *   students      – array of student objects
 *   loading       – boolean, show spinner while fetching
 *   onEdit(student)     – called when Edit is clicked
 *   onDelete(id)        – called when Delete is clicked
 *   currentPage   – current page number (0-indexed)
 *   totalPages    – total number of pages
 *   pageSize      – number of records per page
 *   onNextPage    – called when Next button is clicked
 *   onPrevPage    – called when Previous button is clicked
 */
function StudentTable({ students, loading, onEdit, onDelete, currentPage = 0, totalPages = 0, pageSize = 10, onNextPage, onPrevPage }) {
  if (loading) {
    return (
      <div className="table-placeholder">
        <div className="spinner" aria-label="Loading…" />
        <p>Loading students…</p>
      </div>
    );
  }

  if (!students || students.length === 0) {
    return (
      <div className="table-placeholder">
        <p className="empty-msg">No students found. Add one above! 🎓</p>
      </div>
    );
  }

  return (
    <div className="table-wrapper">
      <table className="student-table">
        <thead>
          <tr>
            <th>#</th>
            <th>ID</th>
            <th>Name</th>
            <th>Email</th>
            <th>Age</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {students.map((student, index) => (
            <tr key={student.id}>
              <td className="row-num">{index + 1}</td>
              <td className="id-cell">{student.id}</td>
              <td>{student.name}</td>
              <td>
                <a href={`mailto:${student.email}`} className="email-link">
                  {student.email}
                </a>
              </td>
              <td>{student.age}</td>
              <td className="actions-cell">
                <button
                  className="btn btn-edit"
                  onClick={() => onEdit(student)}
                  title="Edit student"
                >
                  ✏️ Edit
                </button>
                <button
                  className="btn btn-delete"
                  onClick={() => onDelete(student.id)}
                  title="Delete student"
                >
                  🗑️ Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <div className="table-footer">
        <p className="record-count">
          Showing <strong>{students.length}</strong> student{students.length !== 1 ? "s" : ""} on page <strong>{currentPage + 1}</strong> of <strong>{totalPages || 1}</strong>
        </p>
        <div className="pagination-controls">
          <button
            className="btn btn-pagination"
            onClick={onPrevPage}
            disabled={currentPage === 0 || loading}
            title="Previous page"
          >
            ← Previous
          </button>
          <span className="page-indicator">
            Page {currentPage + 1} / {totalPages || 1}
          </span>
          <button
            className="btn btn-pagination"
            onClick={onNextPage}
            disabled={currentPage >= totalPages - 1 || loading}
            title="Next page"
          >
            Next →
          </button>
        </div>
      </div>
    </div>
  );
}

export default StudentTable;
