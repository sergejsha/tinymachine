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

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class Callbacks {

	public static interface EventIterator {
		void onEvent(Object event);
	}
	
	private final ArrayList<Object> mEvents = new ArrayList<Object>();
	
	protected synchronized void onCallback(Object event) {
		mEvents.add(event);
	}
	
	public synchronized void clearEvents() {
		mEvents.clear();
	}
	
	public synchronized int getEventsCount() {
		return mEvents.size();
	}
	
	public void iterate(EventIterator iterator) {
		for (Object event : mEvents) {
			iterator.onEvent(event);
		}
	}
	
	public void assertNullEvent() {
		Assert.assertEquals(1, mEvents.size());
		Assert.assertSame(null, mEvents.get(0));
	}
	
	public void assertSameEventsList(ArrayList<Object> expectedEvents) {
		Assert.assertEquals(expectedEvents.size(), mEvents.size());
		for(int i=0; i<expectedEvents.size(); i++) {
			Assert.assertSame(expectedEvents.get(0), mEvents.get(0));
		}
	}
	
	public void assertSameEvents(Object... expectedEvents) {
		Assert.assertEquals(expectedEvents.length, mEvents.size());
		for(int i=0; i<expectedEvents.length; i++) {
			Assert.assertSame(expectedEvents[i], mEvents.get(i));
		}
	}
	
	public void assertEqualEvents(Object... expectedEvents) {
		Assert.assertEquals(expectedEvents.length, mEvents.size());
		for(int i=0; i<expectedEvents.length; i++) {
			Assert.assertEquals(expectedEvents[i], mEvents.get(i));
		}
	}
	
	public void assertEventsAnyOrder(Object... expectedEvents) {
		Assert.assertEquals(expectedEvents.length, mEvents.size());
		ArrayList<Object> events = new ArrayList<Object>(Arrays.asList(expectedEvents));
		
		for(int i=0; i<expectedEvents.length; i++) {
			Assert.assertTrue("cannot find event: " + expectedEvents[i], events.remove(expectedEvents[i]));
		}
		Assert.assertEquals("unexpected events: " + events, 0, events.size());
	}
	
	public void assertNoEvents() {
		Assert.assertEquals(0, mEvents.size());
	}
	
}
