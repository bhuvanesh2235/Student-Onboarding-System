// @ts-check
import { defineConfig, devices } from "@playwright/test";

/**
 * Playwright configuration for Student Onboarding System auth tests.
 *
 * webServer: Playwright will auto-start `npm start` if nothing is already
 * listening on port 3000. The backend (Spring Boot) must still be running
 * separately on port 8080.
 *
 * Run: npx playwright test  (from inside the frontend/ directory)
 */
export default defineConfig({
  testDir: "./tests",
  timeout: 60_000,
  retries: 1,
  reporter: "list",

  use: {
    baseURL: "http://localhost:3000",
    headless: true,
    screenshot: "only-on-failure",
    video: "retain-on-failure",
    actionTimeout: 15_000,
  },

  /* Auto-start React dev server if not already running */
  webServer: {
    command: "npm start",
    url: "http://localhost:3000",
    reuseExistingServer: true,   // skip restart if already running
    timeout: 120_000,            // allow up to 2 min for CRA cold start
    stdout: "pipe",
    stderr: "pipe",
    env: {
      // Ensure API calls from the dev server hit the local Spring Boot
      REACT_APP_API_BASE_URL: "http://localhost:8080",
      BROWSER: "none",           // prevent CRA from opening a browser tab
    },
  },

  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"] },
    },
  ],
});
