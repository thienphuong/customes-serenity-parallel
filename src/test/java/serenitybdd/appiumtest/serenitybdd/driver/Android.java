package serenitybdd.appiumtest.serenitybdd.driver;

import io.appium.java_client.android.*;
import io.appium.java_client.remote.*;
import net.thucydides.core.util.*;
import net.thucydides.core.webdriver.*;
import net.thucydides.core.webdriver.MobilePlatform;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.*;

import java.net.*;
import java.util.concurrent.*;

public class Android implements DriverSource {

    @Override
    public WebDriver newDriver() {
        //Thread.dumpStack();
        System.out.println("Created new driver Android");
        DesiredCapabilities capabilities = new DesiredCapabilities();
        EnvironmentVariables properties = SystemEnvironmentVariables.createEnvironmentVariables();
        capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, properties.getProperty("android.automationName"));
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.ANDROID);
        capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, properties.getProperty("android.platformVersion"));
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, properties.getProperty("android.deviceName"));
        capabilities.setCapability(MobileCapabilityType.APP, properties.getProperty("android.app"));
        capabilities.setCapability("appActivity", properties.getProperty("android.appActivity"));
        capabilities.setCapability("appPackage", properties.getProperty("android.appPackage"));

        try {
            System.out.println("Before instantiate android driver");
            AndroidDriver driver = new AndroidDriver(new URL(properties.getProperty("appium.server")), capabilities);
            System.out.println("After instantiate android driver");
            driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
            return driver;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean takesScreenshots() {
        return true;
    }

}