package auta.forums.hypixel;

import com.gargoylesoftware.htmlunit.WebClient;

public class AUTAction {
	public AUTAction(WebClient client) {
		this.client = client;
	}
	
	private WebClient client;
	private ForumThread thread;
	
	public void init() {
		thread = new ForumThread("https://hypixel.net/threads/mathewbeau-is-possibly-the-most-famous-player-on-skyblock-auto.2949136/");
	}
	
	public void loop() {
		
	}
}
