package serenitybdd.appiumtest.features.hybrid;

import net.serenitybdd.junit.runners.SerenityRunner;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.Steps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import serenitybdd.appiumtest.steps.serenity.HybridAppSteps;

@RunWith(SerenityRunner.class)
public class WhenUsingAHybridApp1Story {

   // @Managed(uniqueSession = true)
    @Managed
    public WebDriver webdriver;

    @Steps
    public HybridAppSteps appSteps;



    @Test
    public void adding_an_item_in_the_list_should_show_the_list1() {
        System.out.println("XXXTestThread1 " + Thread.currentThread());
        appSteps.add_list_item("Item1");
        appSteps.list_should_be_visible();
    }

    @Test
    public void test1() {
        System.out.println("XXX1TestThread " + Thread.currentThread());
        appSteps.checkBtnAdd();

    }

    @Test
    public void test2() {
        System.out.println("XXX1TestThread " + Thread.currentThread());
        appSteps.checkList();
    }
} 