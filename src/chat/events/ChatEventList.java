package chat.events;

import java.util.Arrays;
import chat.ChatSite;

public class ChatEventList extends utils.json.JsonList<ChatEvent>
{
	public ChatEventList()
	{
		super();
	}
	public ChatEventList(Iterable<String> eventlists, ChatSite chatsite)
	{
		super();
		eventlists.forEach((raweventarrayjson)->
		{
			String[] messages = raweventarrayjson.substring(1, 
					raweventarrayjson.length()-1).split("\\},\\{(?=\"event_type\")");
			for(int i=0; i<messages.length; ++i)
				this.add(new ChatEvent(messages[i], chatsite));
		});
		this.sort(null);
	}
	public ChatEventList(String raweventarrayjson, ChatSite chatsite)
	{
		this(Arrays.asList(raweventarrayjson.split("\\},\\{(?=\"event_type\")")),chatsite);
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
