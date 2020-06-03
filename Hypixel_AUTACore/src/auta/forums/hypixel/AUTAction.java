/**
Auto-Update-ThreAd Action Class
author: Ivan Volkov
date: 5/31/2020
version: 1.0.0
desc:
This is the class players are encouraged to extend and rewrite in
order to create a automatic thread of their own!

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

import java.util.List;

import com.gargoylesoftware.htmlunit.WebClient;

public class AUTAction {
	
	// the virtual browser that is required to work with forum threads
	private WebClient client;
	
	// we create the virtual client in the AUTACore class
	public AUTAction(WebClient client) {
		this.client = client;
	}
	
	// my threads; feel free to delete / make your own
	private ForumThread best_thread;
	private ForumThread love_thread;

	// some variables I use in multiple functions; feel free to delete / make your own
	private String prev_name;
	private String old_first_recent = "";
	private String old_second_recent = "";


	// any code in here will only be called once; use it to initialize your threads
	public void init() {
		
		// you will not be able to edit the content in these forum links unless you log in with the account that posted them in the first place
		best_thread = new ForumThread("https://hypixel.net/threads/mathewbeau-is-possibly-the-most-famous-player-on-skyblock-auto.2949136/");
		love_thread = new ForumThread("https://hypixel.net/threads/skyblock-romance-are-you-ready-to-find-your-true-love-or-simply-have-some-fun-auta.2956439/");
		
		
		// changing some variables, you can delete this
		prev_name = "";
	}

	
	// any code in here will be run once every [delay] seconds
	// use this for the logic your threads need
	public void loop() {


		romanceThreadFunction();
		love_thread.clearCache();

		bestPlayerFunction();
		best_thread.clearCache();
		System.out.println("--pause--");
	}
	
	// the function that handles all logic for the "PlayerName is the best player on Skyblock!" thread
	// it's messy because I don't want people to reuse it; make your own thread! :)
	public void bestPlayerFunction() {


		String recent_user = best_thread.getLastToReply(client);

		if(!prev_name.contentEquals(recent_user)) {

			String[] options = {"weirdest","wackiest","smexiest","best","smartest","thiccest",
					"hottest", "most beautiful", "funniest", "most intense", "strongest",
					"bravest","greatest","moistest", "weakest", "stinkiest", "skinniest",
					"nicest","itchiest","laziest","wealthiest","classiest", "cutest",
					"most pogchamp","most famous", "oldest","glitchiest"
			};


			String next_adj = options[(int)(Math.random()*options.length)];
			prev_name = recent_user;
			String new_title = ""+ recent_user + " is possibly the "+next_adj+" player on skyblock [auta]";

			int super_rare = (int) (Math.random()* 100);
			if(recent_user.contentEquals("Ivan8or")) {
				new_title = "Nobody is the best player on skyblock [auta]";
				super_rare = 0;
			}
			if(super_rare == 69) {
				System.out.println("super secret title!");
				new_title = ""+ recent_user + " is absolutely the "+next_adj+" player on skyblock [auta]";
				new_title = new_title.toUpperCase();
			}
			best_thread.setTitle(client, new_title);
			System.out.println("Best thread: "+ new_title);
		}
	}
	
	// the function that handles all logic for the "Skyblock Romance! Player1 and Player2 caught kissing..." thread
	// it's messy because I don't want people to reuse it; make your own thread! :)
	public void romanceThreadFunction() {

		List<String> recent_users = love_thread.getLastFewToReply(client);

		String prev = "";
		for(int i = 0; i < recent_users.size(); i++) {

			if(recent_users.get(i).contentEquals(prev)) {
				recent_users.remove(i);
				i--;
			}
			else {
				prev = recent_users.get(i);

			}

		}
		String most_recent = "Ivan8or";
		String second_recent = "Ivan8or";
		try {
			most_recent = recent_users.get(0);
			second_recent = recent_users.get(1);
		}catch(Exception e) {
			System.err.println("FAILURE!");
			return;
		}

		if(!most_recent.contentEquals(second_recent)
				&& (!most_recent.contentEquals(old_first_recent)
						|| !second_recent.contentEquals(old_second_recent))) {

			old_first_recent = most_recent;
			old_second_recent = second_recent;
			String[] location_options = {
					"player","Village","Birch Forest","Spruce Woods","Howling Cave"
					,"Graveyard","Spider's Den","Blazing Fortress","Gunpowder Mines","Lapis Quarry"
					,"Diamond Reserves","Obsidian Sanctuary","Barnyard","Mushroom Desert","Winter Island"
					, "End Island" 
			};

			String[] action_options = {
					"kissed","hugged","twerked together","smooched","had sexy times", "gazed into each other's eyes",
					"talked dirty", "locked lips", "did the dirty", "drank out of the same potion", "were caught making out"

			};

			String[] relative_options = {
					"at the","inside the","under the", "behind the", "in front of the", "above the"
			};

			String action = action_options[(int)(Math.random()*action_options.length)];
			String location = location_options[(int)(Math.random()*location_options.length)];
			String relative = relative_options[(int)(Math.random()*relative_options.length)];

			String new_title = "Skyblock Romance! " + second_recent +
					" and " + most_recent + 
					" " + action + 
					" " + relative + 
					" " + location + "! Scandalous!";

			if(location.contentEquals("player")) {
				if(Math.random() < .5)
					location = second_recent+"'s Island";
				else
					location = most_recent+"'s Island";
				new_title = "Skyblock Romance! " + second_recent + " and " + most_recent + " " + action + " at " + location + "! Scandalous!";
			}

			love_thread.setTitle(client, new_title);
			System.out.println("Love thread: "+ new_title + " [auta]");
		}
	}

}
