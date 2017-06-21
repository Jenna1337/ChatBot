package chat.bot.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.*;

/**
 * 
 * @author Jenna Sloan
 * @version 3.7
 */
public final class MicroAssembler{
	private MicroAssembler(){}
	/**The registers.*/
	private static int[] registers = new int[26];
	
	/**The index of the instruction to be executed next*/
	private static int proginstr;
	/**The user-defined labels and the value of their associated line.*/
	private static Map<String, Integer> labels = new HashMap<>();
	private static ComparisonMask compval=ComparisonMask.NOP;
	private static boolean showWarnings = false;
	private static final Random rand = new Random();
	
	/**Greater than; comparison mask*/
	private static final byte G=0b100;
	/**Less than; comparison mask*/
	private static final byte L=1;
	/**Equal to; comparison mask*/
	private static final byte E=0b10;
	/**No operation; comparison mask*/
	private static final byte N=0;
	
	//TODO add the ability to use these for the jump condition
	private static enum ComparisonMask{
		/**No operation*/
		NOP(N),
		/**Less than*/
		LSS(L),
		/**Equal to*/
		EQU(E),
		/**Less than or Equal to*/
		LEQ(L|E),
		/**Greater than*/
		GTR(G),
		/**NOT Equal to*/
		NEQ(G|L),
		/**Greater than or Equal to*/
		GEQ(G|E),
		;
		private final byte m;
		ComparisonMask(final int mask){
			m=(byte)mask;
		}
		public byte getMask(){return m;}
	}
	
