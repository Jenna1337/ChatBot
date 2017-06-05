package chat.events;

public class EventHandlerImpl extends EventHandler
{
	public void handle(ChatEvent event)
	{
		//System.out.println("Handling event "+event.toString());
		//TODO Finish the switch cases
		switch(event.getEventType())
		{
			case MessagePosted://1
			case MessageEdited://2
				runCommand(event);
				break;
			case UserEntered://3
				break;
			case UserLeft://4
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
				{/*
					String message = "";
					//TODO
					ChatBot.putMessage(event.getChatSite().name(), event.getRoomId(), message);
				*/}
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
				String url = utils.Utils.makeMarkdown(event.getContent());
				break;
			case UserSuspended://29
				System.out.println("Handling event "+event.toString());
				break;
			default:
				break;
			case UserMerged://30
				System.out.println("Handling event "+event.toString());
				break;
			case UserNameOrAvatarChanged://34
				System.out.println("Handling event "+event.toString());
				break;
		}
	}
}
