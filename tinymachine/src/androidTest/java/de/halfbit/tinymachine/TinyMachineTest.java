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

import android.test.AndroidTestCase;

import de.halfbit.tinymachine.StateHandler.Type;

public class TinyMachineTest extends AndroidTestCase {

    private static final int STATE_INITIAL = 0;
    private static final int STATE_INTERMEDIATE = 1;
    private static final int STATE_FINAL = 2;

    private class TinyHandler extends Callbacks {

        //-- STATE_INITIAL

        @StateHandler(state = STATE_INITIAL, type = Type.OnEntry)
        public void onInitialEnter(TinyMachine tm) {
            onCallback(new OnEntry(tm.getCurrentState()));
        }
        @StateHandler(state = STATE_INITIAL)
        public void onInitialStringEvent(String event) {
            onCallback(event);
        }

        @StateHandler(state = STATE_INITIAL, type = Type.OnExit)
        public void onInitialExit(TinyMachine tm) {
            onCallback(new OnExit(tm.getCurrentState()));
        }

        //-- STATE_INTERMEDIATE

        @StateHandler(state = STATE_INTERMEDIATE, type = Type.OnExit)
        public void onIntermediateExit(TinyMachine tm) {
            onCallback(new OnExit(tm.getCurrentState()));
        }

        //-- STATE_FINAL

        @StateHandler(state = STATE_FINAL, type = Type.OnEntry)
        public void onFinalEnter(TinyMachine tm) {
            onCallback(new OnEntry(tm.getCurrentState()));
        }
        @StateHandler(state = STATE_FINAL)
        public void onFinalStringEvent(String event, TinyMachine tm) {
            onCallback(event);
        }

        @StateHandler(state = STATE_FINAL, type = Type.OnExit)
        public void onFinalExit(TinyMachine tm) {
            onCallback(new OnExit(tm.getCurrentState()));
        }

        //-- STATE_ANY

        @StateHandler(state = StateHandler.STATE_ANY, type = Type.OnEntry)
        public void onEntryAny(TinyMachine tm) {
            onCallback(new OnEntry(StateHandler.STATE_ANY));
        }

        @StateHandler(state = StateHandler.STATE_ANY)
        public void onEventAny(Integer event) {
            onCallback(event);
        }

        @StateHandler(state = StateHandler.STATE_ANY, type = Type.OnExit)
        public void onExitAny(TinyMachine tm) {
            onCallback(new OnExit(StateHandler.STATE_ANY));
        }

    }

    private TinyMachine mTinyMachine;
    private TinyHandler mTinyHandler;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
        mTinyHandler = new TinyHandler();
        mTinyMachine = new TinyMachine(mTinyHandler, STATE_INITIAL);
        mTinyMachine.setTraceTag("tinymachine-test");
	}

	@Override
	protected void tearDown() throws Exception {
        mTinyMachine = null;
        mTinyHandler = null;
		super.tearDown();
	}

    public void testAnyStateHandler() {
        mTinyMachine.fireEvent("event1");
        mTinyMachine.fireEvent(new Integer(20));
        mTinyMachine.transitionTo(STATE_FINAL);
        mTinyMachine.fireEvent(new Integer(25));

        mTinyHandler.assertEqualEvents(
                "event1",
                new Integer(20),
                new OnExit(StateHandler.STATE_ANY),
                new OnExit(STATE_INITIAL),
                new OnEntry(StateHandler.STATE_ANY),
                new OnEntry(STATE_FINAL),
                new Integer(25)
        );
    }

	public void testAllHandlersPresent() {
        mTinyMachine.fireEvent("event1");
        mTinyMachine.fireEvent("event2");
        mTinyMachine.transitionTo(STATE_FINAL);
        mTinyMachine.fireEvent("event3");
        mTinyMachine.transitionTo(STATE_INITIAL);

        mTinyHandler.assertEqualEvents(
                "event1",
                "event2",
                new OnExit(StateHandler.STATE_ANY),
                new OnExit(STATE_INITIAL),
                new OnEntry(StateHandler.STATE_ANY),
                new OnEntry(STATE_FINAL),
                "event3",
                new OnExit(StateHandler.STATE_ANY),
                new OnExit(STATE_FINAL),
                new OnEntry(StateHandler.STATE_ANY),
                new OnEntry(STATE_INITIAL)
        );
	}

	public void testSomeHandlersMissing() {
        mTinyMachine.fireEvent("event1");
        mTinyMachine.transitionTo(STATE_INTERMEDIATE);
        mTinyMachine.fireEvent("event2");
        mTinyMachine.fireEvent("event3");
        mTinyMachine.transitionTo(STATE_FINAL);
        mTinyMachine.fireEvent("event4");

        mTinyHandler.assertEqualEvents(
                "event1",
                new OnExit(StateHandler.STATE_ANY),
                new OnExit(STATE_INITIAL),
                new OnEntry(StateHandler.STATE_ANY),
                new OnExit(StateHandler.STATE_ANY),
                new OnExit(STATE_INTERMEDIATE),
                new OnEntry(StateHandler.STATE_ANY),
                new OnEntry(STATE_FINAL),
                "event4"
        );
	}

    public void testOnDuplicateEnterHandler() {
        try {
            TinyMachine tinyMachine = new TinyMachine(new Object() {
                @StateHandler(state = 0, type = Type.OnEntry) public void onEvent1() { }
                @StateHandler(state = 0, type = Type.OnEntry) public void onEvent2() { }
            }, 0);
            fail("IllegalArgumentException is expected");
        } catch (IllegalArgumentException e) {
            // OK
        }
    }

    public void testOnDuplicateExitHandler() {
        try {
            TinyMachine tinyMachine = new TinyMachine(new Object() {
                @StateHandler(state = 0, type = Type.OnExit) public void onEvent1() { }
                @StateHandler(state = 0, type = Type.OnExit) public void onEvent2() { }
            }, 0);
            fail("IllegalArgumentException is expected");
        } catch (IllegalArgumentException e) {
            // OK
        }
    }

    public void testOnDuplicateEventHandler() {
        try {
            TinyMachine tinyMachine = new TinyMachine(new Object() {
                @StateHandler(state = 0, type = Type.OnEvent)
                public void onEvent1(String event) { }

                @StateHandler(state = 0, type = Type.OnEvent)
                public void onEvent2(String event) { }
            }, 0);
            fail("IllegalArgumentException is expected");
        } catch (IllegalArgumentException e) {
            // OK
        }
    }

    public void testNullEvent() {
        try {
            mTinyMachine.fireEvent(null);
            fail("IllegalArgumentException is expected");
        } catch (IllegalArgumentException e) {
            // OK
        }
    }
}
