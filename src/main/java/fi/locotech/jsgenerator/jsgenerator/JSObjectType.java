package fi.locotech.jsgenerator.jsgenerator;



public class JSObjectType {
  public enum JSO {
    OBJECT,
    PRIMITIVE,
    COLLECTION
  }

  private final String name;
  private final JSO type;
  private final boolean typeParameter;

  public JSObjectType(String name, JSO type, boolean typeParameter) {
    this.name = name;
    this.type = type;
    this.typeParameter = typeParameter;
  }
  public JSObjectType(String name, JSO type) {
    this(name, type, false);
  }

  public String getName() {
    return name;
  }

  public JSO getType() {
    return type;
  }

  public boolean isTypeParameter() {
    return typeParameter;
  }

  public boolean isCollection() {
    return this.type == JSO.COLLECTION;
  }
}
