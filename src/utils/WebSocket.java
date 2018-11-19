package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

//TODO: figure out why this doesn't work
public class WebSocket extends Socket
{
	private BufferedWriter dataWriter;
	private BufferedReader dataReader;
	private Thread inputthread;
	private volatile boolean closed = false;
	private Runnable handleIncomingData = ()->{
		String line;
		while(!closed) {
			try
			{
				while(!this.isInputShutdown()){
					synchronized(WebSocket.this) {
						if((line=dataReader.readLine())==null)
							break;
					}
					System.out.println(line);
				}
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try
			{
				System.out.print('.');
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
	public WebSocket(String socketuri) throws URISyntaxException, UnknownHostException, IOException{
		this(new URI(socketuri));
	}
	public WebSocket(URI uri) throws UnknownHostException, IOException
	{
		this(uri, getPort(uri));
	}
	public WebSocket(String socketuri, int port) throws URISyntaxException, UnknownHostException, IOException{
		this(new URI(socketuri), port);
	}
	public WebSocket(URI uri, int port) throws UnknownHostException, IOException
	{
		super(uri.getHost(), port);
		this.setKeepAlive(true);
		this.dataReader = new BufferedReader(new InputStreamReader(this.getInputStream()));
		this.dataWriter = new BufferedWriter(new OutputStreamWriter(this.getOutputStream()));
		this.inputthread = new Thread(this.handleIncomingData);
		inputthread.start();
		System.out.println("connected");
		this.send("TEST DATA");
		// TODO Auto-generated constructor stub
	}
	public synchronized void send(String data) throws IOException {
		dataWriter.write(data);
		dataWriter.flush();
	}
	private static int getPort(URI uri){
		System.out.println(uri.getScheme());
		switch(uri.getScheme()){
			case "wss":
				return 443;
			case "ws":
				return 80;
			default:
				throw new IllegalArgumentException();
		}
	}
	@Override
	public synchronized void close() throws IOException {
		if(!this.isInputShutdown())
			try
			{
				this.shutdownInput();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		if(!this.isOutputShutdown())
			try
			{
				this.shutdownOutput();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		if(!this.isClosed())
			super.close();
		closed=true;
	}
}
