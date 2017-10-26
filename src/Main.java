import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.HashMap;
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
		HashMap<String, Long[]> relation = new HashMap<>(3);
		for(ChatSite chatsite : ChatSite.values())
		{
			String site = chatsite.name();
			String[] siterooms;
			{
					String s = props.getProperty(site); 
					if(s==null){
						s=props.getProperty(site.toLowerCase());
						if(s==null)
							s=props.getProperty(site.toUpperCase());
					}
					try{
						siterooms=s.split(",");
					}catch(Exception e){
						throw new IllegalArgumentException(e);
					}
			}
			Long[] rooms = new Long[siterooms.length];
			for(int i=0;i<rooms.length;++i)
				rooms[i] = Long.parseLong(siterooms[i]);
			relation.put(site.toUpperCase(), rooms);
		}
		ChatBot bot = new ChatBot(props.getProperty("LOGIN-EMAIL"),
				props.getProperty("PASSWORD"),
				new EventHandlerImpl(),
				relation
				);
		bot.setTrigger(props.getProperty("TRIGGER"));
	}
}
