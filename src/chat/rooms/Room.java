package chat.rooms;

import chat.ChatSite;
import chat.JsonObject;
import static utils.Utils.getStringValueJSON;
import static utils.Utils.getBooleanValueJSON;
import static utils.WebRequest.POST;
import java.util.Arrays;

public class Room extends JsonObject<Room>
{
	/**The room's id.*/
	private long id;
	/**The room's name.*/
	private String name;
	/**The room's description.*/
	private String description;
	/**{@code true} iff the room is a favorite of the active user.*/
	private boolean isFavorite;
	/**The HTML for the room's activity column spark line chart.*/
	private String usage;
	/**This room's tags.*/
	private String[] tags;
	/**The corresponding chat site.*/
	private final ChatSite CHATSITE;
	
	public Room(ChatSite chatsite, long roomid){
		CHATSITE=chatsite;
		id = roomid;
		try
		{
			String response = POST("https://chat.stackoverflow.com/rooms/thumbs/138769?showUsage=true&host=", "");
			name = getStringValueJSON("name", response);
			description = getStringValueJSON("description", response);
			isFavorite = getBooleanValueJSON("isFavorite", response);
			usage = getStringValueJSON("usage", response);
			tags = getStringValueJSON("tags", response).replaceAll("(?i)<((\\/)a|a\\s+[^>]+)>", "").split(" ");
		}
		catch(Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public long getId(){
		return id;
	}
	public String getName(){
		return name;
	}
	public String getDescription(){
		return description;
	}
	public boolean isFavorite(){
		return isFavorite;
	}
	public String getUsage(){
		return usage;
	}
	public String[] getTags()
	{
		return Arrays.copyOf(tags, tags.length);
	}
	public ChatSite getChatSite(){
		return CHATSITE;
	}
	public int compareTo(Room o)
	{
		int sitecomp = this.CHATSITE.compareTo(o.CHATSITE);
		if(sitecomp!=0)
			return sitecomp;
		return ((Long)this.id).compareTo(o.id);
	}
}
