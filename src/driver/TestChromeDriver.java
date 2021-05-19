package driver;

import java.sql.SQLException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import com.google.common.base.Strings;

public class TestChromeDriver {

	public static void main(String[] args) throws InterruptedException, SQLException {
		System.setProperty("webdriver.chrome.driver", "C:\\tmp\\chromedriver.exe");

		WebDriver driver = new ChromeDriver();
		driver.get("https://search.yahoo.co.jp/image");
		WebElement searchBox = driver.findElement(By.name("p"));
		searchBox.clear();
		searchBox.sendKeys("asdfsdf");
		searchBox.submit();

		Thread.sleep(10000);
		driver.quit();
	}


	private static String getUrlByImage(WebDriver driver, String prmKeyword) throws InterruptedException {
		driver.get("https://search.yahoo.co.jp/image");
		WebElement searchBox = driver.findElement(By.name("p"));
		searchBox.clear();
		searchBox.sendKeys(prmKeyword);
		searchBox.submit();

		if (isClose(driver)) {
			return "isClose";
		}

//		Thread.sleep(1000);

		try {
			driver.findElements(By.className("sw-Thumbnail")).get(0).click();
		} catch (Exception e) {
			return null;
		}

		Thread.sleep(200);

		String photoUrl = null;
		try {
			photoUrl = driver.findElement(By.className("sw-PreviewPanel__image")).getAttribute("src");
		} catch (Exception e) {
			Thread.sleep(1000);

			try {
				photoUrl = driver.findElement(By.className("sw-PreviewPanel__image")).getAttribute("src");
			} catch (Exception e1) {
				return null;
			}
		}

		if (Strings.isNullOrEmpty(photoUrl)) {
			return null;
		}

		return photoUrl;
	}

	private static String getUrl(WebDriver driver, String prmKeyword) throws InterruptedException {
		driver.get("https://search.yahoo.co.jp/");
		WebElement searchBox = driver.findElement(By.name("p"));
		searchBox.clear();
		searchBox.sendKeys("site:cosme.net " + prmKeyword);
		searchBox.submit();

		if (isClose(driver)) {
			return "isClose";
		}

		// sw-Thumbnail
		// sw-PreviewPanel__image
		// https://search.yahoo.co.jp/image

//		Thread.sleep(1000);

		String keyword = "https://www.cosme.net/";
		String keyword2 = "sku";

		List<WebElement> aElemList = driver.findElements(By.tagName("a"));
		String cosmeUrl = null;
		for (WebElement aElem : aElemList) {
			String tmpUrl = aElem.getAttribute("href");
			if (tmpUrl != null && tmpUrl.contains(keyword) && !tmpUrl.contains(keyword2) && tmpUrl.contains("product_id") && tmpUrl.contains("top")) {
				tmpUrl = tmpUrl.substring(tmpUrl.indexOf(keyword));
				cosmeUrl = tmpUrl.split("&")[0];
				break;
			}
		}

		if (Strings.isNullOrEmpty(cosmeUrl)) {
//			continue;
			return null;
		}

		driver.get(cosmeUrl);
		System.out.println(cosmeUrl);

		// mdImg
		String photoUrl = null;
		try {
			WebElement priceElem = driver.findElement(By.className("main_img"));
			photoUrl = priceElem.findElement(By.tagName("img")).getAttribute("src");
			photoUrl = photoUrl.substring(0, photoUrl.lastIndexOf("?"));
			photoUrl = photoUrl.replaceAll(".jpg", "_xl.jpg");
		} catch (Exception e) {
			try {
				WebElement priceElem = driver.findElement(By.id("mdImg"));
				photoUrl = priceElem.findElement(By.tagName("img")).getAttribute("src");
				photoUrl = photoUrl.substring(0, photoUrl.lastIndexOf("?"));
			} catch (Exception e1) {
//				continue;
				return null;
			}
		}

		return photoUrl;
	}

	private static boolean isClose(WebDriver driver) {
		try {
			driver.findElement(By.id("bd"));
		} catch (Exception e) {
			return false;
		}

		return true;
	}

}
