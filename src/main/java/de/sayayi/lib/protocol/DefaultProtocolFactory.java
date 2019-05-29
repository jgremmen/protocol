package de.sayayi.lib.protocol;

import de.sayayi.lib.protocol.spi.AbstractProtocolFactory;


public class DefaultProtocolFactory extends AbstractProtocolFactory<String>
{
  @Override
  public String processMessage(String message) {
    return message;
  }
}
