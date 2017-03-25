package bot;

import java.net.URL;

public class ChatBot
{
	private final URL domain;
	public ChatBot(ChatWebsite website)
	{
		this.domain = website.getDomain();
		// TODO Auto-generated constructor stub
	}
}

class Main
{
	public static void main(String args)
	{
		ChatBot bot = new ChatBot(ChatWebsite.StackExchange);
	}
}