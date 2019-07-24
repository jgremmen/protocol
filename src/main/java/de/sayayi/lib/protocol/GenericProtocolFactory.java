/*
 * Copyright 2019 Jeroen Gremmen
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
package de.sayayi.lib.protocol;

import de.sayayi.lib.protocol.Protocol.MessageWithLevel;
import de.sayayi.lib.protocol.spi.AbstractProtocolFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * <p>
 *   Generic protocol factory for text messages where the messages are stored internally as {@code String} objects.
 * </p>
 *
 * @author Jeroen Gremmen
 */
public class GenericProtocolFactory extends AbstractProtocolFactory<String>
{
  private static final String TICKET_PREFIX =
      Long.toString(System.currentTimeMillis() / 1000, Character.MAX_RADIX).toUpperCase(Locale.ROOT);
  private static final AtomicInteger TICKET = new AtomicInteger(1);


  @Override
  public String processMessage(@NotNull String message) {
    return message;
  }


  @Override
  public @NotNull String createTicketFor(@NotNull MessageWithLevel<String> message) {
    return TICKET_PREFIX + '-' + String.format("%05d", TICKET.getAndIncrement());
  }
}
