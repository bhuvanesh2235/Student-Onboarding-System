// @ts-check
import { test, expect } from "@playwright/test";

/**
 * Playwright Auth Tests — Student Onboarding System
 *
 * Prerequisites: full stack running on http://localhost:3000
 * Run: cd frontend && npx playwright test
 */

const BASE_URL = "http://localhost:3000";

// Unique email so tests don't clash between runs
const UNIQUE_EMAIL = `pw_${Date.now()}@test.com`;
const TEST_NAME    = "Playwright User";
const TEST_PASS    = "playwright123";

// ─── Helper: clear localStorage and navigate ──────────────────────────────────
/** @param {import('@playwright/test').Page} page */
async function clearSession(page) {
  await page.goto(BASE_URL + "/login");
  await page.evaluate(() => localStorage.clear());
  await page.reload();
}

// ─── 1. Successful Signup ─────────────────────────────────────────────────────
test("successful signup redirects to /login", async ({ page }) => {
  await page.goto(BASE_URL + "/signup");

  await page.fill("#signup-name",     TEST_NAME);
  await page.fill("#signup-email",    UNIQUE_EMAIL);
  await page.fill("#signup-password", TEST_PASS);
  await page.click("#signup-submit");

  await page.waitForURL("**/login", { timeout: 10_000 });
  expect(page.url()).toContain("/login");
});

// ─── 2. Duplicate Email Signup ───────────────────────────────────────────────
test("duplicate signup shows error message", async ({ page }) => {
  await page.goto(BASE_URL + "/signup");

  // Re-use the same email registered above
  await page.fill("#signup-name",     "Another User");
  await page.fill("#signup-email",    UNIQUE_EMAIL);
  await page.fill("#signup-password", TEST_PASS);
  await page.click("#signup-submit");

  const errorEl = page.locator(".auth-error");
  await expect(errorEl).toBeVisible({ timeout: 8_000 });
  await expect(errorEl).toContainText(/email|registered|already/i);
});

// ─── 3. Successful Login ─────────────────────────────────────────────────────
test("successful login redirects to /students", async ({ page }) => {
  await clearSession(page);

  await page.fill("#login-email",    UNIQUE_EMAIL);
  await page.fill("#login-password", TEST_PASS);
  await page.click("#login-submit");

  await page.waitForURL("**/students", { timeout: 10_000 });
  expect(page.url()).toContain("/students");
});

// ─── 4. Failed Login ─────────────────────────────────────────────────────────
test("failed login shows error message", async ({ page }) => {
  await clearSession(page);

  await page.fill("#login-email",    "nobody@example.com");
  await page.fill("#login-password", "wrongpassword");
  await page.click("#login-submit");

  const errorEl = page.locator(".auth-error");
  await expect(errorEl).toBeVisible({ timeout: 8_000 });
  await expect(errorEl).toContainText(/invalid|password|email/i);
});

// ─── 5. Logout ───────────────────────────────────────────────────────────────
test("logout returns to /login", async ({ page }) => {
  await clearSession(page);

  // Login first
  await page.fill("#login-email",    UNIQUE_EMAIL);
  await page.fill("#login-password", TEST_PASS);
  await page.click("#login-submit");
  await page.waitForURL("**/students", { timeout: 10_000 });

  // Click logout
  await page.click("#logout-btn");
  await page.waitForURL("**/login", { timeout: 8_000 });
  expect(page.url()).toContain("/login");
});

// ─── 6. Protected route: unauthenticated access to /students ─────────────────
test("/students redirects to /login when not authenticated", async ({ page }) => {
  await clearSession(page);

  await page.goto(BASE_URL + "/students");
  await page.waitForURL("**/login", { timeout: 8_000 });
  expect(page.url()).toContain("/login");
});
