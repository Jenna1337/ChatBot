import java.io.FileReader;
import java.util.Properties;
import chat.ChatSite;
import chat.bot.ChatBot;
import chat.events.EventHandlerImpl;

public class Main
{
	public static void main(String[] args) throws Exception
	{
		Properties props = new Properties();
		FileReader reader = new FileReader("bot.properties");
		props.load(reader);
		reader.close();
		String[] sites = props.getProperty("SITES", "chat.stackoverflow.com").split(props.getProperty("SITE_DELIMITER", ";"));
		ChatBot bot = new ChatBot(props.getProperty("LOGIN-EMAIL"),
				props.getProperty("PASSWORD"),
				new EventHandlerImpl(),
				sites
				);
		bot.setTrigger(props.getProperty("TRIGGER"));
		for(String site : sites)
		{
			String[] siterooms = props.getProperty(site, "1").split(",");
			Long[] rooms = new Long[siterooms.length];
			for(int i=0;i<rooms.length;++i)
				rooms[i] = Long.parseLong(siterooms[i]);
			ChatBot.joinRoom(ChatSite.valueOf(site.toUpperCase()), rooms);
		}
		//bot.putMessage(sites[0], "ChatBot online.");
	}
}
