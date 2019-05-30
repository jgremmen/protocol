package de.sayayi.lib.protocol;

import java.util.List;


public interface ProtocolQuery<M>
{
  /**
   * Tells whether this protocol object matches the given {@code level} and {@code tag}.
   *
   * @param level  requested protocol level, not {@code null}
   * @param tag    tag to query, not {@code null}
   *
   * @return  {@code true} if the protocol object matches, {@code false} otherwise
   */
  boolean isMatch(Level level, Tag tag);


  /**
   * Returns a list of protocol entries provided by this protocol object for the given {@code level} and {@code tag}.
   *
   * @param level  requested protocol level, not {@code null}
   * @param tag    tag to query, not {@code null}
   *
   * @return  a list of protocol entries, never {@code null}
   */
  List<ProtocolEntry<M>> getEntries(Level level, Tag tag);
}
