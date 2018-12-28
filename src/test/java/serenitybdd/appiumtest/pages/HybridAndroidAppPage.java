package serenitybdd.appiumtest.pages;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSFindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

public class HybridAndroidAppPage extends PageObject {

    @AndroidFindBy(id="android:id/list")
    @iOSFindBy(id="android:id/list")
    private WebElementFacade list;

    //@AndroidFindBy(id="btnAdd")
    //@AndroidFindBy(xpath = "//button[@text='ADD ITEM']")
    //@AndroidFindBy(xpath="//android.widget.Button[@text='ADD ITEM']")
    //@AndroidFindBy(xpath = "//button[@id='btnAdd']")
    @FindBy(id="btnAdd")
    private WebElementFacade btnAdd;

    @FindBy(id="btnToast")
    private WebElementFacade btnToast;

    @FindBy(id="btnDeviceInfo")
    private WebElementFacade btnDeviceInfo;

    @FindBy(id="btnUrl")
    private WebElementFacade btnUrl;

    @FindBy(id="bookmark")
    private WebElementFacade textField;


    public void addItem(String text){
        waitFor(textField);
        textField.sendKeys(text);
        btnAdd.click();
    }

    public HybridAndroidAppPage(WebDriver driver) {
        super(driver);
    }

    public void list_should_be_visible() {
        waitFor(list);
    }

    public void check_text_field(){
        textField.isCurrentlyEnabled();
    }

    public void check_btn_Add(){
        btnAdd.isCurrentlyEnabled();
    }

    public void check_list(){
        list.isCurrentlyEnabled();
    }
}