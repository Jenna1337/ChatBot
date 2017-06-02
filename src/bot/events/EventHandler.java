package bot.events;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class EventHandler
{
	interface Command
	{
		public void run(String args);
	}
	private static final long MAX_COMMAND_TIME = 30000;
	private HashMap<String, Command> commands = new HashMap<>();
	private String trigger;
	public void handle(ChatEvent event)
	{
		System.out.println("Handling event "+event.toString());
		//TODO Finish the switch cases
		switch(event.getEventType())
		{
			case AccessLevelChanged:
				break;
			case DebugMessage:
				break;
			case FeedTicker:
				break;
			case FileAdded:
				break;
			case GlobalNotification:
				break;
			case Invitation:
				break;
			case MessageDeleted:
				break;
			case MessageEdited:
				runCommand(event);
				break;
			case MessageFlagged:
				break;
			case MessageMovedIn:
				break;
			case MessageMovedOut:
				break;
			case MessagePosted:
				runCommand(event);
				break;
			case MessageReply:
				break;
			case MessageStarred:
				break;
			case ModeratorFlag:
				break;
			case RoomNameChanged:
				break;
			case TimeBreak:
				break;
			case UserEntered:
				break;
			case UserLeft:
				break;
			case UserMentioned:
				break;
			case UserMerged:
				break;
			case UserNameOrAvatarChanged:
				break;
			case UserNotification:
				break;
			case UserSettingsChanged:
				break;
			case UserSuspended:
				break;
			default:
				break;
		}
	}
	public void runCommand(ChatEvent message)
	{
		if(!message.getContent().startsWith(trigger))
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
	public void setTrigger(String trigger)
	{
		this.trigger=trigger;
	}
}
