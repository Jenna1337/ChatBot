package bot;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import bot.ChatEvent.EventType;

public class ChatEventList extends LinkedList<ChatEvent>
{
	public ChatEventList(List<String> eventlists)
	{
		// TODO Auto-generated constructor stub
	}
	public ChatEventList(String rawmessages)
	{
		super();
		String[] messages = rawmessages.split("\\v+");
		for(int i=1; i<messages.length-1; ++i){
			//TODO
			this.add(parseMessage(messages[i]));
		}
		String last = messages[messages.length-1];
		int sloc = last.lastIndexOf("]");
		String lastmessage = last.substring(0, sloc);
		this.add(parseMessage(lastmessage));
	}
	
	private static ChatEvent parseMessage(String rawmessages)
	{
		//TODO
		return null;
	}
	//TODO
	
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
		//TODO
		return null;
	}
}
