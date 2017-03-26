package bot;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class ChatIO
{
	private final ChatWebsite website;
	private final String cookie;
	
	public ChatIO(String username, String password, ChatWebsite website)
	{
		this.website = website;
		this.cookie = login(username, password);
	}
	
	/**
	 * 
	 * @param username
	 * @param password
	 * @return The cookies that were set
	 */
	private String login(String username, String password)
	{
		// TODO Auto-generated method stub
		return null;
	}
	public Object getMessages(int roomid)
	{
		try {
			
			String query = URLEncoder.encode("key", "UTF-8") + "=" + URLEncoder.encode("value", "UTF-8");
			String cookies = "session_cookie=value";
			URL url = new URL("http://myweb");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			
			conn.setRequestProperty("Cookie", cookies);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			
			conn.connect();
			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
			out.writeBytes(query);
			out.flush();
			out.close();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String decodedString;
			while ((decodedString = in.readLine()) != null) {
				System.out.println(decodedString);
			}
			in.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	public void putMessage(int roomid, String message)
	{
		// TODO Auto-generated method stub
		
	}
	
	public URL getDomain()
	{
		return website.getDomain();
	}
}
