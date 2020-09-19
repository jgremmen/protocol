package de.sayayi.lib.protocol;

import org.junit.Test;

import java.util.Collections;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * @author Jeroen Gremmen
 */
public class TagTest
{
  @Test
  public void testOf()
  {
    TagSelector selector = Tag.of("system");

    assertTrue(selector.match(asList("system")));
    assertTrue(selector.match(asList("default", "system", "test")));
    assertFalse(selector.match(asList("default")));
    assertFalse(selector.match(Collections.<String>emptyList()));
  }


  @Test
  public void testAllOf()
  {
    TagSelector selector = Tag.allOf("system", "default", "system");

    assertFalse(selector.match(asList("system")));
    assertTrue(selector.match(asList("default", "system", "test")));
    assertFalse(selector.match(asList("default")));
  }


  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test(expected = IllegalArgumentException.class)
  public void testAllOfFail() {
    Tag.allOf();
  }


  @Test
  public void testAny()
  {
    TagSelector selector = Tag.any();

    assertTrue(selector.match(asList("system")));
    assertTrue(selector.match(asList("default", "system", "test")));
    assertFalse(selector.match(Collections.<String>emptyList()));
  }


  @Test
  public void testAnyOf1()
  {
    TagSelector selector = Tag.anyOf("system");

    assertTrue(selector.match(asList("system")));
    assertTrue(selector.match(asList("system", "test")));
    assertFalse(selector.match(asList("default")));
  }


  @Test
  public void testAnyOf2()
  {
    TagSelector selector = Tag.anyOf("test", "default");

    assertFalse(selector.match(asList("system")));
    assertTrue(selector.match(asList("system", "test")));
    assertTrue(selector.match(asList("default")));
  }


  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test(expected = IllegalArgumentException.class)
  public void testAnyOfFail() {
    Tag.anyOf();
  }


  @Test
  public void testNot1()
  {
    TagSelector selector = Tag.not("system");

    assertFalse(selector.match(asList("system")));
    assertFalse(selector.match(asList("default", "system", "test")));
    assertTrue(selector.match(asList("default")));
    assertTrue(selector.match(Collections.<String>emptyList()));
  }


  @Test
  public void testNoneOf()
  {
    TagSelector selector = Tag.noneOf("system", "test");

    assertFalse(selector.match(asList("system")));
    assertFalse(selector.match(asList("default", "system", "test")));
    assertTrue(selector.match(asList("default")));
    assertTrue(selector.match(Collections.<String>emptyList()));
  }


  @Test
  public void testOrFlatten()
  {
    TagSelector selector = Tag.of("system").or("default")
        .or(Tag.anyOf("console", "system"));

    assertTrue(selector.match(asList("system")));
    assertTrue(selector.match(asList("default", "system", "test")));
    assertTrue(selector.match(asList("console")));
    assertFalse(selector.match(Collections.<String>emptyList()));
  }


  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test(expected = IllegalArgumentException.class)
  public void testOrFail() {
    Tag.or();
  }


  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test(expected = IllegalArgumentException.class)
  public void testAndFail() {
    Tag.and();
  }


  @Test
  public void testComplex1()
  {
    TagSelector selector = Tag.of("system").and("test").or("console");

    assertFalse(selector.match(asList("system")));
    assertTrue(selector.match(asList("default", "system", "test")));
    assertTrue(selector.match(asList("console")));
    assertFalse(selector.match(Collections.<String>emptyList()));
  }


  @Test
  public void testComplex2()
  {
    TagSelector selector = Tag.of("system").and(Tag.not("test"));

    assertTrue(selector.match(asList("system")));
    assertFalse(selector.match(asList("default", "system", "test")));
    assertFalse(selector.match(asList("console")));
    assertFalse(selector.match(Collections.<String>emptyList()));
  }
}
