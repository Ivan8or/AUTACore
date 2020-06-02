/**
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

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

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
	private final int ACTION_DELAY = 12; //seconds


	public static void main(String[] args) {
		if(args.length != 0) {
			boolean headless = false;
			if(args[2].contentEquals("true"))
				headless = true;
				
			new AUTACore(args[0],args[1], headless);
		}
		new AUTACore("","", false);

	}
	public AUTACore(String uname, String passwd, boolean headless) {
		if(!headless)
			try {
			new MenuWindow();
			}catch(Exception e) {
				System.out.println("graphics GUI is unavailable on headless machines!");
			}
		else {
			WebClient logged_client = getLoggedWebClient(uname, passwd);
			startAction(logged_client);
		}

	}

	// starts the AUTAction
	private void startAction(WebClient client) {

		// we use a thread so that the GUI can remain interactive
		Thread action_thread = new Thread() {

			public void run() {
				AUTAction action = new AUTAction(client);
				action.init();
				while(true) {
					action.loop();
					try {
						TimeUnit.SECONDS.sleep(ACTION_DELAY);
					}catch(Exception e) {}
				}
			}

		};
		action_thread.start();
	}

	public class MenuWindow extends JFrame {
		public MenuWindow () {

			int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
			int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;

			this.setDefaultCloseOperation(EXIT_ON_CLOSE);
			this.setLayout(null);
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.setAlwaysOnTop(false);
			this.getContentPane().setBackground(new Color(255,255,244));
			this.setResizable(false);
			this.setTitle("Hypixel AUTACore Controller");
			this.setSize((screen_width/4), (screen_height/2));
			this.setLocation(screen_width/8, screen_height/8);

			JTextField uname_field = new JTextField();
			this.add(uname_field);

			int uname_xpos = (screen_width/20);
			int uname_ypos = (screen_height/192) + (screen_height/14);
			int uname_width = (screen_width/5) - (screen_width/120);
			int uname_height = (screen_height/36);
			uname_field.setBounds(uname_xpos, uname_ypos, uname_width, uname_height);

			JLabel uname_label = new JLabel("username: ");
			this.add(uname_label);
			uname_label.setBounds((screen_width/192),uname_ypos, (screen_width/20), uname_height );



			JPasswordField passwd_field = new JPasswordField();
			this.add(passwd_field);
			int passwd_ypos = (screen_height/120) + (screen_height/10);
			passwd_field.setBounds(uname_xpos, passwd_ypos, uname_width, uname_height);

			JLabel passwd_label = new JLabel("password: ");
			this.add(passwd_label);
			passwd_label.setBounds((screen_width/192),passwd_ypos, (screen_width/20), uname_height );

			JLabel title = new JLabel("Thread Controller ");
			this.add(title);
			title.setBounds((screen_width/21),(screen_height/128), (screen_width/5), (screen_height/18));
			title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));


			JButton login_button = new JButton("Login");
			this.add(login_button);
			login_button.setBounds((screen_width/13)+(screen_width/80),(screen_height/7), (screen_width/16), (screen_height/32));

			login_button.addActionListener(new ActionListener() {

				private boolean logged_in = false;

				public void actionPerformed(ActionEvent arg0) {

					if(logged_in)
						return;

					if(uname_field.getText().contentEquals("") || passwd_field.getText().contentEquals(""))
						return;

					WebClient client = getLoggedWebClient(uname_field.getText(),passwd_field.getText());
					uname_field.setText("");
					passwd_field.setText("");
					logged_in = true;
					startAction(client);

				}

			});

			this.setVisible(true);
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
				try {
					int seconds_delay = 25;
					TimeUnit.SECONDS.sleep(seconds_delay); 
				}catch(Exception e) {}
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
				System.out.println("username: "+uname);
				System.out.println("password: "+passwd);
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
		//catch (InterruptedException e) {
		//	System.out.println("IOException - that's weird... are you practicing thread safety?");
		//	e.printStackTrace();
		//}

		//something went horribly wrong...
		System.exit(2);
		return null;
	}
}