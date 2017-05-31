package bot;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.security.sasl.AuthenticationException;
import static bot.Utils.search;
import static bot.Utils.urlencode;
import static bot.WebRequest.GET;
import static bot.WebRequest.POST;

public class ChatIO
{
	private volatile String fkey;
	
	public ChatIO(String username, String password) throws AuthenticationException
	{
		login(username, password);
	}
	private void login(String email, String password) throws AuthenticationException
	{
		try
		{
			int roomid = 1;
			String[][] headers = {
					{"Accept", "*/*"},
					{"Accept-Encoding", "gzip, deflate"},
					{"Accept-Language", "en-US,en;q=0.5"},
					{"DNT", "1"},
					{"Cache-Control", "no-cache"},
					//{"Connection", "keep-alive"},
					{"Content-Type", "application/x-www-form-urlencoded"},
					{"Upgrade-Insecure-Requests", "1"},
					{"User-Agent", "Mozilla/5.0 (X11; Linux i686; rv:17.0) Gecko/20100101 Firefox/17.0"},
			};
			WebRequest.setDefaultHeaders(headers);
			String response_text = GET("https://stackoverflow.com/users/login?ssrc=head&returnurl=https%3a%2f%2fstackoverflow.com%2f");
			
			String fkey = search("name=\"fkey\"\\s+value=\"([^\"]+)\"", response_text);
			String post_data = urlencode(new String[][]{
				{"email", email},
				{"fkey", fkey},
				{"oauth_server", ""},
				{"oauth_version", ""},
				{"openid_identifier", ""},
				{"openid_username", ""},
				{"password", password},
				{"ssrc", "head"},
			});
			response_text = POST("https://stackoverflow.com/users/login?ssrc=head&returnurl=https%3a%2f%2fstackoverflow.com%2f", post_data);
			response_text = GET("http://chat.stackoverflow.com/rooms/" + roomid);
			fkey = search("name=\"fkey\"\\s+type=\"hidden\"\\s+value=\"([^\"]+)\"", response_text);
			this.fkey = fkey;
		}
		catch(Exception e)
		{
			throw new AuthenticationException("Failed to login", e);
		}
	}
	public void logout() throws AuthenticationException
	{
		try
		{
			String response_text = GET("https://stackoverflow.com/users/logout");
			String fkey = search("name=\"fkey\"\\s+value=\"([^\"]+)\"", response_text);
			POST("https://stackoverflow.com/users/logout", urlencode(new String[][]{
				{"fkey", fkey},
				{"returnUrl", "https%3A%2F%2Fstackoverflow.com%2F"}
			}));
		}
		catch(Exception e)
		{
			throw new AuthenticationException("Failed to logout", e);
		}
	}
	private static volatile String t;
	public ChatEventList getChatEvents(List<Long> rooms)
	{
		try
		{
			String response = POST("https://chat.stackoverflow.com/events", urlencode(new String[][]{
				{"fkey", fkey},
				{"r1", t},
				{"r139", t},
				{"r138769", t},
			}));
			System.out.println(response);
			try
			{
				t = search("\"t\"\\:(\\d+)", response);
			}
			catch(IllegalArgumentException iae)
			{
			}
			finally
			{
				try
				{
					Thread.sleep(10000);
				}
				catch(InterruptedException ie)
				{
					ie.printStackTrace();
				}
			}
			try
			{
				Pattern p = Pattern.compile("\"e\"\\:\\[([^\\]]+)");
				Matcher m = p.matcher(response);
				List<String> eventlists = new LinkedList<String>();
				while(m.find())
					eventlists.add(m.group(1));
				return new ChatEventList(eventlists);
				//TODO
			}
			catch(Exception e)
			{
				//No events
			}
		}
		catch(IOException ioe)
		{
			// TODO Auto-generated catch block
			ioe.printStackTrace();
		}
		return null;
	}
	public void putMessage(long roomid, String message)
	{
		try
		{
			POST("https://chat.stackoverflow.com/chats/"+roomid+"/messages/new", urlencode(new String[][]{
				{"fkey", fkey},
				{"text", message}
			}));
		}
		catch(Exception e)
		{
			throw new IllegalArgumentException("Failed to send message to room id "+roomid, e);
		}
	}
}
