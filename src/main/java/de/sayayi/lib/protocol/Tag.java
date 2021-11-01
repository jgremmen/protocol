/*
 * Copyright 2020 Jeroen Gremmen
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

import de.sayayi.lib.protocol.TagSelector.Builder;
import de.sayayi.lib.protocol.TagSelector.SelectorReference;
import de.sayayi.lib.protocol.TagSelector.TagReference;
import de.sayayi.lib.protocol.exception.ProtocolException;
import de.sayayi.lib.protocol.selector.match.MatchAllOf;
import de.sayayi.lib.protocol.selector.match.MatchAnd;
import de.sayayi.lib.protocol.selector.match.MatchAny;
import de.sayayi.lib.protocol.selector.match.MatchAnyOf;
import de.sayayi.lib.protocol.selector.match.MatchFixResult;
import de.sayayi.lib.protocol.selector.match.MatchNot;
import de.sayayi.lib.protocol.selector.match.MatchOr;
import de.sayayi.lib.protocol.selector.parser.TagSelectorParser;

import lombok.NoArgsConstructor;
import lombok.val;
import lombok.var;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static de.sayayi.lib.protocol.TagSelector.MatchType.ALL_OF;
import static de.sayayi.lib.protocol.TagSelector.MatchType.AND;
import static de.sayayi.lib.protocol.TagSelector.MatchType.ANY;
import static de.sayayi.lib.protocol.TagSelector.MatchType.ANY_OF;
import static de.sayayi.lib.protocol.TagSelector.MatchType.FIX;
import static de.sayayi.lib.protocol.TagSelector.MatchType.NOT;
import static de.sayayi.lib.protocol.TagSelector.MatchType.OR;
import static de.sayayi.lib.protocol.selector.match.AbstractTagSelectorBuilder.wrap;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;
import static lombok.AccessLevel.PRIVATE;


/**
 * @author Jeroen Gremmen
 * @since 0.6.0
 */
@SuppressWarnings({"java:S100", "java:S1121"})
@NoArgsConstructor(access = PRIVATE)
public final class Tag
{
  private static final Comparator<TagSelector> CMP_TYPE = comparing(TagSelector::getType);
  private static final Comparator<TagSelector> CMP_NOT_FIRST = comparingInt(o -> (o.getType() == NOT ? 0 : 1));
  private static final Comparator<TagSelector> CMP_ALL_OF_FIRST = comparingInt(o -> (o.getType() == ALL_OF ? 0 : 1));
  private static final Comparator<TagSelector> CMP_ANY_OF_FIRST = comparingInt(o -> (isAnyOfMatcher(o) ? 0 : 1));


  @Contract(value = "_ -> new", pure = true)
  public static @NotNull Builder parse(@NotNull String selector) {
    return wrap(new TagSelectorParser(selector).parseSelector());
  }


  /**
   * Create a tag selector for the given {@code tagName}.
   *
   * @param tagName  tag name
   *
   * @return  tag selector for the given {@code tagName}. The returned object allows for constructing a more
   *          complex selector following the builder pattern.
   */
  @Contract(value = "_ -> new", pure = true)
  public static @NotNull Builder of(@NotNull String tagName) {
    return new MatchAllOf(tagName);
  }


  /**
   * Create a tag selector which matches any non empty set of tag names.
   *
   * @return  tag selector which matches any tag. The returned object allows for constructing a more
   *          complex selector following the builder pattern.
   */
  @Contract(value = "-> new", pure = true)
  public static @NotNull Builder any() {
    return new MatchAny();
  }


  /**
   * Create a tag selector which matches any of the given {@code tagNames}.
   *
   * @param tagNames  tag names
   *
   * @return  tag selector which matches any of the given {@code tagNames}. The returned object allows for
   *          constructing a more complex selector following the builder pattern.
   */
  @Contract(value = "_ -> new", pure = true)
  public static @NotNull Builder anyOf(@NotNull String... tagNames)
  {
    if (tagNames.length == 0)
      throw new IllegalArgumentException("tag name array must not be empty");

    val match = new MatchAnyOf(tagNames);
    tagNames = match.getTagNames();

    return tagNames.length == 1 ? new MatchAllOf(tagNames) : match;
  }


