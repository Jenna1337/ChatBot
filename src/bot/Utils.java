package bot;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils
{
	private Utils(){}
	
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
	public static long getNumValueJSON(String parname, String rawjson)
	{
		try{
			return Long.parseLong(search("\""+parname+"\":(\\d*)", rawjson));
		}catch(Exception e){
			try{
				return Long.parseLong(search("\'"+parname+"\':(\\d*)", rawjson));
			}catch(Exception e2){
				return 0;
			}
		}
	}
	public static String getStringValueJSON(String parname, String rawjson)
	{
		try{
			return search("\""+parname+"\":\"(([^\\\\\"]*(\\\\.)?)*)\"", rawjson);
		}catch(Exception e){
			try{
				return search("\""+parname+"\":\'(([^\\\\\']*(\\\\.)?)*)\'", rawjson);
			}catch(Exception e2){
				try{
					return search("\'"+parname+"\':\"(([^\\\\\"]*(\\\\.)?)*)\"", rawjson);
				}catch(Exception e3){
					try{
						return search("\'"+parname+"\':\'(([^\\\\\']*(\\\\.)?)*)\'", rawjson);
					}catch(Exception e4){
						return "";
					}
				}
			}
		}
	}
}
