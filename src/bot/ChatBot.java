package bot;

import java.util.List;

public class ChatBot
{
	private final int msgCount = 100;
	private final ChatIO chatio;
	private final String trigger;
	private List<Integer> rooms;
	public ChatBot(String login, String password, String trigger, Integer... rooms)
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
	public void putMessage(int roomid, String message)
	{
		chatio.putMessage(roomid, message);
	}
	public MessageList getMessages()
	{
		MessageList messages = new MessageList();
		for(int room_id : rooms)
			messages.addAll(getMessages(room_id));
		return messages;
	}
	public MessageList getMessages(int roomid)
	{
		return chatio.getMessages(roomid, msgCount);
	}
}