  /**
   * Create a tag selector which matches all of the given {@code tagNames}.
   *
   * @param tagNames  tag names
   *
   * @return  tag selector which matches all of the given {@code tagNames}. The returned object allows for
   *          constructing a more complex selector following the builder pattern.
   */
  @Contract(value = "_ -> new", pure = true)
  public static @NotNull Builder allOf(@NotNull String... tagNames)
  {
    if (tagNames.length == 0)
      throw new ProtocolException("tag name array must not be empty");

    return new MatchAllOf(tagNames);
  }


  /**
   * Create a tag selector which matches none of the given {@code tagNames}.
   *
   * @param tagNames  tag names
   *
   * @return  tag selector which matches none of the given {@code tagNames}. The returned object allows for
   *          constructing a more complex selector following the builder pattern.
   */
  @Contract(pure = true)
  public static @NotNull Builder noneOf(@NotNull String... tagNames) {
    return not(anyOf(tagNames));
  }


  /**
   * Create a tag selector which matches if the given {@code tagName} is not contained.
   *
   * @param tagName  tag name
   *
   * @return  tag selector which matches if the given {@code tagName} is not contained. The returned object allows
   *          for constructing a more complex selector following the builder pattern.
   */
  @Contract(pure = true)
  public static @NotNull Builder not(@NotNull String tagName) {
    return not(of(tagName));
  }


  @Contract(pure = true)
  public static @NotNull Builder not(@NotNull TagSelector selector)
  {
    switch(selector.getType())
    {
      case NOT:
        return wrap(((SelectorReference)selector).getSelectors()[0]);

      case FIX:
        return MatchFixResult.valueOf(!selector.match(emptyList()));

      default:
        return new MatchNot(selector);
    }
  }


  @Contract(pure = true)
  public static @NotNull Builder and(@NotNull TagSelector... selectors)
  {
    switch(selectors.length)
    {
      case 0:
        throw new ProtocolException("tag selector array must not be empty");

      case 1:
        return wrap(selectors[0]);

      default:
        val selectorList = new ArrayList<TagSelector>(selectors.length);
        Collections.addAll(selectorList, selectors);
        return and(selectorList);
    }
  }


  private static @NotNull Builder and(@NotNull List<TagSelector> selectors)
  {
    val size = selectors.size();

    if (size == 0)
      return MatchFixResult.FALSE;
    else if (size > 1)
    {
      and_flatten(selectors);
      and_checkFix(selectors);
      and_bundleAllOf(selectors);
      and_reduceAny(selectors);
      and_bundleNot(selectors);
    }

    if (selectors.size() == 1)
      return wrap(selectors.get(0));

    selectors.sort(CMP_TYPE);

    return new MatchAnd(selectors.toArray(new TagSelector[0]));
  }


  private static void and_reduceAny(List<TagSelector> selectors)
  {
    var hasOf = false;

    for(val selector: selectors)
      if (selector.getType().isOf())
      {
        hasOf = true;
        break;
      }

    if (hasOf)
      selectors.removeIf(tagSelector -> tagSelector.getType() == ANY);
  }


  private static void and_checkFix(List<TagSelector> selectors)
  {
    TagSelector selector;

    for(Iterator<TagSelector> selectorIterator = selectors.iterator(); selectorIterator.hasNext();)
      if ((selector = selectorIterator.next()).getType() == FIX)
      {
        if (selector.match(emptyList()))
          selectorIterator.remove();
        else
        {
          selectors.clear();
          selectors.add(selector);
          break;
        }
      }
  }


  @SuppressWarnings({"java:S1121"})
  private static void and_flatten(List<TagSelector> selectors)
  {
    val collectedAndSelectors = new ArrayList<TagSelector>();
    TagSelector selector;

    for(Iterator<TagSelector> selectorIterator = selectors.iterator(); selectorIterator.hasNext();)
      if ((selector = selectorIterator.next()).getType() == AND)
      {
        selectorIterator.remove();
        Collections.addAll(collectedAndSelectors, ((SelectorReference)selector).getSelectors());
      }

    selectors.addAll(collectedAndSelectors);
  }


