import java.io.FileReader;
import java.util.Properties;
import bot.ChatBot;

public class Main
{
	public static void main(String[] args) throws Exception
	{
		Properties props = new Properties();
		FileReader reader = new FileReader("bot.properties");
		props.load(reader);
		reader.close();
		ChatBot bot = new ChatBot(props.getProperty("LOGIN-EMAIL"),
				props.getProperty("PASSWORD")
				);
		bot.setTrigger(props.getProperty("TRIGGER"));
		String site_delimiter = props.getProperty("SITE_DELIMITER", ";");
		String[] sites = props.getProperty("SITES", "chat.stackoverflow.com").split(site_delimiter);
		for(String site : sites)
		{
			String[] siterooms = props.getProperty(site, "1").split(",");
			Long[] rooms = new Long[siterooms.length];
			for(int i=0;i<rooms.length;++i)
				rooms[i] = Long.parseLong(siterooms[i]);
			ChatBot.joinRoom(site, rooms);
		}
		//bot.putMessage(sites[0], "ChatBot online.");
	}
}
