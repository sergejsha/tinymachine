/*
 * Copyright (C) 2015 Sergej Shafarenka, halfbit.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.halfbit.tinymachine;

import java.lang.reflect.Method;
import java.util.HashMap;

import android.util.Log;
import android.util.SparseArray;

import de.halfbit.tinymachine.StateHandler.Type;

/**
 * Finite-state machine implementation class.
 * <p/>
 * <p/>
 * Once created <code>TinyMachine</code> is capable to accept events
 * and deliver them to a handler instance given in constructor. Handler
 * instance defines public methods with {@link de.halfbit.tinymachine.StateHandler}
 * annotation. Every method is intended to handler certain events in certain
 * state. You can have as many handler methods as you need.
 * <p/>
 * <p/>
 * When you receive an event, you just need to forward it to a <code>TinyMachine</code>
 * instance and it will deliver it to the right handler method. Simply call
 * {@link #fireEvent(Object)} for this. If there is no handler method for given event
 * in given state, <code>TinyMachine</code> will silently ignore it.
 * <p/>
 * <p/>
 * There is no static configuration for transitions between states. You need to program
 * is inside your handler methods. Call {@link #transitionTo(int)} methods to move your
 * state machine into a new state. <code>TinyMachine</code> will automatically fire
 * <code>Type.OnExit</code> event for the current state and <code>Type.OnEntry</code> event
 * for the new state. If there are handlers for this events, then they will be called.
 *
 * @author Sergej Shafarenka
 */
public class TinyMachine {

    private static class OnEntry {
    }

    private static class OnExit {
    }

    private final Object mHandler;
    private final SparseArray<HashMap<Class<?>, Method>> mCallbacks;

    private String mTraceTag;
    private int mCurrentState;

    /**
     * Creates new instance of FSM machine and assigns handler class
     * with public methods handling state transitions and events. Handler
     * methods must have {@link de.halfbit.tinymachine.StateHandler}
     * annotation describing which event in which state it will handle.
     *
     * @param handler      instance with handler methods
     * @param initialState initial state to put state machine into.
     *                     <code>Type.OnEntry</code> event is not reported
     *                     for the initial state.
     */
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
                    case Type.OnEntry: {
                        eventType = OnEntry.class;
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
                if (callbacks.put(eventType, method) != null) {
                    throw new IllegalArgumentException("Duplicate handler methods not allowed" +
                            ", method: " + method.getName());
                }
            }
        }
    }

    //-- public api

    /**
     * Forwards an event into state machine. State machine will deliver the event to a
     * handler methods responsible for its processing. If there is no handler method found,
     * then event gets silently ignored and this call has no effect.
     *
     * @param event event to be delivered to a handler method
     */
    public void fireEvent(Object event) {
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null.");
        }
        fire(event.getClass(), event);
    }

    /**
     * Moves state machine in a new given state. If state machine is already in that state,
     * then this method has no effect. Otherwise, if exists, <code>Type.OnExit</code> event
     * handler for the current state is called first and then <code>Type.OnEntry</code> event
     * handler for new state is called.
     *
     * @param state new state to put state machine into
     */
    public void transitionTo(int state) {
        if (mCurrentState != state) {
            fire(OnExit.class, null);
            mCurrentState = state;
            if (mTraceTag != null) {
                log("new state", null);
            }
            fire(OnEntry.class, null);
        }
    }

    /**
     * Enables traces and sets tag to be used for <code>Log.d()</code> output.
     * <code>TinyMachine</code> will trace all processed events and state transitions including
     * events for which handlers in current state are missed.
     *
     * @param tag
     * @return
     */
    public TinyMachine setTraceTag(String tag) {
        mTraceTag = tag;
        if (mTraceTag != null) {
            log("current state", null);
        }
        return this;
    }

    /**
     * Returns current machine state.
     *
     * @return current machine state
     */
    public int getCurrentState() {
        return mCurrentState;
    }

    //-- implementation

    private void fire(Class<?> handlerType, Object event) {
        Method method = null;
        HashMap<Class<?>, Method> callbacks = mCallbacks.get(mCurrentState);
        if (callbacks != null) {
            method = callbacks.get(handlerType);
        }

        if (method == null) {
            if (mTraceTag != null) {
                // log missing handler method
                if (handlerType == OnEntry.class) {
                    log("OnEntry", "no handler method");
                } else if (handlerType == OnExit.class) {
                    log("OnExit", "no handler method");
                } else {
                    log("OnEvent", "no handler method, event=" + event);
                }
            }
            return; // no handler, exit
        }

        try {
            int paramsCount = method.getParameterTypes().length;
            if (event == null) {
                if (mTraceTag != null) {
                    if (handlerType == OnEntry.class) {
                        log("OnEntry", null);
                    } else {
                        log("OnExit", null);
                    }
                }
                switch (paramsCount) {
                    case 0:
                        method.invoke(mHandler);
                        break;
                    case 1:
                        method.invoke(mHandler, this);
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "@StateEventHandler method must have 0 or 1 parameters");
                }
            } else {
                if (mTraceTag != null) {
                    log("OnEvent", "OnEvent, event=" + event);
                }
                switch (paramsCount) {
                    case 1:
                        method.invoke(mHandler, event);
                        break;
                    case 2:
                        method.invoke(mHandler, event, this);
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "@StateEventHandler method must have 1 or 2 parameters");
                }
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new IllegalStateException("Exception in @StateEventHandler method. "
                        + "See stack trace for more details", e);
            }
        }
    }

    private void log(String eventType, String message) {
        Log.d(mTraceTag, "  [" + mCurrentState + "] "
                + eventType + (message == null ? "" : ", " + message));
    }
}
