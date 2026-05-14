package base;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.openqa.selenium.TimeoutException;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import io.github.bonigarcia.wdm.WebDriverManager;

public class BasePage {

	protected WebDriver driver;
	protected WebDriverWait wait;
	
	protected static ExtentReports extent;
	protected static ExtentTest test;
	
	protected static Properties config = new Properties();
	
	@BeforeSuite
	public void suitesetup()
	{
		loadConfig();
		initExtentReports();
	}
	
	@BeforeMethod
	public void setup()
	{
		String browser = config.getProperty("browser","chrome");
		
		if(browser.equalsIgnoreCase("chrome"))
		{
			WebDriverManager.chromedriver().setup();
			
			ChromeOptions optional = new ChromeOptions();
			
			optional.addArguments("--no-sandbox");
			optional.addArguments("--disable-dev-shm-usage");
			optional.addArguments("--window-size=1920,1080");
			
			driver = new ChromeDriver(optional);
		}
		
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
		
		wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		
		driver.get(config.getProperty("base.url"));
	}
	
	@AfterMethod
	public void tearDown(ITestResult result)
	{
		if(result.getStatus() == ITestResult.FAILURE)
		{
			String screenshotpath = takeScreenshot(result.getName());
			
			if(test != null && screenshotpath != null)
			{
				try {
					test.fail("Test Failed: " + result.getThrowable().getMessage());
				}catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}
		else if (result.getStatus() == ITestResult.SUCCESS) {
			if (test != null) test.pass("Test Passed");
		}else {
			if(test != null) test.skip("Test Skipped");
		}
		
		if(driver != null) {
			driver.quit();
		}
	}
	
	@AfterSuite
	public void suitetearDown()
	{
		if(extent != null)
		{
			extent.flush();
		}
	}
	
	protected void click(By locator) {
		wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
	}
	
	protected void type(By locator, String text) {
		WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
		element.clear();
		element.sendKeys(text);
	}
	
	protected String getText(By locator) {
	    return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText();
	}
	
	protected boolean isVisible(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }
	
	protected WebElement waitForElement(By locator) {
	        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
	    }
	  
	public void loadConfig() {
		try {
			FileInputStream fis = new FileInputStream("src/test/resources/config.properties");
			config.load(fis);
		} catch (IOException e) {
			// TODO: handle exception
			throw new RuntimeException("Could not load config.properties — check the file path", e);
		}
	}
	  
	public void initExtentReports() {
		  
		  String  timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
		  String reportPath = "reports/TestReport_" + timestamp + ".html";
		
		  ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
		  spark.config().setDocumentTitle("BankFlow QA Report");
		  spark.config().setReportName("Automation Test Results");
		  spark.config().setTheme(com.aventstack.extentreports.reporter.configuration.Theme.DARK);
		  
		  extent = new ExtentReports();
		  extent.attachReporter(spark);
		  extent.setSystemInfo("Project", "BankFlow QA Framework");
		  extent.setSystemInfo("Tester", "Abhijith Joseph");
		  extent.setSystemInfo("Environment", config.getProperty("env", "QA"));
		  extent.setSystemInfo("Browser", config.getProperty("browser", "chrome"));
		  extent.setSystemInfo("OS", System.getProperty("os.name"));
		  extent.setSystemInfo("Java Version", System.getProperty("java.version"));
	}
	  
	public String takeScreenshot(String testName) {
		try {
			String  timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
			String dir = "screenshots/";
			Files.createDirectories(Paths.get(dir));
			String path= dir + testName + "_" + timestamp + ".png";
			
			TakesScreenshot ts = (TakesScreenshot) driver;
			byte[] screenshot = ts.getScreenshotAs(OutputType.BYTES);
			Files.write(Paths.get(path), screenshot);
			
			return path;
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;
	}
}	

