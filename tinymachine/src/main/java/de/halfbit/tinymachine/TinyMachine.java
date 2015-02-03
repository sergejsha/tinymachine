package de.halfbit.tinymachine;

import java.lang.reflect.Method;
import java.util.HashMap;

import android.util.Log;
import android.util.SparseArray;
import de.halfbit.tinymachine.StateHandler.Type;

public class TinyMachine {

	private static class OnEnter {}
	private static class OnExit {}
	
	private final Object mHandler;
	private final SparseArray<HashMap<Class<?>, Method>> mCallbacks;
	
	private String mTraceTag;
	private int mCurrentState;
	
	public TinyMachine(Object handler, int initialState) {
		mHandler = handler;
		mCurrentState = initialState;
		mCallbacks = new SparseArray<>();
		
		final Method[] methods = handler.getClass().getMethods();
		Class<?>[] params; 
		StateHandler ann; 
		Class<?> eventType;
		for (Method method : methods) {
			if (method.isBridge() || method.isSynthetic()) {
				continue;
			}
			ann = method.getAnnotation(StateHandler.class);
			if (ann != null) {
				switch (ann.type()) {
					case Type.OnEnter: {
						eventType = OnEnter.class;
						break;
					}
					case Type.OnExit: {
						eventType = OnExit.class;
						break;
					}
					case Type.OnEvent: {
						params = method.getParameterTypes();
						if (params.length < 1) {
							throw new IllegalArgumentException(
									"Expect event parameter in @StateEventHandler method: " 
											+ method.getName());
						}
						eventType = params[0];
						break;
					}
					default: 
						throw new IllegalArgumentException(
								"Unsupported event type: " + ann.type());
				}
				HashMap<Class<?>, Method> callbacks = mCallbacks.get(ann.state());
				if (callbacks == null) {
					callbacks = new HashMap<>();
					mCallbacks.put(ann.state(), callbacks);
				}
				callbacks.put(eventType, method);
			}
		}	
	}
	
	//-- public api
	
	public void fireEvent(Object event) {
		if (event == null) {
			throw new IllegalArgumentException("Event must not be null.");
		}
		fire(event.getClass(), event);
	}
	
	public void gotoState(int state) {
		if (mCurrentState != state) {
			fire(OnExit.class, null);
			mCurrentState = state;
			if (mTraceTag != null) {
				Log.d(mTraceTag, "  [" + mCurrentState + "] - new state");
			}
			fire(OnEnter.class, null);
		}
	}
	
	public TinyMachine setTracesTag(String tag) {
		mTraceTag = tag;
		if (mTraceTag != null) {
			Log.d(mTraceTag, "  [" + mCurrentState + "] - current state");
		}
		return this;
	}
	
	//-- implementation
	
	private void fire(Class<?> handlerType, Object event) {
		Method method = null;
		HashMap<Class<?>, Method> callbacks = mCallbacks.get(mCurrentState);
		if (callbacks != null) {
			method = callbacks.get(handlerType);
		}
		if (method == null) {
			return; // simply ignore missing handler method for now
		}
		try {
			int paramsCount = method.getParameterTypes().length; 
			if (event == null) {
				if (mTraceTag != null) {
					if (handlerType == OnEnter.class) {
						Log.d(mTraceTag, "  [" + mCurrentState + "] onEnter");
					} else {
						Log.d(mTraceTag, "  [" + mCurrentState + "] onExit");
					}
				}
				switch(paramsCount) {
					case 0: method.invoke(mHandler); break; 
					case 1: method.invoke(mHandler, this); break; 
					default: throw new IllegalArgumentException(
							"@StateEventHandler method must have 0 or 1 parameters"); 
				}
			} else {
				if (mTraceTag != null) {
					Log.d(mTraceTag, "  [" + mCurrentState + "] onEvent, " + event);
				}
				switch(paramsCount) {
					case 1: method.invoke(mHandler, event); break; 
					case 2: method.invoke(mHandler, event, this); break; 
					default: throw new IllegalArgumentException(
							"@StateEventHandler method must have 1 or 2 parameters"); 
				}
			}
		} catch(Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new IllegalStateException("Exception in @StateEventHandler method. "
                        + "See stacktrace for more details", e);
			}
		}
	}

}
