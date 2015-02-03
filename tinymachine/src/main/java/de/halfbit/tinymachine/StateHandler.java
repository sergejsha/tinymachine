package de.halfbit.tinymachine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface StateHandler {

	public static class Type {
		public static final int OnEnter = 0;
		public static final int OnEvent = 1;
		public static final int OnExit = 2;
	}
	
	int state();
	int type() default Type.OnEvent;

}
