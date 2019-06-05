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

import de.sayayi.lib.protocol.ProtocolEntry.FormattableMessage;
import de.sayayi.lib.protocol.ProtocolEntry.Message;


/**
 * @author Jeroen Gremmen
 */
public interface ProtocolFormatter<M,R>
{
  void message(Message<M> message, boolean lastEntry);


  @SuppressWarnings("unused")
  void group(int group, FormattableMessage<M> groupMessage, boolean lastEntry, boolean hasGroupEntries);


  R getResult();


  interface InitializableProtocolFormatter<M,R> extends ProtocolFormatter<M,R>
  {
    void init(Level level, Tag tag);
  }
}
