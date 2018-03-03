package chat;

public enum ChatSite
{
	STACKOVERFLOW    ("chat.stackoverflow.com"     , "SO"  ),
	STACKEXCHANGE    ("chat.stackexchange.com"     , "SE"  ),
	METASTACKEXCHANGE("chat.meta.stackexchange.com", "META"),
	;//abbreviation  s
	
	private final String url, abbreviation;
	private ChatSite(String url, String abbr){
		this.url=url;
		this.abbreviation=abbr;
	}
	public String getUrl(){
		return url;
	}
	public String getAbbreviation()
	{
		return abbreviation;
	}
	@Override
	public String toString(){
		return getUrl();
	}
}
