package bot.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.security.sasl.AuthenticationException;
import bot.events.ChatEventList;
import static bot.Utils.search;
import static bot.Utils.urlencode;
import static bot.web.WebRequest.GET;
import static bot.web.WebRequest.POST;

public class ChatIO
{
	private static final String fkeyHtmlRegex = "name=\"fkey\"\\s+type=\"hidden\"\\s+value=\"([^\"]+)\"";
	private static final String replStr = "\u007F";
	private static final String replStrUrlEnc = urlencode(replStr);
	private volatile String fkey;
	private final String CHATSITE;
	private final long myUserId;
	private static boolean logged_in;
	private SortedSet<Long> rooms = Collections.synchronizedSortedSet(new java.util.TreeSet<Long>());
	private static Object lock_logged_in = new Object();
	
	public ChatIO(final String chatsite) throws AuthenticationException, IllegalStateException
	{
		if(!isLoggedIn())
			throw new IllegalStateException("Not logged in.");
		CHATSITE = chatsite;
		long roomid = 1;
		try
		{
			String response_text = GET("http://"+CHATSITE+"/rooms/" + roomid);
			fkey = search(fkeyHtmlRegex, response_text);
			this.fkey = fkey;
		}
		catch(Exception e)
		{
			throw new AuthenticationException("Failed to get fkey from "+CHATSITE+"/rooms/" + roomid, e);
		}
		//TODO get chat bot's user ID
		//Note it's different for different sites
		myUserId=0;
	}
	public static synchronized void login(final String email, final String password) throws AuthenticationException
	{
		try
		{
			synchronized(lock_logged_in){
				if(!logged_in){
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
					System.out.println("Successfully logged in.");// Success
					logged_in=true;
				}
				else
					System.out.println("Already logged in.");
			}
		}
		catch(Exception e)
		{
			throw new AuthenticationException("Failed to login", e);
		}
	}
	public static synchronized void logout() throws AuthenticationException
	{
		try
		{
			synchronized(lock_logged_in){
				if(!logged_in)
					throw new IllegalStateException("Not logged in.");
				String response_text = GET("https://stackoverflow.com/users/logout");
				String fkey = search("name=\"fkey\"\\s+value=\"([^\"]+)\"", response_text);
				POST("https://stackoverflow.com/users/logout", urlencode(new String[][]{
					{"fkey", fkey},
					{"returnUrl", "https%3A%2F%2Fstackoverflow.com%2F"}
				}));
				logged_in=false;
			}
		}
		catch(Exception e)
		{
			throw new AuthenticationException("Failed to logout", e);
		}
	}
	private String cacheChatEventGetterString;
	private String t="0";
	public ChatEventList getChatEvents()
	{
		String getStr = cacheChatEventGetterString.replace(replStrUrlEnc, t);
		try
		{
			String response = POST("https://"+CHATSITE+"/events", getStr);
			try
			{
				t = search("\"t\"\\:(\\d+)", response);
			}
			catch(IllegalArgumentException iae)
			{
			}
			LinkedList<String> eventlists = new LinkedList<String>();
			try
			{
				Pattern p = Pattern.compile("\"e\"\\:\\[([^\\]]+)");
				Matcher m = p.matcher(response);
				while(m.find())
					eventlists.add(m.group(1));
			}
			catch(Exception e)
			{
				//No events
			}
			return new ChatEventList(eventlists, CHATSITE);
		}
		catch(Exception ioe)
		{
			System.err.println("Failed to get messages for "+CHATSITE);
			ioe.printStackTrace();
		}
		return null;
	}
	public void putMessage(final long roomid, final String message)
	{
		try
		{
			POST("https://"+CHATSITE+"/chats/"+roomid+"/messages/new", urlencode(new String[][]{
				{"fkey", fkey},
				{"text", message}
			}));
		}
		catch(Exception e)
		{
			throw new IllegalArgumentException("Failed to send message to room id "+roomid, e);
		}
	}
	public void editMessage(final long messageid, final String message)
	{
		try
		{
			POST("https://"+CHATSITE+"/messages/"+messageid, urlencode(new String[][]{
				{"fkey", fkey},
				{"text", message}
			}));
		}
		catch(Exception e)
		{
			throw new IllegalArgumentException("Failed to edit message id "+messageid, e);
		}
	}
	public void inviteUser(final long userid, final long roomid)
	{
		try
		{
			POST("https://"+CHATSITE+"/users/invite", urlencode(new String[][]{
				{"fkey", fkey},
				{"UserId", ""+userid},
				{"RoomId", ""+roomid}
			}));
		}
		catch(Exception e)
		{
			throw new IllegalArgumentException("Failed to invite user id "+
					userid+" to room id "+roomid+".", e);
		}
	}
	public void bookmarkConversation(final long roomid, final String title, long firstMessageId, long lastMessageId)
	{
		try
		{
			POST("https://"+CHATSITE+"/conversation/new", urlencode(new String[][]{
				{"fkey", fkey},
				{"roomId", ""+roomid},
				{"firstMessageId", ""+firstMessageId},
				{"lastMessageId", ""+lastMessageId},
				{"title", title}
			}));
			
		}
		catch(Exception e)
		{
			throw new IllegalArgumentException("Failed to bookmark room id "+
					roomid+" from message id "+firstMessageId+" to "+
					lastMessageId+".", e);
		}
	}
	public static boolean isLoggedIn()
	{
		synchronized(lock_logged_in){
			return logged_in;
		}
	}
	public void addRoom(Long... room)
	{
		for(Long r : room)
			rooms.add(r);
		updateChatEventGetterStringCache();
	}
	public void removeRoom(Long... room)
	{
		for(Long r : room)
			rooms.remove(r);
		updateChatEventGetterStringCache();
	}
	public SortedSet<Long> getRoomSet()
	{
		return rooms;
	}
	public boolean isInRoom(Long roomid)
	{
		return rooms.contains(roomid);
	}
	public void changeBotAboutText(String newtext)
	{
		try
		{
			String fkey = search(fkeyHtmlRegex, GET("https://chat.stackoverflow.com/users/"+myUserId));
			POST("https://chat.stackoverflow.com/users/usermessage/"+myUserId, urlencode(new String[][]{
				{"fkey", fkey},
				{"message", newtext}
			}));
		}
		catch(Exception e)
		{
			throw new IllegalArgumentException("Failed to change bot's about text for site "+
					CHATSITE, e);
		}
	}
	private static Comparator<String[]> mappedStringArrayComparator = new Comparator<String[]>()
	{
		public int compare(String[] o1, String[] o2)
		{
			return o1[0].compareTo(o2[0]);
		}
	};
	private void updateChatEventGetterStringCache(){
		ArrayList<String[]> listtopost = new ArrayList<>();
		listtopost.add(new String[]{"fkey", fkey});
		for(long roomid : rooms)
			listtopost.add(new String[]{"r"+roomid, replStr});
		Collections.sort(listtopost, mappedStringArrayComparator);
		cacheChatEventGetterString = urlencode(listtopost.toArray(new String[][]{}));
	}
}
