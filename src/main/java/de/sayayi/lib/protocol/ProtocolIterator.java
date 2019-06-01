package de.sayayi.lib.protocol;

import de.sayayi.lib.protocol.ProtocolEntry.Group;
import de.sayayi.lib.protocol.ProtocolEntry.Message;
import de.sayayi.lib.protocol.ProtocolIterator.DepthEntry;

import java.util.Iterator;


public interface ProtocolIterator<M> extends Iterator<DepthEntry>
{
  Level getLevel();

  Tag getTag();


  interface DepthEntry
  {
    /**
     * <p>
     *   Returns the depth for this entry.
     * </p>
     * <p>
     *
     * </p>
     *
     * @return  entry depth
     */
    int getDepth();

    boolean isFirst();

    boolean isLast();
  }


  interface MessageEntry<M> extends DepthEntry, Message<M> {
  }


  interface GroupEntry<M> extends DepthEntry, Group<M>
  {
    boolean hasEntryAfterGroup();
  }
}
