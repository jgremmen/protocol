package de.sayayi.lib.protocol;

import de.sayayi.lib.protocol.spi.ProtocolFactoryImpl;
import org.junit.Test;

import static de.sayayi.lib.protocol.Level.Shared.DEBUG;
import static de.sayayi.lib.protocol.Level.Shared.INFO;
import static de.sayayi.lib.protocol.Tag.LevelMatch.AT_LEAST;


public class ProtocolTest
{
  @Test
  public void testBasics()
  {
    ProtocolFactory factory = new ProtocolFactoryImpl();

    factory.createTag("ui").match(AT_LEAST, INFO)
           .createTag("technical").dependsOn("ui").implies("system");

    Tag ui = factory.getTagByName("ui");

    Protocol protocol = factory.createProtocol();


    protocol.add(DEBUG).message("Just sayin'")
            .debug().forTags(ui).message("MSG-048");


    ProtocolGroup gp = protocol.createGroup().setGroupMessage("Huhu");

    gp.debug().message("MSG-104").with("test", true)
        .setGroupMessage("GRP-771").with("idx", 45);

  }
}
