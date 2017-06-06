package chat;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public abstract class JsonObject<T> implements Comparable<T>
{
	/**
	 * Debugging method.
	 * @return A string containing all the instance variable names and their corresponding values.
	 */
	protected String varDumpAll(){
		String result="ChatEvent[";
		java.lang.reflect.Field[] fields = this.getClass().getDeclaredFields();
		for(int i=0;i<fields.length-1;++i)
			if(!Modifier.isStatic(fields[i].getModifiers()))
				result+=varDump(fields[i])+",";
		result+=varDump(fields[fields.length-1])+"]";
		return result;
	}
	/**
	 * Debugging method.
	 * @return A string containing the variable's name and its corresponding values.
	 */
	protected String varDump(Field f){
		String result="\""+f.getName()+"\":\"";
		try
		{
			f.setAccessible(true);
			Object o = f.get(this);
			return result+o+"\"";
		}
		catch(IllegalArgumentException | IllegalAccessException
				| SecurityException e)
		{
			// Something horrible happened!
			System.err.println("Failed to get value of variable "+f.getName()+
					" of object at "+Integer.toHexString(System.identityHashCode(this)));
			e.printStackTrace();
			return result+"null\"";
		}
	}
}
