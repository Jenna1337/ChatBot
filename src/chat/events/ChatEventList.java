package chat.events;

import java.util.LinkedList;
import java.util.List;
import chat.ChatSite;

public class ChatEventList extends LinkedList<ChatEvent>
{
	public ChatEventList()
	{
		super();
	}
	public ChatEventList(List<String> eventlists, ChatSite chatsite)
	{
		super();
		for(String raweventarrayjson : eventlists)
		{
			String[] messages = raweventarrayjson.substring(1, 
					raweventarrayjson.length()-1).split("\\},\\{(?=\"event_type\")");
			for(int i=0; i<messages.length; ++i)
				this.add(new ChatEvent(messages[i], chatsite));
		}
	}
	public ChatEventList(String raweventarrayjson, ChatSite chatsite)
	{
		super();
		String[] messages = raweventarrayjson.split("\\},\\{(?=\"event_type\")");
		for(int i=0; i<messages.length; ++i)
			this.add(new ChatEvent(messages[i], chatsite));
	}
	
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
		return filteredlist;
	}
}
