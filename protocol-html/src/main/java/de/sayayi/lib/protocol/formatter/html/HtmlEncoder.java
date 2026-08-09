package de.sayayi.lib.protocol.formatter.html;

import org.intellij.lang.annotations.Language;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.currentThread;


/**
 * Abstract base class for HTML encoding strategies. Implementations delegate to a specific HTML escaping library
 * (e.g. Spring Web, Guava, Apache Commons Text, Unbescape, or OWASP).
 * <p>
 * The active encoder is resolved automatically via Java {@link ServiceLoader} or by probing the classpath for known
 * libraries. A single shared instance is obtained through {@link #getInstance()}.
 *
 * @author Jeroen Gremmen
 * @since 1.6.0
 *
 * @see CommonsTextHtmlEncoder
 * @see GuavaHtmlEncoder
 * @see OwaspHtmlEncoder
 * @see SpringWebHtmlEncoder
 * @see UnbescapeHtmlEncoder
 */
@SuppressWarnings("SpellCheckingInspection")
public abstract class HtmlEncoder
{
  private static final Map<String,String> ENCODER_MAP = new LinkedHashMap<>();
  private static final Lock LOCK = new ReentrantLock();
  private static volatile HtmlEncoder INSTANCE = null;


  static
  {
    ENCODER_MAP.put("org.springframework.web.util.HtmlUtils", "SpringWebHtmlEncoder");
    ENCODER_MAP.put("com.google.common.html.HtmlEscapers", "GuavaHtmlEncoder");
    ENCODER_MAP.put("org.apache.commons.text.StringEscapeUtils", "CommonsTextHtmlEncoder");
    ENCODER_MAP.put("org.unbescape.html.HtmlEscape", "UnbescapeHtmlEncoder");
    ENCODER_MAP.put("org.owasp.encoder.Encode", "OwaspHtmlEncoder");
  }


  /**
   * Encodes the given plain text for safe inclusion in HTML content.
   *
   * @param text  plain text to encode, not {@code null}
   *
   * @return  HTML-encoded text, never {@code null}
   */
  @Contract(pure = true)
  public abstract @Language("HTML") @NotNull String encodeHtml(@NotNull String text);


  /**
   * Returns the shared {@link HtmlEncoder} instance. On first invocation, the encoder is resolved via
   * {@link ServiceLoader} or by probing the classpath for known HTML escaping libraries.
   *
   * @return  shared encoder instance, never {@code null}
   *
   * @throws UnsupportedOperationException  if no supported HTML encoding library is found
   */
  public static @NotNull HtmlEncoder getInstance()
  {
    var instance = INSTANCE;
    if (instance == null)
    {
      LOCK.lock();
      try {
        if ((instance = INSTANCE) == null)
        {
          instance = ServiceLoader
              .load(HtmlEncoder.class)
              .findFirst()
              .orElseGet(HtmlEncoder::probeForImplementations);

          if (instance == null)
          {
            throw new UnsupportedOperationException("no html encoders found, please provide any of " +
               ENCODER_MAP.keySet());
          }

          INSTANCE = instance;
        }
      } finally {
        LOCK.unlock();
      }
    }

    return instance;
  }


  @Contract(pure = true)
  private static HtmlEncoder probeForImplementations()
  {
    final var classLoader = currentThread().getContextClassLoader();

    for(final var encoderEntry: ENCODER_MAP.entrySet())
    {
      try {
        Class.forName(encoderEntry.getKey(), false, classLoader);
        return (HtmlEncoder)Class
            .forName(HtmlEncoder.class.getPackageName() + '.' + encoderEntry.getValue())
            .getDeclaredConstructor()
            .newInstance();
      } catch(Exception ignored) {
      }
    }

    return null;
  }
}
