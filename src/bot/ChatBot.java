package bot;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class ChatBot
{
	private final ChatIO chatio;
	private final String trigger;
	private List<Integer> rooms;
	public ChatBot(String login, String password, String trigger,
			ChatWebsite site, Integer[] rooms)
	{
		this.chatio = new ChatIO(login, password, site);
		this.trigger = trigger;
		this.rooms = java.util.Arrays.asList(rooms);
		// TODO Auto-generated constructor stub
	}
	public String getTrigger()
	{
		return trigger;
	}
}

class Main
{
	public static void main(String args) throws IOException
	{
		Properties props = new Properties();
		FileReader reader = new FileReader("bot.properties");
		props.load(reader);
		reader.close();
		String[] strings = props.getProperty("ROOMS", "1").split("\\D");
		Integer[] rooms = new Integer[strings.length];
		for(int i=0;i<rooms.length;++i)
			rooms[i] = Integer.parseInt(strings[i]);
		ChatBot bot = new ChatBot(props.getProperty("LOGIN-EMAIL"),
				props.getProperty("PASSWORD"),
				props.getProperty("TRIGGER"),
				ChatWebsite.valueOf(props.getProperty("SITE")),
				rooms
				);
	}
}
