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

public class StacklessProcessingTest extends AndroidTestCase {

    private static final int STATE_UNDEFINED = -1;
    private static final int STATE_INITIAL = 0;
    private static final int STATE_FINAL = 1;

    private class TinyHandler extends Callbacks {

        @StateHandler(state = STATE_INITIAL, type = Type.OnEntry)
        public void onInitialEntry(TinyMachine tm) {
            onCallback(new OnEntry(tm.getCurrentState()));
        }

        @StateHandler(state = STATE_INITIAL, type = Type.OnExit)
        public void onInitialExit(TinyMachine tm) {
            onCallback(new OnExit(tm.getCurrentState()));
        }

        @StateHandler(state = STATE_FINAL, type = Type.OnEntry)
        public void onFinalEntry(TinyMachine tm) {
            onCallback(new OnEntry(tm.getCurrentState()));
        }

        @StateHandler(state = STATE_FINAL)
        public void onFinalString(String event, TinyMachine tm) {
            onCallback(event);
            tm.transitionTo(STATE_INITIAL);
        }

        @StateHandler(state = STATE_FINAL, type = Type.OnExit)
        public void onFinalExit(TinyMachine tm) {
            onCallback(new OnExit(tm.getCurrentState()));
        }

        // any handler transitions into final state immediately

        @StateHandler(state = StateHandler.STATE_ANY, type = Type.OnEntry)
        public void onAnyEnter(TinyMachine tm) {
            onCallback(new OnEntry(StateHandler.STATE_ANY));
            tm.transitionTo(STATE_FINAL);
        }

    }

    private TinyMachine mTinyMachine;
    private TinyHandler mTinyHandler;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
        mTinyHandler = new TinyHandler();
        mTinyMachine = new TinyMachine(mTinyHandler, STATE_UNDEFINED);
        mTinyMachine.setTraceTag("tinymachine-test");
	}

	@Override
	protected void tearDown() throws Exception {
        mTinyMachine = null;
        mTinyHandler = null;
		super.tearDown();
	}

    public void testStacklessProcessing() {
        mTinyMachine.transitionTo(STATE_INITIAL);

        mTinyHandler.assertEqualEvents(
                new OnEntry(StateHandler.STATE_ANY),
                new OnEntry(STATE_INITIAL),

                // !!! Exit must be reported before entry into the new state
                new OnExit(STATE_INITIAL),

                new OnEntry(StateHandler.STATE_ANY),
                new OnEntry(STATE_FINAL)
        );
    }

    public void testChainedTransitions() {

        // UNDEFINED -> INITIAL -> FINAL -> event -> INITIAL -> FINAL

        mTinyMachine.transitionTo(STATE_INITIAL);
        mTinyMachine.fireEvent("event1");

        mTinyHandler.assertEqualEvents(
                new OnEntry(StateHandler.STATE_ANY),
                new OnEntry(STATE_INITIAL),
                new OnExit(STATE_INITIAL),
                new OnEntry(StateHandler.STATE_ANY),
                new OnEntry(STATE_FINAL),
                "event1",
                new OnExit(STATE_FINAL),
                new OnEntry(StateHandler.STATE_ANY),
                new OnEntry(STATE_INITIAL),
                new OnExit(STATE_INITIAL),
                new OnEntry(StateHandler.STATE_ANY),
                new OnEntry(STATE_FINAL)
        );


    }

}
