package bot.events;

import java.io.IOException;
import java.net.MalformedURLException;
import static bot.Utils.getNumValueJSON;
import static bot.Utils.getStringValueJSON;
import static bot.web.WebRequest.GET;

public class ChatEvent
{
	public enum EventType{
		MessagePosted(1),
		MessageEdited(2),
		UserEntered(3),
		UserLeft(4),
		RoomNameChanged(5),
		MessageStarred(6),
		DebugMessage(7),
		UserMentioned(8),
		MessageFlagged(9),
		MessageDeleted(10),
		FileAdded(11),
		ModeratorFlag(12),
		UserSettingsChanged(13),
		GlobalNotification(14),
		AccessLevelChanged(15),
		UserNotification(16),
		Invitation(17),
		MessageReply(18),
		MessageMovedOut(19),
		MessageMovedIn(20),
		TimeBreak(21),
		FeedTicker(22),
		UserSuspended(29),
		UserMerged(30),
		UserNameOrAvatarChanged(34)
		;
		final int id;
		EventType(int id){
			this.id=id;
		}
		public int getEventTypeId(){
			return this.id;
		}
		public static EventType forEventId(long l){
			for(EventType evt : EventType.values())
				if(l==evt.getEventTypeId())
					return evt;
			return null;
		}
	}
	private final EventType event_type;
	private final long time_stamp;
	private final long id;
	
	private final long message_id;
	private String content;
	private final long room_id;
	private final String room_name;
	private final long user_id;
	private final String user_name;
	
	private final long parent_id;
	private final long target_user_id;
	
	private final String CHATSITE;
	
	public ChatEvent(String raweventjson, String chatsite)
	{
		CHATSITE = chatsite;
		event_type = EventType.forEventId(getNumValueJSON("event_type", raweventjson));
		time_stamp = getNumValueJSON("time_stamp", raweventjson);
		id = getNumValueJSON("id", raweventjson);
		message_id = getNumValueJSON("message_id", raweventjson);
		/*
		 * Note: the value of the JSON "content" may not be the entire message.
		 */
		content = null;
		room_id = getNumValueJSON("room_id", raweventjson);
		room_name = getStringValueJSON("room_name", raweventjson);
		user_id = getNumValueJSON("user_id", raweventjson);
		user_name = getStringValueJSON("user_name", raweventjson);
		parent_id = getNumValueJSON("parent_id", raweventjson);
		target_user_id = getNumValueJSON("target_user_id", raweventjson);
	}
	
	private String getRawMessageContentNoException(long message_id)
	{
		try
		{
			return getRawMessageContent(message_id);
		}
		catch(IOException e)
		{
			if(message_id!=0)
				System.err.println("Warning: could not read raw content of message id "+message_id);
			e.printStackTrace();
			return "";
		}
	}
	private String getRawMessageContent(long message_id) throws IOException
	{
		try{
			return GET("https://"+CHATSITE+"/message/"+message_id);
		}catch(MalformedURLException murle){
			throw new IllegalArgumentException("Invalid message id "+message_id);
		}catch(IOException ioe){
			throw new IOException("Failed to read message id "+message_id, ioe);
		}
	}
	
	@Override
	public String toString()
	{
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
		if(content!=null)
			return content;
		return (content=getRawMessageContentNoException(message_id));
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
	private String varDumpAll(){
		String result="ChatEvent[";
		java.lang.reflect.Field[] fields = this.getClass().getDeclaredFields();
		for(int i=0;i<fields.length-1;++i)
			result+=varDump(fields[i].getName())+",";
		result+=fields[fields.length-1]+"]";
		return result;
	}
	private String varDump(String varname){
		String result="\""+varname+"\":\"";
		try
		{
			return result+this.getClass().getDeclaredField(varname).get(varname)+"\"";
		}
		catch(IllegalArgumentException | IllegalAccessException
				| NoSuchFieldException | SecurityException e)
		{
			// Something horrible happened!
			System.err.println("Failed to get value of variable "+varname+
					" of object at "+Integer.toHexString(System.identityHashCode(this)));
			e.printStackTrace();
			return result+"null\"";
		}
	}
}
