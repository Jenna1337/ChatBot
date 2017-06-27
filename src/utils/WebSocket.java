package utils;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

public class WebSocket extends Socket
{
	public WebSocket(String socketuri) throws URISyntaxException, UnknownHostException, IOException{
		this(new URI(socketuri));
	}
	public WebSocket(URI uri) throws UnknownHostException, IOException
	{
		super(uri.getHost(), getPort(uri));
		
		
		// TODO Auto-generated constructor stub
	}
	private static int getPort(URI uri){
		switch(uri.getScheme()){
			case "wss":
				return 443;
			case "ws":
				return 80;
			default:
				throw new IllegalArgumentException();
		}
	}
}
