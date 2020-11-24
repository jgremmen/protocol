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

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertTrue;


/**
 * @author Jeroen Gremmen
 */
@SuppressWarnings({ "ResultOfMethodCallIgnored", "ConstantConditions" })
public class TagDefBuilderTest
{
  @Test(expected = NullPointerException.class)
  public void testMatchNullCondition() {
    new StringProtocolFactory().createTag("tag").match(null, Level.Shared.INFO);
  }


  @Test(expected = NullPointerException.class)
  public void testMatchNullLevel() {
    new StringProtocolFactory().createTag("tag").match(TagDef.MatchCondition.AT_LEAST, null);
  }


  @Test
  public void testImplies()
  {
    StringProtocolFactory factory = new StringProtocolFactory();
    TagDef tagDefC = factory.createTag("C").getTagDef();
    TagDef tagDefB = factory.createTag("B").getTagDef();
    TagDef tagDefA = factory.createTag("A").implies("B", "C").getTagDef();

    Set<TagDef> impliedTagDefs = tagDefA.getImpliedTags();

    assertTrue(impliedTagDefs.contains(tagDefB));
    assertTrue(impliedTagDefs.contains(tagDefC));
  }


  @SuppressWarnings("squid:S2699")
  @Test
  public void testFactoryDelegate()
  {
    StringProtocolFactory factory = new StringProtocolFactory();
    ProtocolFactory.TagBuilder<String> tag = factory.createTag("tag");

    tag.createProtocol();
    tag.createTag("tag2");
    tag.getDefaultParameterValues();
    tag.getDefaultTag();
    tag.getTagByName("tag");
    tag.getTagDefs();
    tag.modifyTag("tag");
    tag.hasTag("xyz");
    tag.getMessageProcessor().processMessage("Hello");
  }


  @Test(expected = NullPointerException.class)
  public void testTagMatches()
  {
    StringProtocolFactory factory = new StringProtocolFactory();
    factory.createTag("tag")
        .match(TagDef.MatchCondition.EQUAL, Level.Shared.INFO)
        .getTagDef()
        .matches(null);
  }


  @Test
  public void testTagToString()
  {
    StringProtocolFactory factory = new StringProtocolFactory();

    assertTrue(factory
        .createTag("T1")
        .match(TagDef.MatchCondition.EQUAL, Level.Shared.INFO)
        .getTagDef()
        .toString()
        .contains("INFO"));

    assertTrue(factory
        .createTag("T2")
        .match(TagDef.MatchCondition.AT_LEAST, Level.Shared.INFO)
        .getTagDef()
        .toString()
        .contains("(>=)"));

    assertTrue(factory
        .createTag("T3")
        .match(TagDef.MatchCondition.NOT_EQUAL, Level.Shared.INFO)
        .getTagDef()
        .toString()
        .contains("(!=)"));

    assertTrue(factory
        .createTag("T4")
        .match(TagDef.MatchCondition.UNTIL, Level.Shared.INFO)
        .getTagDef()
        .toString()
        .contains("(<=)"));

    assertTrue(factory
        .createTag("T5")
        .implies("T4", "T3", "T2", "T1")
        .getTagDef()
        .toString()
        .contains("{T1,T2,T3,T4}"));
  }
}
