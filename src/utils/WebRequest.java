package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;

public class WebRequest
{
	private WebRequest(){}
	
	static{
		final CookieManager cook = new CookieManager();
		cook.setCookiePolicy(java.net.CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(cook);
		HttpURLConnection.setFollowRedirects(true);
	}
	static{
		String[][] headers = {
				{"Accept", "*/*"},
				{"Accept-Encoding", "gzip, deflate"},
				{"Accept-Language", "en-US,en;q=0.5"},
				{"DNT", "1"},
				{"Cache-Control", "no-cache"},
				//{"Connection", "keep-alive"},
				{"Content-Type", "application/x-www-form-urlencoded"},
				{"Upgrade-Insecure-Requests", "1"},
				{"User-Agent", "Mozilla/5.0 (X11; Linux i686; rv:17.0) Gecko/20100101 Firefox/17.0"},
		};
		WebRequest.setDefaultHeaders(headers);
	}
	private static String[][] defaultHeaders = new String[0][0];
	private static Charset chset = java.nio.charset.StandardCharsets.UTF_8;
	
	
	
	public static synchronized String GET(String url, String[]... optionalheaders) throws MalformedURLException, IOException
	{
		return GET(new URL(url), optionalheaders);
	}
	public static synchronized String GET(URL url, String[]... optionalheaders) throws IOException
	{
		HttpURLConnection connection = request("GET", url, optionalheaders);
		return read(connection);
	}
	public static synchronized String POST(String url, String data, String[]... optionalheaders) throws MalformedURLException, IOException
	{
		return POST(new URL(url), data, optionalheaders);
	}
	public static synchronized String POST(URL url, String data, String[]... optionalheaders) throws IOException
	{
		HttpURLConnection connection = request("POST", url, optionalheaders);
		connection.setDoOutput(true);
		send(connection, data);
		if(connection.getResponseCode()==409)
			return null;
		try{
			return read(connection);
		}catch(NullPointerException npe){
			npe.printStackTrace();
			return null;
		}
	}
	public static synchronized Map<String, List<String>> HEAD(String url, String[]... optionalheaders) throws MalformedURLException, IOException
	{
		return HEAD(new URL(url), optionalheaders);
	}
	public static synchronized Map<String, List<String>> HEAD(URL url, String[]... optionalheaders) throws IOException
	{
		HttpURLConnection connection = request("HEAD", url, optionalheaders);
		return connection.getHeaderFields();
	}
	public static void setDefaultHeaders(String[][] headers)
	{
		WebRequest.defaultHeaders = headers;
	}
	private static HttpURLConnection setHeaders(HttpURLConnection connection, String[][]... headermaplist){
		for(String[][] headermaps : headermaplist){
		for(String[] headermap : headermaps){
			if(headermap.length!=2)
				throw new IllegalArgumentException("Invalid header "+java.util.Arrays.deepToString(headermap));
			connection.setRequestProperty(headermap[0], headermap[1]);
		}
		}
		return connection;
	}
	public static HttpURLConnection request(String method, URL url, String[]... optionalheaders)
	{
		try{
			HttpURLConnection connection = setHeaders((HttpURLConnection) url.openConnection(), defaultHeaders, optionalheaders);
			connection.setRequestMethod(method);
			return connection;
		}catch(IOException e){
			throw new InternalError(e);
		}
	}
	/**
	 * Reads the content from a server.
	 * @param connection the HttpURLConnection to read data from.
	 * @return The content read from the server.
	 * @throws IOException if an IOException occurs.
	 */
	private static synchronized String read(HttpURLConnection connection) throws IOException
	{
		connection.connect();
		String encoding = (connection.getContentEncoding()!=null) ? connection.getContentEncoding().toLowerCase() : "";
		InputStream is = connection.getInputStream();
		switch(encoding){
			case "gzip":
				is=new GZIPInputStream(is);
				break;
			case "deflate":
				is=new DeflaterInputStream(is);
				break;
			default:
				break;
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String response_text="", line;
		while((line=reader.readLine()) != null)
			response_text+=line+System.lineSeparator();
		reader.close();
		return response_text;
	}
	/**
	 * Sends the data to the server via the {@code connection}.
	 * @param connection the connection to the server to send data to.
	 * @param data the data to send.
	 * @throws IOException if an IOException occurs.
	 */
	private static synchronized void send(HttpURLConnection connection, String data) throws IOException
	{
		try{
			connection.connect();
			OutputStream os = connection.getOutputStream();
			os.write(data.getBytes(chset));
			os.close();
		}catch(Exception e){
			try{
				Thread.sleep(1000);
				send(connection, data);
			}
			catch(Exception | StackOverflowError err){
				if(err.getClass().isAssignableFrom(StackOverflowError.class))
					System.err.println("Connection timed out to "+connection.getURL());
			}
		}
	}
	/**
	 * Reads the raw response from a server.
	 * @param connection the HttpURLConnection to read the raw response from.
	 * @return The raw response from the server.
	 * @throws IOException if an IOException occurs.
	 */
	public static synchronized String readRaw(HttpURLConnection connection) throws IOException
	{
		connection.connect();
		Map<String,List<String>> headMap = connection.getHeaderFields();
		String raw = "";
		//A status line which includes the status code and reason message
		// (e.g., HTTP/1.1 200 OK).
		raw += connection.getHeaderField(null)+'\n';
		//Response header fields
		for(String head : headMap.keySet())
		{
			if(head==null)
				continue;
			raw += head + ": ";
			List<String> vals = headMap.get(head);
			//Response header field values (e.g., Content-Type: text/html).
			for(String v : vals)
				raw += v;
			raw += '\n';
		}
		//An empty line.
		raw += '\n';
		//An optional message body.
		raw += read(connection);
		return raw;
	}
}
