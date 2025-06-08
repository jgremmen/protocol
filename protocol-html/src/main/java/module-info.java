module de.sayayi.lib.protocol.html {

  requires de.sayayi.lib.protocol;

  requires static com.google.common;
  requires static org.apache.commons.text;
  requires static org.jetbrains.annotations;
  requires static owasp.encoder;
  requires static spring.web;
  requires static unbescape;

  exports de.sayayi.lib.protocol.formatter.html;

  uses de.sayayi.lib.protocol.formatter.html.HtmlEncoder;

}