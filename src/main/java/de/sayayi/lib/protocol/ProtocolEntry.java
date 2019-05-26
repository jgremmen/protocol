package de.sayayi.lib.protocol;

import java.util.Locale;


public interface ProtocolEntry extends ProtocolQuery
{
  interface Message extends ProtocolEntry
  {
    String format(Locale locale);
  }


  interface Group extends ProtocolEntry
  {
    String formatGroupMessage(Locale locale);
  }
}
