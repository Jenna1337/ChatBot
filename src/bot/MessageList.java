package bot;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import static bot.WebRequest.*;

public class MessageList extends LinkedList<ChatMessage>
{
	long time;
	long sync;
	int ms;
	
	public MessageList()
	{
		super();
	}
	public MessageList(String rawmessages)
	{
		this();
		String[] messages = rawmessages.split("\\v+");
		for(int i=1; i<messages.length-1; ++i){
			//TODO
			this.add(parseMessage(messages[i]));
		}
		String last = messages[messages.length-1];
		int sloc = last.lastIndexOf("]");
		String lastmessage = last.substring(0, sloc);
		this.add(parseMessage(lastmessage));
		/**
		 * It should be something like: <pre>time:75680598,sync:1496121265,ms:3</pre>
		 */
		String listpars = last.substring(sloc+2, last.length()-1).replaceAll("\"", "");
		String[] pararr = listpars.split(",");
		// TODO
	}
	
	private static ChatMessage parseMessage(String rawmessages)
	{
		//TODO
		return null;
	}
	//TODO

}
