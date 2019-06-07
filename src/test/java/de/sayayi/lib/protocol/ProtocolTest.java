package de.sayayi.lib.protocol;

import org.junit.Test;

import static de.sayayi.lib.protocol.Level.Shared.DEBUG;
import static de.sayayi.lib.protocol.Level.Shared.INFO;
import static de.sayayi.lib.protocol.Tag.MatchCondition.AT_LEAST;


/**
 * @author Jeroen Gremmen
 */
public class ProtocolTest
{
  @Test
  public void testBasics()
  {
    DefaultProtocolFactory factory = new DefaultProtocolFactory();

    factory.createTag("ui").match(AT_LEAST, INFO)
           .createTag("technical").dependsOn("ui").implies("system");

    Tag ui = factory.getTagByName("ui");

    Protocol protocol = factory.createProtocol();

    protocol.add(DEBUG).message("Just sayin'")
            .error(new NullPointerException()).forTags(ui).message("MSG-048");

    ProtocolGroup gp = protocol.createGroup().setGroupMessage("Huhu");

    gp.error().message("MSG-104").with("test", true)
      .setGroupMessage("GRP-771").with("idx", 45);
  }
}
