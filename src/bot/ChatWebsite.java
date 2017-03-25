package bot;

import java.net.MalformedURLException;
import java.net.URL;

public enum ChatWebsite
{
	StackExchange("chat.stackexchange.com"),
	;
	private final URL domain;
	private ChatWebsite(String url)
	{
		if(!url.startsWith("http://"))
			url="http://"+url;
		try
		{
			this.domain = new URL(url);
		}
		catch(MalformedURLException e)
		{
			throw new InternalError(e);
		}
	}
	public URL getDomain()
	{
		return domain;
	}
}
