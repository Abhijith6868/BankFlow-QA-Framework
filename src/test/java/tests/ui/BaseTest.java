package tests.ui;

import base.BasePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BaseTest extends BasePage {

    @Test
    public void verifyBaseURL() {
        test = extent.createTest("Verify Base URL")
        .assignAuthor("Abhijith Joseph")
        .assignCategory("Smoke Test")
        .assignDevice("Chrome Browser");
        String currentURL = driver.getCurrentUrl();
        System.out.println("Current URL: " + currentURL);
        Assert.assertTrue(currentURL.contains("parabank"), 
            "URL should contain parabank");
    }
}