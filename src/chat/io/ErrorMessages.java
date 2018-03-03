package chat.io;

import chat.events.ChatEvent;
import static utils.Utils.randomElement;
import static utils.Utils.replaceAllAll;

public class ErrorMessages
{
	private ErrorMessages(){}
	
	private static final String msgUserNameRef = "!!USERNAME!!";
	private static final String msgRoomNameRef = "!!ROOMNAME!!";
	
	
	private static String format(ChatEvent event, String message){
		String[][] replacementRef={
				{msgUserNameRef, event.getUserName()},
				{msgRoomNameRef, event.getRoomName()},
		};
		return replaceAllAll(message, replacementRef);
	}
	private static String selectRandom(ChatEvent event, String[] choices){
		return format(event, randomElement(choices));
	}
	
	public static enum ErrorType{
		GENERIC,
		BADINPUT,
		CMD_NOTFOUND,
		CMD_ALREADYEXISTS,
		CMD_UNFORGETABLE,
	}
	
	public static String getErrorText(ChatEvent event, ErrorType errtype){
		switch(errtype){
			case BADINPUT:
				return selectRandom(event, badInputMessages);
			case CMD_ALREADYEXISTS:
				return selectRandom(event, cmdAlreadyExistsMessages);
			case CMD_NOTFOUND:
				return selectRandom(event, cmdNotFoundMessages);
			case CMD_UNFORGETABLE:
				return selectRandom(event, cmdUnforgetableMessages);
			case GENERIC:
				return selectRandom(event, genericMessages);
			default:
				throw new IllegalArgumentException();
		}
	}
	
	private static final String[] genericMessages = {
			"I don't understand.",
	};
	private static final String[] cmdNotFoundMessages = {
			"Command does not exist.",
			"Command not found.",
			"That command is not defined.",
	};
	private static final String[] cmdAlreadyExistsMessages = {
			"Command already exists.",
			"There already exists a command with that name."
	};
	private static final String[] cmdUnforgetableMessages = {
			"I'm sorry "+msgUserNameRef+", but I'm afraid I can't do that..",
			"No.",
			"Access denied",
	};
	private static final String[] badInputMessages = {
			"Invalid input.",
			"Bad input.",
	};
	
}
