/**
Forum Thread Class
author: Ivan Volkov
date: 5/31/2020
version: 1.0.0
desc:
This is a class meant to retrieve and store all information about your thread.
You may want to extend it to add further functionality!

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
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

public class ForumThread {

	// if the thread is further than this many pages back in the list,
	// operations that require knowing where it is in the list will
	// only return cached values, and won't get fresh ones!
	private final int NUM_PAGES_TO_BE_IRRELEVANT = 5;

	/*
	 'static' variables; these may not change often (or at all)
	 however, they may change due to something unexpected 
	 (like your thread being moved by a staff member)
	 so be sure to account for that!
	 */

	// the url that a user would go to in order to view the thread
	private String thread_url; 

	// the 7 digit UUID for the thread; this will never change
	private String thread_id; 

	// the section of the forums that this thread is in (may change if thread is moved)
	private String section; 

	// the owner of the thread
	private String original_poster;

	// the current title of the thread (may or may not change, depending on what the software does)
	private String title;


	/*
	 'fluid' variables; these will likely change very often
	 it is recommended to run the the appropriate getter method
	 whenever you need to guarantee the most recent value of these variables
	 */

	// how many views the thread has
	private String num_views; 

	// how many replies the thread has
	private String num_replies; 

	// how many pages the thread has
	private String num_pages; 

	// how many positive reactions the thread has
	private String num_positive_reactions; 

	// the name of the last user to reply to the thread
	private String last_to_reply; 
	
	// a list of names of the last 5-15 people who replied to the thread (in order)
	private List<String> last_few_to_reply;

	// the time since the last user responded
	private String last_response_time; 

	// the page number of the thread in the forums
	private String page_on; 


	public ForumThread (String thread_url) {

		// makes sure the thread url passed in isn't weird / unusable
		this.thread_url = thread_url;
		this.thread_id = getThreadID();
		this.thread_url = "https://hypixel.net/threads/" + this.thread_id+"/";

	}


	// changes the thread title to the specified text
	public void setTitle(WebClient client, String title_text) {

		HtmlPage page = null;
		try {
			page = client.getPage(thread_url+"edit");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// getting the second form on the page; the first is the search bar
		HtmlForm form = page.getForms().get(1);
		HtmlTextInput new_title_input = form.getInputByName("title"); 
		
		if(!title_text.contains("[auta]")) {
			title_text += " [auta]";
		}
		
		new_title_input.setText(title_text);
		
		List<Object> buttons = page.getByXPath("//button[@class='button--primary button button--icon button--icon--save']");

		HtmlButton button = (HtmlButton) buttons.get(0);

		// pushing the 'submit' button
		try {
			button.click();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	// deletes all cached fluid values forcing them to be recalculated
	// call it as often as you need new values
	// don't overuse this or else you'll be making a LOT of requests
	// to hypixel's servers, which could break your program or get you in trouble
	public void clearCache() {

		this.page_on = null;
		this.last_response_time = null;
		this.last_to_reply = null;
		this.num_replies = null;
		this.title = null;
		this.section = null;		
		this.num_pages = null;
		this.last_few_to_reply = null;

		// only deletes cached valus for reactions and views
		// if 
		boolean is_thread_relevant = true;
		try {
			int page_in_listing = Integer.parseInt(this.page_on);
			if(page_in_listing >= (NUM_PAGES_TO_BE_IRRELEVANT-1))
				is_thread_relevant = false;

		}catch(NumberFormatException e) {
			is_thread_relevant = false;
		}
		// only deletes cached values for reactions / views if there is no risk
		// of the thread going irrelevant (and leaving null values inside cache)
		if(is_thread_relevant) {
			this.num_positive_reactions = null;
			this.num_views = null;
		}

	}


	// returns the amount of time passed (in minutes) since the last response
	public String getLastResponseTime(WebClient client) {

		// prevents too many un-needed requests
		// override this by using clearCache()
		if(this.last_response_time != null) {
			return this.last_response_time;
		}

		String full_url = "";
		String content = "";
		try {

			//using the same page to get both the URL and the html content
			HtmlPage last_page = client.getPage(thread_url+"latest");
			full_url = last_page.getUrl().toString();
			content = last_page.getWebResponse().getContentAsString();

		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String match_post_id_regex = "^https:\\/\\/hypixel.net\\/threads\\/[^\\/]+\\/(?:page-\\d+)?#post-(\\d+)$";
		String post_id = matchRegex(full_url, match_post_id_regex, 1).get(0);

		//chopping off the earlier responses
		content = content.substring(content.indexOf(post_id));

		String match_recent_time_regex = "title=\".*\">(.+)<\\/time>";
		String last_post_time = matchRegex(content, match_recent_time_regex, 1).get(0);

		this.last_response_time = last_post_time;
		return last_post_time;
	}


	// returns the username of the last user to reply
	public String getLastToReply(WebClient client) {

		// prevents too many un-needed requests
		// override this by using clearCache()
		if(this.last_to_reply != null) {
			return this.last_to_reply;
		}
		//getting URL of 'https://hypixel.net/threads/example-thread.1234567/latest'
		// the '/latest' at the end makes the page jump to last reply
		// we can get the name of the last replier from there
		String full_url = "";
		String content = "";
		try {
			HtmlPage last_page = client.getPage(thread_url+"latest");
			full_url = last_page.getUrl().toString();
			content = last_page.getWebResponse().getContentAsString();
		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String match_post_id_regex = "^https:\\/\\/hypixel.net\\/threads\\/[^\\/]+\\/(?:page-\\d+)?#post-(\\d+)$";
		List<String> post_ids = matchRegex(full_url, match_post_id_regex, 1);
		String post_id = post_ids.get(post_ids.size()-1);

		String match_last_poster_regex = "data-author=\"(.+)\" data-content=\"post-"+post_id+"\"";
		String last_poster = matchRegex(content, match_last_poster_regex, 1).get(0);

		this.last_to_reply = last_poster;
		return last_poster;

	}
	
	// returns a lis of the last 3 - 23 players to have replied to the thread
	public List<String> getLastFewToReply(WebClient client) {

		// prevents too many un-needed requests
		// override this by using clearCache()
		if(this.last_few_to_reply != null) {
			return this.last_few_to_reply;
		}
		//getting URL of 'https://hypixel.net/threads/example-thread.1234567/latest'
		// the '/latest' at the end makes the page jump to last reply
		// we can get the name of the last replier from there
		List<String> last_posters = new LinkedList<String>();
		do {
		String full_url = "";
		String content = "";
		try {
			HtmlPage last_page = client.getPage(thread_url+"latest");
			full_url = last_page.getUrl().toString();
			content = last_page.getWebResponse().getContentAsString();
		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String match_posters_regex = "js-inlineModContainer  \" data-author=\"([^\"]+)\"";
		
		last_posters.addAll(matchRegex(content, match_posters_regex, 1));
		
		//if the current page only has one or two responses, it'll check the prior page too
		}while(last_posters.size() < 3 && (this.getNumPages(client) != "1"));
			
		List<String> toReturn = new LinkedList<String>();
		for(int i = last_posters.size()-1; i >=0;  i--) {
			toReturn.add(last_posters.get(i));
		}
		this.last_few_to_reply = toReturn;
		return toReturn;

	}



	// returns the total number of positive reactions the thread has received
	public String getNumPositiveReactions(WebClient client) {

		// prevents too many un-needed requests
		// override this by using clearCache()
		if(this.num_positive_reactions != null) {
			return this.num_positive_reactions;
		}

		getSection(client);
		getPageOn(client);
		try {
			Integer.parseInt(this.page_on);
		}catch(NumberFormatException e) {
			return this.num_positive_reactions;
		}

		String content = "";
		try {

			//if the thread is too far back in the listings (too old / irrelevant)
			// we give up on finding the current reactions and just use the cached number
			content = client.getPage("https://hypixel.net/forums/"+this.section+"/"+"page-"+page_on).getWebResponse().getContentAsString();

		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//2949136.*<dt>Views<\/dt> <dd>(.+)<\/dd>.*2949136

		int last_pos = content.lastIndexOf(this.thread_id);
		content = content.substring(0,last_pos);

		int first_pos = content.lastIndexOf(this.thread_id);
		content = content.substring(first_pos);

		content = content.replaceAll("\n"," ");

		String get_reactions_regex = "<div class=\"structItem-cell structItem-cell--meta\" title=\"First message reaction score: (\\d+)\">";

		List<String> reactions_list = matchRegex(content, get_reactions_regex,1);
		// returns the page as soon as the thread is seen on the current page
		if(reactions_list.size() == 1) {
			this.num_positive_reactions = reactions_list.get(0);
			return ""+reactions_list.get(0);
		}

		//this should never happen but who knows!
		return this.num_positive_reactions;

	}


	// returns the numbers of pages long the thread is
	public String getNumPages(WebClient client) {

		// prevents too many un-needed requests
		// override this by using clearCache()
		if(this.num_pages != null) {
			return this.num_pages;
		}

		//getting URL of 'https://hypixel.net/threads/example-thread.1234567/latest'
		// the '/latest' at the end makes the page jump to last reply
		// and the last reply is on the last page :)
		String full_url = "";
		try {
			full_url = client.getPage(this.thread_url+"latest").getUrl().toString();
		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String match_num_pages_regex = "^https:\\/\\/hypixel.net\\/threads\\/[^\\/]+\\/(?:page-(\\d+))?#post-\\d+$";
		List<String> toReturn = matchRegex(full_url, match_num_pages_regex,1);

		//the url won't include a page if it's on page 1
		if(toReturn.size() == 0) {
			this.num_pages = "1";
			return "1";
		}

		this.num_pages = ""+toReturn.get(0);
		return ""+toReturn.get(0);
	}


	// returns the number of replies the thread has received
	public String getNumReplies(WebClient client) {


		// prevents too many un-needed requests
		// override this by using clearCache()
		if(this.num_replies != null) {
			return this.num_replies;
		}

		int toReturn = 0;

		// taking a shortcut by finding the number of pages,
		// and multiplying that (minus 1) by the responses per page!
		String num_pages = getNumPages(client);
		int replies_per_page = 20;
		toReturn += (Integer.parseInt(num_pages) -1) * replies_per_page;

		String content = "";
		try {
			content = client.getPage(thread_url).getWebResponse().getContentAsString();
		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// counting up the number of responses on the last page ourselves!
		String get_num_replies_regex = "data-author=\".*\" data-content=\"post-\\d+\"";
		List<String> section_list = matchRegex(content, get_num_replies_regex);

		// subtracting 1 from the final result because the original poster doesn't count!
		this.num_replies = (toReturn + section_list.size()-1)+"";
		return (toReturn + section_list.size()-1)+"";
	}


	// returns the original creator of the thread (probably you?)
	public String getOriginalPoster(WebClient client) {
		if(this.original_poster != null)
			return this.original_poster;

		String content = "";
		try {
			content = client.getPage(thread_url).getWebResponse().getContentAsString();
		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String get_poster_regex = "<a href=\"\\/members\\/.+\\..+\\/\" class=\"username  u-concealed\" dir="
				+ "\"auto\" data-user-id=\".*\" data-xf-init=\"member-tooltip\">(.+)<\\/a>";
		List<String> section_list = matchRegex(content, get_poster_regex,1);
		this.original_poster = section_list.get(0);
		return section_list.get(0);
	}


	// returns the current title of the thread
	public String getTitle(WebClient client) {


		// prevents too many un-needed requests
		// override this by using clearCache()
		if(this.title != null) {
			return this.title;
		}

		String content = "";
		try {
			content = client.getPage(thread_url).getWebResponse().getContentAsString();
		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		content = content.substring(content.indexOf("<title>"),content.lastIndexOf("</title>")+10);

		String get_title_regex = "^<title>(.*).{3}Hypixel - Minecraft Server and Maps<\\/title>";

		List<String> section_list = matchRegex(content, get_title_regex,1);
		this.title = section_list.get(0);
		return section_list.get(0);
	}


	// returns the section of the forums the thread is in
	public String getSection(WebClient client) {

		// prevents too many un-needed requests
		// override this by using clearCache()
		if(this.section != null)
			return this.section;

		String content = "";
		try {
			content = client.getPage(thread_url).getWebResponse().getContentAsString();
		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String get_section_regex = "<a href=\"\\/forums\\/(.+)\\/\" itemprop=\"item\">";
		List<String> section_list = matchRegex(content, get_section_regex,1);
		this.section = section_list.get(0);
		return section_list.get(0);
	}


	// returns the page number that the thread is on in the forum threads list
	// this will request a bunch of pages very quickly, making it very intensive to use
	// so try to use this sparingly; if you need a rough idea just use the cached page number!
	public String getPageOn(WebClient client) {


		// prevents too many un-needed requests
		// override this by using clearCache()
		if(this.page_on != null) {
			return this.page_on;
		}


		int cur_page = 0;
		int max_pages = NUM_PAGES_TO_BE_IRRELEVANT;
		do {
			cur_page++;
			String content = "";
			try {
				content = client.getPage("https://hypixel.net/forums/"+this.section+"/page-"+cur_page).getWebResponse().getContentAsString();
			} catch (FailingHttpStatusCodeException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			content = content.replaceAll("\n", " ");

			String locate_thread_regex = "href=\"\\/threads\\/.+\\."+this.thread_id;

			// returns the page as soon as the thread is seen on the current page
			if(matchRegex(content, locate_thread_regex).size() != 0) {
				this.page_on = ""+cur_page;
				return ""+cur_page;
			}
		}while(cur_page <= max_pages);

		//returns X+ when the number of pages exceeds the limit
		this.page_on = max_pages+"+";
		return max_pages+"+";

	}


	// gets the number of views this thread has accumulated.
	// PS. - Hypixel's view counter is a bit strange and 
	// may not always represent the actual amount of views,
	// so use this feature sparingly!
	public String getNumViews(WebClient client) {


		// prevents too many un-needed requests
		// override this by using clearCache()
		if(this.num_views != null) {
			return this.num_views;
		}

		getSection(client);
		getPageOn(client);

		try {
			Integer.parseInt(this.page_on);
		}catch(NumberFormatException e) {
			return this.num_views;
		}

		String content = "";
		try {

			//if the thread is too far back in the listings (too old / irrelevant)
			// we give up on finding the current views and just use the cached number
			content = client.getPage("https://hypixel.net/forums/"+this.section+"/"+"page-"+page_on).getWebResponse().getContentAsString();

		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		int first_pos = content.indexOf(this.thread_id+"");
		int last_pos = content.lastIndexOf(this.thread_id+"");
		content = content.substring(first_pos-1,last_pos+(this.thread_id.length())+2);
		content = content.replaceAll("\n"," ");

		String get_views_regex = this.thread_id+".*<dt>Views<\\/dt> <dd>(.+)<\\/dd>.*"+this.thread_id;

		List<String> views_list = matchRegex(content, get_views_regex,1);

		// returns the page as soon as the thread is seen on the current page
		if(views_list.size() == 1) {
			num_views = views_list.get(0);
			return ""+views_list.get(0);
		}

		//this should never happen but who knows, right!
		return num_views;
	}


	// gets the thread ID value (used to search for other information about the thread)
	private String getThreadID() {

		// prevents too many un-needed requests
		if(thread_id != null) {
			return this.thread_id;
		}
		String thread_id_regex = "^https:\\/\\/hypixel.net\\/threads\\/(?:[\\w-]+\\.)?(\\d+)\\/$";
		List<String> result = matchRegex(thread_url,thread_id_regex,1);

		//didn't find the id in the url provided
		if(result.size() == 0) {
			System.err.println("Invalid thread URL!");
			System.err.println("make sure it looks like: https://hypixel.net/threads/this-is-an-example.1234567/");
			System.err.println("or like: https://hypixel.net/threads/1234567/");
			System.exit(-3);
		}

		this.thread_id = result.get(0);
		return result.get(0);
	}


	// returns all results of a regex filter on a body of text, defaults to group 0 (the entire result)
	private List<String> matchRegex(String body, String pattern) {
		return matchRegex(body, pattern, 0);
	}


	// returns all results of a regex filter on a body of text, allows to specify the group to return
	private List<String> matchRegex(String body, String pattern, int group_num) {
		LinkedList<String> toReturn = new LinkedList<String>();

		Pattern view_pattern = Pattern.compile(pattern);
		Matcher view_matcher = view_pattern.matcher(body);

		// finds each matching phrase and includes it in the list
		while(view_matcher.find()) {
			toReturn.add(view_matcher.group(group_num));
		}

		return toReturn;
	}
}