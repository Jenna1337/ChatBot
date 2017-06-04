package chat;

public enum ChatSite
{
	STACKOVERFLOW("chat.stackoverflow.com"),
	STACKEXCHANGE("chat.stackexchange.com"),
	;
	private final String url;
	private ChatSite(String url)
	{
		this.url=url;
	}
	public String getUrl()
	{
		return url;
	}
	@Override
	public String toString()
	{
		new UnsupportedOperationException().printStackTrace();
		return "";
	}
}