	//Reserved: ;!=+-*/?&|~<>#"$.\
	//Operators: ;!=+-*/?&|~<>#".
	//Special use in output: $;\
	//Not used: ()[]{}%^,':_
	//Purposely unused: @
	/**The character to separate lines*/
	private static final char delimiter=';';
	/**The character to output data*/
	private static final char op_out='\"';
	/**The character to set a register to a random number from 0 to arg2*/
	private static final char op_rnd='!';
	/**The character to set registers*/
	private static final char op_mov='=';
	/**The character to add registers*/
	private static final char op_add='+';
	/**The character to subtract registers*/
	private static final char op_sub='-';
	/**The character to multiply registers*/
	private static final char op_mul='*';
	/**The character to divide registers*/
	private static final char op_div='/';
	/**The character to compare registers*/
	private static final char op_cmp='?';
	/**The character to bitwise AND on registers*/
	private static final char op_and='&';
	/**The character to bitwise OR on registers*/
	private static final char op_orr='|';
	/**The character to bitwise NOT on registers*/
	private static final char op_not='~';
	/**The character to bitshift left without carry on registers*/
	private static final char op_lft='<';
	/**The character to bitshift right without carry on registers*/
	private static final char op_rgt='>';
	/**The character to jump to a label*/
	private static final char op_jmp='#';
	/**The character to output the value of a register and obtaining values from arguments*/
	private static final char rvalch = '$';
	/**Temporary substitute character for rvalch*/
	private static final char rvalchsub = '\uF024';
	/**The character to escape the following character*/
	private static final char escvalch = '\\';
	/**The escaped version of rvalch*/
	private static final String rvalchesc = rvalch+""+rvalch;
	/**Marks the end of the assembly code and beginning of the code's arguments*/
	private static final String inputseperator = delimiter+".";
	/**The regex for matching registers.*/
	private static final String rRegx="[A-Za-z]";
	/**The regex for printing the value of a register.*/
	private static final String regx = "\\"+rvalch+"("+rRegx+")";
	/**The regex to check if a string contains {@link #regx}.*/
	private static final String contregx = "^.*"+regx+".*$";
	/**The Pattern to check if a string contains {@link #regx}.*/
	private static final Pattern rpat= Pattern.compile(regx);
	/**The output text.*/
	private static String output;
	/**The various instructions*/
	private static enum Instruction{
		//Note: OUT has to be first to print all the other ops
		/**The output instruction*/
		OUT(op_out,ParType.Text){
			void action(Object[] args){
				String txt = (String)args[0];
				if(txt.matches(contregx)){
					Matcher m = rpat.matcher(txt);
					while(txt.matches(contregx)){
						m.find();
						char ch = m.group(1).charAt(0);
						txt = txt.replace(rvalch+""+ch, ""+registers[correct(ch)]);
					}
				}
				output+=txt;
			}
		},
		/**The set data to a random value from 0 (inclusive) to arg2 (exclusive) instruction*/
		RND(op_rnd,ParType.AlfaAlfanum){
			void action(Object[] args){
				registers[correct((Character)args[0])] = rand.nextInt((Integer)args[1]);
			}
		},
		/**The move data instruction*/
		MOV(op_mov,ParType.AlfaAlfanum){
			void action(Object[] args){
				registers[correct((Character)args[0])] = (Integer)args[1];
			}
		},
		/**The addition instruction*/
		ADD(op_add,ParType.AlfaAlfanum){
			void action(Object[] args){
				registers[correct((Character)args[0])] += (Integer)args[1];
			}
		},
		/**The subtraction instruction*/
		SUB(op_sub,ParType.AlfaAlfanum){
			void action(Object[] args){
				registers[correct((Character)args[0])] -= (Integer)args[1];
			}
		},
		/**The multiplication instruction*/
		MUL(op_mul,ParType.AlfaAlfanum){
			void action(Object[] args){
				registers[correct((Character)args[0])] *= (Integer)args[1];
			}
		},
		/**The division instruction*/
		DIV(op_div,ParType.AlfaAlfanum){
			void action(Object[] args){
				registers[correct((Character)args[0])] /= (Integer)args[1];
			}
		},
		/**The compare instruction*/
		CMP(op_cmp,ParType.AlfaAlfanum){
			void action(Object[] args){
				int val1 = registers[correct((Character)args[0])];
				int val2 = (Integer)args[1];
				if(val1>val2)
					compval=ComparisonMask.GTR;
				else if(val1<val2)
					compval=ComparisonMask.LSS;
				else if(val1==val2)
					compval=ComparisonMask.EQU;
				else
					compval=ComparisonMask.NOP;
			}
		},
		/**The bitwise AND instruction*/
		AND(op_and,ParType.AlfaAlfanum){
			void action(Object[] args){
				registers[correct((Character)args[0])] &= (Integer)args[1];
			}
		},
		/**The bitwise OR instruction*/
		ORR(op_orr,ParType.AlfaAlfanum){
			void action(Object[] args){
				registers[correct((Character)args[0])] |= (Integer)args[1];
			}
		},
		/**The bitwise NOT instruction*/
		NOT(op_not,ParType.Alfa){
			void action(Object[] args){
				int r = correct((Character)args[0]);
				registers[r] = ~registers[r];
			}
		},
		/**The bit-shift left without carry instruction*/
		LFT(op_lft,ParType.AlfaAlfanum){
			void action(Object[] args){
				registers[correct((Character)args[0])] <<= (Integer)args[1];
			}
		},
		/**The bit-shift right without carry instruction*/
		RGT(op_rgt,ParType.AlfaAlfanum){
			void action(Object[] args){
				registers[correct((Character)args[0])] >>>= (Integer)args[1];
			}
		},
		/**The jump to label instruction*/
		JMP(op_jmp,ParType.NumWord){
			void action(Object[] args){
				int condition = (Integer)args[0];
				if(condition==0)
					return;
				String target = ((String)args[1]).toLowerCase();
				try{
					int targetline = labels.get(target);
					if((compval.getMask()&condition)>0)
						proginstr = targetline;
				}catch(NullPointerException npe){
					throw new IllegalArgumentException("No line found with label \""+target+"\"");
				}
			}
		},
		;
		/**The instruction's mnemonic*/
		private final char mne;
		/**The instruction's input type*/
		private final ParType kind;
		private Instruction(char mnemonic, ParType kind){
			this.mne=mnemonic;
			this.kind=kind;
		}
		/**The action this instruction performs*/
		abstract void action(Object[] args);
		/**Input types*/
		private static enum ParType{
			Alfa,
			AlfaAlfanum,
			NumWord,
			Text,
			;
		}
		/**Parses the argument(s)*/
		private Object[] parseargs(String args)
		{
			switch(kind){
				case Alfa:
					if(args.matches(rRegx))
						return new Object[]{args.charAt(0)};
					break;
				case AlfaAlfanum:
					if(args.matches(rRegx+rRegx))
						return new Object[]{args.charAt(0), registers[correct(args.charAt(1))]};
					if(args.matches(rRegx+"\\-?\\d+"))
						return new Object[]{args.charAt(0), Integer.parseInt(args.substring(1))};
					break;
				case NumWord:
					if(args.matches("\\d\\w+") || (args.charAt(0)=='0'))
						return new Object[]{Integer.parseInt(""+args.charAt(0)), args.substring(1)};
					break;
				case Text:
					return new Object[]{args};
				default:
					throw new InternalError("Invalid kind \""+kind.name()+"\"");
			}
			throw new IllegalArgumentException("The argument \""+args+"\" is invalid for the \'"+this.getMne()+"\' instruction ");
		}
		/**Performs the action with the arguments*/
		public void performAction(String args){
			Object[] pargs=parseargs(args);
			this.action(pargs);
		}
		/**Returns this instruction's mnemonic*/
		public char getMne(){
			return this.mne;
		}
	}
	/**Return the index of the register referred to by the given character*/
	private static int correct(char ch){
		if('A'<=ch && ch<='Z')
			return ch-'A';
		if('a'<=ch && ch<='z')
			return ch-'a';
		throw new IllegalArgumentException("'"+ch+"' is not a valid register.");
	}
	public static synchronized String assemble(String input){
		
		int argstart = input.indexOf(inputseperator);
		String arguments = "";
		if(argstart>=0){
			arguments = input.substring(argstart+inputseperator.length());
			input=input.substring(0,argstart);
		}
		return assemble(input, arguments);
	}
	/**Runs the code specified in <code>input</code> and returns it's output.<br/>
	 * Format: [label]opcode(parameters)
	 * @param input The code to assemble and execute.
	 * @return The output of the assembled code.
	 */
	public static synchronized String assemble(String input, String arguments){
		String[] progargs=arguments.split(" ");
		input = input.replace(rvalchesc, ""+rvalchsub);
		input=input.replace(rvalch+"0", arguments);
		for(int i=0;i<progargs.length && i<9;++i)
			input=input.replace(rvalch+""+(i+1), progargs[i]);
		String[] lines=input.split("(?<!\\"+escvalch+")"+delimiter);
		Instruction[] lblinstr = new Instruction[lines.length];
		String[] lblcmds=new String[lines.length];
		String line;
		labels.clear();
		/*parse the input*/
		prolines:
			for(proginstr=0; proginstr<lines.length;++proginstr){
				line=lines[proginstr];
				line=line.replaceAll("\\"+escvalch+"(.)", "$1");
				for(Instruction instruct : Instruction.values())
				{
					char operator = instruct.getMne();
					if(line.indexOf(operator)>=0){
						String[] lblcmd = line.split("\\"+operator, 2);
						lblinstr[proginstr] = instruct;
						lblcmds[proginstr] = lblcmd[1];
						if(!lblcmd[0].isEmpty())
							labels.put(lblcmd[0], proginstr-1);
						continue prolines;
					}
				}
				if(!line.isEmpty() && showWarnings)
					System.out.println("Warning: No valid instruction was found for \""+line+"\".");
				labels.put(line, proginstr-1);
			}
		output="";
		try{
			/*execute the parsed input*/
			for(proginstr=0; proginstr<lines.length;++proginstr)
			{
				Instruction instr = lblinstr[proginstr];
				if(instr!=null)
					instr.performAction(lblcmds[proginstr]);
			}
		}catch(Exception e){
		}
		output = output.replace(""+rvalchsub, ""+rvalch);
		String retval = output;
		output="";
		return retval;
	}
}
