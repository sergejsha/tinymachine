
TinyMachine is fun to use [finit-state machine][1]. It helps you to write compact and easy to read code when used with event bus based application design. No deep if-else or switch-case statements, no static state transition tables, no complex, hard to understand models. Just clearly structured state handling and transition logic.

# TinyMachine is
 - extra small (~ 5K jar)
 - easy to use
 - well tested
 - annotation based (no requiremens on method names, no interfaces to implement)
 
# TinyMachine API in a nutshell
 - `@StateHandler(state=STATE_A, type=Type.OnEntry)` annotates handler methods receiving `OnEntry` event in `STATE_A`.
 - `@StateHandler(state=STATE_A, type=Type.OnExit)` annotates handler methods receiving `OnExit` event in `STATE_A`.
 - `@StateHandler(state=STATE_A, type=Type.OnEvent)` annotates handler methods receiving custom events in `STATE_A`.
 - `TinyMachine.fireEvent(Object event)` forwards given event to the corresponding handler method.
 - `TinyMachine.transitionTo(int state)` transtions the state machine into a new state.

# Usage example
```java
public class Example {

    // Define states
    private static final int STATE_A = 0;
    private static final int STATE_B = 1;

    // Define state handler class with handler methods
    public static class TinyHandler {

        // Handlers for STATE_A

        @StateHandler(state=STATE_A, type=Type.OnEntry)
        public void onEntryStateA() {
            // This method is called when machine enters STATE_A
        }

        @StateHandler(state=STATE_A, type=Type.OnExit)
        public void onExitStateA() {
            // This method is called when machine exits STATE_A
        }
      
        @StateHandler(state=STATE_A)
        public void onEventStateA(String event, TinyMachine tm) {
            // It's called when an event of type String is fired while machine is in STATE_A
        
            // As an example, let's transition into STATE_B when event "DONE" is received
            if ("DONE".equals(event)) {
              tm.transitionTo(STATE_B);
            }
        }
      
        // Handlers for STATE_B
      
        @StateHandler(state=STATE_B, type=Type.OnEntry)
        public void onEntryStateB() {
            // This method is called when machine enters STATE_B
        }

        // etc ...
    }
    
    private TinyMachine mTinyMachine;
    
    public Example() {
        // Create state machine with TinyHandler and put it into initial STATE_A state
        mTinyMachine = new TinyMachine(new TinyHandler(), STATE_A);
    }
    
    // Now, when we receive events we just need to forward them to TinyMachine. TinyMachine 
    // is responsible to deliver them to the right handler depending on the current state.
    public void onEvent(String event) {
        mTinyMachine.fireEvent(event);
    }
    
    // Here we can forward more events in the same manner.
}
```

Gradle dependencies
=======
```
dependencies {
    compile 'de.halfbit:tinymachine:1.0.+'
}
```

# ProGuard configuration

If you use Gradle build, then you don't need to configure anything, because it will use proper configuration already delivered with Android library archive. Otherwise you can use the configuration below:
```
-keepclassmembers, allowobfuscation class ** {
    @de.halfbit.tinymachine.StateHandler public *;
}
```

Used in
=======
 - [Settings Extended][2]

License
=======

    Copyright (c) 2015 Sergej Shafarenka, halfbit.de

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

  [1]: http://en.wikipedia.org/wiki/Finite-state_machine
  [2]: https://play.google.com/store/apps/details?id=com.hb.settings
