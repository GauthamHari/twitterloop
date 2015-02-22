package services;

import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import commands.DB;

@Path("/twitter")
public class TwitterService {
	String consumerKey = "C3EiPjBiEhFInJnugvJ0nuyUM";
	String consumerSecret = "qIz9TK0p6d2CWBmsLgHmen7pkMEpbnL3ipX5oJpPlO0y6oCNSN";

	@GET
	@Path("/request")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAuthentication(@Context HttpServletResponse response,
			@Context HttpServletRequest request, @QueryParam("user") String user) {
		Twitter twitter = new TwitterFactory().getInstance();
		try {
			twitter.setOAuthConsumer(consumerKey, consumerSecret);
		} catch (Exception e) {
			System.out.println("The OAuthConsumer has likely already been set");
		}

		try {
			RequestToken requestToken = twitter.getOAuthRequestToken();
			
			request.getSession().setAttribute("requestToken", requestToken);
			request.getSession().setAttribute("username", user);
			response.sendRedirect(requestToken.getAuthorizationURL());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	@GET
	@Path("/success")
	@Produces(MediaType.APPLICATION_JSON)
	public String success(@QueryParam("oauth_token") String otoken,
			@QueryParam("oauth_verifier") String oauth_verifier,
			@Context HttpServletRequest request) {
		Twitter twitter = new TwitterFactory().getInstance();
		Status tweetStatus = null;
		AccessToken accessToken = null;
		RequestToken requestToken = null;
		String user = null;
		try {
			twitter.setOAuthConsumer(consumerKey, consumerSecret);
		} catch (Exception e) {
			System.out
					.println("The OAuthConsumer has likely already been set, ignore");
		}
		try {
			requestToken = (RequestToken) request.getSession().getAttribute(
					"requestToken");
			if (requestToken == null)
				throw new Exception();
		} catch (Exception e1) {
			return "Could not find valid Request Token";
		}
		try {
			accessToken = twitter.getOAuthAccessToken(requestToken,
					oauth_verifier);
		} catch (TwitterException e1) {
			return "Could not find valid token";
		}
		try {
			DB db = new DB();
			user = (String) request.getSession().getAttribute("username");
			db.saveOAuthToken(accessToken.getToken(), user, "twitter",
					accessToken.getTokenSecret());
		} catch (Exception e) {
			System.out.println("Could not store access token to DB");
		}
		
		try {
			tweetStatus = twitter.updateStatus("Registration message from Gautham Hari's heroku app 'twitterloop'." 
					+ " Your OAuthToken has been saved." + System.currentTimeMillis());
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		if (tweetStatus != null)
			return "Please check your Twitter, your tweet has been posted:"
					+ tweetStatus.getText();
		else
			return "BOO! didn't work";
	}

	@GET
	@Path("/status")
	@Produces(MediaType.APPLICATION_JSON)
	public String success(@QueryParam("user") String user) {
		Twitter twitter = new TwitterFactory().getInstance();
		Status tweetStatus = null;
		AccessToken accessToken = null;
		try {
			twitter.setOAuthConsumer(consumerKey, consumerSecret);
		} catch (Exception e) {
			System.out.println("The OAuthConsumer has likely already been set");
		}
		try {
			DB db = new DB();
			accessToken = db.getOAuthToken(user, "twitter");
			twitter.setOAuthAccessToken(accessToken);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			tweetStatus = twitter.updateStatus("Status Update Message from Gautham Hari's Heroku app 'twitterloop' "
					+ System.currentTimeMillis());
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		if (tweetStatus != null)
			return "Please check your Twitter, your tweet has been posted: "
					+ tweetStatus.getText();
		else
			return "BOO! didn't work";
	}
	
	@GET
	@Path("/post")
	@Produces(MediaType.APPLICATION_JSON)
	public String postGroupMessage() {
		Twitter twitter = new TwitterFactory().getInstance();
		Status tweetStatus = null;
		AccessToken accessToken = null;
		ArrayList<String> temp = new ArrayList<String>();
		try {
			twitter.setOAuthConsumer(consumerKey, consumerSecret);
		} catch (Exception e) {
			System.out.println("The OAuthConsumer has likely already been set");
		}
		try {
			DB db = new DB();
			temp = db.getUsernames();
		}
		catch (Exception e1) {
			e1.printStackTrace();
		}
		for (String user : temp)
		{
			DB db = new DB();
			accessToken = db.getOAuthToken(user, "twitter");
			twitter.setOAuthAccessToken(accessToken); 
			try {
				tweetStatus = twitter.updateStatus("Group message from Gautham Hari's Heroku app 'twitterloop' "
					+ System.currentTimeMillis());
			} catch (TwitterException e) {
				e.printStackTrace();
			}
		}
		if (tweetStatus != null)
			return "The group message has been posted to all twitter accounts";
		else
			return "BOO! didn't work";
	}
}
