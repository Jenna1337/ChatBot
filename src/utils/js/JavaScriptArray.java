package utils.js;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public final class JavaScriptArray
{
	private final String content;

	public JavaScriptArray(String content){
		this.content=content;
	}
	
	
	private static final ScriptEngine jsengine = new ScriptEngineManager().getEngineByName("js");
	
	public <T> T to(Class<T> type) throws ScriptException{
		//if(!type.isArray())
		//	throw new IllegalArgumentException("Type "+type+" is not an array.");
		return type.cast(jsengine.eval("Java.to("+this.content+",'"+type.getCanonicalName()+"')"));
	}
/*
	public Stream<T> filter(Predicate<? super T> predicate){
		// TODO Auto-generated method stub
		return null;
	}

	public <R> Stream<R> map(Function<? super T, ? extends R> mapper){
		// TODO Auto-generated method stub
		return null;
	}
*/
	public static JavaScriptArray fromResult(String jsf){
		return new JavaScriptArray("(function(){"+jsf+"})()");
	}
}
