package chat.bot;

import java.util.HashMap;
import javax.security.sasl.AuthenticationException;
import chat.ChatSite;
import chat.events.ChatEvent;
import chat.events.ChatEventList;
import chat.events.EventHandler;
import chat.io.ChatIO;
import static utils.Utils.getDateTime;

public class ChatBot
{
	private static final long chatRefreshDelay = 5000;
	private static HashMap<String,ChatIO> chatio = new HashMap<>();
	private final EventHandler eventhandler;
	private static Thread eventhandlerthread;
	public ChatBot(final String login, final String password, 
			final EventHandler event_handler, String... sites) throws AuthenticationException
	{
		eventhandler = event_handler;
		System.out.println("Logging in...");
		for(String site : sites)
		{
			site=site.toUpperCase();
			ChatSite chatsite = ChatSite.valueOf(site);
			if(!chatio.containsKey(site))
				chatio.put(site, new ChatIO(chatsite, login, password));
		}
		eventhandlerthread = new Thread(new Runnable()
		{
			public void run()
			{
				while(true){
					ChatEventList eventlist = ChatBot.getAllChatEvents();
					for(ChatEvent event : eventlist)
						if(event.getUserId()!=ChatBot.chatio.get(event.getChatSite().name()).getMyUserId())
							eventhandler.handle(event);
					System.gc();
					try{
						Thread.sleep(chatRefreshDelay);
					}catch(InterruptedException ie){
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
		site=site.toUpperCase();
		System.out.println("Joining "+site+" rooms "+java.util.Arrays.toString(rooms));
		
		chatio.get(site).joinRoom(rooms);
	}
	public static void leaveRoom(String site, Long... rooms){
		site=site.toUpperCase();
		System.out.println("Leaving "+site+" rooms "+java.util.Arrays.toString(rooms));
		if(chatio.containsKey(site))
			chatio.get(site).leaveRoom(rooms);
	}
	public static void putMessage(String site, final long roomid, final String message)
	{
		System.out.println(getDateTime()+" Sending message to "+site+" room "+roomid+
				" with content \""+message+"\".");
		if(!chatio.containsKey(site))
			throw new IllegalStateException("No available IO for site \""+site+"\".");
		ChatIO io = chatio.get(site);
		if(!io.isInRoom(roomid))
			throw new IllegalStateException("Not in room "+roomid+
					" on site \""+site+"\".");
		io.putMessage(roomid, message);
	}
	public static void putMessage(final ChatEvent event, final String message){
		putMessage(event.getChatSite().name(), event.getRoomId(), message);
	}
	public static void replyToMessage(ChatEvent event, String message){
		putMessage(event, ":"+event.getMessageId()+" "+message);
	}
	public static ChatEventList getAllChatEvents()
	{
		//System.out.println("Getting chat events from all sites");
		ChatEventList eventlist = new ChatEventList();
		for(ChatIO chatiosites : chatio.values())
			eventlist.addAll(chatiosites.getChatEvents());
		//if(eventlist.isEmpty())
		//	System.out.println("No new events.");
		return eventlist;
	}
}
