package bot;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.security.sasl.AuthenticationException;
import bot.events.ChatEvent;
import bot.events.ChatEventList;
import bot.web.ChatIO;

public class ChatBot
{
	interface Command
	{
		public void run(String args);
	}
	private static ChatIO chatio;
	private static final int MAX_COMMAND_TIME = 30000;
	private final String trigger;
	private Map<String, Command> commands;
	public ChatBot(final String login, final String password, final String trigger, Long... rooms) throws AuthenticationException
	{
		if(chatio!=null && chatio.isLoggedIn())
			throw new IllegalStateException("Bot already active.");
		ChatIO.login(login, password);
		chatio = new ChatIO("chat.stackoverflow.com");
		this.trigger = trigger;
		chatio.addRoom(rooms);
		//TODO add a Map<String,ChatIO> or something to support multiple sites 
	}
	public String getTrigger()
	{
		return trigger;
	}
	//TODO automatically call this method
	public void runCommand(ChatEvent message)
	{
		if(!message.getContent().startsWith(getTrigger()))
			return;
		String[] arr = message.getContent().substring(2).split(" ",2);
		runCommand(arr[0], arr[1]);
	}
	public void runCommand(final String command, final String args)
	{
		if(!commands.containsKey(command))
		{
			System.out.println("Invalid command: "+command);
			return;
		}
		final Command c = commands.get(command);
		
		Timer countdown = new Timer();
		//Start a new Thread to run the command
		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				Timer runner = new Timer();
				TimerTask task = new TimerTask()
				{
					public void run()
					{
						c.run(args);
						countdown.cancel();
					}
				};
				runner.schedule(task, 1);
			}
		});/*{
		};
		 */
		
		try
		{
			java.lang.reflect.Method method_resume = Thread.class.getMethod("resume0");
			java.lang.reflect.Method method_stop = Thread.class.getMethod("stop0", Object.class);
			method_resume.setAccessible(true);
			method_stop.setAccessible(true);
			countdown.schedule(new TimerTask(){
				@SuppressWarnings("deprecation")
				public void run(){
					//Interrupt the thread
					System.out.println("Command timed out.");
					//Kill thread
					try
					{
						thread.stop();
						if(thread.isAlive()){
							// A "NEW" status can't change to not-NEW
							if (thread.getState() != Thread.State.NEW) {
								method_resume.invoke(thread); // Wake up thread if it was suspended; no-op otherwise
							}
							
							// The VM can handle all thread states
							method_stop.invoke(thread, new ThreadDeath());
						}
					}
					catch(IllegalAccessException | IllegalArgumentException
							| InvocationTargetException e)
					{
						e.printStackTrace();
					}
				}
			}, MAX_COMMAND_TIME);
		}
		catch(SecurityException | NoSuchMethodException e)
		{
			System.err.println("Failed to stop thread for command \""+command+
					"\" with arguments \""+args+"\"");
			e.printStackTrace();
		}
	}
	public void putMessage(final long roomid, final String message)
	{
		chatio.putMessage(roomid, message);
	}
	public ChatEventList getChatEvents()
	{
		return chatio.getChatEvents();
	}
}
