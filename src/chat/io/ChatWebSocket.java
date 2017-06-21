package chat.io;

import static utils.Utils.urlencode;
import static utils.Utils.getStringValueJSON;
import static utils.WebRequest.POST;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

public class ChatWebSocket
{
	private Socket sock;
	public ChatWebSocket(ChatIO chat, long roomid)
	{
		try
		{
			final String fkey = chat.getFkey();
			String urljson = POST("https://chat.stackoverflow.com/ws-auth", urlencode(new String[][]{
				{"fkey",fkey},
				{"roomid",""+roomid}
			}));
			String url = getStringValueJSON("url", urljson)+"?l="+chat.getT();
			connectSocket(url);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		// TODO Auto-generated constructor stub
	}
	public void onMessage(String message)
	{
		System.out.println("Recieved message from socket: "+message);
		ChatWebSocket.this.onMessage(message);
	}
	private void connectSocket(String socketurl)
	{
		try
		{
			URI uri = new URI(socketurl);
			int port;
			switch(uri.getScheme()){
				case "wss":
					port = 443;
					break;
				case "ws":
					port = 80;
					break;
				default:
					throw new IllegalArgumentException();
			}
			try
			{
				sock = new Socket(uri.getHost(), port);
				InetSocketAddress addr = new InetSocketAddress(uri.getHost(), port);
				sock.connect(addr);
			}
			catch(Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		catch(URISyntaxException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
