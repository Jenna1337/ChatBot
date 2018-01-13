import java.io.OutputStream;
import java.io.PrintStream;
import chat.bot.ChatBot;
import utils.Utils;




class LoggingOutputStream extends PrintStream{
	public LoggingOutputStream(OutputStream out){
		super(out);
	}
	@Override
	public void println(Object x)
	{
		// TODO Auto-generated method stub
		super.println(Utils.getDateTime()+" "+x);
	}
}




public class Main
{
	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception
	{
		System.setOut(new LoggingOutputStream(System.out));
		System.setErr(new LoggingOutputStream(System.err));
		ChatBot bot = new ChatBot("bot.properties");
	}
}


