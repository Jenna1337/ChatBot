package chat.events;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import chat.ChatSite;
import chat.bot.ChatBot;

public abstract class EventHandler
{
	public interface Command
	{
		public abstract void run(ChatEvent event, String args);
	}
	private static final long MAX_COMMAND_TIME = 30000;
	private HashMap<String, Command> commands = new HashMap<>();
	enum BuiltinComands implements Command
	{
		LIST_COMMANDS{
			public void run(ChatEvent event, String args)
			{
				String message = "test";
				//TODO
				ChatBot.putMessage(event.getChatSite().name(), event.getRoomId(), message);
			}
		},
		//TODO add more built-in commands
	}
	{
		addCommand("help", BuiltinComands.LIST_COMMANDS);
	}
	private String trigger;
	public abstract void handle(ChatEvent event);
	public void runCommand(ChatEvent event)
	{
		if(event.getContent()==null || !event.getContent().startsWith(trigger))
			return;
		String[] arr = event.getContent().substring(2).split(" ",2);
		try{
			runCommand(event, arr[0], arr[1]);
		}catch(Exception e){
			runCommand(event, arr[0], "");
		}
	}
	public void runCommand(ChatEvent event, String command, final String args)
	{
		command=command.trim().toLowerCase();
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
				c.run(event, args);
				countdown.cancel();
			}
		});/*{
		};
		 */
		final String cmd = command;
		countdown.schedule(new TimerTask(){
			@SuppressWarnings("deprecation")
			public void run(){
				//Interrupt the thread
				System.out.println("Command timed out.");
				//Kill thread
				try
				{
					while(thread.getState().equals(Thread.State.NEW))
						Thread.sleep(1);
					thread.interrupt();
					Thread.sleep(1000);
					if(thread.isAlive()){
						thread.stop();
					}
				}
				catch(IllegalArgumentException | InterruptedException e)
				{
					System.err.println("Failed to stop thread for command \""+cmd+
							"\" with arguments \""+args+"\"");
					e.printStackTrace();
				}
			}
		}, MAX_COMMAND_TIME);
		thread.start();
	}
	public void setTrigger(String trigger)
	{
		this.trigger=trigger;
	}
	/**
	 * Adds the command to the command list.
	 * @param command The command to add
	 * @return {@code true} if the command was added, {@code false} otherwise.
	 */
	public boolean addCommand(String name, Command command)
	{
		name=name.trim().toLowerCase();
		boolean canAdd = !commands.containsKey(name);
		if(canAdd)
			commands.put(name, command);
		return canAdd;
	}
	/**
	 * Adds the command to the command list.
	 * @param command The command to add
	 * @return {@code true} if the command was added, {@code false} otherwise.
	 */
	public boolean removeCommand(String name)
	{
		name=name.trim().toLowerCase();
		boolean canAdd = commands.containsKey(name);
		if(canAdd)
			commands.remove(name);
		return canAdd;
	}
}
