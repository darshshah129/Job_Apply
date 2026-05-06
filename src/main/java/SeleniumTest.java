import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import org.openqa.selenium.By;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.Keys;
import java.awt.datatransfer.*;
import java.awt.Toolkit;
import java.io.File;

public class SeleniumTest {

    public WebDriver driver;

    // ✅ DataProvider (unchanged logic, correct usage)
    @DataProvider(name = "emailData")
    public Object[][] getEmailData() throws IOException {
        List<String> emails = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader("src/main/resources/emails.csv"));
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

    // ✅ Fixed method signature (removed int port)
    @Test(dataProvider = "emailData")
    void Setup(List<String> emails) throws Exception {

        int port = 9222; // ✅ fixed: hardcoded port

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("profile-directory=Default");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.setExperimentalOption("debuggerAddress", "localhost:" + port);

        driver = new ChromeDriver(options);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        // Open Gmail in new tab
        driver.switchTo().newWindow(WindowType.TAB);
        driver.get("https://mail.google.com/");
        System.out.println("Navigated to Gmail");

        for (String email : emails) {
            try {
                System.out.println("Starting email send to " + email);

                waitUntilElementDisplayed("//div[text()='Compose']");
                driver.findElement(By.xpath("//div[text()='Compose']")).click();

                waitUntilElementDisplayed("//input[@aria-label='To recipients']");
                driver.findElement(By.xpath("//input[@aria-label='To recipients']")).sendKeys(email);
                driver.findElement(By.xpath("//input[@aria-label='To recipients']")).sendKeys(Keys.ENTER);

                waitUntilElementDisplayed("//input[@aria-label='Subject']");
                driver.findElement(By.xpath("//input[@aria-label='Subject']")
                        ).sendKeys("Regarding Open Position for QA Engineer");

                waitUntilElementDisplayed("//div[@aria-label='Message Body']");
                driver.findElement(By.xpath("//div[@aria-label='Message Body']"))
                        .sendKeys("Hello sir/mam,\n\n"
                                + "I hope you're well. I came across your job posting for a QA Automation Engineer and I'm very interested in the opportunity.\n\n"
                                + "I have one year of hands-on experience in QA Automation along with manual testing.\n\n"
                                + "I've attached my resume for your review.\n\n"
                                + "Best regards,\nContact: 9724795489");

                // ⚠️ Clipboard attachment (not reliable but kept)
                copyFileToClipboard("src/main/resources/Darsh shah CV.pdf");
                driver.findElement(By.xpath("//div[@aria-label='Message Body']")).click();
                driver.findElement(By.xpath("//div[@aria-label='Message Body']"))
                        .sendKeys(Keys.chord(Keys.CONTROL, "v"));

                Thread.sleep(4000); // small wait for upload

                driver.findElement(By.xpath("//div[@aria-label='Send ‪(Ctrl-Enter)‬']")).click();

                waitUntilElementDisplayed("//span[text()='Message sent']");
                System.out.println("Email sent to " + email);

            } catch (Exception e) {
                System.out.println("Failed for " + email + " : " + e.getMessage());
            }

            Thread.sleep(1500); // avoid rate limiting
        }

        driver.quit();
    }

    // ✅ Reusable wait method
    public void waitUntilElementDisplayed(String xpath) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
    }

    // ⚠️ Clipboard method (works only in some environments)
    public void copyFileToClipboard(String filePath) {
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