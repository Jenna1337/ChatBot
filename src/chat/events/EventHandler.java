package chat.events;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import chat.bot.ChatBot;
import chat.bot.tools.MicroAsmExamples;
import chat.bot.tools.MicroAssembler;
import utils.Utils;
import static utils.Utils.parseLongs;

public abstract class EventHandler
{
	private static final boolean DEBUG = false;
	private interface Command
	{
		public abstract void run(ChatEvent event, String args);
	}
	private static final long MAX_COMMAND_TIME = 30000;// 30 seconds
	private static final long WAVE_TIMER_SLEEP = 60000*5;// 60 seconds *5
	private static final String waveRight = "o/", waveLeft = "\\o";
	private static final String cwd = System.getProperty("user.dir");
	private static final String cmdfileext = ".txt";
	private static volatile int instanceNumber = 1;
	private final String cmdSaveDirectory = cwd+"/SEChatBot/"+
			(instanceNumber++)+"/commands/";
	private Map<String, Command> commands = new TreeMap<>();
	private Map<String, Command> builtincommands = new TreeMap<>();
	private String trigger;
	private volatile boolean justWaved = false;
	private Runnable waveTimer = ()->{
		try{
			Thread.sleep(WAVE_TIMER_SLEEP);
		}catch(Exception e){e.printStackTrace();}
		finally{
			justWaved=false;
		}
	};
	private boolean wave(final ChatEvent event){
		switch(event.getContent()){
			case waveRight:
				ChatBot.putMessage(event, waveLeft);
				break;
			case waveLeft:
				ChatBot.putMessage(event, waveRight);
				break;
			default:
				return false;
		}
		justWaved=true;
		new Thread(waveTimer, "WaveTimer").start();
		return true;
	}
	public abstract void handle(final ChatEvent event);
	private static volatile int threadNumber = 1;
	/**
	 * Runs a command associated with the chat event, if any.
	 * @param event The chat event
	 * @return {@code true} iff the input caused a command to execute.
	 */
	protected boolean runCommand(final ChatEvent event)
	{
		System.out.println(utils.Utils.getDateTime()+" "+event.getEventType().toString()+
				" by user \""+event.getUserName()+"\" (id "+event.getUserId()+
				") in "+event.getChatSite()+
				" room \""+event.getRoomName()+"\" (id "+event.getRoomId()+
				") with content \""+event.getContent().replace("\"", "\\\"")+"\"");
		if(event.getContent()==null)
			return false;
		if(!justWaved && wave(event))
			return true;
		String content = event.getContent().trim();
		
		switch(event.getEventType()){
			case UserMentioned:
			case MessageReply:
				if(content.contains("@")){
					try{
						content = content.replace("@"+ChatBot.getMyUserName(), "").trim();
						if(content.startsWith(trigger))
							break;
					}
					catch(ArrayIndexOutOfBoundsException aioobe){
						//it's an empty mention
						//TODO reply with "help"?
						return false;
					}
				}
				else
				{
					//it's a direct reply with a onebox
					return false;
				}
				break;
			case MessagePosted:
			case MessageEdited:
				if(!content.startsWith(trigger))
					return false;
				else
					break;
			default:
				throw new UnsupportedOperationException(event.getEventType().name());
		}
		
		String[] arr = content.substring(trigger.length()).split(" ",2);
		
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
		}, "Command-"+command+"-"+(threadNumber++));
		final String cmd = command;
		countdown.schedule(new TimerTask(){
			public void run(){
				if(DEBUG)
					return;
				System.out.println("Command timed out.");
				try
				{
					Thread.State s = thread.getState();
					switch(s)
					{
						case NEW:
							throw new InternalError("Thread not started.");
						case BLOCKED:
						case RUNNABLE:
						case TIMED_WAITING:
						case WAITING:
							//Interrupt the thread
							thread.interrupt();
							Thread.sleep(1000);
							if(thread.isAlive()){
								//Kill thread
								thread.stop();
							}
							break;
						case TERMINATED:
							break;
						default:
							throw new InternalError("Unknown thread state: ");
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
	private Command putCommand(String name, String text){
		name=name.trim().toLowerCase();
		return commands.put(name, (ChatEvent _event, String _args)->{
			ChatBot.putMessage(_event, MicroAssembler.assemble('\"'+text, _args));
		});
	}
	/**
	 * Adds the command to the command list.
	 * @param text The assembly command to add
	 * @return {@code true} if the command was added, {@code false} otherwise.
	 */
	public boolean addCommand(String name, String text)
	{
		name=name.trim().toLowerCase();
		boolean canAdd = !(commands.containsKey(name) || builtincommands.containsKey(name));
		if(canAdd){
			putCommand(name, text);
			writeCommandFile(name, text);
		}
		return canAdd;
	}
	/**
	 * Removes the command from the command list.
	 * @param command The command to remove
	 * @return {@code true} if the command was removed, {@code false} otherwise.
	 */
	public boolean removeCommand(String name)
	{
		name=name.trim().toLowerCase();
		boolean canRemove = commands.containsKey(name);
		if(canRemove){
			commands.remove(name);
			removeCommandFile(name);
		}
		return canRemove;
	}
	private boolean writeCommandFile(String name, String text)
	{
		File f = new File(cmdSaveDirectory+name+cmdfileext);
		
		f.getParentFile().mkdirs();
		if(f.exists() && f.isFile())
			f.delete();
		try
		{
			if(f.createNewFile())
			{
				FileOutputStream fos = new FileOutputStream(f);
				fos.write(text.getBytes());
				fos.close();
				return true;
			}
		}
		catch(IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	private boolean removeCommandFile(String name)
	{
		File f = new File(cmdSaveDirectory+name+cmdfileext);
		f.mkdirs();
		return f.exists() && f.isFile() && f.delete();
	}
	
	{
		Command listcommands = (ChatEvent event, String args)->{
			String message = "Available commands:\nBuiltin: ";
			String[] builtin = builtincommands.keySet().toArray(new String[0]);
			String[] cmds = commands.keySet().toArray(new String[0]);
			message+=builtin[0];
			for(int i=1;i<builtin.length;++i)
				message+=", "+builtin[i];
			message+="\nLearned: ";
			if(cmds.length>0)
			{
				message+=cmds[0];
				for(int i=1;i<cmds.length;++i)
					message+=", "+cmds[i];
			}
			else
				message+="none";
			ChatBot.putMessage(event, message);
		};
		Command assembly = (ChatEvent event, String args)->{
			String message = MicroAssembler.assemble(args);
			if(message.isEmpty())
				message = "Invalid input.";
			ChatBot.replyToMessage(event, message);
		};
		Command learn = (ChatEvent event, String args)->{
			if(!args.isEmpty() && args.contains(" ")){
				String[] args2 = args.split(" ", 2);
				final String name = args2[0];
				final String text = args2[1];
				if(addCommand(name, text))
					ChatBot.replyToMessage(event, "Learned command: "+name);
				else
					ChatBot.replyToMessage(event, "Command already exists.");
			}
			else{
			}
		};
		Command unlearn = (ChatEvent event, String args)->{
			if(removeCommand(args))
				ChatBot.replyToMessage(event, "Forgot command: "+args);
			else
			{
				if(builtincommands.containsKey(args))
					ChatBot.replyToMessage(event, "I'm sorry "+event.getUserName()+", I'm afraid I can't do that.");
				else
					ChatBot.replyToMessage(event, "Command does not exists.");
			}
		};
		Command joinroom = (ChatEvent event, String args)->{
			ChatBot.joinRoom(event.getChatSite(), parseLongs(args));
		};
		Command leaveroom = (ChatEvent event, String args)->{
			ChatBot.leaveRoom(event.getChatSite(), parseLongs(args));
		};
		Command rolldice = (ChatEvent event, String args)->{
			args=args.trim();
			String[] argarr = args.split("\\s+");
			int argcount = args.contains(" ")?argarr.length:0;
			switch(argcount){
				case 0:
					args = "1 6";
					break;
				case 1:
					args = "1 "+args;
					break;
				case 2:
					ChatBot.putMessage(event, MicroAsmExamples.rolldice(args));
					break;
				default:
					args = argarr[0]+" "+argarr[1];
					break;
			}
			ChatBot.putMessage(event, MicroAsmExamples.rolldice(args));
		};
		Command fibonacci = (ChatEvent event, String args)->{
			if(args.trim().isEmpty())
				args = "0";
			ChatBot.putMessage(event, MicroAsmExamples.fibonacci(args));
		};
		Command rand = (ChatEvent event, String args)->{
			args=args.trim();
			String[] argarr = args.trim().split("\\s+");
			int argcount = args.contains(" ")?argarr.length:0;
			switch(argcount){
				case 0:
					ChatBot.putMessage(event, MicroAsmExamples.rand0(args));
					break;
				case 1:
					ChatBot.putMessage(event, MicroAsmExamples.rand1(args));
					break;
				case 2:
					ChatBot.putMessage(event, MicroAsmExamples.rand2(args));
					break;
				default:
					args = argarr[0]+" "+argarr[1];
					ChatBot.putMessage(event, MicroAsmExamples.rand0(args));
					break;
			}
		};
		/*Command echo = (ChatEvent event, String args)->{
			ChatBot.putMessage(event, MicroAsmExamples.echo(args));
		};*/
		Command cointoss = (ChatEvent event, String args)->{
			ChatBot.putMessage(event, MicroAsmExamples.cointoss(args));
		};
		Command eval = (ChatEvent event, String args)->{
			ChatBot.replyToMessage(event, Utils.eval(args));
		};
		Command room = (ChatEvent event, String args)->{
			ChatBot.putMessage(event, MicroAssembler.assemble("\"https://"+event.getChatSite().getUrl()+"/rooms/$0", args));
		};
		builtincommands.put("help", listcommands);
		builtincommands.put("list", listcommands);
		builtincommands.put("listcommands", listcommands);
		builtincommands.put("asm", assembly);
		builtincommands.put("learn", learn);
		builtincommands.put("unlearn", unlearn);
		builtincommands.put("joinroom", joinroom);
		builtincommands.put("leaveroom", leaveroom);
		builtincommands.put("rolldice", rolldice);
		builtincommands.put("fibonacci", fibonacci);
		builtincommands.put("rand", rand);
		//builtincommands.put("echo", echo);
		builtincommands.put("cointoss", cointoss);
		builtincommands.put("coinflip", cointoss);
		builtincommands.put("eval", eval);
		builtincommands.put("room", room);
	}
	{
		File cmddir = new File(cmdSaveDirectory);
		cmddir.mkdirs();
		File[] cmdfiles = cmddir.listFiles();
		System.out.println("Loading external commands...");
		for(File f : cmdfiles)
		{
			String cmdname = f.getName().endsWith(cmdfileext) ? 
					f.getName().substring(0, f.getName().length() - cmdfileext.length())
					: f.getName();
			System.out.println("Loading command: "+cmdname);
			try
			{
				String text = "";
				FileReader reader = new FileReader(f);
				int ch;
				while((ch=reader.read())!=-1)
					text+=(char)ch;
				reader.close();
				//TODO read in file contents
				putCommand(cmdname, text);
			}
			catch(IOException e)
			{
				System.out.println("Failed to load command: "+cmdname);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Done loading commands...");
	}
}
