package chat.users;

import chat.ChatSite;
import static utils.Utils.getBooleanValueJSON;
import static utils.Utils.getNumValueJSON;
import static utils.Utils.getStringValueJSON;
import static utils.Utils.unescapeHtml;
import static utils.WebRequest.GET;

public class ChatUser implements Comparable<ChatUser>
{
	private final long id;
	private String name;
	/**
	 * may contain a link to the profile image with a "!" before the "http" 
	 */
	private String email_hash;
	private String user_message;
	private String profileUrl;
	private String host;
	private long reputation;
	private boolean is_moderator;
	private boolean is_owner;
	private long last_post;
	private long last_seen;
	public ChatUser(String rawjson, ChatSite chatsite)
	{
		id=getNumValueJSON("id", rawjson);
		name=unescapeHtml(getStringValueJSON("name", rawjson));
		email_hash=unescapeHtml(getStringValueJSON("email_hash", rawjson).substring(1));
		reputation=getNumValueJSON("reputation", rawjson);
		is_moderator=getBooleanValueJSON("is_moderator", rawjson);
		is_owner=getBooleanValueJSON("is_owner", rawjson);
		last_post=getNumValueJSON("last_post", rawjson);
		last_seen=getNumValueJSON("last_seen", rawjson);
		try{
			String response = GET("https://chat.stackexchange.com/users/thumbs/"+id);
			user_message = getStringValueJSON("user_message", response);
			profileUrl = getStringValueJSON("profileUrl", response);
			host = getStringValueJSON("host", response);
			//TODO are these required?
			//"rooms":[{"id":1,"name":"Sandbox","last_post":null,"activity":0}]
			//"usage":null
			//"invite_targets":null
			//"issues":null
			//boolean is_registered = getBooleanValueJSON("is_registered", response);
			//boolean may_pairoff = getBooleanValueJSON("may_pairoff", response);
		}catch(Exception e){
			
		}
	}
	public long getId()
	{
		return id;
	}
	public String getName()
	{
		return name;
	}
	public String getEmailhash()
	{
		return email_hash;
	}
	public String getUserMessage()
	{
		return user_message;
	}
	public String getProfileUrl()
	{
		return profileUrl;
	}
	public String getHost()
	{
		return host;
	}
	public long getReputation()
	{
		return reputation;
	}
	public boolean isModerator()
	{
		return is_moderator;
	}
	public boolean isOwner()
	{
		return is_owner;
	}
	public long getLastPost()
	{
		return last_post;
	}
	public long getLastSeen()
	{
		return last_seen;
	}
	public int compareTo(ChatUser o)
	{
		long t1=this.getId();
		long t2=o.getId();
		return t1>t2?1:(t1<t2?-1:0);
	}
}
