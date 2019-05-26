package de.sayayi.lib.protocol;


public interface Level
{
  /**
   * <p>
   *   Returns the severity for this level.
   * </p>
   * <p>
   *   A higher severity number indicates a more severe problem.
   * </p>
   *
   * @return  severity number
   *
   * @see Shared
   */
  int severity();


  /**
   * Level constants
   */
  enum Shared implements Level
  {
    /** Constant representing a level with the lowest possible severity */
    ALL(Integer.MIN_VALUE),

    /** Constant representing DEBUG level (severity = 100) */
    DEBUG(100),

    /** Constant representing INFO level (severity = 200) */
    INFO(200),

    /** Constant representing WARNING level (severity = 300) */
    WARN(300),

    /** Constant representing ERROR level (severity = 400) */
    ERROR(400);


    private final int severity;


    Shared(int severity) {
      this.severity = severity;
    }


    @Override
    public int severity() {
      return severity;
    }
  }
}
