import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import bot.ChatBot;
import bot.ChatMessage;
import bot.MessageList;

public class Main
{
	public static void main(String[] args) throws IOException
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
				rooms
				);
		//bot.putMessage(138769, "ChatBot online.");
		MessageList messages = bot.getMessages();
		for(ChatMessage message : messages){
			System.out.println(message);
		}
	}
}
