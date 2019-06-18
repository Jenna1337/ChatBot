package chat.bot.tools;

public final class MicroAsmExamples
{
	private MicroAsmExamples(){}
	
	private static final String rolldice = "\"Rolling $1 $2-sided dice: ;=b$2;=d0;=t0;l1?d$1;#6end;!ab;+a1;+ta;\"$a ;+d1;#1l1;end\" total=$t";
	/**Rolls $1 $2-sided dice*/
	public static String rolldice(String args){
		return MicroAssembler.assemble(rolldice, args);
	}
	
	private static final String fibonacci = "\"First $1 numbers in the Fibonacci sequence: ;=d0;=a1;=b1;=c0;l1?d$1;#6end;\"$a, ;=cb;+ba;=ac;+d1;#1l1;end\"...";
	/**Computes the first $1 values of the Fibonacci sequence*/
	public static String fibonacci(String args){
		return MicroAssembler.assemble(fibonacci, args);
	}
	
	private static final String rand2 = "=l$1;=h$2;?hl;#4two;=hl;=l0;two=nh;-nl;!rn;+rl;\"$r";
	/**Outputs a random number from $1, inclusive, to $2, exclusive. [$1, $2)*/
	public static String rand2(String args){
		return MicroAssembler.assemble(rand2, args);
	}
	
	private static final String rand0 = "!r2;\"$r";
	/**Outputs a random number from 0 to 1, inclusive. [0, 1]*/
	public static String rand0(String args){
		return MicroAssembler.assemble(rand0, args);
	}
	
	private static final String rand1 = "!r$1;\"$r";
	/**Outputs a random number from 0, inclusive, to $1, exclusive. [0, $1)*/
	public static String rand1(String args){
		return MicroAssembler.assemble(rand1, args);
	}
}
