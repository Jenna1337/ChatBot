package chat.events;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import chat.bot.ChatBot;

public abstract class EventHandler
{
	public interface Command
	{
		public abstract void run(ChatEvent event, String args);
	}
	private static final long MAX_COMMAND_TIME = 300000;
	private Map<String, Command> commands = new TreeMap<>();
	private Map<String, Command> builtincommands = new TreeMap<>();
	{
		builtincommands.put("listcommands", (ChatEvent event, String args)->{
			String message = "Available commands:\nBuiltin: ";
			String[] builtin = builtincommands.keySet().toArray(new String[0]);
			String[] cmds = commands.keySet().toArray(new String[0]);
			message+=builtin[0];
			for(int i=1;i<builtin.length;++i)
				message+=", "+builtin[i];
			message+="\nLearned: "+args;
			if(cmds.length>0)
			{
				message+=cmds[0];
				for(int i=1;i<cmds.length;++i)
					message+=", "+cmds[i];
			}
			else
				message+="*none*";//FIXME figure out how to italicize things
			ChatBot.putMessage(event, message);
		});
		builtincommands.put("eval", (ChatEvent event, String args)->{
			String message = "todo";
			//TODO
			ChatBot.putMessage(event, message);
		});
	}
	private String trigger;
	private volatile boolean justWaved = false;
	private Runnable waveTimer = ()->{
		try{
			Thread.sleep(MAX_COMMAND_TIME);
		}catch(Exception e){}
		justWaved=false;
	};
	public abstract void handle(ChatEvent event);
	public boolean runCommand(ChatEvent event)
	{
		if(event.getContent()==null)
			return false;
		String content = event.getContent().trim();
		if(content.equals("o/")&&!justWaved ){
			ChatBot.putMessage(event, "\\o");
			justWaved=true;
			new Thread(waveTimer).start();
			return true;
		}
		if(content.equals("\\o")&&!justWaved){
			ChatBot.putMessage(event, "o/");
			justWaved=true;
			return true;
		}
		if(!content.startsWith(trigger))
			return false;
		
		String[] arr = content.substring(2).split(" ",2);
		
		final String command=arr[0].trim().toLowerCase();
		String extra = arr.length>1?arr[1]:"";
		final Command c;
		if(builtincommands.containsKey(command))
			c = builtincommands.get(command);
		else if(commands.containsKey(command))
			c = commands.get(command);
		else{
			System.out.println("Invalid command: "+command);
			return false;
		}
		
		final String args = extra;
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
		return true;
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
