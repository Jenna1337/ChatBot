package bot;

import static bot.WebRequest.GET;
import java.io.IOException;
import java.net.MalformedURLException;

public class ChatMessage
{
	int time_stamp;
	int user_id;
	int room_id;
	int message_id;
	int parent_id;//optional
	int message_edits;//optional
	
	public ChatMessage()
	{
		
	}
	//TODO
	
	private static String getRawMessageContent(int message_id) throws IOException
	{
		try{
			return GET("https://chat.stackoverflow.com/message/"+message_id);
		}catch(MalformedURLException murle){
			throw new IllegalArgumentException("Invalid message id "+message_id);
		}catch(IOException ioe){
			throw new IOException("Failed to read message id "+message_id, ioe);
		}
	}
}
