package chat.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.security.sasl.AuthenticationException;
import chat.ChatSite;
import chat.events.ChatEventList;
import chat.users.ChatUser;
import chat.users.ChatUserList;
import utils.WebRequest;
import utils.js.JavaScriptArray;
import static utils.Utils.search;
import static utils.Utils.urlencode;
import static utils.WebRequest.GET;
import static utils.WebRequest.POST;

public class ChatIO
{
	private static final String protocol = "https";
	private static final String fkeyHtmlRegex = "name=\"fkey\"\\s+(?>type=\"hidden\"\\s+)?value=\"([^\"]+)\"";
	private static final String useridHtmlRegex = "id=\"active-user\" class=\"user-container user-(\\d+)\"";
	private static final String replStr = "\u007F";
	private static final String replStrUrlEnc = urlencode(replStr);
	private volatile String fkey;
	private final ChatSite CHATSITE;
	private ChatUser me;
	private boolean logged_in;
	private SortedSet<Long> rooms = Collections.synchronizedSortedSet(new TreeSet<Long>());
	private boolean firstTime = true;
	private static Object lock_logged_in = new Object();
	private static Object lock_roomcacheupdate = new Object();
	private SortedSet<Long> initialRooms;
	static{
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
	}
	public ChatIO(final ChatSite chatsite, final String email, final String password, Long[] longs) throws AuthenticationException, IllegalStateException
	{
		login(chatsite, email, password);
		if(!isLoggedIn())
			throw new IllegalStateException("Not logged in to "+chatsite);
		CHATSITE = chatsite;
		String url = protocol+"://"+CHATSITE.getUrl()+"/rooms/" + longs[0];
		try
		{
			String response_text = GET(url);
			fkey = search(fkeyHtmlRegex, response_text);
			this.fkey = fkey;
		}
		catch(Exception e){
			throw new AuthenticationException("Failed to get fkey from "+url, e);
		}
		try{
			long myUserId=Long.parseLong(search(useridHtmlRegex, GET(url)));
			System.out.println(CHATSITE.name()+" user id: "+myUserId);
			me = new ChatUser(myUserId, CHATSITE);
		}catch(Exception e){
			new AuthenticationException("Failed to get myUserId from "+url, e).printStackTrace();
		}
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable(){
			public void run()
			{
				try{
					logout();
				}
				catch(AuthenticationException e){
					e.printStackTrace();
				}
			}
		}));
		this.initialRooms = new TreeSet<Long>(Arrays.asList(longs));
		joinRoom(longs);
	}
	private static volatile boolean loggedin = false;
	private synchronized void login(final ChatSite site, final String email, final String password) throws AuthenticationException
	{
		if(!loggedin)
			loop:
				while(true){
					try{
						synchronized(lock_logged_in){
							String response_text;
							String fkey;
							switch(site){
								case STACKOVERFLOW:
									response_text = GET("https://stackoverflow.com/users/login");
									fkey = search(fkeyHtmlRegex, response_text);
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
									response_text = POST("https://stackoverflow.com/users/login", post_data);
									GET("https://"+site.getUrl());
									break loop;
								case STACKEXCHANGE:
									response_text = GET("https://stackexchange.com/users/login");
									//response_text = GET(response_text);
									fkey = search(fkeyHtmlRegex,response_text);
									response_text = POST("https://meta.stackexchange.com/users/login?returnurl=https%3a%2f%2fstackexchange.com%2fusers%2flogin-or-signup%2fdelegated%3freturnurl%3dhttp%253a%252f%252fchat.stackexchange.com", urlencode(new String[][]{
										{"cdl","1"},
										{"email", email},
										{"password", password},
										{"fkey", fkey},
									}));
									//GET(search("var target = \'([^\']+)", response_text));
									break loop;
								case METASTACKEXCHANGE:/*
							response_text = POST("https://stackexchange.com/users/signin", urlencode(new String[][]{
								{"from", "https://meta.stackexchange.com/users/login#log-in"},
								{"_", Long.toString(getUnixTimeMillis())}
							}));
							response_text = GET(response_text);
							fkey = search(fkeyHtmlRegex,response_text);
							response_text = POST("https://meta.stackexchange.com/users/login", urlencode(new String[][]{
								{"email", email},
								{"password", password},
								{"affId", "11"},
								{"fkey", fkey},
							}));
							GET(search("var target = \'([^\']+)", response_text));*/
									GET("https://"+site.getUrl());
									break loop;
								default:
									throw new UnsupportedOperationException("Site \""+site.name()+"\" does not have login handling.");
							}
						}
					}
					catch(java.net.SocketTimeoutException e){
						try
						{
							Thread.sleep(5000);
						}
						catch(InterruptedException e1)
						{
							e1.printStackTrace();
						}
						continue loop;
					}
					catch(Exception e){
						throw new AuthenticationException("Failed to login", e);
					}
				}
		System.out.println("Successfully logged in to "+site.getUrl());// Success
		logged_in=true;
	}
	private synchronized void logout() throws AuthenticationException
	{
		try
		{
			synchronized(lock_logged_in){
				if(!logged_in)
					throw new IllegalStateException("Not logged in.");
				rooms.forEach(this::leaveRoom);
				//leaveRoom(rooms.toArray(new Long[0]));
				String response_text = GET("https://stackoverflow.com/users/logout");
				String fkey = search(fkeyHtmlRegex, response_text);
				POST(protocol+"://stackoverflow.com/users/logout", urlencode(new String[][]{
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
	/**
	 * Gets the events to be handled from this ChatIO's main chat server.
	 * @return a list containing all unread events for this ChatIO.
	 */
	public ChatEventList getChatEvents()
	{
		synchronized(lock_roomcacheupdate)
		{
			String getStr = cacheChatEventGetterString.replace(replStrUrlEnc, t);
			try
			{
				String response = POST(protocol+"://"+CHATSITE.getUrl()+"/events", getStr);
				try
				{
					t = search("\"t\"\\:(\\d+)", response, false);
				}
				catch(IllegalArgumentException iae)
				{
					if(t==null)
						t = "0";
				}
				LinkedList<String> eventlists = new LinkedList<String>();
				try
				{
					String jsf = "var a=" + response + ";var list=[];for(var i in a){"
							+ " var b=a[i].e;"
							+ " if(b)"
							+ "  for(var j in b)"
							+ "   list.push(JSON.stringify(b[j]))"
							+ "}"
							+ "return list;";
					String[] result = JavaScriptArray.fromResult(jsf).to(String[].class);
					
					for(String event : result)
					{
						if(!eventlists.contains(event))
							eventlists.add(event);
					}
				}
				catch(Exception e)
				{
					//No events
				}
				if(firstTime)
				{
					firstTime = false;
					System.out.println("Joined "+CHATSITE.name());
					return new ChatEventList();
				}
				return new ChatEventList(eventlists, CHATSITE);
			}
			catch(Exception e)
			{
				String errMsg = e.getMessage();
				Matcher m = Pattern.compile("HTTP response code.*?(\\d+)").matcher(errMsg);
				if(m.find()){
					int httpcode;
					switch(httpcode=Integer.parseInt(m.group(1))){
						case 500:
							System.out.println("The following chat encountered an internal server error: "+CHATSITE);
							break;
						case 503:
							System.out.println("The following chat is currently unavailable: "+CHATSITE);
							break;
						default:
							System.err.println("Encountered HTTP code "+httpcode+" on "+CHATSITE);
							e.printStackTrace();
							break;
					}
					return new ChatEventList();
				}
				System.err.println("Failed to get messages for "+CHATSITE);
				e.printStackTrace();
				return new ChatEventList();
			}
		}
	}
	public void putMessage(final long roomid, final String message)
	{
		if(CHATSITE==ChatSite.METASTACKEXCHANGE && roomid!=1153) return;
		if(message.trim().isEmpty()) return;
		try
		{
			POST(protocol+"://"+CHATSITE.getUrl()+"/chats/"+roomid+"/messages/new", urlencode(new String[][]{
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
			POST(protocol+"://"+CHATSITE.getUrl()+"/messages/"+messageid, urlencode(new String[][]{
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
			POST(protocol+"://"+CHATSITE.getUrl()+"/users/invite", urlencode(new String[][]{
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
	public void bookmarkConversation(final long roomid, final String title, final long firstMessageId, final long lastMessageId)
	{
		try
		{
			POST(protocol+"://"+CHATSITE.getUrl()+"/conversation/new", urlencode(new String[][]{
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
	/**
	 * This should only be used to help control an off-topic discussion.
	 * @param roomid - The id of the room to be put in timeout.
	 * @param duration - The duration, in seconds, the room will be in timeout.
	 * @param reason - Explanation as to why the room is in timeout.
	 */
	public void roomTimeout(final long roomid, final long duration, final String reason)
	{
		try
		{
			POST(protocol+"://"+CHATSITE.getUrl()+"/rooms/timeout/"+roomid, urlencode(new String[][]{
				{"fkey", fkey},
				{"duration", ""+duration},
				{"reason", reason}
			}));
			
		}
		catch(Exception e)
		{
			throw new IllegalArgumentException("Failed to set timeput for room id "+
					roomid+".", e);
		}
	}
	public boolean isLoggedIn()
	{
		synchronized(lock_logged_in){
			return logged_in;
		}
	}
	public void joinRoom(final Long... room)
	{
		synchronized(rooms){
			for(Long r : room)
				rooms.add(r);
			updateChatEventGetterStringCache();
		}
	}
	public void leaveRoom(final Long... room)
	{
		boolean isLogout = false;
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		for(StackTraceElement stele : trace){
			String methodname = stele.getMethodName();
			if(methodname.contains("logout"))
			{
				isLogout = true;
				break;
			}
		}
		ArrayList<Long> cantleave = new ArrayList<>();
		synchronized(rooms){
			for(Long r : room){
				if((this.CHATSITE.equals(ChatSite.STACKOVERFLOW)
						|| this.CHATSITE.equals(ChatSite.STACKEXCHANGE))
						&& (r==1) && !isLogout){
					System.out.println("Attempted to leave sandbox room of "+CHATSITE);
				}
				else if(rooms.size()==1 && !isLogout){
					System.out.println("Attempted to leave last room of "+CHATSITE);
				}
				else if(initialRooms.contains(r)){
					cantleave.add(r);
				}
				else if(rooms.remove(r))
				{
					try
					{
						POST(protocol+"://"+CHATSITE.getUrl()+"/chats/leave/"+r, urlencode(new String[][]{
							{"fkey",fkey},
							{"quiet", "true"}
						}));
					}
					catch(IOException e)
					{
						System.out.println("Failed to leave "+CHATSITE+" room "+r);
						e.printStackTrace();
					}
				}
			}
			updateChatEventGetterStringCache();
		}
	}
	public SortedSet<Long> getRoomSet()
	{
		synchronized(rooms){
			return rooms;
		}
	}
	public boolean isInRoom(final Long roomid)
	{
		synchronized(rooms){
			return rooms.contains(roomid);
		}
	}
	public void changeBotAboutText(final String newtext)
	{
		try
		{
			String fkey = search(fkeyHtmlRegex, GET(protocol+"://chat.stackoverflow.com/users/"+getMyUserId()));
			POST(protocol+"://chat.stackoverflow.com/users/usermessage/"+getMyUserId(), urlencode(new String[][]{
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
	public ChatUserList getUserInfo(final long roomid, final Long... userid)
	{
		try
		{
			String useridstring = ""+userid[0];
			for(int i=1;i<userid.length;++i)
				useridstring+=","+userid[i];
			return new ChatUserList(POST(protocol+"://chat.stackexchange.com/user/info", urlencode(new String[][]{
				{"ids", useridstring},
				{"roomId", ""+roomid}
			})), CHATSITE);
		}
		catch(Exception e)
		{
			throw new IllegalArgumentException("Failed to get user info from site "+
					CHATSITE+" room "+roomid+" userid "+Arrays.toString(userid), e);
		}
	}
	public long getMyUserId(){
		return me.getId();
	}
	private static Comparator<String[]> mappedStringArrayComparator = new Comparator<String[]>()
	{
		public int compare(final String[] o1, final String[] o2)
		{
			return o1[0].compareTo(o2[0]);
		}
	};
	private void updateChatEventGetterStringCache(){
		synchronized(lock_roomcacheupdate){
			ArrayList<String[]> listtopost = new ArrayList<>();
			listtopost.add(new String[]{"fkey", fkey});
			
			for(long roomid : rooms)
				listtopost.add(new String[]{"r"+roomid, replStr});
			Collections.sort(listtopost, mappedStringArrayComparator);
			cacheChatEventGetterString = urlencode(listtopost.toArray(new String[][]{}));
		}
	}
	public String getFkey(){
		return fkey;
	}
	public String getT(){
		return t;
	}
	public static String getProtocol(){
		return protocol;
	}
	public String getMyUsername(){
		return me.getName();
	}
	public ChatUser getMyUserInstance(){
		return me;
	}
}
