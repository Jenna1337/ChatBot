package chat.events;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import chat.ChatSite;
import static utils.Utils.getNumValueJSON;
import static utils.Utils.getStringValueJSON;
import static utils.Utils.unescapeHtml;
import static utils.Utils.search;
import static utils.WebRequest.GET;

public class ChatEvent implements Comparable<ChatEvent>
{
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
	
	private final long message_stars;
	
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
		
		room_id = getNumValueJSON("room_id", raweventjson);
		room_name = unescapeHtml(getStringValueJSON("room_name", raweventjson));
		user_id = getNumValueJSON("user_id", raweventjson);
		user_name = unescapeHtml(getStringValueJSON("user_name", raweventjson));
		parent_id = getNumValueJSON("parent_id", raweventjson);
		target_user_id = getNumValueJSON("target_user_id", raweventjson);
		//System.out.println("Received event: "+raweventjson);
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
	/**
	 * Debugging method.
	 * @return A string containing all the instance variable names and their corresponding values.
	 */
	private String varDumpAll(){
		String result="ChatEvent[";
		java.lang.reflect.Field[] fields = this.getClass().getDeclaredFields();
		for(int i=0;i<fields.length-1;++i)
			if(!Modifier.isStatic(fields[i].getModifiers()))
				result+=varDump(fields[i])+",";
		result+=varDump(fields[fields.length-1])+"]";
		return result;
	}
	/**
	 * Debugging method.
	 * @return A string containing the variable's name and its corresponding values.
	 */
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

	public int compareTo(ChatEvent o)
	{
		long t1=this.getTimeStamp();
		long t2=o.getTimeStamp();
		return t1>t2?1:(t1<t2?-1:0);
	}
}
