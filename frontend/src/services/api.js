/**
 * api.js
 * Centralised service layer for the Student Onboarding API.
 *
 * In Docker: nginx proxies /api → http://backend:8080, so BASE_URL = "/api".
 * Locally:   set REACT_APP_API_BASE_URL=http://localhost:8080 in .env.
 */

const BASE_URL = process.env.REACT_APP_API_BASE_URL || "/api";

/** Shared JSON headers */
const JSON_HEADERS = { "Content-Type": "application/json" };

/**
 * Throw a descriptive error for non-2xx responses.
 */
async function checkResponse(res) {
  if (!res.ok) {
    let msg = `HTTP ${res.status}`;
    try {
      const body = await res.json();
      msg = body.message || body.error || JSON.stringify(body);
    } catch (_) {
      msg = await res.text().catch(() => msg);
    }
    throw new Error(msg);
  }
  return res;
}

/** GET /students — fetch all students (with pagination support) */
export async function getStudents(page = 0, size = 10, sort = "id,asc") {
  // Ensure page and size are numbers, not objects
  const pageNum = Number(page) || 0;
  const sizeNum = Number(size) || 10;
  const sortStr = String(sort) || "id,asc";
  
  const params = new URLSearchParams({
    page: pageNum.toString(),
    size: sizeNum.toString(),
    sort: sortStr
  });
  const res = await fetch(`${BASE_URL}/students?${params}`);
  await checkResponse(res);
  // Return full paginated response (content, totalPages, etc.)
  return res.json();
}

/**
 * POST /students/bulk — add a single student.
 * The bulk endpoint accepts an array; we wrap the object.
 */
export async function addStudent(student) {
  const res = await fetch(`${BASE_URL}/students/bulk`, {
    method: "POST",
    headers: JSON_HEADERS,
    body: JSON.stringify([student]),
  });
  await checkResponse(res);
  return res.json();
}

/** PUT /students/:id — update an existing student */
export async function updateStudent(id, student) {
  const res = await fetch(`${BASE_URL}/students/${id}`, {
    method: "PUT",
    headers: JSON_HEADERS,
    body: JSON.stringify(student),
  });
  await checkResponse(res);
  return res.json();
}

/** DELETE /students/:id — remove a student */
export async function deleteStudent(id) {
  const res = await fetch(`${BASE_URL}/students/${id}`, {
    method: "DELETE",
  });
  await checkResponse(res);
  // 204 No Content returns no body
  return res.status === 204 ? null : res.json().catch(() => null);
}
