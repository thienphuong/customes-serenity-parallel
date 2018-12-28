package serenitybdd.appiumtest.pages;

import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import net.serenitybdd.core.pages.PageObject;
import net.thucydides.core.webdriver.WebDriverFacade;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public class AppiumPageObject extends PageObject {
    public AppiumPageObject(WebDriver driver) {
        super(driver);
        PageFactory.initElements(new AppiumFieldDecorator(((WebDriverFacade) getDriver()).getProxiedDriver()), this);


    }
}