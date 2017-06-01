package bot.events;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import bot.events.ChatEvent.EventType;

public class ChatEventList extends LinkedList<ChatEvent>
{
	public ChatEventList(List<String> eventlists, String chatsite)
	{
		super();
		for(String raweventjsonjson : eventlists)
			this.addAll(new ChatEventList(raweventjsonjson, chatsite));
	}
	public ChatEventList(String raweventjsonjson, String chatsite)
	{
		super();
		//TODO
		String[] messages = raweventjsonjson.split("\\v+");
		for(int i=1; i<messages.length-1; ++i){
			//TODO
			this.add(parseMessage(messages[i], chatsite));
		}
		String last = messages[messages.length-1];
		int sloc = last.lastIndexOf("]");
		String lastmessage = last.substring(0, sloc);
		this.add(parseMessage(lastmessage, chatsite));
	}
	
	private static ChatEvent parseMessage(String raweventjson, String chatsite)
	{
		return new ChatEvent(raweventjson, chatsite);
	}
	//TODO add sorting methods
	
	public void sortByTimeStamp(){
		Collections.sort(this, compTimeStamp);
	}
	
	private static final Comparator<ChatEvent> compTimeStamp = new Comparator<ChatEvent>()
	{
		public int compare(ChatEvent o1, ChatEvent o2)
		{
			long t1=o1.getTimeStamp();
			long t2=o2.getTimeStamp();
			return t1>t2?1:(t1<t2?-1:0);
		}
	};
	
	public ChatEventList getEventsWithTypes(EventType... type){
		//TODO copy this list and filter it
		return null;
	}
}
