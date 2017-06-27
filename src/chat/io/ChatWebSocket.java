package chat.io;

import static utils.Utils.urlencode;
import static utils.Utils.getStringValueJSON;
import static utils.WebRequest.POST;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import utils.WebSocket;

public class ChatWebSocket extends WebSocket
{
	public ChatWebSocket(ChatIO chat, long roomid) throws UnknownHostException, MalformedURLException, URISyntaxException, IOException
	{
		super(getUrl(chat, roomid));
		// TODO Auto-generated constructor stub
	}
	private static String getUrl(ChatIO chat, long roomid) throws MalformedURLException, IOException
	{
		final String fkey = chat.getFkey();
		String urljson = POST("https://chat.stackoverflow.com/ws-auth", urlencode(new String[][]{
			{"fkey",fkey},
			{"roomid",""+roomid}
		}));
		String url = getStringValueJSON("url", urljson)+"?l="+chat.getT();
		return url;
	}
	public void onMessage(String message)
	{
		System.out.println("Recieved message from socket: "+message);
		ChatWebSocket.this.onMessage(message);
	}
}
