package chat.bot;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import javax.security.sasl.AuthenticationException;
import chat.ChatSite;
import chat.events.ChatEvent;
import chat.events.ChatEventList;
import chat.events.EventHandler;
import chat.events.EventHandlerImpl;
import chat.io.ChatIO;
import chat.io.ErrorMessages;
import chat.io.ErrorMessages.ErrorType;
import chat.users.ChatUser;
import utils.Utils;
import utils.eval.EvalResult;

public class ChatBot
{
	private static final long chatRefreshDelay = 5000;
	private static HashMap<ChatSite,ChatIO> chatio = new HashMap<>();
	private final EventHandler eventhandler;
	private static Thread eventhandlerthread;
	public ChatBot(String propfile) throws IOException{
		this(Utils.loadProperties(propfile));
	}
	public ChatBot(Properties props) throws AuthenticationException{
		eventhandler = new EventHandlerImpl();
		HashMap<String, Long[]> relation = new HashMap<>(3);
		for(ChatSite chatsite : ChatSite.values())
		{
			String site = chatsite.name();
			String[] siterooms;
			{
					String s = props.getProperty(site); 
					if(s==null){
						s=props.getProperty(site.toLowerCase());
						if(s==null)
							s=props.getProperty(site.toUpperCase());
					}
					try{
						siterooms=s.split(",");
					}catch(Exception e){
						siterooms=new String[0];
						//throw new IllegalArgumentException(e);
					}
			}
			Long[] rooms = new Long[siterooms.length];
			for(int i=0;i<rooms.length;++i)
				rooms[i] = Long.parseLong(siterooms[i]);
			relation.put(site.toUpperCase(), rooms);
		}
		init(props.getProperty("LOGIN-EMAIL"),
				props.getProperty("PASSWORD"),
				relation
				);
		this.setTrigger(props.getProperty("TRIGGER"));
	}
	public ChatBot(final String login, final String password,
			Map<String,Long[]> initialsiterooms) throws AuthenticationException
	{
		eventhandler = new EventHandlerImpl();
		init(login, password, initialsiterooms);
	}
	private void init(final String login, final String password, 
			Map<String,Long[]> initialsiterooms) throws AuthenticationException
	{
		System.out.println("Logging in...");
		for(Entry<String, Long[]> relation : initialsiterooms.entrySet())
		{
			String site=relation.getKey().toUpperCase();
			ChatSite chatsite = ChatSite.valueOf(site.toUpperCase());
			if(!chatio.containsKey(chatsite))
				chatio.put(chatsite, new ChatIO(chatsite, login, password, relation.getValue()));
		}
		for(ChatSite site : ChatSite.values())
		{
			Long[] rooms;
			{
				File f = new File(eventhandler.getRoomSaveDirectory()+site.name());
				if(!f.exists())
					continue;
				File[] files = f.listFiles();
				rooms = new Long[files.length];
				for(int i=0;i<files.length;++i)
					rooms[i] = Long.parseLong(files[i].getName());
				//for(File)
			}
			System.out.println("Rejoining "+site+" rooms "+java.util.Arrays.toString(rooms));
			chatio.get(site).joinRoom(rooms);
		}
		eventhandlerthread = new Thread(new Runnable()
		{
			public void run()
			{
				while(true){
					ChatEventList eventlist = ChatBot.getAllChatEvents();
					for(ChatEvent event : eventlist)
						if(event.getUserId()!=ChatBot.chatio.get(event.getChatSite()).getMyUserId())
							new Thread(()->{
								eventhandler.handle(event);
							}).start();
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
	public static void joinRoom(ChatSite site, Long... rooms){
		System.out.println("Joining "+site+" rooms "+java.util.Arrays.toString(rooms));
		
		chatio.get(site).joinRoom(rooms);
	}
	public static void leaveRoom(ChatSite site, Long... rooms){
		System.out.println("Leaving "+site+" rooms "+java.util.Arrays.toString(rooms));
		if(chatio.containsKey(site))
			chatio.get(site).leaveRoom(rooms);
	}
	public static void putMessage(ChatSite site, final long roomid, final String message)
	{
		if(message.trim().isEmpty()) return;
		System.out.println("Sending message to "+site+" room "+roomid+
				" with content \""+message.replace("\n", "\\n")+"\".");
		if(!chatio.containsKey(site))
			throw new IllegalStateException("No available IO for site \""+site+"\".");
		ChatIO io = chatio.get(site);
		if(!io.isInRoom(roomid))
			throw new IllegalStateException("Not in room "+roomid+
					" on site \""+site+"\".");
		io.putMessage(roomid, message);
	}
	public static void putMessage(final ChatEvent event, final String message){
		if(message.trim().isEmpty()){
			putMessage(event, ErrorMessages.getErrorText(event, ErrorType.GENERIC));
			return;
		}
		putMessage(event.getChatSite(), event.getRoomId(), message);
	}
	public static void replyToMessage(ChatEvent event, String message){
		putMessage(event, ":"+event.getMessageId()+" "+message);
	}
	public static void replyToMessageByEval(ChatEvent event, String args){
		EvalResult result = Utils.eval(args);
		//TODO use the rest of result somewhere
		String message = result.getResultDefault();
		if(message.isEmpty() || message.matches("\\s*")){
			double rngval = Math.random();
			if(rngval>.98)
				message = "\\u203D";
			else if(rngval>.65)
				message = "!?";
			else
				message = "?!";
		}
		putMessage(event, ":"+event.getMessageId()+" "+message);
	}
	public static void acknowledge(ChatEvent event){
		ChatIO io = chatio.get(event.getChatSite());
		io.acknowledge(event.getMessageId());
	}
	/**
	 * Gets the events to be handled from all chat sites.
	 * @return a list containing all unread events
	 */
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
	public static ChatIO getChatIO(ChatSite site){
		return chatio.get(site);
	}
	public static long getDelay(){
		return chatRefreshDelay;
	}
	public static String getMyUserName(){
		String username = "ERROR";
		for(ChatSite site : ChatSite.values()){
			if(chatio.get(site)!=null){
				username = getMyUserInstance(site).getName();
				break;
			}
		}
		return username;
	}
	public static ChatUser getMyUserInstance(ChatSite site){
		return chatio.get(site).getMyUserInstance();
	}
}
