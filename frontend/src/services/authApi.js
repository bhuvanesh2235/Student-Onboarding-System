/**
 * authApi.js
 * Auth service layer — signup and login calls to the backend.
 *
 * In Docker: nginx proxies /api → http://backend:8080
 * Locally:   REACT_APP_API_BASE_URL=http://localhost:8080
 */
const BASE_URL = process.env.REACT_APP_API_BASE_URL || "/api";
const JSON_HEADERS = { "Content-Type": "application/json" };

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

/** POST /auth/signup */
export async function signup(name, email, password) {
  const res = await fetch(`${BASE_URL}/auth/signup`, {
    method: "POST",
    headers: JSON_HEADERS,
    body: JSON.stringify({ name, email, password }),
  });
  await checkResponse(res);
  return res.json();
}

/** POST /auth/login */
export async function login(email, password) {
  const res = await fetch(`${BASE_URL}/auth/login`, {
    method: "POST",
    headers: JSON_HEADERS,
    body: JSON.stringify({ email, password }),
  });
  await checkResponse(res);
  return res.json();
}
