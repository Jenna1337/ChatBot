package chat.users;

import chat.ChatSite;
import utils.json.JsonObject;
import static utils.Utils.getBooleanValueJSON;
import static utils.Utils.getNumValueJSON;
import static utils.Utils.getStringValueJSON;
import static utils.Utils.unescapeHtml;
import static utils.WebRequest.GET;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

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
	/**
	 * Corresponds to the user's profile picture.<br/>
	 * Either contains a link to the profile image with a "!" before the "http", or
	 * <pre>"//www.gravatar.com/avatar/" + email_hash + "?s=32&d=identicon&r=PG"</pre>
	 */
	private URL profilePictureURL;
	/**The user's "about" text.*/
	private String user_message;
	/**The URL of the user's profile page.*/
	private final URL profileUrl;
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
	
	public ChatUser(long userid, ChatSite chatsite)
	{
		CHATSITE=chatsite;
		String rawjson;
		try
		{
			rawjson = GET("https://chat.stackoverflow.com/users/thumbs/"+
					userid+"?showUsage=true");
		}
		catch(IOException e)
		{
			throw new IllegalArgumentException(e);
		}
		id=getNumValueJSON("id", rawjson);
		try
		{
			profileUrl=new URL("https://"+chatsite.getUrl()+"/users/"+id);
		}
		catch(MalformedURLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new InternalError(e);
		}
		updateInfo(rawjson, chatsite);
	}
	public ChatUser(String rawjson, ChatSite chatsite)
	{
		CHATSITE=chatsite;
		id=getNumValueJSON("id", rawjson);
		try
		{
			profileUrl=new URL("https://"+chatsite.getUrl()+"/users/"+id);
		}
		catch(MalformedURLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new InternalError(e);
		}
		updateInfo(rawjson, chatsite);
	}
	public void updateInfo(String rawjson, ChatSite chatsite)
	{
		name=unescapeHtml(getStringValueJSON("name", rawjson));
		try{
		email_hash=unescapeHtml(getStringValueJSON("email_hash", rawjson).substring(1));
		}catch(Exception e){
			//TODO
			e.printStackTrace();
		}
		try
		{
			profilePictureURL = new URL(
					email_hash.startsWith("!/")
					?("https://"+chatsite.getUrl()+email_hash.substring(1))
							:email_hash.startsWith("!")
							?(email_hash.substring(1))
									:("https://www.gravatar.com/avatar/"+email_hash+"?s=128"));
		}
		catch(MalformedURLException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		reputation=getNumValueJSON("reputation", rawjson);
		is_moderator=getBooleanValueJSON("is_moderator", rawjson);
		is_owner=getBooleanValueJSON("is_owner", rawjson);
		last_post=getNumValueJSON("last_post", rawjson);
		last_seen=getNumValueJSON("last_seen", rawjson);
		try{
			String response = GET("https://chat.stackexchange.com/users/thumbs/"+id);
			user_message = getStringValueJSON("user_message", response);
			usage = getStringValueJSON("usage", response);
			//TODO are these required?
			/*Chat rooms we can invite this user to*/
			//"invite_targets":null
			//"issues":null
			//boolean is_registered = getBooleanValueJSON("is_registered", response);
		}catch(Exception e){
			
		}
	}
	public long getId(){
		return id;
	}
	public String getName(){
		return name;
	}
	public void setName(String newName){
		name = newName;
	}
	public String getEmailhash(){
		return email_hash;
	}
	public void setEmailhash(String emailHash){
		email_hash = emailHash;
	}
	public URL getProfilePictureURL()
	{
		return profilePictureURL;
	}
	public void setProfilePictureURL(URL profilePictureURL)
	{
		this.profilePictureURL = profilePictureURL;
	}
	public String getUserMessage(){
		return user_message;
	}
	public void setUserMessage(String userMessage){
		user_message = userMessage;
	}
	public URL getProfileUrl(){
		return profileUrl;
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
