# User-Defined Levels

- Level as a functional interface: custom severity values via lambda or implementation
- When standard DEBUG/INFO/WARN/ERROR are not sufficient
- Defining domain-specific levels (e.g. TRACE, NOTICE, CRITICAL, FATAL)
- Using custom levels with Protocol#add(Level)
- How custom levels interact with:
  - Level matchers (is, between)
  - Group level limits
  - The expression language (level('name') requires string matching). Make a reference to ../matcher/expression-language.md
- Recommendations for severity value ranges
