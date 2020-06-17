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
public class TagBuilderTest
{
  @Test(expected = NullPointerException.class)
  public void testMatchNullCondition() {
    new GenericProtocolFactory().createTag("tag").match(null, Level.Shared.INFO);
  }


  @Test(expected = NullPointerException.class)
  public void testMatchNullLevel() {
    new GenericProtocolFactory().createTag("tag").match(Tag.MatchCondition.AT_LEAST, null);
  }


  @Test
  public void testImplies()
  {
    GenericProtocolFactory factory = new GenericProtocolFactory();
    Tag tagC = factory.createTag("C").getTag();
    Tag tagB = factory.createTag("B").getTag();
    Tag tagA = factory.createTag("A").implies("B", "C").getTag();

    Set<Tag> impliedTags = tagA.getImpliedTags();

    assertTrue(impliedTags.contains(tagB));
    assertTrue(impliedTags.contains(tagC));
  }


  @SuppressWarnings("squid:S2699")
  @Test
  public void testFactoryDelegate()
  {
    GenericProtocolFactory factory = new GenericProtocolFactory();
    ProtocolFactory.TagBuilder<String> tag = factory.createTag("tag");

    tag.createProtocol();
    tag.createTag("tag2");
    tag.getDefaultParameterValues();
    tag.getDefaultTag();
    tag.getTagByName("tag");
    tag.getTags();
    tag.modifyTag("tag");
    tag.hasTag("xyz");
    tag.isRegisteredTag(tag.getTag());
    tag.processMessage("Hello");
  }


  @Test(expected = NullPointerException.class)
  public void testTagMatches()
  {
    GenericProtocolFactory factory = new GenericProtocolFactory();
    factory.createTag("tag")
        .match(Tag.MatchCondition.EQUAL, Level.Shared.INFO)
        .getTag()
        .matches(null);
  }


  @Test
  public void testTagToString()
  {
    GenericProtocolFactory factory = new GenericProtocolFactory();

    assertTrue(factory
        .createTag("T1")
        .match(Tag.MatchCondition.EQUAL, Level.Shared.INFO)
        .getTag()
        .toString()
        .contains("INFO"));

    assertTrue(factory
        .createTag("T2")
        .match(Tag.MatchCondition.AT_LEAST, Level.Shared.INFO)
        .getTag()
        .toString()
        .contains("(>=)"));

    assertTrue(factory
        .createTag("T3")
        .match(Tag.MatchCondition.NOT_EQUAL, Level.Shared.INFO)
        .getTag()
        .toString()
        .contains("(!=)"));

    assertTrue(factory
        .createTag("T4")
        .match(Tag.MatchCondition.UNTIL, Level.Shared.INFO)
        .getTag()
        .toString()
        .contains("(<=)"));

    assertTrue(factory
        .createTag("T5")
        .implies("T4", "T3", "T2", "T1")
        .getTag()
        .toString()
        .contains("{T1,T2,T3,T4}"));
  }
}
