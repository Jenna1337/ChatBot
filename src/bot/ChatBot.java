package bot;

import java.util.List;
import javax.security.sasl.AuthenticationException;

public class ChatBot
{
	private final ChatIO chatio;
	private final String trigger;
	private List<Long> rooms;
	public ChatBot(String login, String password, String trigger, Long... rooms) throws AuthenticationException
	{
		this.chatio = new ChatIO(login, password);
		this.trigger = trigger;
		this.rooms = java.util.Arrays.asList(rooms);
		// TODO
	}
	public String getTrigger()
	{
		return trigger;
	}
	public void putMessage(long roomid, String message)
	{
		chatio.putMessage(roomid, message);
	}
	public ChatEventList getChatEvents()
	{
		ChatEventList messages = chatio.getChatEvents(rooms);
		return messages;
	}
}

