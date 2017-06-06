package chat.users;

import chat.ChatSite;
import chat.JsonObject;
import static utils.Utils.getBooleanValueJSON;
import static utils.Utils.getNumValueJSON;
import static utils.Utils.getStringValueJSON;
import static utils.Utils.unescapeHtml;
import static utils.WebRequest.GET;

public class ChatUser extends JsonObject<ChatUser>
{
	/**The user's id.*/
	private final long id;
	/**The user's username.*/
	private String name;
	/**
	 * Corresponds to the user's profile picture.<br/>
	 * Either contains a link to the profile image with a "!" before the "http", or
	 * <pre>"//www.gravatar.com/avatar/" + email_hash + "?s=32&d=identicon&r=PG"</pre>
	 */
	private String email_hash;
	/**The user's "about" text.*/
	private String user_message;
	/**The URL of the user's profile page.*/
	private String profileUrl;
	/**The website where the user signed up*/
	private String host;
	/**The user's reputation.*/
	private long reputation;
	/**Indicates whether the user is a moderator or not.*/
	private boolean is_moderator;
	/**Indicates whether the user is an owner or not.*/
	private boolean is_owner;
	/**The timestamp the user last posted a message.*/
	private long last_post;
	/**The timestamp the user last was seen.*/
	private long last_seen;
	/**The HTML for the user's activity column spark line chart.*/
	private String usage;
	/**The corresponding chat site.*/
	private final ChatSite CHATSITE;
	
	public ChatUser(String rawjson, ChatSite chatsite)
	{
		CHATSITE=chatsite;
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
			usage = getStringValueJSON("usage", response);
			//"invite_targets":null
			//"issues":null
			//boolean is_registered = getBooleanValueJSON("is_registered", response);
			//boolean may_pairoff = getBooleanValueJSON("may_pairoff", response);
		}catch(Exception e){
			
		}
	}
	public long getId(){
		return id;
	}
	public String getName(){
		return name;
	}
	public String getEmailhash(){
		return email_hash;
	}
	public String getUserMessage(){
		return user_message;
	}
	public String getProfileUrl(){
		return profileUrl;
	}
	public String getHost(){
		return host;
	}
	public long getReputation(){
		return reputation;
	}
	public boolean isModerator(){
		return is_moderator;
	}
	public boolean isOwner(){
		return is_owner;
	}
	public long getLastPost(){
		return last_post;
	}
	public long getLastSeen(){
		return last_seen;
	}
	public String getUsage(){
		return usage;
	}
	public ChatSite getChatSite(){
		return CHATSITE;
	}
	public int compareTo(ChatUser o)
	{
		long t1=this.getId();
		long t2=o.getId();
		return t1>t2?1:(t1<t2?-1:0);
	}
}
