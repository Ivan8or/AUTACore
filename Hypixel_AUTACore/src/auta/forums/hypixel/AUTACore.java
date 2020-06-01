/*
Auto-Update-ThreAd Core Class
author: Ivan Volkov
date: 5/31/2020
version: 1.0.1
desc:
This is the core class for any auto-updating threads on the hypixel forums.
It is intended for use with ONLY hypixel.net forums, and WILL NOT WORK for other websites.
disclaimer:
I (Ivan Volkov) am not responsible for any malicious or otherwise harmful results that the use
of this software may bring. The user of this software will bear sole responsibility for their
actions regarding it's use.
Everybody is free to use this software, provided they:
 a) do not claim it as their own work
 b) do not use this software to any harmful / malicious end as decided by the Hypixel team or any governing body
 c) do not attempt to profit off of any software that makes use of this code

 */

package auta.forums.hypixel;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

public class AUTACore {
	
	// this is the delay between each iteration of your code
	// DON'T make it too small because it will have a negative
	// impact on both your connection and on the hypixel servers
	private final int ACTION_DELAY = 12000; //milliseconds


	public static void main(String[] args) {

	
	}
	public AUTACore(String uname, String passwd, String url) {

		WebClient logged_client = getLoggedWebClient(uname, passwd);
		//System.out.println(System.getProperty("user.dir"));
		
		AUTAction action = new AUTAction(logged_client);

		
		action.init();
		while(true) {
			action.loop();
			try {
				TimeUnit.SECONDS.sleep(ACTION_DELAY);
			}catch(Exception e) {}
			
			
		}
	}
	

	// returns a virtual 'client' that acts as a browser; used to interact with
	// the hypixel website in order to get data to \ from the thread 
	private WebClient getLoggedWebClient(String uname, String passwd) {

		//creating a WebClient object (a virtual browser, essentially)
		WebClient webClient = new WebClient(BrowserVersion.CHROME);

		//CSS will just make things messier, probably don't need to turn it on 
		webClient.getOptions().setCssEnabled(false);

		//the WebClient may act weird if you enable javascript
		webClient.getOptions().setJavaScriptEnabled(false); 

		//we want redirects for CloudFlare, as well as to make everything easier on us
		webClient.getOptions().setRedirectEnabled(true);

		//other stuff...
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getCache().setMaxSize(0);
		webClient.waitForBackgroundJavaScript(10000);
		webClient.setJavaScriptTimeout(10000);
		webClient.waitForBackgroundJavaScriptStartingBefore(7000);

		try {

			HtmlPage login_page = webClient.getPage("https://hypixel.net/login/");

			//waiting so cloudflare forwards us to hypixel's site
			//since this is the very first page our browser connects to 
			synchronized(login_page) {
				int time_to_wait = 7000; //milliseconds
				login_page.wait(time_to_wait);

			}

			//the login form is the 2nd 'form' on the login page (the first one is the search bar)
			int login_form_index = 1;
			HtmlForm login_form = login_page.getForms().get(login_form_index);

			//entering Player login info 
			HtmlTextInput login_val = login_form.getInputByName("login");
			login_val.type(uname);

			HtmlPasswordInput password_val = login_form.getInputByName("password");
			password_val.type(passwd);


			//login form only has one button
			List<Object> buttons = login_page.getByXPath("//button[@class='button--primary button button--icon button--icon--login']");
			HtmlButton button = (HtmlButton) buttons.get(0);

			//submitting form and logging in to hypixel forums!
			HtmlPage result_page = button.click();


			if(result_page.asXml().contains("<div class=\"blockMessage blockMessage--error blockMessage--iconic\">")) {

				//login was not accepted - did you double check your username and password? (they're case sensitive)
				System.out.println("Login failed! Aborting! Check your username and password.");
				System.exit(23);
			}

			//successfully logged in the client! yay!
			return webClient;

		} catch (FailingHttpStatusCodeException e) {
			System.out.println("FailingHttpStatusCodeException - is it CloudFlare, or maybe a bad url?");
		} catch (MalformedURLException e) {
			System.out.println("MalformedURLException - is something wrong with your URLs?");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException - that's strange...");
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			System.out.println("IOException - that's weird... are you practicing thread safety?");
			e.printStackTrace();
		}

		//something went horribly wrong...
		System.exit(2);
		return null;
	}
}