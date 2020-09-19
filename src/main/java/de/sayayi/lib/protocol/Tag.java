package de.sayayi.lib.protocol;

import de.sayayi.lib.protocol.TagSelector.MatchType;
import de.sayayi.lib.protocol.TagSelector.SelectorReference;
import de.sayayi.lib.protocol.TagSelector.TagReference;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static de.sayayi.lib.protocol.TagSelector.MatchType.ALL_OF;
import static de.sayayi.lib.protocol.TagSelector.MatchType.AND;
import static de.sayayi.lib.protocol.TagSelector.MatchType.ANY;
import static de.sayayi.lib.protocol.TagSelector.MatchType.ANY_OF;
import static de.sayayi.lib.protocol.TagSelector.MatchType.FIX;
import static de.sayayi.lib.protocol.TagSelector.MatchType.NOT;
import static de.sayayi.lib.protocol.TagSelector.MatchType.OR;


/**
 * @author Jeroen Gremmen
 */
@SuppressWarnings({"java:S100", "java:S1121"})
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Tag
{
  private static final Comparator<TagSelector> CMP_NOT_FIRST = new Comparator<TagSelector>() {
    @Override
    public int compare(TagSelector o1, TagSelector o2) {
      return (o1.getType() == NOT ? 0 : 1) - (o2.getType() == NOT ? 0 : 1);
    }
  };


  private static final Comparator<TagSelector> CMP_ALL_OF_FIRST = new Comparator<TagSelector>() {
    @Override
    public int compare(TagSelector o1, TagSelector o2) {
      return (o1.getType() == ALL_OF ? 0 : 1) - (o2.getType() == ALL_OF ? 0 : 1);
    }
  };


  private static final Comparator<TagSelector> CMP_ANY_OF_FIRST = new Comparator<TagSelector>() {
    @Override
    public int compare(TagSelector o1, TagSelector o2) {
      return (isAnyOfMatcher(o1) ? 0 : 1) - (isAnyOfMatcher(o2) ? 0 : 1);
    }
  };


  @Contract(pure = true)
  public static @NotNull TagSelector.Builder of(@NotNull String tagName) {
    return allOf(tagName);
  }


  @Contract(pure = true)
  public static @NotNull TagSelector.Builder any() {
    return new MatchAny();
  }


  @Contract(pure = true)
  public static @NotNull TagSelector.Builder anyOf(@NotNull String... tagNames)
  {
    tagNames = removeDuplicates(tagNames);

    switch(tagNames.length)
    {
      case 0:
        throw new IllegalArgumentException("tag name array must not be empty");

      case 1:
        return of(tagNames[0]);

      default:
        return new MatchAny(removeDuplicates(tagNames));
    }
  }


  @Contract(pure = true)
  public static @NotNull TagSelector.Builder allOf(@NotNull String... tagNames)
  {
    if (tagNames.length == 0)
      throw new IllegalArgumentException("tag name array must not be empty");

    return new MatchAll(removeDuplicates(tagNames));
  }


  @Contract(pure = true)
  public static @NotNull TagSelector.Builder noneOf(@NotNull String... tagNames) {
    return not(anyOf(tagNames));
  }


  @Contract(pure = true)
  public static @NotNull TagSelector.Builder not(@NotNull String tagName) {
    return not(of(tagName));
  }


  @Contract(pure = true)
  public static @NotNull TagSelector.Builder not(@NotNull TagSelector selector)
  {
    switch(selector.getType())
    {
      case NOT:
        return wrap(((SelectorReference)selector).getSelectors()[0]);

      case FIX:
        return new MatchFixResult(!selector.match(Collections.<String>emptyList()));

      default:
        return new MatchNot(selector);
    }
  }


  @Contract(pure = true)
  public static @NotNull TagSelector.Builder and(@NotNull TagSelector... selectors)
  {
    if (selectors.length == 0)
      throw new IllegalArgumentException("tag selector array must not be empty");

    List<TagSelector> selectorList = new ArrayList<TagSelector>(selectors.length);
    Collections.addAll(selectorList, selectors);

    return and(selectorList);
  }


  private static @NotNull TagSelector.Builder and(@NotNull List<TagSelector> selectors)
  {
    final int size = selectors.size();

    if (size == 0)
      return new MatchFixResult(false);
    else if (size > 1)
    {
      and_flatten(selectors);
      and_checkFix(selectors);
      and_bundleAllOf(selectors);
      and_reduceAny(selectors);
      and_bundleNot(selectors);
    }

    return selectors.size() == 1
        ? wrap(selectors.get(0))
        : new MatchAnd(selectors.toArray(new TagSelector[0]));
  }


  private static void and_reduceAny(List<TagSelector> selectors)
  {
    boolean hasOf = false;

    for(TagSelector selector: selectors)
      if (selector.getType().isOf())
      {
        hasOf = true;
        break;
      }

    if (hasOf)
      for(Iterator<TagSelector> selectorIterator = selectors.iterator(); selectorIterator.hasNext();)
        if (selectorIterator.next().getType() == ANY)
          selectorIterator.remove();
  }


  private static void and_checkFix(List<TagSelector> selectors)
  {
    TagSelector selector;

    for(Iterator<TagSelector> selectorIterator = selectors.iterator(); selectorIterator.hasNext();)
      if ((selector = selectorIterator.next()).getType() == FIX)
      {
        if (selector.match(Collections.<String>emptyList()))
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
    final List<TagSelector> collectedAndSelectors = new ArrayList<TagSelector>();
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
    Collections.sort(selectors, CMP_NOT_FIRST);

    while(selectors.size() >= 2)
    {
      final TagSelector selector0 = selectors.get(0);
      final TagSelector selector1 = selectors.get(1);

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
    Collections.sort(selectors, CMP_ALL_OF_FIRST);

    while(selectors.size() >= 2)
    {
      final TagSelector selector0 = selectors.get(0);
      final TagSelector selector1 = selectors.get(1);

      if (selector0.getType() != ALL_OF || selector1.getType() != ALL_OF)
        break;

      selectors.remove(1);
      selectors.set(0, new MatchAll(merge(
          ((TagReference)selector0).getTagNames(),
          ((TagReference)selector1).getTagNames())));
    }
  }


  @Contract(pure = true)
  public static @NotNull TagSelector.Builder or(@NotNull TagSelector... selectors)
  {
    if (selectors.length == 0)
      throw new IllegalArgumentException("tag selector array must not be empty");

    List<TagSelector> selectorList = new ArrayList<TagSelector>(selectors.length);
    Collections.addAll(selectorList, selectors);

    return or(selectorList);
  }


  @SuppressWarnings({"java:S1121", "java:S131"})
  private static @NotNull TagSelector.Builder or(@NotNull List<TagSelector> selectors)
  {
    final int size = selectors.size();

    if (size == 0)
      return new MatchFixResult(false);
    else if (size > 1)
    {
      or_flatten(selectors);
      or_checkFix(selectors);
      or_bundleAnyOf(selectors);
      or_bundleNot(selectors);
    }

    return selectors.size() == 1
        ? wrap(selectors.get(0))
        : new MatchOr(selectors.toArray(new TagSelector[0]));
  }


  @SuppressWarnings("java:S1121")
  private static void or_checkFix(List<TagSelector> selectors)
  {
    TagSelector selector;

    for(Iterator<TagSelector> selectorIterator = selectors.iterator(); selectorIterator.hasNext();)
      if ((selector = selectorIterator.next()).getType() == FIX)
      {
        if (selector.match(Collections.<String>emptyList()))
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
    final List<TagSelector> collectedOrSelectors = new ArrayList<TagSelector>();
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
    Collections.sort(selectors, CMP_ANY_OF_FIRST);

    while(selectors.size() >= 2)
    {
      final TagSelector selector0 = selectors.get(0);
      final TagSelector selector1 = selectors.get(1);

      if (!isAnyOfMatcher(selector0) || !isAnyOfMatcher(selector1))
        break;

      selectors.remove(1);
      selectors.set(0, new MatchAny(merge(
          ((TagReference)selector0).getTagNames(),
          ((TagReference)selector1).getTagNames())));
    }
  }


  private static void or_bundleNot(List<TagSelector> selectors)
  {
    Collections.sort(selectors, CMP_NOT_FIRST);

    while(selectors.size() >= 2)
    {
      final TagSelector selector0 = selectors.get(0);
      final TagSelector selector1 = selectors.get(1);

      if (selector0.getType() != NOT || selector1.getType() != NOT)
        break;

      selectors.remove(1);
      selectors.set(0, Tag.not(Tag.and(
          ((SelectorReference)selector0).getSelectors()[0],
          ((SelectorReference)selector1).getSelectors()[0])));
    }
  }


  private static String[] merge(String[] array1, String[] array2)
  {
    final Set<String> set = new TreeSet<String>();

    Collections.addAll(set, array1);
    Collections.addAll(set, array2);

    return set.toArray(new String[0]);
  }


  private static String[] removeDuplicates(String[] tagNames)
  {
    return tagNames.length <= 1
        ? tagNames : new TreeSet<String>(Arrays.asList(tagNames)).toArray(new String[0]);
  }


  private static TagSelector.Builder wrap(TagSelector tagSelector)
  {
    if (tagSelector instanceof TagSelector.Builder)
      return (TagSelector.Builder)tagSelector;

    if (tagSelector instanceof TagReference)
      return new WrapperTagReference(tagSelector);

    if (tagSelector instanceof SelectorReference)
      return new WrapperSelectorReference(tagSelector);

    throw new IllegalArgumentException("malformed tag selector: " + tagSelector);
  }


  private static boolean isAnyOfMatcher(@NotNull TagSelector selector)
  {
    final MatchType type = selector.getType();

    return type == ANY_OF || (type == ALL_OF && ((TagReference)selector).getTagNames().length == 1);
  }




  private abstract static class AbstractTagSelectorBuilder implements TagSelector.Builder, TagSelector
  {
    @NotNull
    @Override
    public TagSelector.Builder and(@NotNull TagSelector tagSelector) {
      return Tag.and(this, tagSelector);
    }


    @NotNull
    @Override
    public TagSelector.Builder and(@NotNull String tagName) {
      return Tag.and(this, Tag.of(tagName));
    }


    @NotNull
    @Override
    public TagSelector.Builder or(@NotNull TagSelector tagSelector) {
      return Tag.or(this, tagSelector);
    }


    @NotNull
    @Override
    public TagSelector.Builder or(@NotNull String tagName) {
      return Tag.or(this, Tag.of(tagName));
    }
  }




  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  private static final class MatchAll extends AbstractTagSelectorBuilder implements TagReference
  {
    @Getter private final String[] tagNames;


    @NotNull
    @Override
    public MatchType getType() {
      return ALL_OF;
    }


    @Override
    public boolean match(@NotNull Collection<String> tagNames)
    {
      if (tagNames.isEmpty())
        return false;

      for(String tagName: this.tagNames)
        if (!tagNames.contains(tagName))
          return false;

      return true;
    }


    @Override
    public String toString()
    {
      if (tagNames.length == 1)
        return tagNames[0];

      final StringBuilder s = new StringBuilder("allOf(");
      boolean first = true;

      for(String tagName: tagNames)
      {
        if (first)
          first = false;
        else
          s.append(',');

        s.append(tagName);
      }

      return s.append(')').toString();
    }
  }




  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  private static final class MatchAny extends AbstractTagSelectorBuilder implements TagReference
  {
    @Getter private final String[] tagNames;


    private MatchAny() {
      tagNames = null;
    }


    @NotNull
    @Override
    public MatchType getType() {
      return tagNames == null ? ANY : ANY_OF;
    }


    @Override
    public boolean match(@NotNull Collection<String> tagNames)
    {
      if (!tagNames.isEmpty())
      {
        if (this.tagNames == null)
          return true;

        for(String tagName: tagNames)
          if (Arrays.binarySearch(this.tagNames, tagName) >= 0)
            return true;
      }

      return false;
    }


    @Override
    public String toString()
    {
      if (tagNames == null)
        return "any()";

      final StringBuilder s = new StringBuilder("anyOf(");
      boolean first = true;

      for(String tagName: tagNames)
      {
        if (first)
          first = false;
        else
          s.append(',');

        s.append(tagName);
      }

      return s.append(')').toString();
    }
  }




  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  private static final class MatchNot extends AbstractTagSelectorBuilder implements SelectorReference
  {
    private final TagSelector selector;


    @NotNull
    @Override
    public MatchType getType() {
      return NOT;
    }


    @NotNull
    @Override
    public TagSelector[] getSelectors() {
      return new TagSelector[] { selector };
    }


    @Override
    public boolean match(@NotNull Collection<String> tagNames) {
      return !selector.match(tagNames);
    }


    @Override
    public String toString() {
      return "not(" + selector + ')';
    }
  }




  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  private static final class MatchAnd extends AbstractTagSelectorBuilder
  {
    @Getter private final TagSelector[] selectors;


    @NotNull
    @Override
    public MatchType getType() {
      return AND;
    }


    @Override
    public boolean match(@NotNull Collection<String> tagNames)
    {
      for(TagSelector tagSelector: selectors)
        if (!tagSelector.match(tagNames))
          return false;

      return true;
    }


    @Override
    public String toString()
    {
      if (selectors.length == 1)
        return selectors[0].toString();

      final StringBuilder s = new StringBuilder("and(");
      boolean first = true;

      for(TagSelector selector: selectors)
      {
        if (first)
          first = false;
        else
          s.append(',');

        s.append(selector);
      }

      return s.append(')').toString();
    }
  }




  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  private static final class MatchOr extends AbstractTagSelectorBuilder implements SelectorReference
  {
    @Getter private final TagSelector[] selectors;


    @NotNull
    @Override
    public MatchType getType() {
      return OR;
    }


    @Override
    public boolean match(@NotNull Collection<String> tagNames)
    {
      for(TagSelector selector: selectors)
        if (selector.match(tagNames))
          return true;

      return false;
    }


    @Override
    public String toString()
    {
      if (selectors.length == 1)
        return selectors[0].toString();

      final StringBuilder s = new StringBuilder("or(");
      boolean first = true;

      for(TagSelector selector: selectors)
      {
        if (first)
          first = false;
        else
          s.append(',');

        s.append(selector);
      }

      return s.append(')').toString();
    }
  }




  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  private static final class MatchFixResult extends AbstractTagSelectorBuilder
  {
    private final boolean result;


    @NotNull
    @Override
    public MatchType getType() {
      return MatchType.FIX;
    }


    @Override
    public boolean match(@NotNull Collection<String> tagNames) {
      return result;
    }


    @Override
    public String toString() {
      return result ? "true()" : "false()";
    }
  }




  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  private static final class WrapperTagReference extends AbstractTagSelectorBuilder
      implements TagReference
  {
    private final TagSelector selector;


    @NotNull
    @Override
    public MatchType getType() {
      return selector.getType();
    }


    @Override
    public String[] getTagNames() {
      return ((TagReference)selector).getTagNames();
    }


    @Override
    public boolean match(@NotNull Collection<String> tagNames) {
      return selector.match(tagNames);
    }


    @Override
    public String toString() {
      return selector.toString();
    }
  }




  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  private static final class WrapperSelectorReference extends AbstractTagSelectorBuilder
      implements SelectorReference
  {
    private final TagSelector selector;


    @NotNull
    @Override
    public MatchType getType() {
      return selector.getType();
    }


    @NotNull
    @Override
    public TagSelector[] getSelectors() {
      return ((SelectorReference)selector).getSelectors();
    }


    @Override
    public boolean match(@NotNull Collection<String> tagNames) {
      return selector.match(tagNames);
    }


    @Override
    public String toString() {
      return selector.toString();
    }
  }




  public static final class Parser
  {
    public static @NotNull TagSelector from(@NotNull String selector)
    {
      return Tag.any();
    }
  }
}
