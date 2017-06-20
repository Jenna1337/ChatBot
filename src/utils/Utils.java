package utils;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.TimeZone;
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
	public static String urlencode(String[] singlemap)
	{
		return urlencode(new String[][]{singlemap});
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
			return Boolean.parseBoolean(searchJSON("\""+parname+"\":(?i)(true|false|null)", rawjson));
		}catch(Exception e){
			try{
				return Boolean.parseBoolean(searchJSON("\'"+parname+"\':(?i)(true|false|null)", rawjson));
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
		int startindex = rawjson.indexOf(QUOTE+parname+QUOTE+':');
		if(startindex<0)
			return "";
		int index = startindex+parname.length()+3;
		boolean escapenext = false;
		char nextchar=rawjson.charAt(++index);
		try{
			while(nextchar!=QUOTE || escapenext){
				escapenext=false;
				match+=nextchar;
				if(nextchar=='\\' && !escapenext)
					escapenext=true;
				nextchar=rawjson.charAt(++index);
			}
		}catch(Exception e){
			System.err.println("Partial: "+match);
			e.printStackTrace();
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
	private static Matcher htmlescapematcher = Pattern.compile("\\&\\#(x?[A-Fa-f0-9]+);").matcher("");
	private static String replaceHtmlEscapes(String text)
	{
		htmlescapematcher.reset(text);
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		while(htmlescapematcher.find())
		{
			String match = htmlescapematcher.group(1);
			text=text.replaceAll("&#"+match+";", ""+((char)Integer.parseUnsignedInt(match, match.startsWith("x")?16:10)));
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
	/**
	 * @see <a href="https://en.wikipedia.org/wiki/List_of_XML_and_HTML_character_entity_references#Character_entity_references_in_HTML">
	 * List of XML and HTML character entity references - Wikipedia</a>
	 */
	private static final String[][] replacementRegexes = {
			{"</?b>","**"},
			{"</?i>","*"},
			{"\\\\\"","\""},
			{"\\\\\'","\'"},
			{"<\\/?code>","\u0060"},
	};
	private static final Matcher htmllinkmatcher = Pattern.compile("<a\\s+.*href=\"([^\"]+)\"(?>\\s+title=\"([^\"]+)\")?[^>]*>(.*)<\\/a>").matcher("");
	public static String unescapeHtml(String text)
	{
		if(text==null)
			return "";
		text = replaceAllAll(
				replaceAllAll(
						replaceHtmlEscapes(
								replaceUnicodeEscapes(
										text.replaceAll("&zwnj;&#8203;", ""))), 
						replacementRegexes), 
				htmlCharacterEntityReferences);
		return text;
	}
	public static String makeLinksMarkdown(String text)
	{
		if(text==null)
			return "";
		htmllinkmatcher.reset(text);
		while(htmllinkmatcher.find()){
			String optional = htmllinkmatcher.group(2);
			String replacement = "["+htmllinkmatcher.group(3)+"]("+
					htmllinkmatcher.group(1)+(optional!=null?(" \""+optional+"\""):"")+")";
			text=text.replace(htmllinkmatcher.group(), replacement);
		}
		return text;
	}
	private static final SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd_HHmm:ss.SSS");
	static{
		//new java.util.SimpleTimeZone(rawOffset, ID, startMonth, startDay, startDayOfWeek, startTime, startTimeMode, endMonth, endDay, endDayOfWeek, endTime, endTimeMode, dstSavings)
		dtf.setTimeZone(TimeZone.getDefault());
	}
	public static String getDateTime(){
		return dtf.format(System.currentTimeMillis());
	}
	
	private static String[][] htmlCharacterEntityReferences =
		{
				//And here is the long list of html character entity references
				//See https://en.wikipedia.org/wiki/List_of_XML_and_HTML_character_entity_references
				{"&quot;", "\""},
				{"&amp;", "&"},
				{"&apos;", "\'"},
				{"&lt;", "<"},
				{"&gt;", ">"},
				{"&nbsp;", "\u00A0"},
				{"&iexcl;", "\u00A1"},
				{"&cent;", "\u00A2"},
				{"&pound;", "\u00A3"},
				{"&curren;", "\u00A4"},
				{"&yen;", "\u00A5"},
				{"&brvbar;", "\u00A6"},
				{"&sect;", "\u00A7"},
				{"&uml;", "\u00A8"},
				{"&copy;", "\u00A9"},
				{"&ordf;", "\u00AA"},
				{"&laquo;", "\u00AB"},
				{"&not;", "\u00AC"},
				{"&shy;", "\u00AD"},
				{"&reg;", "\u00AE"},
				{"&macr;", "\u00AF"},
				{"&deg;", "\u00B0"},
				{"&plusmn;", "\u00B1"},
				{"&sup2;", "\u00B2"},
				{"&sup3;", "\u00B3"},
				{"&acute;", "\u00B4"},
				{"&micro;", "\u00B5"},
				{"&para;", "\u00B6"},
				{"&middot;", "\u00B7"},
				{"&cedil;", "\u00B8"},
				{"&sup1;", "\u00B9"},
				{"&ordm;", "\u00BA"},
				{"&raquo;", "\u00BB"},
				{"&frac14;", "\u00BC"},
				{"&frac12;", "\u00BD"},
				{"&frac34;", "\u00BE"},
				{"&iquest;", "\u00BF"},
				{"&Agrave;", "\u00C0"},
				{"&Aacute;", "\u00C1"},
				{"&Acirc;", "\u00C2"},
				{"&Atilde;", "\u00C3"},
				{"&Auml;", "\u00C4"},
				{"&Aring;", "\u00C5"},
				{"&AElig;", "\u00C6"},
				{"&Ccedil;", "\u00C7"},
				{"&Egrave;", "\u00C8"},
				{"&Eacute;", "\u00C9"},
				{"&Ecirc;", "\u00CA"},
				{"&Euml;", "\u00CB"},
				{"&Igrave;", "\u00CC"},
				{"&Iacute;", "\u00CD"},
				{"&Icirc;", "\u00CE"},
				{"&Iuml;", "\u00CF"},
				{"&ETH;", "\u00D0"},
				{"&Ntilde;", "\u00D1"},
				{"&Ograve;", "\u00D2"},
				{"&Oacute;", "\u00D3"},
				{"&Ocirc;", "\u00D4"},
				{"&Otilde;", "\u00D5"},
				{"&Ouml;", "\u00D6"},
				{"&times;", "\u00D7"},
				{"&Oslash;", "\u00D8"},
				{"&Ugrave;", "\u00D9"},
				{"&Uacute;", "\u00DA"},
				{"&Ucirc;", "\u00DB"},
				{"&Uuml;", "\u00DC"},
				{"&Yacute;", "\u00DD"},
				{"&THORN;", "\u00DE"},
				{"&szlig;", "\u00DF"},
				{"&agrave;", "\u00E0"},
				{"&aacute;", "\u00E1"},
				{"&acirc;", "\u00E2"},
				{"&atilde;", "\u00E3"},
				{"&auml;", "\u00E4"},
				{"&aring;", "\u00E5"},
				{"&aelig;", "\u00E6"},
				{"&ccedil;", "\u00E7"},
				{"&egrave;", "\u00E8"},
				{"&eacute;", "\u00E9"},
				{"&ecirc;", "\u00EA"},
				{"&euml;", "\u00EB"},
				{"&igrave;", "\u00EC"},
				{"&iacute;", "\u00ED"},
				{"&icirc;", "\u00EE"},
				{"&iuml;", "\u00EF"},
				{"&eth;", "\u00F0"},
				{"&ntilde;", "\u00F1"},
				{"&ograve;", "\u00F2"},
				{"&oacute;", "\u00F3"},
				{"&ocirc;", "\u00F4"},
				{"&otilde;", "\u00F5"},
				{"&ouml;", "\u00F6"},
				{"&divide;", "\u00F7"},
				{"&oslash;", "\u00F8"},
				{"&ugrave;", "\u00F9"},
				{"&uacute;", "\u00FA"},
				{"&ucirc;", "\u00FB"},
				{"&uuml;", "\u00FC"},
				{"&yacute;", "\u00FD"},
				{"&thorn;", "\u00FE"},
				{"&yuml;", "\u00FF"},
				{"&OElig;", "\u0152"},
				{"&oelig;", "\u0153"},
				{"&Scaron;", "\u0160"},
				{"&scaron;", "\u0161"},
				{"&Yuml;", "\u0178"},
				{"&fnof;", "\u0192"},
				{"&circ;", "\u02C6"},
				{"&tilde;", "\u02DC"},
				{"&Alpha;", "\u0391"},
				{"&Beta;", "\u0392"},
				{"&Gamma;", "\u0393"},
				{"&Delta;", "\u0394"},
				{"&Epsilon;", "\u0395"},
				{"&Zeta;", "\u0396"},
				{"&Eta;", "\u0397"},
				{"&Theta;", "\u0398"},
				{"&Iota;", "\u0399"},
				{"&Kappa;", "\u039A"},
				{"&Lambda;", "\u039B"},
				{"&Mu;", "\u039C"},
				{"&Nu;", "\u039D"},
				{"&Xi;", "\u039E"},
				{"&Omicron;", "\u039F"},
				{"&Pi;", "\u03A0"},
				{"&Rho;", "\u03A1"},
				{"&Sigma;", "\u03A3"},
				{"&Tau;", "\u03A4"},
				{"&Upsilon;", "\u03A5"},
				{"&Phi;", "\u03A6"},
				{"&Chi;", "\u03A7"},
				{"&Psi;", "\u03A8"},
				{"&Omega;", "\u03A9"},
				{"&alpha;", "\u03B1"},
				{"&beta;", "\u03B2"},
				{"&gamma;", "\u03B3"},
				{"&delta;", "\u03B4"},
				{"&epsilon;", "\u03B5"},
				{"&zeta;", "\u03B6"},
				{"&eta;", "\u03B7"},
				{"&theta;", "\u03B8"},
				{"&iota;", "\u03B9"},
				{"&kappa;", "\u03BA"},
				{"&lambda;", "\u03BB"},
				{"&mu;", "\u03BC"},
				{"&nu;", "\u03BD"},
				{"&xi;", "\u03BE"},
				{"&omicron;", "\u03BF"},
				{"&pi;", "\u03C0"},
				{"&rho;", "\u03C1"},
				{"&sigmaf;", "\u03C2"},
				{"&sigma;", "\u03C3"},
				{"&tau;", "\u03C4"},
				{"&upsilon;", "\u03C5"},
				{"&phi;", "\u03C6"},
				{"&chi;", "\u03C7"},
				{"&psi;", "\u03C8"},
				{"&omega;", "\u03C9"},
				{"&thetasym;", "\u03D1"},
				{"&upsih;", "\u03D2"},
				{"&piv;", "\u03D6"},
				{"&ensp;", "\u2002"},
				{"&emsp;", "\u2003"},
				{"&thinsp;", "\u2009"},
				{"&zwnj;", "\u200C"},
				{"&zwj;", "\u200D"},
				{"&lrm;", "\u200E"},
				{"&rlm;", "\u200F"},
				{"&ndash;", "\u2013"},
				{"&mdash;", "\u2014"},
				{"&lsquo;", "\u2018"},
				{"&rsquo;", "\u2019"},
				{"&sbquo;", "\u201A"},
				{"&ldquo;", "\u201C"},
				{"&rdquo;", "\u201D"},
				{"&bdquo;", "\u201E"},
				{"&dagger;", "\u2020"},
				{"&Dagger;", "\u2021"},
				{"&bull;", "\u2022"},
				{"&hellip;", "\u2026"},
				{"&permil;", "\u2030"},
				{"&prime;", "\u2032"},
				{"&Prime;", "\u2033"},
				{"&lsaquo;", "\u2039"},
				{"&rsaquo;", "\u203A"},
				{"&oline;", "\u203E"},
				{"&frasl;", "\u2044"},
				{"&euro;", "\u20AC"},
				{"&image;", "\u2111"},
				{"&weierp;", "\u2118"},
				{"&real;", "\u211C"},
				{"&trade;", "\u2122"},
				{"&alefsym;", "\u2135"},
				{"&larr;", "\u2190"},
				{"&uarr;", "\u2191"},
				{"&rarr;", "\u2192"},
				{"&darr;", "\u2193"},
				{"&harr;", "\u2194"},
				{"&crarr;", "\u21B5"},
				{"&lArr;", "\u21D0"},
				{"&uArr;", "\u21D1"},
				{"&rArr;", "\u21D2"},
				{"&dArr;", "\u21D3"},
				{"&hArr;", "\u21D4"},
				{"&forall;", "\u2200"},
				{"&part;", "\u2202"},
				{"&exist;", "\u2203"},
				{"&empty;", "\u2205"},
				{"&nabla;", "\u2207"},
				{"&isin;", "\u2208"},
				{"&notin;", "\u2209"},
				{"&ni;", "\u220B"},
				{"&prod;", "\u220F"},
				{"&sum;", "\u2211"},
				{"&minus;", "\u2212"},
				{"&lowast;", "\u2217"},
				{"&radic;", "\u221A"},
				{"&prop;", "\u221D"},
				{"&infin;", "\u221E"},
				{"&ang;", "\u2220"},
				{"&and;", "\u2227"},
				{"&or;", "\u2228"},
				{"&cap;", "\u2229"},
				{"&cup;", "\u222A"},
				{"&int;", "\u222B"},
				{"&there4;", "\u2234"},
				{"&sim;", "\u223C"},
				{"&cong;", "\u2245"},
				{"&asymp;", "\u2248"},
				{"&ne;", "\u2260"},
				{"&equiv;", "\u2261"},
				{"&le;", "\u2264"},
				{"&ge;", "\u2265"},
				{"&sub;", "\u2282"},
				{"&sup;", "\u2283"},
				{"&nsub;", "\u2284"},
				{"&sube;", "\u2286"},
				{"&supe;", "\u2287"},
				{"&oplus;", "\u2295"},
				{"&otimes;", "\u2297"},
				{"&perp;", "\u22A5"},
				{"&sdot;", "\u22C5"},
				{"&lceil;", "\u2308"},
				{"&rceil;", "\u2309"},
				{"&lfloor;", "\u230A"},
				{"&rfloor;", "\u230B"},
				{"&lang;", "\u2329"},
				{"&rang;", "\u232A"},
				{"&loz;", "\u25CA"},
				{"&spades;", "\u2660"},
				{"&clubs;", "\u2663"},
				{"&hearts;", "\u2665"},
				{"&diams;", "\u2666"}
		};
}
