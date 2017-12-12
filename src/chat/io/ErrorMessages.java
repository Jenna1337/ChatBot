package chat.io;

import chat.events.ChatEvent;
import static utils.Utils.randomElement;

public class ErrorMessages
{
	private ErrorMessages(){}
	
	private static final String msgUsernameRef = "!!USERNAME!!";
	
	private static String format(ChatEvent event, String message){
		return message.replaceAll(msgUsernameRef, event.getUserName());
	}
	
	public static String generic(ChatEvent event){
		return randomElement(genericMessages);
	}
	public static String commandNotFound(ChatEvent event){
		return randomElement(cmdNotFoundMessages);
	}
	public static String cmdAlreadyExists(ChatEvent event){
		return format(event, randomElement(cmdAlreadyExists));
	}
	public static String cannotForgetCmd(ChatEvent event){
		return format(event, randomElement(cmdUnforgetable));
	}
	public static String badInput(ChatEvent event){
		return format(event, randomElement(badInput));
	}
	
	private static final String[] genericMessages = {
			"I don't understand."
	};
	private static final String[] cmdNotFoundMessages = {
			"Command does not exist.",
			"Command not found."
	};
	private static final String[] cmdAlreadyExists = {
			"Command already exists.",
	};
	private static final String[] cmdUnforgetable = {
			"I'm sorry "+msgUsernameRef+", I'm afraid I can't do that.."
	};
	private static final String[] badInput = {
			"Invalid input."
	};
	
}
