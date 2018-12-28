package net.serenitybdd.core.webdriver.driverproviders;

import com.google.common.eventbus.Subscribe;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import net.serenitybdd.core.buildinfo.DriverCapabilityRecord;
import net.serenitybdd.core.di.WebDriverInjectors;
import net.serenitybdd.core.webdriver.appium.AppiumDevicePool;
import net.serenitybdd.core.webdriver.appium.AppiumServerPool;
import net.thucydides.core.ThucydidesSystemProperty;
import net.thucydides.core.events.TestLifecycleEvents;
import net.thucydides.core.fixtureservices.FixtureProviderService;
import net.thucydides.core.steps.StepEventBus;
import net.thucydides.core.util.EnvironmentVariables;
import net.thucydides.core.webdriver.*;
import net.thucydides.core.webdriver.appium.AppiumConfiguration;
import net.thucydides.core.webdriver.stubs.WebDriverStub;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

import static net.thucydides.core.webdriver.SupportedWebDriver.ANDROID;
import static net.thucydides.core.webdriver.SupportedWebDriver.IPHONE;

public class AppiumDriverProvider implements DriverProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppiumDriverProvider.class);

    private final DriverCapabilityRecord driverProperties;

    private final FixtureProviderService fixtureProviderService;

    public AppiumDriverProvider(FixtureProviderService fixtureProviderService) {
        this.fixtureProviderService = fixtureProviderService;
        this.driverProperties = WebDriverInjectors.getInjector().getInstance(DriverCapabilityRecord.class);
    }

    @Override
    public WebDriver newInstance(String options, EnvironmentVariables environmentVariables) {

        //Thread.dumpStack();
        //LOGGER.info("Creating a new appium driver instance with options " + options, new Exception());
        LOGGER.error("getProxiedDriver-" + this.hashCode() + " Creating a new appium driver instance with options " + options);

        EnvironmentVariables testEnvironmentVariables = environmentVariables.copy();
        String deviceName = AppiumDevicePool.instance(testEnvironmentVariables).requestDevice();
        LOGGER.info("  - Using deviceName " + deviceName);
        LOGGER.error("getProxiedDriver-" + this.hashCode() + " Using deviceName " + deviceName);

        URL appiumUrl = appiumUrl(testEnvironmentVariables, deviceName);
        System.out.println("  - Using appium server at " + appiumUrl);
        LOGGER.info("  - Using appium server at " + appiumUrl);
        LOGGER.error("getProxiedDriver-" + this.hashCode() + " Using appium server at " + appiumUrl);

        testEnvironmentVariables.setProperty(ThucydidesSystemProperty.APPIUM_DEVICE_NAME.getPropertyName(), deviceName);
        testEnvironmentVariables.setProperty("appium.udid", deviceName);
        testEnvironmentVariables.clearProperty(ThucydidesSystemProperty.APPIUM_DEVICE_NAMES.getPropertyName());

        CapabilityEnhancer enhancer = new CapabilityEnhancer(testEnvironmentVariables, fixtureProviderService);

        if (StepEventBus.getEventBus().webdriverCallsAreSuspended()) {
            return new WebDriverStub();
        }
        switch (appiumTargetPlatform(testEnvironmentVariables)) {
            case ANDROID:
                LOGGER.info("  - Using android appium server at " + appiumUrl);
                LOGGER.info("  - Using appium capabilities " +  enhancer.enhanced(appiumCapabilities(options,testEnvironmentVariables), ANDROID));
                LOGGER.error("getProxiedDriver-" + this.hashCode() +"  - Using appium capabilities " +  enhancer.enhanced(appiumCapabilities(options,testEnvironmentVariables), ANDROID));
                AndroidDriver androidDriver = null;
                try {
                    androidDriver = new AndroidDriver(appiumUrl, enhancer.enhanced(appiumCapabilities(options, testEnvironmentVariables), ANDROID));
                } catch(Exception e) {
                    LOGGER.error("getProxiedDriver-" + this.hashCode() +" Created ANDROID DRiver failed" + androidDriver, e);
                    throw e;
                }
                LOGGER.error("getProxiedDriver-" + this.hashCode() +" Created ANDROID DRiver " + androidDriver);

                driverProperties.registerCapabilities("appium", capabilitiesToProperties(androidDriver.getCapabilities()));
                WebDriverInstanceEvents.bus().register(listenerFor(androidDriver, deviceName));
                LOGGER.error("getProxiedDriver-" + this.hashCode() + " -> driver created" + androidDriver);
                LOGGER.info("  -> driver created" + androidDriver);
                return androidDriver;
            case IOS:
                LOGGER.info("  - Using ios appium server at " + appiumUrl);
                LOGGER.info("  - Using appium capabilities " +  enhancer.enhanced(appiumCapabilities(options,testEnvironmentVariables), IPHONE));
                IOSDriver iosDriver = new IOSDriver(appiumUrl, enhancer.enhanced(appiumCapabilities(options,testEnvironmentVariables), IPHONE));

                driverProperties.registerCapabilities("appium", capabilitiesToProperties(iosDriver.getCapabilities()));
                WebDriverInstanceEvents.bus().register(listenerFor(iosDriver, deviceName));
                LOGGER.info("  -> driver created" + iosDriver);
                return iosDriver;
        }
        throw new UnsupportedDriverException(appiumTargetPlatform(testEnvironmentVariables).name());

    }

    private DesiredCapabilities appiumCapabilities(String options, EnvironmentVariables environmentVariables) {
        return AppiumConfiguration.from(environmentVariables).getCapabilities(options);
    }

    private MobilePlatform appiumTargetPlatform(EnvironmentVariables environmentVariables) {
        return AppiumConfiguration.from(environmentVariables).getTargetPlatform();
    }

    private URL appiumUrl(EnvironmentVariables environmentVariables, String deviceName) {
        return AppiumServerPool.instance(environmentVariables).urlFor(deviceName);
    }

    private WebDriverInstanceEventListener listenerFor(WebDriver driver, String deviceName) {
        return new AppiumEventListener(driver, deviceName);
    }

    private static class AppiumEventListener implements WebDriverInstanceEventListener {
        private final String deviceName;
        private WebDriver appiumDriver;
        private AppiumDevicePool devicePool;
        private Thread currentThread;

        private AppiumEventListener(WebDriver appiumDriver, String deviceName, AppiumDevicePool devicePool) {
            this.appiumDriver = appiumDriver;
            this.deviceName = deviceName;
            this.devicePool = devicePool;
            this.currentThread = Thread.currentThread();

            TestLifecycleEvents.register(this);
        }

        @Subscribe
        public void testFinishes(TestLifecycleEvents.TestFinished testFinished) {
            LOGGER.info("Appium test finished - releasing all devices");
            releaseAllDevicesUsedInThread(Thread.currentThread());
        }

        @Subscribe
        public void testSuiteFinishes(TestLifecycleEvents.TestSuiteFinished testSuiteFinished) {
            LOGGER.info("Appium test suite finished - shutting down servers");
            shutdownAllAppiumServersUsedInThread(Thread.currentThread());
        }

        private AppiumEventListener(WebDriver appiumDriver, String deviceName) {
            this(appiumDriver, deviceName, AppiumDevicePool.instance());
        }

        @Override
        public void close(WebDriver driver) {
            quit(driver);
        }

        @Override
        public void quit(WebDriver driver) {
            if (appiumDriver == driver) {
                devicePool.freeDevice(deviceName);
                appiumDriver = null;
            }
        }

        void releaseAllDevicesUsedInThread(Thread thread) {
            if (appiumDriver != null && currentThread == thread) {
                devicePool.freeDevice(deviceName);
                appiumDriver = null;
            }
        }

        void shutdownAllAppiumServersUsedInThread(Thread thread) {
            AppiumServerPool.instance().shutdownAllServersRunningOnThread(thread);
        }
    }
}
