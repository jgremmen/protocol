/*
 * Copyright 2022 Jeroen Gremmen
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
package de.sayayi.lib.protocol.matcher.antlr;

import org.antlr.v4.runtime.Vocabulary;

import lombok.AllArgsConstructor;
import lombok.val;

import org.jetbrains.annotations.NotNull;

import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;


/**
 * Convenience class for creating a custom vocabulary.
 *
 * @author Jeroen Gremmen
 * @since 1.2.0
 */
public abstract class AbstractVocabulary implements Vocabulary
{
  private final SortedMap<Integer,Name> vocabulary = new TreeMap<>();


  protected AbstractVocabulary() {
    addTokens();
  }


  protected abstract void addTokens();


  protected void add(int tokenType, @NotNull String literal, @NotNull String symbol) {
    vocabulary.put(tokenType, new Name(literal, symbol));
  }


  @Override
  public int getMaxTokenType() {
    return vocabulary.lastKey();
  }


  @Override
  public String getLiteralName(int tokenType) {
    return vocabulary.containsKey(tokenType) ? vocabulary.get(tokenType).literal : null;
  }


  @Override
  public String getSymbolicName(int tokenType) {
    return vocabulary.containsKey(tokenType) ? vocabulary.get(tokenType).symbol : null;
  }


  @Override
  public String getDisplayName(int tokenType)
  {
    return !vocabulary.containsKey(tokenType)
        ? Integer.toString(tokenType) : vocabulary.get(tokenType).literal;
  }


  @Override
  public String toString()
  {
    return vocabulary.entrySet()
        .stream()
        .map(this::toString_entry)
        .collect(joining(",", "Vocabulary[", "]"));
  }


  private @NotNull String toString_entry(@NotNull Entry<Integer,Name> entry)
  {
    val name = entry.getValue();
    return "{token=" + entry.getKey() + ",literal=" + name.literal + ",symbol=" + name.symbol + '}';
  }


  @AllArgsConstructor(access = PRIVATE)
  private static final class Name
  {
    final String literal;
    final String symbol;
  }
}