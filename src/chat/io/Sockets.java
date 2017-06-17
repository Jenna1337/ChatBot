package chat.io;

import static utils.Utils.urlencode;
import static utils.WebRequest.POST;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import javax.net.ssl.SSLSocket;

@SuppressWarnings("unused")
public class Sockets
{
	
	public Sockets()
	{
		// TODO Auto-generated constructor stub
	}
	private void connectSocket(String socketurl, long time) throws IOException
	{
		try{
			String hostName = socketurl;//POST("https://chat.stackoverflow.com/ws-auth", 
					//urlencode(new String[]{"l",""+time}));
			int portNumber = 0;//TODO
			Socket sock = new Socket(hostName, portNumber);
			PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
			BufferedReader in =new BufferedReader(new InputStreamReader(sock.getInputStream()));
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			//TODO
			sock.close();
		}
		catch(Exception e){
			
		}
	}
	private String getSocketURL(long roomid, String fkey) throws IOException
	{
		return POST("https://chat.stackoverflow.com/ws-auth", urlencode(new String[][]{
			{"fkey",fkey},
			{"roomid",""+roomid}
		}));
	}
}
