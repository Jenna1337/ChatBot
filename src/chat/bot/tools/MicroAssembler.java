package chat.bot.tools;

import java.util.*;
import java.util.regex.*;

public final class MicroAssembler{
	private MicroAssembler(){}
	/**The registers.*/
	private static int[] registers = new int[26];
	
	/**The index of the instruction to be executed next*/
	private static volatile int proginstr;
	/**The user-defined labels and the value of their associated line.*/
	private static Map<String, Integer> labels = new HashMap<String, Integer>();
	private static volatile byte compval=0;
	
	/**Greater than; comparison mask*/
	private static final byte GRT=0b100;
	/**Less than; comparison mask*/
	private static final byte LSS=1;
	/**Equal to; comparison mask*/
	private static final byte EQU=0b10;
	/**No operation; comparison mask*/
	private static final byte NOP=0;
	
	//Reserved: ;=+-*/?&|~<>#"$
	/**The character to separate lines*/
	private static final char delimiter=';';
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
	/**The character to output data*/
	private static final char op_out='\"';
	/**The character to output the value of a register*/
	private static final char rvalch = '$';
	
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
		OUT(op_out,ParType.NotDelim){
			void action(Object[] args){
				String txt = (String)args[0];
				if(txt.matches(contregx)){
					Matcher m = rpat.matcher(txt);
					while(txt.matches(contregx)){
						m.find();
						char ch = m.group(1).charAt(0);
						txt = txt.replaceAll("\\"+rvalch+ch, ""+registers[correct(ch)]);
					}
				}
				/*
        for(char ch='a';ch<='z';++ch)
          txt=txt.replaceAll("$"+ch, ""+registers[correct(ch)]);
				 */
				output+=txt;
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
					compval=GRT;
				else if(val1<val2)
					compval=LSS;
				else if(val1==val2)
					compval=EQU;
				else
					compval=NOP;
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
		/**The bitshift left without carry instruction*/
		LFT(op_lft,ParType.AlfaAlfanum){
			void action(Object[] args){
				registers[correct((Character)args[0])] <<= (Integer)args[1];
			}
		},
		/**The bitshift right without carry instruction*/
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
					if((compval&condition)>0 || condition==(GRT|EQU|LSS))
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
			NotDelim,
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
					if(args.matches(rRegx+"\\d+"))
						return new Object[]{args.charAt(0), Integer.parseInt(args.substring(1))};
					break;
				case NumWord:
					if(args.matches("\\d\\w+") || (args.charAt(0)=='0'))
						return new Object[]{Integer.parseInt(""+args.charAt(0)), args.substring(1)};
				case NotDelim:
					if(args.matches("[^"+delimiter+"]*"))
						return new Object[]{args};
					break;
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
	/**Runs the code specified in <code>input</code> and returns it's output.<br/>
	 * Format: [label]opcode(parameters)
	 * @param input The code to assemble and execute.
	 * @return The output of the assembled code.
	 */
	public static synchronized String assemble(String input){
		String[] lines=input.split(""+delimiter);
		Instruction[] lblinstr = new Instruction[lines.length];
		String[] lblcmds=new String[lines.length];
		String line;
		labels.clear();
		/*parse the input*/
		prolines:
			for(proginstr=0; proginstr<lines.length;++proginstr){
				line=lines[proginstr];
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
				if(!line.isEmpty())
					labels.put(line, proginstr-1);
				System.out.println("Warning: No valid instruction was found for \""+line+"\".");
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
		String retval = output;
		output="";
		return retval;
	}
}
