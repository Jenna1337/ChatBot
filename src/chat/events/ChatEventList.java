package chat.events;

import java.util.List;
import chat.ChatSite;
import utils.json.JsonList;

public class ChatEventList extends JsonList<ChatEvent>
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
		this.sort(null);
	}
	public ChatEventList(String raweventarrayjson, ChatSite chatsite)
	{
		super();
		String[] messages = raweventarrayjson.split("\\},\\{(?=\"event_type\")");
		for(int i=0; i<messages.length; ++i)
			this.add(new ChatEvent(messages[i], chatsite));
		this.sort(null);
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
		filteredlist.sort(null);
		return filteredlist;
	}
}
