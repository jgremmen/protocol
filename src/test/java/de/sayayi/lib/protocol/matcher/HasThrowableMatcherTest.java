/*
 * Copyright 2021 Jeroen Gremmen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.sayayi.lib.protocol.matcher;

import de.sayayi.lib.protocol.Protocol;
import org.junit.jupiter.api.Test;

import lombok.val;

import static de.sayayi.lib.protocol.Level.Shared.HIGHEST;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * @author Jeroen Gremmen
 */
public class HasThrowableMatcherTest
{
  @Test
  public void testMatchesDefault()
  {
    //noinspection unchecked
    val message = (Protocol.Message<Object>)mock(Protocol.Message.class);
    when(message.getThrowable()).thenReturn(new NullPointerException());

    assertTrue(MessageMatchers.hasThrowable().matches(HIGHEST, message));

    when(message.getThrowable()).thenReturn(null);

    assertFalse(MessageMatchers.hasThrowable().matches(HIGHEST, message));
  }


  @Test
  public void testMatchesTyped()
  {
    //noinspection unchecked
    val message = (Protocol.Message<Object>)mock(Protocol.Message.class);
    when(message.getThrowable()).thenReturn(new NullPointerException());

    assertTrue(MessageMatchers.hasThrowable(RuntimeException.class).matches(HIGHEST, message));
    assertTrue(MessageMatchers.hasThrowable(NullPointerException.class).matches(HIGHEST, message));

    when(message.getThrowable()).thenReturn(null);

    assertFalse(MessageMatchers.hasThrowable(RuntimeException.class).matches(HIGHEST, message));
  }
}
