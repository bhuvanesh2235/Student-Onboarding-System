import React, { useState, useEffect } from "react";

/**
 * StudentForm
 * -----------
 * Handles both ADD (no editingStudent prop) and EDIT mode.
 *
 * Props:
 *   onSubmit(data)          – called with { name, email, age }
 *   editingStudent          – student object when editing, null otherwise
 *   onCancelEdit()          – clears edit mode
 */
function StudentForm({ onSubmit, editingStudent, onCancelEdit }) {
  const EMPTY = { name: "", email: "", age: "" };
  const [form, setForm] = useState(EMPTY);
  const [errors, setErrors] = useState({});

  /* Populate form when editing a student */
  useEffect(() => {
    if (editingStudent) {
      setForm({
        name: editingStudent.name || "",
        email: editingStudent.email || "",
        age: String(editingStudent.age ?? ""),
      });
      setErrors({});
    } else {
      setForm(EMPTY);
      setErrors({});
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [editingStudent]);

  function handleChange(e) {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
    // Clear the error for this field while the user types
    setErrors((prev) => ({ ...prev, [name]: "" }));
  }

  function validate() {
    const errs = {};
    if (!form.name.trim()) errs.name = "Name is required.";
    if (!form.email.trim()) {
      errs.email = "Email is required.";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) {
      errs.email = "Enter a valid email address.";
    }
    const age = Number(form.age);
    if (!form.age.trim()) {
      errs.age = "Age is required.";
    } else if (!Number.isInteger(age) || age <= 0) {
      errs.age = "Age must be a positive whole number.";
    }
    return errs;
  }

  function handleSubmit(e) {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length > 0) {
      setErrors(errs);
      return;
    }
    onSubmit({
      name: form.name.trim(),
      email: form.email.trim(),
      age: Number(form.age),
    });
    if (!editingStudent) setForm(EMPTY);
  }

  const isEditing = Boolean(editingStudent);

  return (
    <div className="form-card">
      <h2 className="form-title">
        {isEditing ? "✏️ Edit Student" : "➕ Add New Student"}
      </h2>

      <form onSubmit={handleSubmit} noValidate>
        {/* Name */}
        <div className="form-group">
          <label htmlFor="name">Full Name</label>
          <input
            id="name"
            name="name"
            type="text"
            placeholder="e.g. Alice Johnson"
            value={form.name}
            onChange={handleChange}
            className={errors.name ? "input-error" : ""}
          />
          {errors.name && <span className="error-msg">{errors.name}</span>}
        </div>

        {/* Email */}
        <div className="form-group">
          <label htmlFor="email">Email Address</label>
          <input
            id="email"
            name="email"
            type="email"
            placeholder="e.g. alice@example.com"
            value={form.email}
            onChange={handleChange}
            className={errors.email ? "input-error" : ""}
          />
          {errors.email && <span className="error-msg">{errors.email}</span>}
        </div>

        {/* Age */}
        <div className="form-group">
          <label htmlFor="age">Age</label>
          <input
            id="age"
            name="age"
            type="number"
            placeholder="e.g. 21"
            min="1"
            value={form.age}
            onChange={handleChange}
            className={errors.age ? "input-error" : ""}
          />
          {errors.age && <span className="error-msg">{errors.age}</span>}
        </div>

        {/* Actions */}
        <div className="form-actions">
          <button type="submit" className="btn btn-add">
            {isEditing ? "Update Student" : "Add Student"}
          </button>
          {isEditing && (
            <button
              type="button"
              className="btn btn-cancel"
              onClick={onCancelEdit}
            >
              Cancel
            </button>
          )}
        </div>
      </form>
    </div>
  );
}

export default StudentForm;
