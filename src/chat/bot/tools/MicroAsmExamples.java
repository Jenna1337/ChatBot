package chat.bot.tools;

public final class MicroAsmExamples
{
	private MicroAsmExamples(){}
	
	private static final String rolldice = "\"Rolling $1 $2-sided dice: ;=b$2;=d0;=t0;l1?d$1;#6end;!ab;+a1;+ta;\"$a ;+d1;#1l1;end\" total=$t";
	/**Rolls $1 $2-sided dice*/
	public static String rolldice(String args){
		return MicroAssembler.assemble(rolldice, args);
	}
	private static final String fibonacci = "\"First $1 numbers in the Fibbonacci sequence: ;=d0;=a1;=b1;=c0;l1?d$1;#6end;\"$a, ;=cb;+ba;=ac;+d1;#1l1;end\"...;.7";
	/**Computes the first $1 values of the Fibonacci sequence*/
	public static String fibonacci(String args){
		return MicroAssembler.assemble(fibonacci, args);
	}
}