  private static void and_bundleNot(List<TagSelector> selectors)
  {
    selectors.sort(CMP_NOT_FIRST);

    while(selectors.size() >= 2)
    {
      val selector0 = selectors.get(0);
      val selector1 = selectors.get(1);

      if (selector0.getType() != NOT || selector1.getType() != NOT)
        break;

      selectors.remove(1);
      selectors.set(0, Tag.not(Tag.or(
          ((SelectorReference)selector0).getSelectors()[0],
          ((SelectorReference)selector1).getSelectors()[0])));
    }
  }


  private static void and_bundleAllOf(List<TagSelector> selectors)
  {
    selectors.sort(CMP_ALL_OF_FIRST);

    while(selectors.size() >= 2)
    {
      val selector0 = selectors.get(0);
      val selector1 = selectors.get(1);

      if (selector0.getType() != ALL_OF || selector1.getType() != ALL_OF)
        break;

      selectors.remove(1);
      selectors.set(0,
          new MatchAllOf(((TagReference)selector0).getTagNames(), ((TagReference)selector1).getTagNames()));
    }
  }


  @Contract(pure = true)
  public static @NotNull Builder or(@NotNull TagSelector... selectors)
  {
    switch(selectors.length)
    {
      case 0:
        throw new ProtocolException("tag selector array must not be empty");

      case 1:
        return wrap(selectors[0]);

      default:
        val selectorList = new ArrayList<TagSelector>(selectors.length);
        Collections.addAll(selectorList, selectors);
        return or(selectorList);
    }
  }


  @SuppressWarnings({"java:S1121", "java:S131"})
  private static @NotNull Builder or(@NotNull List<TagSelector> selectors)
  {
    val size = selectors.size();

    if (size == 0)
      return MatchFixResult.FALSE;
    else if (size > 1)
    {
      or_flatten(selectors);
      or_checkFix(selectors);
      or_bundleAnyOf(selectors);
      or_bundleNot(selectors);
    }

    if (selectors.size() == 1)
      return wrap(selectors.get(0));

    selectors.sort(CMP_TYPE);

    return new MatchOr(selectors.toArray(new TagSelector[0]));
  }


  @SuppressWarnings("java:S1121")
  private static void or_checkFix(List<TagSelector> selectors)
  {
    TagSelector selector;

    for(Iterator<TagSelector> selectorIterator = selectors.iterator(); selectorIterator.hasNext();)
      if ((selector = selectorIterator.next()).getType() == FIX)
      {
        if (selector.match(emptyList()))
        {
          selectors.clear();
          selectors.add(selector);
          break;
        }
        else
          selectorIterator.remove();
      }
  }


  @SuppressWarnings("java:S1121")
  private static void or_flatten(List<TagSelector> selectors)
  {
    val collectedOrSelectors = new ArrayList<TagSelector>();
    TagSelector selector;

    for(Iterator<TagSelector> selectorIterator = selectors.iterator(); selectorIterator.hasNext();)
      if ((selector = selectorIterator.next()).getType() == OR)
      {
        selectorIterator.remove();
        Collections.addAll(collectedOrSelectors, ((SelectorReference)selector).getSelectors());
      }

    selectors.addAll(collectedOrSelectors);
  }


  private static void or_bundleAnyOf(List<TagSelector> selectors)
  {
    selectors.sort(CMP_ANY_OF_FIRST);

    while(selectors.size() >= 2)
    {
      val selector0 = selectors.get(0);
      val selector1 = selectors.get(1);

      if (!isAnyOfMatcher(selector0) || !isAnyOfMatcher(selector1))
        break;

      selectors.remove(1);
      selectors.set(0,
          new MatchAnyOf(((TagReference)selector0).getTagNames(), ((TagReference)selector1).getTagNames()));
    }
  }


  private static void or_bundleNot(List<TagSelector> selectors)
  {
    selectors.sort(CMP_NOT_FIRST);

    while(selectors.size() >= 2)
    {
      val selector0 = selectors.get(0);
      val selector1 = selectors.get(1);

      if (selector0.getType() != NOT || selector1.getType() != NOT)
        break;

      selectors.remove(1);
      selectors.set(0, Tag.not(Tag.and(
          ((SelectorReference)selector0).getSelectors()[0],
          ((SelectorReference)selector1).getSelectors()[0])));
    }
  }


  private static boolean isAnyOfMatcher(@NotNull TagSelector selector)
  {
    val type = selector.getType();

    return type == ANY_OF || (type == ALL_OF && ((TagReference)selector).getTagNames().length == 1);
  }
}
