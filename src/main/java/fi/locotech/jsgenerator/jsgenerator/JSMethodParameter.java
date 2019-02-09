package fi.locotech.jsgenerator.jsgenerator;

import java.util.List;

public class JSMethodParameter {

  private final List<JSObjectType> chain;

  /**
   * Name of the parameter even though it will be arg0, arg1 ... argN for now.
   */
  private final String parameterName;

  /**
   * Annotated parameter name if it exists.
   */
  private final String annotatedName;

  public JSMethodParameter(String parameterName, String annotatedName, List<JSObjectType> chain) {
    this.parameterName = parameterName;
    this.annotatedName = annotatedName;
    this.chain = chain;
  }

  public List<JSObjectType> getChain() {
    return chain;
  }

  public String getAnnotatedName() {
    return annotatedName;
  }

  public String getParameterName() {
    return parameterName;
  }
}
