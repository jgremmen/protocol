/*
 * Copyright 2021 Jeroen Gremmen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.sayayi.lib.protocol.matcher;

import de.sayayi.lib.protocol.ProtocolEntry.Message;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import lombok.val;

import static de.sayayi.lib.protocol.Level.Shared.HIGHEST;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Jeroen Gremmen
 */
public class BooleanMatcherTest
{
  @Test
  public void testMatches()
  {
    //noinspection unchecked
    val message = (Message<Object>)Mockito.mock(Message.class);

    assertTrue(MessageMatchers.any().matches(HIGHEST, message));
    assertFalse(MessageMatchers.none().matches(HIGHEST, message));
  }
}