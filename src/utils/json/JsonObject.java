package utils.json;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import utils.Utils;

public abstract class JsonObject<T> implements Comparable<T>
{
	/**
	 * Debugging method.
	 * @return A string containing all the instance variable names and their corresponding values.
	 */
	protected String varDumpAll(){
		String result = this.getClass().getSimpleName()+"[";
		Field[] fields = this.getClass().getDeclaredFields();
		result += String.join(",", 
					java.util.Arrays.stream(fields)
						.filter(field->
								!Modifier.isStatic(field.getModifiers())
						).map(this::varDump).toArray(String[]::new)
				);
		result += "]";
		return result;
	}
	/**
	 * Debugging method.
	 * @return A string containing the variable's name and its corresponding values.
	 */
	protected String varDump(Field f){
		String result="\""+f.getName()+"\":";
		try
		{
			f.setAccessible(true);
			Object o = f.get(this);
			return result + "\""+ Utils.escapeString(o.toString()) + "\"";
		}
		catch(NullPointerException npe){
			return result + "null";
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
