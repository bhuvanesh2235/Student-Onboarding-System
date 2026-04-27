package com.bhuvanesh.onboarding.student_service.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.UUID;

/**
 * Selenium UI tests for Signup flows.
 *
 * Prerequisites:
 *  - App running on http://localhost:3000
 *  - Backend running on http://localhost:8080
 *
 * Run: cd student-service && ./mvnw test -Dtest=SignupTest
 * Run both:  ./mvnw test -Dtest=SignupTest,LoginTest
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SignupTest {

    private static WebDriver driver;
    private static final String BASE_URL = "http://localhost:3000";

    /** Unique email so each test run does not collide with previous runs */
    private static final String UNIQUE_EMAIL = "sel_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";

    @BeforeAll
    static void setUp() {
        // Skip gracefully if frontend or backend is not running
        Assumptions.assumeTrue(isFrontendRunning(),
                "Skipping: frontend not reachable at " + BASE_URL + ". Run: cd frontend && npm start");
        Assumptions.assumeTrue(isBackendRunning(),
                "Skipping: backend not reachable at http://localhost:8080. Run: cd student-service && ./mvnw spring-boot:run");

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

    @BeforeEach
    void goToSignup() {
        driver.get(BASE_URL + "/signup");
    }

    // ─── Health check helpers ─────────────────────────────────────────────────
    private static boolean isFrontendRunning() { return isPortOpen("localhost", 3000); }
    private static boolean isBackendRunning()  { return isPortOpen("localhost", 8080); }
    private static boolean isPortOpen(String host, int port) {
        try (java.net.Socket s = new java.net.Socket()) {
            s.connect(new java.net.InetSocketAddress(host, port), 3000);
            return true;
        } catch (Exception e) { return false; }
    }

    // ─── Test 1: Valid signup ─────────────────────────────────────────────────
    @Test
    @Order(1)
    void testValidSignup() {
        driver.findElement(By.id("signup-name")).sendKeys("Selenium User");
        driver.findElement(By.id("signup-email")).sendKeys(UNIQUE_EMAIL);
        driver.findElement(By.id("signup-password")).sendKeys("selenium123");
        driver.findElement(By.id("signup-submit")).click();

        // On success, app navigates to /login with a success notification
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.urlContains("/login"));

        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"),
                "Should redirect to /login after successful signup");
    }

    // ─── Test 2: Duplicate email → error message ──────────────────────────────
    @Test
    @Order(2)
    void testDuplicateEmailSignup() {
        // Use the same email as Test 1 (already registered)
        driver.findElement(By.id("signup-name")).sendKeys("Duplicate User");
        driver.findElement(By.id("signup-email")).sendKeys(UNIQUE_EMAIL);
        driver.findElement(By.id("signup-password")).sendKeys("selenium123");
        driver.findElement(By.id("signup-submit")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement errorDiv = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.className("auth-error")));

        Assertions.assertTrue(errorDiv.isDisplayed(),
                "An error message should appear for duplicate email");
    }
}
