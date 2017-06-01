import java.io.FileReader;
import java.util.Properties;
import bot.ChatBot;
import bot.events.ChatEvent;
import bot.events.ChatEventList;

public class Main
{
	public static void main(String[] args) throws Exception
	{
		Properties props = new Properties();
		FileReader reader = new FileReader("bot.properties");
		props.load(reader);
		reader.close();
		String[] strings = props.getProperty("ROOMS", "1").split("\\D");
		Long[] rooms = new Long[strings.length];
		for(int i=0;i<rooms.length;++i)
			rooms[i] = Long.parseLong(strings[i]);
		ChatBot bot = new ChatBot(props.getProperty("LOGIN-EMAIL"),
				props.getProperty("PASSWORD"),
				props.getProperty("TRIGGER"),
				rooms
				);
		//bot.putMessage(138769, "ChatBot online.");
		ChatEventList eventlist = bot.getChatEvents();
		for(ChatEvent event : eventlist)
			System.out.println(event);
		try
		{
			Thread.sleep(10000);
		}
		catch(InterruptedException ie)
		{
			ie.printStackTrace();
		}
	}
}
