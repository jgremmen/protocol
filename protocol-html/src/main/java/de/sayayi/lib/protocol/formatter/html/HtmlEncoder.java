package de.sayayi.lib.protocol.formatter.html;

import org.intellij.lang.annotations.Language;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;


/**
 * @author Jeroen Gremmen
 * @since 0.6.0
 */
public abstract class HtmlEncoder
{
  private static final Map<String,String> ENCODER_MAP = new LinkedHashMap<>();

  static {
    ENCODER_MAP.put("org.unbescape.html.HtmlEscape", "UnbescapeHtmlEncoder");
    ENCODER_MAP.put("org.owasp.encoder.Encode", "OwaspHtmlEncoder");
    ENCODER_MAP.put("org.springframework.web.util.HtmlUtils", "SpringWebHtmlEncoder");
    ENCODER_MAP.put("com.google.common.html.HtmlEscapers", "GuavaHtmlEncoder");
    ENCODER_MAP.put("org.apache.commons.text.StringEscapeUtils", "CommonsTextHtmlEncoder");
  }

  private static HtmlEncoder INSTANCE = null;


  @Contract(pure = true)
  public abstract @Language("HTML") @NotNull String encodeHtml(@NotNull String text);


  public static @NotNull HtmlEncoder getInstance()
  {
    if (INSTANCE == null)
    {
      INSTANCE = ServiceLoader.load(HtmlEncoder.class).findFirst().orElseGet(HtmlEncoder::probeForImplementations);
      if (INSTANCE == null)
        throw new UnsupportedOperationException("no html encoders found, please provide any of " + ENCODER_MAP.keySet());
    }

    return INSTANCE;
  }


  @Contract(pure = true)
  private static HtmlEncoder probeForImplementations()
  {
    final var classLoader = Thread.currentThread().getContextClassLoader();

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
