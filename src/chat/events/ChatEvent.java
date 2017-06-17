package chat.events;

import chat.ChatSite;
import utils.json.JsonObject;
import static utils.Utils.getNumValueJSON;
import static utils.Utils.getStringValueJSON;
import static utils.Utils.unescapeHtml;
import static utils.Utils.search;
import static utils.WebRequest.GET;

public class ChatEvent extends JsonObject<ChatEvent>
{
	/**The type of event.*/
	private final EventType event_type;
	/**The event's timestamp.*/
	private final long time_stamp;
	/**The event's id.*/
	private final long id;
	/**The id of the message affected.*/
	private final long message_id;
	/**The content of the event.*/
	private String content;
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
		CHATSITE = chatsite;
		event_type = EventType.forEventId(getNumValueJSON("event_type", raweventjson));
		time_stamp = getNumValueJSON("time_stamp", raweventjson);
		id = getNumValueJSON("id", raweventjson);
		message_id = getNumValueJSON("message_id", raweventjson);
		message_stars = getNumValueJSON("message_stars", raweventjson);
		/*
		 * Note: the value of the JSON "content" may not be the entire message.
		 */
		switch(event_type){
			case MessagePosted:
			case MessageEdited:
			case UserMentioned:
			case MessageReply:
				content = getRawMessageContentNoException(message_id);
				break;
			default:
				content = getStringValueJSON("content", raweventjson);
		}
		content = unescapeHtml(content);
		if(content.contains("class=\"onebox"))
			content=chatsite.getUrl()+search("href=\"([^\"]+)", content);
		content = content.trim();
		room_id = getNumValueJSON("room_id", raweventjson);
		room_name = unescapeHtml(getStringValueJSON("room_name", raweventjson));
		user_id = getNumValueJSON("user_id", raweventjson);
		user_name = unescapeHtml(getStringValueJSON("user_name", raweventjson));
		parent_id = getNumValueJSON("parent_id", raweventjson);
		target_user_id = getNumValueJSON("target_user_id", raweventjson);
		//System.out.println("Received event: "+raweventjson);
	}
	
	/**
	 * For debug purposes only.
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

	private String getRawMessageContentNoException(final long message_id)
	{
		try
		{
			if(message_id==0)
				return null;
			return GET("https://"+CHATSITE.getUrl()+"/message/"+message_id);
		}
		catch(Exception e)
		{
			System.err.println("Failed to read message id "+message_id);
			return null;
		}
	}
}
