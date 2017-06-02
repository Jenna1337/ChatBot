package bot.events;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import bot.events.ChatEvent.EventType;

public class ChatEventList extends LinkedList<ChatEvent>
{
	public ChatEventList()
	{
		super();
	}
	public ChatEventList(List<String> eventlists, String chatsite)
	{
		super();
		for(String raweventarrayjson : eventlists)
		{
			String[] messages = raweventarrayjson.split("\\},\\{(?=\"event_type\")");
			for(int i=0; i<messages.length; ++i)
				this.add(new ChatEvent(messages[i], chatsite));
		}
	}
	public ChatEventList(String raweventarrayjson, String chatsite)
	{
		super();
		String[] messages = raweventarrayjson.split("\\},\\{(?=\"event_type\")");
		for(int i=0; i<messages.length; ++i)
			this.add(new ChatEvent(messages[i], chatsite));
	}
	
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
		ChatEventList filteredlist = new ChatEventList();
		
		eventloop:
		for(ChatEvent event : this)
			if(event.getEventType()!=null)
				for(EventType evttype : type)
					if(evttype.equals(event.getEventType()))
					{
						filteredlist.add(event);
						continue eventloop;
					}
		return null;
	}
}
