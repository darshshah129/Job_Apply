// Selenium imports
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

// TestNG imports
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.cucumber.java.lu.an;
// WebDriver Manager
import io.github.bonigarcia.wdm.WebDriverManager;

// Java IO imports
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

// Java AWT/Clipboard imports
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

// Java Utility imports
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * SeleniumTest - Automated Gmail email sending test
 * Sends emails to multiple recipients using Selenium WebDriver
 */
public class SeleniumTest {

    // ============ CONSTANTS ============
    private static final int CHROME_DEBUG_PORT = 9222;
    private static final int WAIT_TIMEOUT_SECONDS = 30;
    private static final int ELEMENT_WAIT_SECONDS = 30;
    private static final long UPLOAD_WAIT_MS = 4000;
    private static final long RATE_LIMIT_DELAY_MS = 1500;
    private static final String GMAIL_URL = "https://mail.google.com/";
    private static final String EMAIL_DATA_PATH = "src/main/resources/emails.csv";
    private static final String RESUME_PATH = "src/main/resources/Darsh shah CV.pdf";

    // Gmail XPath Locators
    private static final String COMPOSE_BUTTON_XPATH = "//div[text()='Compose']";
    private static final String TO_RECIPIENTS_XPATH = "//input[@aria-label='To recipients']";
    private static final String SUBJECT_XPATH = "//input[@aria-label='Subject']";
    private static final String MESSAGE_BODY_XPATH = "//div[@aria-label='Message Body']";
    private static final String SEND_BUTTON_XPATH = "//div[@aria-label='Send ‪(Ctrl-Enter)‬']";
    private static final String MESSAGE_SENT_XPATH = "//span[text()='Message sent']";

    // Email content
    private static final String EMAIL_SUBJECT = "Regarding Open Position for QA Automation Engineer";
private static final String EMAIL_BODY =
    "I hope this message finds you well.\n\n" +
    "My name is Darsh Shah, and I am a QA Automation Engineer with over a year and a half " +
    "of hands-on experience building and executing automation frameworks across web, mobile, and API layers. I came across your organization and was genuinely impressed — which is why I'm reaching out directly.\n\n" +
    "I bring practical experience with Selenium, Playwright, Cypress, and Appium, along with strong API testing skills using Postman and Swagger. I have worked on enterprise-grade platforms spanning finance, AI, logistics, e-commerce, and ERP integrations — including Odoo, Shopify, and WooCommerce — where I have tested complex, multi-system workflows end to end.\n\n" +
    "What sets me apart is not just the tools I know, but the problems I have solved — from " +
    "preventing duplicate invoicing in a US-based logistics platform to building CI/CD-integrated " +
    "pipelines that automatically validate an entire product at deployment.\n\n" +
    "I have attached my resume for your reference. I would love the opportunity to connect for " +
    "even a brief conversation — I am confident you will find it worth your time.\n\n" +
    "Thank you for considering my application.\n\n" +
    "Warm regards,\n" +
    "Darsh Shah\n" +
    "QA Automation Engineer\n" +
    "+91 97247-95489";

    private WebDriver driver;

    // ============ DATA PROVIDER ============

    // ============ DATA PROVIDER ============

    /**
     * Provides email data from CSV file for parametrized testing
     * @return 2D array of email data
     */
    @DataProvider(name = "emailData")
    public Object[][] getEmailData() throws IOException {
        List<String> emails = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(EMAIL_DATA_PATH));
        String line;
        boolean firstLine = true;

        while ((line = br.readLine()) != null) {
            if (firstLine) {
                firstLine = false;
                continue;
            }
            emails.add(line.trim());
        }
        br.close();

