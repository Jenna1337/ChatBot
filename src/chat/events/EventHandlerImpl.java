package chat.events;

import static utils.WebRequest.POST;
import java.util.Vector;
import chat.ChatSite;
import chat.bot.ChatBot;
import utils.Utils;
import static utils.Utils.urlencode;

public class EventHandlerImpl extends EventHandler
{
	private static final int recenteventbuffersize = 30;
	private static Vector<Long> recentevents = new Vector<>(recenteventbuffersize);
	private static synchronized boolean previouslyHandled(final ChatEvent event)
	{
		//Check if this event was already handled
		if(recentevents.contains(event.getId()))
			return true;
		
		if(recentevents.size()>=recenteventbuffersize)
			recentevents.remove(0);
		recentevents.add(event.getId());
		return false;
	}
	public synchronized void handle(final ChatEvent event)
	{
		if(previouslyHandled(event))
			return;
		//System.out.println("Handling event "+event.toString());
		//TODO Finish the switch cases
		switch(event.getEventType())
		{
			case MessagePosted://1
			case MessageEdited://2
				runCommand(event);
				break;
			case UserEntered://3
				if((event.getRoomId()==138769) 
						&& event.getChatSite().equals(ChatSite.STACKOVERFLOW))
					ChatBot.putMessage(event, "Welcome, "+event.getUserName()+"!");
				break;
			case UserLeft://4
				if((event.getRoomId()==138769) 
						&& event.getChatSite().equals(ChatSite.STACKOVERFLOW))
					ChatBot.putMessage(event, "User "+event.getUserName()+" left the room.");
				break;
			case RoomNameChanged://5
				break;
			case MessageStarred://6
				break;
			case DebugMessage://7
				System.out.println("Handling event "+event.toString());
				break;
			case UserMentioned://8
			case MessageReply://18
				if(!runCommand(event))
				{
					final String args = event.getContent().replace(
							"@"+ChatBot.getMyUserName(), "").trim();
					if(!args.isEmpty())
						ChatBot.replyToMessageByEval(event, args);
				}
				break;
			case MessageFlagged://9
				break;
			case MessageDeleted://10
				break;
			case FileAdded://11
				System.out.println("Handling event "+event.toString());
				break;
			case ModeratorFlag://12
				System.out.println("Handling event "+event.toString());
				break;
			case UserSettingsChanged://13
				System.out.println("Handling event "+event.toString());
				break;
			case GlobalNotification://14
			case UserNotification://16
				System.out.println("Handling event "+event.toString());
				try{
					//TODO acknowledge notification 
					/*POST("https://chat.stackoverflow.com/messages/ack", urlencode(new String[][]{
						{"id", ""+event.getMessageId()}
					}));*/
				}catch(Exception e){
					e.printStackTrace();
				}
				break;
			case AccessLevelChanged://15
				break;
			case Invitation://17
				System.out.println("Handling event "+event.toString());
				break;
			case MessageMovedOut://19
				break;
			case MessageMovedIn://20
				break;
			case TimeBreak://21
				System.out.println("Handling event "+event.toString());
				break;
			case FeedTicker://22
				//String url = utils.Utils.makeMarkdown(event.getContent());
				break;
			case UserSuspended://29
				System.out.println("Handling event "+event.toString());
				break;
			case UserMerged://30
				System.out.println("Handling event "+event.toString());
				break;
			case UserNameOrAvatarChanged://34
				System.out.println("Handling event "+event.toString());
				break;
			default:
				System.out.println("Handling event "+event.toString());
				System.err.println("Unknown event type" + event.toString());
				break;
		}
	}
}
