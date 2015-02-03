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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Apply this annotation to public methods responsible for handling
 * certain events in certain states.
 * <p>
 *     There is three types of events:
 *     <li><code>OnEntry</code> gets called when machine enters given state</li>
 *     <li><code>OnExit</code> gets called when machine leaves given state</li>
 *     <li><code>OnEvent (default)</code> gets called when machine receives
 *          given event in given state</li>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface StateHandler {

    public static class Type {
        public static final int OnEntry = 0;
        public static final int OnEvent = 1;
        public static final int OnExit = 2;
    }

    /**
     * Defines in which state this handler must be active
     * @return  a state constant
     */
    int state();

    /**
     * Defines type of event this handler processes
     * @return  the type of event
     */
    int type() default Type.OnEvent;

}
