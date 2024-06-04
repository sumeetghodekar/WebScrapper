package demo;

import java.io.File;
import java.io.FileWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bouncycastle.jcajce.provider.asymmetric.ec.SignatureSpi.ecCVCDSA;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.github.bonigarcia.wdm.WebDriverManager;

public class TestCases 
{
    WebDriver driver;
    private static List<HashMap<String, Object>> allTeamData;
    private static List<HashMap<String, Object>> oscarDataList;
    @BeforeClass
    public void setUp() 
    {
        System.out.println("Constructor: TestCases");

        //WebDriverManager.chromedriver().browserVersion("125.0.6422.61").setup();
        driver = new ChromeDriver();
        ChromeOptions options = new ChromeOptions();
        LoggingPreferences logs = new LoggingPreferences();
        allTeamData = new ArrayList<>();
        oscarDataList = new ArrayList<>();

        logs.enable(LogType.BROWSER, Level.ALL);
        logs.enable(LogType.DRIVER, Level.ALL);
        options.setCapability("goog:loggingPrefs", logs);
        options.addArguments("start-maximized");
        options.addArguments("--disable-blink-features=AutomationControlled");

        System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, "chromedriver.log");
    }

     @AfterClass
    public void tearDown() 
    {
        System.out.println("End Test: TestCases");
        driver.quit();
    }


    @Test(priority = 1)
    public void TestCase01()
    {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        try 
        {
            //Navigate to WebScrapper URL
            navigatetoWebScrapper(driver,"https://www.scrapethissite.com/pages/");


            //Click on Hockey Team Link
            //selectHocketTeam(driver, By.xpath("//a[@href='/pages/forms/']"));
            selectLink(driver, By.xpath("//a[text()='Hockey Teams: Forms, Searching and Pagination']"));

            //get data from table
            collectionofData(driver, By.xpath("//table/tbody/tr/td[1]"), By.xpath("//table/tbody/tr/td[2]"),
            By.xpath("//table/tbody/tr/td[6]"),By.xpath("//a[@aria-label='Next']"));
            js.executeScript("window.scrollBy(0,1000)");


            //Write data to file
            writeData(allTeamData, "output/hockey-team-data.json");
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("Failure occurred while automating Hockey Team Data " + e.getMessage());
        }
    }

    @Test(priority = 2)
    public void TestCase02()
    {
        try 
        {
            //Navigate to WebScrapper URL
            navigatetoWebScrapper(driver, "https://www.scrapethissite.com/pages/");

            //click on Oscar Link
            selectLink(driver, By.xpath("//a[text()='Oscar Winning Films: AJAX and Javascript']"));

            //Iterate through years and select top movies
            yearList(driver, By.xpath("//a[@class='year-link']"));

            //Write data to file
            writeData(oscarDataList, "output/oscar-winner-data.json");

        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("Failure occurred while automating Oscar Film Data " + e.getMessage());
        }
    }

    private static void navigatetoWebScrapper(WebDriver driver,String expectedurl)
    {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            if (!(driver.getCurrentUrl().equals(expectedurl))) {
                driver.get(expectedurl);
                wait.until(ExpectedConditions.urlToBe(expectedurl));

                String actualurl = driver.getCurrentUrl();
                Assert.assertEquals(actualurl, expectedurl, "Failed to load url");
            }

        } catch (Exception e) {
            System.out.println("Exception occurred while navigating: " + e.getMessage());
        }
    }

    private static void selectLink(WebDriver driver,By hockeylink)
    {
        try 
        {
            WebElement link = driver.findElement(hockeylink);
            link.click();
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("Exception occurred while searching Hockey Teams: " + e.getMessage());
        }
    }

    private static void collectionofData(WebDriver driver,By teamData,By yearData, By winData, By nxtbtn)
    {
        for(int page=0;page<4;page++)
        {
            try 
            {
    
                List<WebElement> teamName = driver.findElements(teamData);
                List<WebElement> year = driver.findElements(yearData);
                List<WebElement> winPercent = driver.findElements(winData);
                
    
                for(int i=0;i<winPercent.size();i++)
                {
                    double winPercentage = Double.parseDouble(winPercent.get(i).getText());
                    if(winPercentage<0.40)
                    {
                        HashMap<String, Object> team = new HashMap<>();
                        team.put("Epoch Time ", Instant.now().getEpochSecond());
                        team.put("Team Name ", teamName.get(i).getText());
                        team.put("Year ", year.get(i).getText());
                        team.put("win%", winPercentage);

                        allTeamData.add(team);
                    }
                    
                }

                if(page<3)
                {
                    WebElement nextbutton = driver.findElement(nxtbtn);
                    nextbutton.click();
                }
            } catch (Exception e) {
                // TODO: handle exception
                System.out.println("Exception occurred while collecting data: " + e.getMessage());
            }
        }

    }

    private static void writeData(List<HashMap<String, Object>> data,String filePath)
    {
        
        ObjectMapper mapper = new ObjectMapper();
        try 
        {
            File outputFile = new File(filePath);
            outputFile.getParentFile().mkdirs();
            mapper.writeValue(outputFile, data);

            Assert.assertTrue(outputFile.exists(), "Output file does not exist");
            Assert.assertTrue(outputFile.length() > 0, "Output file is empty");
            
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("Failed to write data to JSON: " + e.getMessage());
        }
    }

    private static void yearList(WebDriver driver,By years)
    {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try 
        {
            List<WebElement> year = driver.findElements(years);
            for (WebElement yearElement : year) 
            {
                String currentYear = yearElement.getText();
                yearElement.click();
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[@class='film-title']")));
                
                List<WebElement> movie = driver.findElements(By.xpath("//td[@class='film-title']"));
                List<WebElement> nomination = driver.findElements(By.xpath("//td[@class='film-nominations']"));
                List<WebElement> award = driver.findElements(By.xpath("//td[@class='film-awards']"));
                
                for(int i=0;i<Math.min(5,movie.size());i++)
                {
                    WebElement movieTitle = movie.get(i);
                    WebElement nomTitle = nomination.get(i);
                    WebElement awardTitle = award.get(i);

                    String movieText = movieTitle.getText();
                    String nominationText = nomTitle.getText();
                    String awardText = awardTitle.getText();


                    boolean isWinner = (i == 0);
                    
                    
                    HashMap<String, Object> movieData = new HashMap<>();
                    movieData.put("Epoch Time", Instant.now().getEpochSecond());
                    movieData.put("Year", currentYear);
                    movieData.put("Title", movieText);
                    movieData.put("Nominations", nominationText);
                    movieData.put("Awards", awardText);
                    movieData.put("isWinner", isWinner);

                    oscarDataList.add(movieData);                     
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("Exception occurred while selecting year " + e.getMessage());
        }
    }
}
