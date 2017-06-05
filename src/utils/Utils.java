package utils;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils
{
	private Utils(){}
	
	private static HashMap<String, Matcher> jsonmatchers = new HashMap<>();
	
	/**
	 * 
	 * @param regex
	 * @param in
	 * @return The match in <code>in</code> for the first group in <code>regex</code>.
	 * @throws IllegalArgumentException  If no match was found or if there is no capturing group in the regex
	 */
	public static String search(String regex, String in)
	{
		try
		{
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(in);
			m.find();
			return m.group(1);
		}
		catch(Exception e)
		{
			throw new IllegalArgumentException(e);
		}
	}
	private static String searchJSON(String regex, String input){
		Matcher m;
		if(!jsonmatchers.containsKey(regex))
		{
			jsonmatchers.put(regex, m=Pattern.compile(regex).matcher(input));
		}
		else
		{
			m = jsonmatchers.get(regex);
			m.reset(input);
		}
		m.find();
		return m.group(1);
	}
	public static String urlencode(String text)
	{
		try
		{
			return URLEncoder.encode(text, "UTF-8");
		}
		catch(UnsupportedEncodingException e)
		{
			throw new InternalError(e);
		}
	}
	public static String urlencode(String[][] map)
	{
		try
		{
			String encoded = "";
			for(int i=0;i<map.length;++i){
				String[] parmap = map[i];
				if(parmap.length!=2)
					throw new IllegalArgumentException("Invalid parameter mapping "+java.util.Arrays.deepToString(parmap));
				encoded += parmap[0] + "=" + 
						URLEncoder.encode(parmap[1], "UTF-8") +
						(i+1<map.length ? "&" : "");
			}
			return encoded;
		}
		catch(UnsupportedEncodingException e)
		{
			throw new InternalError(e);
		}
	}
	public static boolean getBooleanValueJSON(String parname, String rawjson)
	{
		try{
			return Boolean.parseBoolean(searchJSON("\""+parname+"\":(\\d*)", rawjson));
		}catch(Exception e){
			try{
				return Boolean.parseBoolean(searchJSON("\'"+parname+"\':(\\d*)", rawjson));
			}catch(Exception e2){
				String s = getStringValueJSON(parname, rawjson);
				return s.isEmpty() ? false : Boolean.parseBoolean(s);
			}
		}
	}
	public static long getNumValueJSON(String parname, String rawjson)
	{
		try{
			return Long.parseLong(searchJSON("\""+parname+"\":(\\d*)", rawjson));
		}catch(Exception e){
			try{
				return Long.parseLong(searchJSON("\'"+parname+"\':(\\d*)", rawjson));
			}catch(Exception e2){
				//String s = getStringValueJSON(parname, rawjson);
				return 0;
			}
		}
	}
	public static String getStringValueJSON(String parname, String rawjson)
	{
		String match = "";
		char QUOTE = '\"';
		int startindex = rawjson.indexOf(QUOTE+parname+QUOTE+':'+QUOTE);
		if(startindex<0)
			return "";
		int index = startindex+parname.length()+3;
		boolean escapenext = false;
		char nextchar;
		try{
			while((nextchar=rawjson.charAt(++index))!=QUOTE || escapenext){
				escapenext=false;
				match+=nextchar;
				if(nextchar=='\\')
					escapenext=true;
			}
		}catch(Exception e){
			System.out.println(match);
		}
		return match;
		/*
		try{
			return searchJSON("\""+parname+"\":\"(([^\\\\\"]*(\\\\.)?)*)\"", rawjson);
		}catch(Exception e){
			try{
				return searchJSON("\""+parname+"\":\'(([^\\\\\']*(\\\\.)?)*)\'", rawjson);
			}catch(Exception e2){
				try{
					return searchJSON("\'"+parname+"\':\"(([^\\\\\"]*(\\\\.)?)*)\"", rawjson);
				}catch(Exception e3){
					try{
						return searchJSON("\'"+parname+"\':\'(([^\\\\\']*(\\\\.)?)*)\'", rawjson);
					}catch(Exception e4){
						return "";
					}
				}
			}
		}*/
	}
	private static Matcher unicodematcher = Pattern.compile("\\\\u(....)").matcher("");
	private static String replaceUnicodeEscapes(String text)
	{
		unicodematcher.reset(text);
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		while(unicodematcher.find())
		{
			String match = unicodematcher.group(1);
			text=text.replaceAll("\\\\u"+match, ""+((char)Integer.parseUnsignedInt(match, 16)));
			unicodematcher.reset(text);
		}
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
		return text;
	}
	private static Matcher htmlescapematcher = Pattern.compile("\\&\\#(..);").matcher("");
	private static String replaceHtmlEscapes(String text)
	{
		htmlescapematcher.reset(text);
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		while(htmlescapematcher.find())
		{
			String match = htmlescapematcher.group(1);
			text=text.replaceAll("&#"+match+";", ""+((char)Integer.parseUnsignedInt(match, 16)));
		}
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
		return text;
	}
	private static String replaceAllAll(String target, String[][] rr)
	{
		for(String[] r : rr)
			target=target.replaceAll(r[0], r[1]);
		return target;
	}
	private static final String[][] replacementRegexes = {
			{"</?b>","**"},
			{"</?i>","*"},
			{"&quot;","\""},
			{"&lt;","<"},
			{"&gt;",">"},
			//{"&$39;","\'"},
			{"&amp;","&"},
			{"\\\\\"","\""},
			{"\\\\\'","\'"},
			{"<\\/?code>","\u0096"}
	};
	private static final Matcher htmllinkmatcher = Pattern.compile("<a\\s+.*href=\"([^\"]+)\"(?>\\s+title=\"([^\"]+)\")?[^>]*>(.*)<\\/a>").matcher("");
	public static String unescapeHtml(String text)
	{
		if(text==null)
			return "";
		text =replaceAllAll(replaceHtmlEscapes(replaceUnicodeEscapes(text)), replacementRegexes);
		return text;
	}
	public static String makeMarkdown(String text)
	{
		if(text==null)
			return "";
		text =unescapeHtml(text);
		htmllinkmatcher.reset(text);
		while(htmllinkmatcher.find()){
			String optional = htmllinkmatcher.group(2);
			String replacement = "["+htmllinkmatcher.group(3)+"]("+
					htmllinkmatcher.group(1)+(optional!=null?(" \""+optional+"\""):"")+")";
			text=text.replace(htmllinkmatcher.group(), replacement);
		}
		return text;
	}
}
