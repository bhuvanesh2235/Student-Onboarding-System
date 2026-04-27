package com.bhuvanesh.onboarding.student_service.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Selenium UI tests for Login flows.
 *
 * Self-contained: registers its own test user in @BeforeAll via the API.
 * No need to run SignupTest first.
 *
 * Prerequisites:
 *  - Frontend running on http://localhost:3000
 *  - Backend  running on http://localhost:8080
 *
 * Run: cd student-service && ./mvnw test -Dtest=LoginTest
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LoginTest {

    private static WebDriver driver;
    private static final String BASE_URL   = "http://localhost:3000";
    private static final String API_URL    = "http://localhost:8080";
    private static final String TEST_EMAIL = "selenium_login@test.com";
    private static final String TEST_PASS  = "selenium123";
    private static final String TEST_NAME  = "Selenium Login User";

    @BeforeAll
    static void setUp() {
        // Skip gracefully if frontend or backend is not running
        Assumptions.assumeTrue(isPortOpen("localhost", 3000),
                "Skipping: frontend not reachable at " + BASE_URL + ". Run: cd frontend && npm start");
        Assumptions.assumeTrue(isPortOpen("localhost", 8080),
                "Skipping: backend not reachable at " + API_URL + ". Run: cd student-service && ./mvnw spring-boot:run");

        // Register the test user (ignore 409 = already registered)
        try {
            registerTestUser();
        } catch (Exception e) {
            throw new RuntimeException("Could not seed test user: " + e.getMessage(), e);
        }

        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        opts.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage", "--window-size=1280,800");
        driver = new ChromeDriver(opts);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(8));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) driver.quit();
    }

    /** Clear localStorage so each test starts unauthenticated */
    @BeforeEach
    void clearSession() {
        driver.get(BASE_URL + "/login");
        ((JavascriptExecutor) driver).executeScript("localStorage.clear();");
        driver.navigate().refresh();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /** POST /auth/signup — 201 (created) or 409 (already registered) are both OK */
    private static void registerTestUser() throws Exception {
        String body = String.format(
                "{\"name\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}",
                TEST_NAME, TEST_EMAIL, TEST_PASS);
        HttpURLConnection conn = (HttpURLConnection)
                URI.create(API_URL + "/auth/signup").toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        int status = conn.getResponseCode();
        conn.disconnect();
        if (status != 201 && status != 409) {
            throw new RuntimeException("Failed to seed test user — HTTP " + status);
        }
    }

    private static boolean isPortOpen(String host, int port) {
        try (java.net.Socket s = new java.net.Socket()) {
            s.connect(new java.net.InetSocketAddress(host, port), 3000);
            return true;
        } catch (Exception e) { return false; }
    }

    // ─── Test 1: Valid credentials → redirect to /students ───────────────────
    @Test
    @Order(1)
    void testValidLogin() {
        driver.get(BASE_URL + "/login");

        driver.findElement(By.id("login-email")).sendKeys(TEST_EMAIL);
        driver.findElement(By.id("login-password")).sendKeys(TEST_PASS);
        driver.findElement(By.id("login-submit")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.urlContains("/students"));

        Assertions.assertTrue(driver.getCurrentUrl().contains("/students"),
                "Expected redirect to /students after valid login");
    }

    // ─── Test 2: Invalid credentials → error message shown ───────────────────
    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get(BASE_URL + "/login");

        driver.findElement(By.id("login-email")).sendKeys("wrong@example.com");
        driver.findElement(By.id("login-password")).sendKeys("wrongpassword");
        driver.findElement(By.id("login-submit")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement errorDiv = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.className("auth-error")));

        Assertions.assertTrue(errorDiv.isDisplayed(),
                "Error message should be visible for invalid credentials");
    }

    // ─── Test 3: After login, /students is accessible ─────────────────────────
    @Test
    @Order(3)
    void testRedirectToStudentsAfterLogin() {
        driver.get(BASE_URL + "/login");

        driver.findElement(By.id("login-email")).sendKeys(TEST_EMAIL);
        driver.findElement(By.id("login-password")).sendKeys(TEST_PASS);
        driver.findElement(By.id("login-submit")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.urlContains("/students"));

        driver.get(BASE_URL + "/students");
        Assertions.assertTrue(driver.getCurrentUrl().contains("/students"));
    }

    // ─── Test 4: Logout → redirected back to /login ───────────────────────────
    @Test
    @Order(4)
    void testLogout() {
        driver.get(BASE_URL + "/login");
        driver.findElement(By.id("login-email")).sendKeys(TEST_EMAIL);
        driver.findElement(By.id("login-password")).sendKeys(TEST_PASS);
        driver.findElement(By.id("login-submit")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.urlContains("/students"));

        WebElement logoutBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("logout-btn")));
        logoutBtn.click();

        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"),
                "Should redirect to /login after logout");
    }
}