        return new Object[][] {
            { emails }
        };
    }

    // ============ MAIN TEST METHOD ============

    /**
     * Main test method - Sets up Chrome driver and sends emails
     */
    @Test(dataProvider = "emailData")
    void Setup(List<String> emails) throws Exception {
        initializeDriver();
        navigateToGmail();

        for (String email : emails) {
            try {
                System.out.println("Starting email send to " + email);
                sendEmailToRecipient(email);
                System.out.println("Email sent to " + email);
            } catch (Exception e) {
                System.out.println("Failed for " + email + " : " + e.getMessage());
            }

            Thread.sleep(RATE_LIMIT_DELAY_MS); // avoid rate limiting
        }

        driver.quit();
    }

    // ============ WEBDRIVER INITIALIZATION ============

    /**
     * Initialize Chrome driver with required options
     */
    private void initializeDriver() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("profile-directory=Default");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.setExperimentalOption("debuggerAddress", "localhost:" + CHROME_DEBUG_PORT);

        driver = new ChromeDriver(options);
    }

    /**
     * Navigate to Gmail and open in new tab
     */
    private void navigateToGmail() {
        driver.switchTo().newWindow(WindowType.TAB);
        driver.get(GMAIL_URL);
        System.out.println("Navigated to Gmail");
    }

    // ============ EMAIL SENDING OPERATIONS ============

    /**
     * Sends email to a specific recipient
     */
    private void sendEmailToRecipient(String email) throws InterruptedException {
        clickCompose();
        fillToRecipient(email);
        fillSubject();
        fillMessageBody();
        attachResume();
        clickSend();
        waitForSendConfirmation();
    }

    /**
     * Clicks the Compose button
     */
    private void clickCompose() {
        waitUntilElementDisplayed(COMPOSE_BUTTON_XPATH);
        driver.findElement(By.xpath(COMPOSE_BUTTON_XPATH)).click();
    }

    /**
     * Fills the "To" field with recipient email
     */
    private void fillToRecipient(String email) {
        waitUntilElementDisplayed(TO_RECIPIENTS_XPATH);
        driver.findElement(By.xpath(TO_RECIPIENTS_XPATH)).sendKeys(email);
        driver.findElement(By.xpath(TO_RECIPIENTS_XPATH)).sendKeys(Keys.ENTER);
    }

    /**
     * Fills the Subject field
     */
    private void fillSubject() {
        waitUntilElementDisplayed(SUBJECT_XPATH);
        driver.findElement(By.xpath(SUBJECT_XPATH)).sendKeys(EMAIL_SUBJECT);
    }

    /**
     * Fills the Message Body
     * @throws InterruptedException 
     */
    private void fillMessageBody() throws InterruptedException {
        waitUntilElementDisplayed(MESSAGE_BODY_XPATH);
        driver.findElement(By.xpath(MESSAGE_BODY_XPATH)).sendKeys(EMAIL_BODY);
        Thread.sleep(2000); // wait for body to be filled
    }

    /**
     * Attaches resume file via clipboard
     */
    private void attachResume() throws InterruptedException {
        copyFileToClipboard(RESUME_PATH);
        driver.findElement(By.xpath(MESSAGE_BODY_XPATH)).click();
        driver.findElement(By.xpath(MESSAGE_BODY_XPATH)).sendKeys(Keys.chord(Keys.CONTROL, "v"));
        Thread.sleep(UPLOAD_WAIT_MS);
    }

    /**
     * Clicks the Send button
     */
    private void clickSend() {
        driver.findElement(By.xpath(SEND_BUTTON_XPATH)).click();
    }

    /**
     * Waits for send confirmation message
     */
    private void waitForSendConfirmation() {
        waitUntilElementDisplayed(MESSAGE_SENT_XPATH);
    }

    // ============ UTILITY METHODS ============

    /**
     * Waits until element is displayed with specified timeout
     */
    private void waitUntilElementDisplayed(String xpath) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(ELEMENT_WAIT_SECONDS));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
    }

    /**
     * Copies file to clipboard for attachment
     * Note: Only works in specific environments
     */
    private void copyFileToClipboard(String filePath) {
        File file = new File(filePath);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        clipboard.setContents(new Transferable() {
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[]{DataFlavor.javaFileListFlavor};
            }

            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return DataFlavor.javaFileListFlavor.equals(flavor);
            }

            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                if (!isDataFlavorSupported(flavor)) {
                    throw new UnsupportedFlavorException(flavor);
                }
                List<File> files = new ArrayList<>();
                files.add(file);
                return files;
            }
        }, null);
    }
}