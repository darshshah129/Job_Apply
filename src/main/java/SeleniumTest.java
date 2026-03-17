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
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import org.openqa.selenium.Keys;
import java.awt.Toolkit;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
// "C:\Program Files\Google\Chrome\Application\chrome.exe" --remote-debugging-port=9222 --user-data-dir="C:\chrome-debug"
public class SeleniumTest {

    public WebDriver driver;

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

    Object[][] data = new Object[1][1];
    data[0][0] = emails;
    return data;
}

    @Test(dataProvider = "emailData")
    void Setup(List<String> emails, int port) throws InterruptedException, IOException, UnsupportedFlavorException {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("profile-directory=Default");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.setExperimentalOption("debuggerAddress", "localhost:" + port);
        driver = new ChromeDriver(options);

                Thread.sleep(2000);
                driver.switchTo().newWindow(WindowType.TAB);
                Thread.sleep(2000);
                driver.get("https://mail.google.com/");
                System.out.println("Navigated to Gmail");

        for (String email : emails) {
            try {
                System.out.println("Starting email send to " + email);
                waitUntilElementDisplayed("//div[normalize-space(text())='Compose']");
                driver.findElement(By.xpath("//div[normalize-space(text())='Compose']")).click();
                Thread.sleep(2000);
                System.out.println("Click on Compose button");

                waitUntilElementDisplayed("//input[@aria-label='To recipients']");
                driver.findElement(By.xpath("//input[@aria-label='To recipients']")).sendKeys(email);
                driver.findElement(By.xpath("//input[@aria-label='To recipients']")).sendKeys(Keys.ENTER);
                System.out.println("Email address entered: " + email);
                
                waitUntilElementDisplayed("//input[@aria-label='Subject']");
                driver.findElement(By.xpath("//input[@aria-label='Subject']")).sendKeys("Regarding Open Position for QA Engineer");
                System.out.println("Subject entered");

                waitUntilElementDisplayed("//div[@aria-label='Message Body']");
                driver.findElement(By.xpath("//div[@aria-label='Message Body']"))
                .sendKeys("Hello sir/mam,\n\nI hope you're well. I came across your job posting for a QA Automation Engineer and I'm very interested in the opportunity. I have one year of hands-on experience in QA Automation along with manual testing, and I believe my skills would be a good fit for your team.\n\nI've attached my resume for your review. I'd be happy to discuss how I can contribute.\n\nThanks, looking forward to hearing from you.\n\nBest regards,\nContact: 9724795489");
                System.out.println("Message body entered");
                // Copy the file to clipboard and paste into the message body
                copyFileToClipboard("src/main/resources/Darsh shah CV.pdf");
                driver.findElement(By.xpath("//div[@aria-label='Message Body']")).click();
                driver.findElement(By.xpath("//div[@aria-label='Message Body']")).sendKeys(Keys.chord(Keys.CONTROL, "v"));
                System.out.println("Resume attached");
                Thread.sleep(5000);
                driver.findElement(By.xpath("//div[@aria-label='Send ‪(Ctrl-Enter)‬']")).click();
                waitUntilElementDisplayed("//span[normalize-space(text())='Message sent']");
                System.out.println("Email sent to " + email + " at " + System.currentTimeMillis());
            } catch (Exception e) {
                System.out.println("Failed to send email to " + email + ": " + e.getMessage());
                // Close the tab if open
                try {
                    driver.close();
                } catch (Exception ex) {
                    // ignore
                }
            }
            // Delay between emails to avoid rate limiting
            Thread.sleep(1000);
        }
        driver.close(); // close the browser
    }

    public void waitUntilElementDisplayed(String element) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(element)));
    }

    public void copyFileToClipboard(String filePath) throws IOException, UnsupportedFlavorException {
        File file = new File(filePath);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[]{DataFlavor.javaFileListFlavor};
            }
            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return DataFlavor.javaFileListFlavor.equals(flavor);
            }
            @Override
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                if (DataFlavor.javaFileListFlavor.equals(flavor)) {
                    List<File> files = new ArrayList<>();
                    files.add(file);
                    return files;
                }
                throw new UnsupportedFlavorException(flavor);
            }
        }, null);
    }
}