package bot;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;
import static bot.WebRequest.*;

public class ChatIO
{
	private volatile String fkey;
	
	public ChatIO(String username, String password)
	{
		login(username, password);
	}
	/**
	 * 
	 * @param email
	 * @param password
	 * @return The cookies that were set
	 */
	private void login(String email, String password)
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
			
			String fkey = search("name=\""+"fkey"+"\"\\s+value=\"([^\"]+)\"", response_text);
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
			throw new Error(e);
		}
	}
	private static String urlencode(String[] mapping)
	{
		return urlencode(new String[][]{mapping});
	}
	private static String urlencode(String[][] map)
	{
		try
		{
			String encoded = "";
			for(int i=0;i<map.length;++i){
				String[] parmap = map[i];
				if(parmap.length!=2)
					throw new IllegalArgumentException("Invalid parameter mapping "+java.util.Arrays.deepToString(parmap));
				//TODO
				encoded += parmap[0] + "=" + 
						URLEncoder.encode(parmap[1], "UTF-8") +
						(i+1<map.length ? "&" : "");
			}
			return encoded;
		}
		catch(UnsupportedEncodingException e)
		{
			throw new InternalError(e);
		}
	}
	/**
	 * 
	 * @param regex
	 * @param in
	 * @return The match in <code>in</code> for the first group in <code>regex</code>.
	 * @throws IllegalArgumentException  If no match was found or if there is no capturing group in the regex
	 */
	private static String search(String regex, String in)
	{
		try
		{
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(in);
			m.find();
			return m.group(1);
		}
		catch(Exception e)
		{
			throw new IllegalArgumentException(e);
		}
	}
	public MessageList getMessages(int roomid, int msgCount)
	{
		try
		{
			String rawmessages = POST("https://chat.stackoverflow.com/chats/"+roomid+"/events", urlencode(new String[][]{
				{"mode", "Messages"},
				{"msgCount", ""+msgCount}
			}));
			return new MessageList(rawmessages);
		}
		catch(Exception e)
		{
			throw new IllegalArgumentException("Failed to read messages from room id "+roomid, e);
		}
	}
	public void doSockets(int roomid)
	{
		try
		{
			String post_data = urlencode(new String[][]{
				{"fkey", fkey},
				{"roomid", ""+roomid}
			});
			POST("https://chat.stackoverflow.com/ws-auth", post_data);
			Socket sock = HttpsURLConnection.getDefaultSSLSocketFactory().createSocket("chat.sockets.overflow.com", 80);
			// I don't even know if this is correct :/
			String response = GET("https://chat.sockets.stackexchange.com/events/"+roomid+"/"+fkey);
			//TODO
		}
		catch(IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void putMessage(int roomid, String message)
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
