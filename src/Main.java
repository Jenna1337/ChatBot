import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import chat.bot.ChatBot;
import utils.Utils;




class LoggingOutputStream extends PrintStream{
	private static final String placeholder = "\u0091";
	private static String datetimestring = System.getProperty("user.dir")
			+ "/SEChatBot/"
			+ Utils.getDateTime().replaceAll("[-:]", "").replace('.', '_')
			+ "."+placeholder+".log";
	private final FileWriter fw;
	public LoggingOutputStream(OutputStream out){
		super(out);
		fw=null;
	}
	public LoggingOutputStream(OutputStream out, String name) throws IOException{
		super(out);
		fw = new FileWriter(datetimestring.replace(placeholder, name));
	}
	@Override
	public void println(String x){
		String output = Utils.getDateTime()+" "+x;
		super.println(output);
		if(fw!=null){
			try{
				fw.write(output);
				fw.flush();
			}
			catch(IOException e){
				e.printStackTrace(System.out);
			}
		}
	}
}




public class Main
{
	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception
	{
		System.setOut(new LoggingOutputStream(System.out));
		System.setErr(new LoggingOutputStream(System.err, "err"));
		ChatBot bot = new ChatBot("bot.properties");
	}
}


