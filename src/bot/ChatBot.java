package bot;

import java.util.HashMap;
import javax.security.sasl.AuthenticationException;
import bot.events.ChatEvent;
import bot.events.ChatEventList;
import bot.events.EventHandler;
import bot.web.ChatIO;

public class ChatBot
{
	private static HashMap<String,ChatIO> chatio = new HashMap<>();
	private final EventHandler eventhandler;
	private static Thread eventhandlerthread;
	public ChatBot(final String login, final String password) throws AuthenticationException
	{
		System.out.println("Logging in...");
		ChatIO.login(login, password);
		eventhandler = new EventHandler();
		eventhandlerthread = new Thread(new Runnable()
		{
			public void run()
			{
				while(true){
					ChatEventList eventlist = ChatBot.getAllChatEvents();
					for(ChatEvent event : eventlist)
					{
						eventhandler.handle(event);
					}
					try
					{
						Thread.sleep(10000);
					}
					catch(InterruptedException ie)
					{
						ie.printStackTrace();
					}
				}
			}
		});
		eventhandlerthread.start();
	}
	public void setTrigger(final String trigger)
	{
		eventhandler.setTrigger(trigger);
	}
	public static void joinRoom(String site, Long... rooms) throws AuthenticationException, IllegalStateException{
		site=site.toLowerCase();
		System.out.println("Joining "+site+" rooms "+java.util.Arrays.toString(rooms));
		if(!chatio.containsKey(site))
			chatio.put(site, new ChatIO(site));
		chatio.get(site).addRoom(rooms);
	}
	public static void leaveRoom(String site, Long... rooms){
		site=site.toLowerCase();
		System.out.println("Leaving "+site+" rooms "+java.util.Arrays.toString(rooms));
		if(chatio.containsKey(site))
			chatio.get(site).removeRoom(rooms);
	}
	public static void putMessage(String site, final long roomid, final String message)
	{
		site=site.toLowerCase();
		System.out.println("Sending message to "+site+" room "+roomid+
				" with content \""+message+"\".");
		if(!chatio.containsKey(site))
			throw new IllegalStateException("No available IO for site \""+site+"\".");
		ChatIO io = chatio.get(site);
		if(!io.isInRoom(roomid))
			throw new IllegalStateException("Not in room "+roomid+
					" on site \""+site+"\".");
		io.putMessage(roomid, message);
	}
	public static ChatEventList getChatEvents(String site)
	{
		site=site.toLowerCase();
		System.out.println("Getting chat events from "+site);
		ChatEventList eventlist = new ChatEventList();
		for(ChatIO chatiosites : chatio.values())
			eventlist.addAll(chatiosites.getChatEvents());
		return eventlist;
	}
	public static ChatEventList getAllChatEvents()
	{
		System.out.println("Getting chat events from all sites");
		ChatEventList eventlist = new ChatEventList();
		for(ChatIO chatiosites : chatio.values())
			eventlist.addAll(chatiosites.getChatEvents());
		if(eventlist.isEmpty())
			System.out.println("No new events.");
		return eventlist;
	}
}
