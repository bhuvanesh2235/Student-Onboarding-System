import React, { useState, useEffect, useCallback } from "react";
import StudentForm from "./components/StudentForm";
import StudentTable from "./components/StudentTable";
import { getStudents, addStudent, updateStudent, deleteStudent } from "./services/api";
import "./App.css";

function App() {
  const [students, setStudents]         = useState([]);
  const [loading, setLoading]           = useState(false);
  const [editingStudent, setEditing]    = useState(null);   // null = "add" mode
  const [notification, setNotification] = useState(null);   // { type, message }
  const [currentPage, setCurrentPage]   = useState(0);
  const [totalPages, setTotalPages]     = useState(0);
  const pageSize = 10;  // Records per page

  /* ── helpers ─────────────────────────────────────────────────────────── */

  function showNotification(type, message) {
    setNotification({ type, message });
    setTimeout(() => setNotification(null), 4000);
  }

  const fetchStudents = useCallback(async (page = 0) => {
    setLoading(true);
    try {
      // Ensure page is a number
      const pageNum = Number(page) || 0;
      const data = await getStudents(pageNum, pageSize);
      setStudents(data.content || []);
      setTotalPages(data.totalPages || 1);
      setCurrentPage(Number(data.number) || 0);
    } catch (err) {
      showNotification("error", `Failed to load students: ${err.message}`);
    } finally {
      setLoading(false);
    }
  }, []);

  /* Fetch on mount */
  useEffect(() => {
    fetchStudents();
  }, [fetchStudents]);

  /* ── CRUD handlers ────────────────────────────────────────────────────── */

  async function handleAddOrUpdate(formData) {
    try {
      if (editingStudent) {
        // UPDATE
        await updateStudent(editingStudent.id, formData);
        showNotification("success", `Student "${formData.name}" updated successfully.`);
        setEditing(null);
      } else {
        // ADD
        await addStudent(formData);
        showNotification("success", `Student "${formData.name}" added successfully.`);
      }
      await fetchStudents();
    } catch (err) {
      showNotification("error", `Operation failed: ${err.message}`);
    }
  }

  async function handleDelete(id) {
    const student = students.find((s) => s.id === id);
    const name = student ? student.name : `ID ${id}`;
    if (!window.confirm(`Delete "${name}"? This cannot be undone.`)) return;
    try {
      await deleteStudent(id);
      showNotification("success", `Student "${name}" deleted.`);
      if (editingStudent?.id === id) setEditing(null);
      await fetchStudents(currentPage);
    } catch (err) {
      showNotification("error", `Delete failed: ${err.message}`);
    }
  }

  function handleNextPage(e) {
    e?.preventDefault?.();
    const nextPage = Number(currentPage) + 1;
    if (nextPage < totalPages) {
      fetchStudents(nextPage);
    }
  }

  function handlePrevPage(e) {
    e?.preventDefault?.();
    const prevPage = Number(currentPage) - 1;
    if (prevPage >= 0) {
      fetchStudents(prevPage);
    }
  }

  function handleEdit(student) {
    setEditing(student);
    // Smooth scroll to form on mobile
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  function handleCancelEdit() {
    setEditing(null);
  }

  /* ── render ───────────────────────────────────────────────────────────── */

  return (
    <div className="app-container">
      {/* ── Header ── */}
      <header className="app-header">
        <div className="header-inner">
          <span className="header-logo">🎓</span>
          <div>
            <h1>Student Onboarding System</h1>
            <p className="header-subtitle">Manage student records with ease</p>
          </div>
        </div>
      </header>

      {/* ── Notification Banner ── */}
      {notification && (
        <div className={`notification notification-${notification.type}`} role="alert">
          <span>{notification.type === "success" ? "✅" : "❌"}</span>
          <span>{notification.message}</span>
          <button className="notif-close" onClick={() => setNotification(null)}>×</button>
        </div>
      )}

      <main className="main-content">
        {/* ── Form Panel ── */}
        <section className="panel panel-form">
          <StudentForm
            onSubmit={handleAddOrUpdate}
            editingStudent={editingStudent}
            onCancelEdit={handleCancelEdit}
          />
        </section>

        {/* ── Table Panel ── */}
        <section className="panel panel-table">
          <div className="table-header">
            <h2>📋 Student Records</h2>
            <button
              className="btn btn-refresh"
              onClick={fetchStudents}
              disabled={loading}
              title="Refresh list"
            >
              {loading ? "⏳ Loading…" : "🔄 Refresh"}
            </button>
          </div>

          <StudentTable
            students={students}
            loading={loading}
            onEdit={handleEdit}
            onDelete={handleDelete}
            currentPage={currentPage}
            totalPages={totalPages}
            pageSize={pageSize}
            onNextPage={handleNextPage}
            onPrevPage={handlePrevPage}
          />
        </section>
      </main>

      <footer className="app-footer">
        <p>Student Onboarding System &mdash; Spring Boot + React</p>
      </footer>
    </div>
  );
}

export default App;
