package chat.events;

import chat.ChatSite;
import utils.json.JsonObject;
import static utils.Utils.getNumValueJSON;
import static utils.Utils.getStringValueJSON;
import static utils.Utils.replaceAllAll;
import static utils.Utils.unescapeHtml;
import static utils.WebRequest.GET;

public class ChatEvent extends JsonObject<ChatEvent>
{
	/**The raw JSON data<br>
	 * Note: Used in {@link #toString()} and {@link #varDumpAll()}
	 */
	@SuppressWarnings("unused")
	private final String rawEventJson;
	/**The type of event.*/
	private final EventType event_type;
	/**The event's timestamp.*/
	private final long time_stamp;
	/**The event's id.*/
	private final long id;
	/**The id of the message affected.*/
	private final long message_id;
	/**The content of the event.*/
	private final String content;
	/**The room id in which this event took place.*/
	private final long room_id;
	/**The room name in which this event took place.*/
	private final String room_name;
	/**The id of the user that initiated this event.*/
	private final long user_id;
	/**The username of the user that initiated this event.*/
	private final String user_name;
	/**The id of the event that is referred to by this event.*/
	private final long parent_id;
	/**The id of the user that is targeted by this event.*/
	private final long target_user_id;
	/**The number of stars this message has.*/
	private final long message_stars;
	/**The corresponding chat site.*/
	private final ChatSite CHATSITE;
	
	public ChatEvent(final String raweventjson, final ChatSite chatsite)
	{
		rawEventJson = raweventjson;
		CHATSITE = chatsite;
		event_type = EventType.forEventId(getNumValueJSON("event_type", raweventjson));
		time_stamp = getNumValueJSON("time_stamp", raweventjson);
		id = getNumValueJSON("id", raweventjson);
		message_id = getNumValueJSON("message_id", raweventjson);
		message_stars = getNumValueJSON("message_stars", raweventjson);
		room_id = getNumValueJSON("room_id", raweventjson);
		room_name = unescapeHtml(getStringValueJSON("room_name", raweventjson));
		user_id = getNumValueJSON("user_id", raweventjson);
		user_name = unescapeHtml(getStringValueJSON("user_name", raweventjson));
		parent_id = getNumValueJSON("parent_id", raweventjson);
		target_user_id = getNumValueJSON("target_user_id", raweventjson);
		
		String plaincontent = null;
		try{
			plaincontent = GET("https://"+CHATSITE.getUrl()+"/messages/"+room_id+'/'+message_id+"?plain=true");
		}
		catch(Exception e1){
			try{
				plaincontent = unescapeHtml(getStringValueJSON("content", raweventjson));
			}catch(Exception e3){
				System.err.println("Full Erroring text:\n"+raweventjson);
				plaincontent="";
			}
		}
		
		content = plaincontent;
		
		//System.out.println("Received event: "+raweventjson);
	}
	
	/**
	 * For debug purposes only.<br>
	 * @see {@link #varDumpAll()}
	 */
	@Override
	public String toString(){
		return varDumpAll();
	}
	
	public EventType getEventType(){
		return event_type;
	}
	public long getTimeStamp(){
		return time_stamp;
	}
	public long getId(){
		return id;
	}
	public long getMessageId(){
		return message_id;
	}
	public String getContent(){
		return content;
	}
	public long getMessage_stars()
	{
		return message_stars;
	}
	public long getRoomId(){
		return room_id;
	}
	public String getRoomName(){
		return room_name;
	}
	public long getUserId(){
		return user_id;
	}
	public String getUserName(){
		return user_name;
	}
	public long getParentId(){
		return parent_id;
	}
	public long getTargetUserId(){
		return target_user_id;
	}
	public ChatSite getChatSite(){
		return CHATSITE;
	}
	public int compareTo(ChatEvent o)
	{
		long t1=this.getTimeStamp();
		long t2=o.getTimeStamp();
		return t1>t2?1:(t1<t2?-1:0);
	}
	private static final String[][] escapees = {
			{"\\\\","\\\\\\\\"},
			{"\r","\\\\r"},
			{"\n","\\\\n"},
			{"\"","\\\\\""},
	};
	public String getEscapedContent()
	{
		return replaceAllAll(this.getContent(), escapees);
	}
}
