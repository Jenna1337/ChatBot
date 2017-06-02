package bot.events;

import java.lang.reflect.Field;
import static bot.Utils.getNumValueJSON;
import static bot.Utils.getStringValueJSON;
import static bot.web.WebRequest.GET;

public class ChatEvent
{
	public enum EventType{
		/**
		 * <dt><code>{@link #getEventTypeId()} = 1</code></dt><br/>
		 * Somebody posted a new message
		 */
		MessagePosted(1),
		/**
		 * <dt><code>{@link #getEventTypeId()} = 2</code></dt><br/>
		 * Somebody edited a message
		 */
		MessageEdited(2),
		/**<dt><code>{@link #getEventTypeId()} = 3</code></dt><br/>
		 * Somebody joined the room
		 */
		UserEntered(3),
		/**
		 * <dt><code>{@link #getEventTypeId()} = 4</code></dt><br/>
		 * Somebody left the room
		 */
		UserLeft(4),
		/**
		 * <dt><code>{@link #getEventTypeId()} = 5</code></dt><br/>
		 * The room name or description was changed.
		 * TODO: Verify
		 */
		RoomNameChanged(5),
		/**
		 * <dt><code>{@link #getEventTypeId()} = 6</code></dt><br/>
		 * A post was starred.
		 * TODO: Verify
		 */
		MessageStarred(6),
		/**
		 * <dt><code>{@link #getEventTypeId()} = 7</code></dt><br/>
		 * TODO: Figure out what this is
		 */
		DebugMessage(7),
		/**
		 * <dt><code>{@link #getEventTypeId()} = 8</code></dt><br/>
		 * Somebody pinged the user
		 */
		UserMentioned(8),
		/**
		 * <dt><code>{@link #getEventTypeId()} = 9</code></dt><br/>
		 * A post was flagged.
		 * TODO: Verify; Might not be possible.
		 */
		MessageFlagged(9),
		/**
		 * <dt><code>{@link #getEventTypeId()} = 10</code></dt><br/>
		 * Somebody deleted a message.
		 */
		MessageDeleted(10),
		/**
		 * <dt><code>{@link #getEventTypeId()} = 11</code></dt><br/>
		 * TODO: Figure out what this is
		 */
		FileAdded(11),
		/**
		 * <dt><code>{@link #getEventTypeId()} = 12</code></dt><br/>
		 * A post was flagged.
		 * TODO: Verify; Might not be possible.
		 */
		ModeratorFlag(12),
		/**
		 * <dt><code>{@link #getEventTypeId()} = 13</code></dt><br/>
		 * Somebody blocked or unblocked somebody.
		 * TODO: Verify
		 */
		UserSettingsChanged(13),
		/**
		 * <dt><code>{@link #getEventTypeId()} = 14</code></dt><br/>
		 * TODO: Figure out what this is
		 */
		GlobalNotification(14),
		/**
		 * <dt><code>{@link #getEventTypeId()} = 15</code></dt><br/>
		 * Somebody changed somebody's access level<br/>
		 * User affected: <code>"target_user_id"</code><br/>
		 * Possible values:<br/>
		 * <ul style="list-style:none">
		 * <li><code>"content":"Access now (default)"</code></li>
		 * <li><code>"content":"Access now read-only"</code></li>
		 * <li><code>"content":"Access now read-write"</code></li>
		 * <li><code>"content":"Access now owner"</code></li><ul>
		 */
		AccessLevelChanged(15),
		/**
		 * <dt><code>{@link #getEventTypeId()} = 16</code></dt><br/>
		 * TODO: Figure out what this is
		 */
		UserNotification(16),
		/**
		 * <dt><code>{@link #getEventTypeId()} = 17</code></dt><br/>
		 * Somebody was invited to another room.
		 * TODO: Verify
		 */
		Invitation(17),
		/**
		 * <dt><code>{@link #getEventTypeId()} = 18</code></dt><br/>
		 * Somebody responded to one of our messages.
		 */
		MessageReply(18),
		/**
		 * <dt><code>{@link #getEventTypeId()} = 19</code></dt><br/>
		 * Messages were moved out of the room.
		 */
		MessageMovedOut(19),
		/**
		 * <dt><code>{@link #getEventTypeId()} = 20</code></dt><br/>
		 * Messages were moved into the room.
		 */
		MessageMovedIn(20),
		/**
		 * <dt><code>{@link #getEventTypeId()} = 21</code></dt><br/>
		 * TODO: Figure out what this is
		 */
		TimeBreak(21),
		/**
		 * <dt><code>{@link #getEventTypeId()} = 22</code></dt><br/>
		 * The ticker feed added new feed items.
		 */
		FeedTicker(22),
		/**
		 * <dt><code>{@link #getEventTypeId()} = 29</code></dt><br/>
		 * TODO: Figure out what this is
		 */
		UserSuspended(29),
		/**
		 * <dt><code>{@link #getEventTypeId()} = 30</code></dt><br/>
		 * TODO: Figure out what this is
		 */
		UserMerged(30),
		/**
		 * <dt><code>{@link #getEventTypeId()} = 34</code></dt><br/>
		 * Somebody changed their avatar or name.
		 * TODO: Verify
		 */
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
	
	public ChatEvent(final String raweventjson, final String chatsite)
	{
		CHATSITE = chatsite;
		event_type = EventType.forEventId(getNumValueJSON("event_type", raweventjson));
		time_stamp = getNumValueJSON("time_stamp", raweventjson);
		id = getNumValueJSON("id", raweventjson);
		message_id = getNumValueJSON("message_id", raweventjson);
		/*
		 * Note: the value of the JSON "content" may not be the entire message.
		 */
		content = this.event_type.name().startsWith("Message") ? null :
			getStringValueJSON("content", raweventjson);
		room_id = getNumValueJSON("room_id", raweventjson);
		room_name = getStringValueJSON("room_name", raweventjson);
		user_id = getNumValueJSON("user_id", raweventjson);
		user_name = getStringValueJSON("user_name", raweventjson);
		parent_id = getNumValueJSON("parent_id", raweventjson);
		target_user_id = getNumValueJSON("target_user_id", raweventjson);
		System.out.println(raweventjson);
	}
	
	private String getRawMessageContentNoException(final long message_id)
	{
		try
		{
			if(message_id==0)
				return null;
			return GET("https://"+CHATSITE+"/message/"+message_id);
		}
		catch(Exception e)
		{
			System.err.println("Failed to read message id "+message_id);
			return null;
		}
	}
	
	@Override
	public String toString()
	{
		if(content==null)
			getContent();
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
	public String getChatSite(){
		return CHATSITE;
	}
	private String varDumpAll(){
		String result="ChatEvent[";
		java.lang.reflect.Field[] fields = this.getClass().getDeclaredFields();
		for(int i=0;i<fields.length-1;++i)
			result+=varDump(fields[i])+",";
		result+=varDump(fields[fields.length-1])+"]";
		return result;
	}
	private String varDump(Field f){
		String result="\""+f.getName()+"\":\"";
		try
		{
			f.setAccessible(true);
			Object o = f.get(this);
			return result+o+"\"";
		}
		catch(IllegalArgumentException | IllegalAccessException
				| SecurityException e)
		{
			// Something horrible happened!
			System.err.println("Failed to get value of variable "+f.getName()+
					" of object at "+Integer.toHexString(System.identityHashCode(this)));
			e.printStackTrace();
			return result+"null\"";
		}
	}
}
