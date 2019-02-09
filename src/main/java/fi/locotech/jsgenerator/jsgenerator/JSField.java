package fi.locotech.jsgenerator.jsgenerator;

import fi.locotech.jsgenerator.filetree.FileTree;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class JSField {

  private static final String LB = "\r\n";
  private static final String S = " ";
  private static final String S2 = S + S;
  private static final String S4 = S + S + S + S;
  private static final String C = ";";

  private static final Class[] primitives = {Long.class, Integer.class, Short.class, Boolean.class, Date.class, Float.class, Double.class, Character.class, Byte.class, String.class};
  private static final String[] numbers = {"long", "int", "short", "float", "double"};

  private static Set<String> classNames;
  private static Set<String> primitiveNames;
  private static Set<String> numberNames;

  static {
    classNames = new HashSet<>();
    primitiveNames = new HashSet<>();
    numberNames = new HashSet<>();
    for (Class c : primitives) {
      classNames.add(c.getSimpleName());
      primitiveNames.add(c.getSimpleName().toLowerCase());
    }
    primitiveNames.add("int");
    for (String s : numbers) {
      numberNames.add(s);
    }

  }

  String name;
  boolean needsImport;
  String commentName; // the comment name kind of
  Class type;
  String importName;
  String importPath;
  String staticImport;

  JSField(String name, Type tt, Field f, String currentClass, FileTree tree) {
    this.name = name;
    this.type = (Class) tt;
    if (Collection.class.isAssignableFrom(type)) {
      this.needsImport = true;
      // we have a collection
      ParameterizedType genericClass = (ParameterizedType) f.getGenericType();
      this.importName = ((Class) genericClass.getActualTypeArguments()[0]).getSimpleName();

      // and here we go again
      if (!(classNames.contains(importName) || primitiveNames.contains(importName))) {
        String newImport = tree.createImport(currentClass, this.importName);
        if (newImport != null) {
          this.importPath = newImport;
        }
        else {
          this.needsImport = false;
          this.commentName = "Object";
        }
      }

      if (Set.class.isAssignableFrom(type)) {
        this.commentName = "Set<" + this.importName + ">";
      }
      else {
        this.commentName = "Array<" + this.importName + ">";
      }

    }
    else if (f.isAnnotationPresent(JSType.class)) {
      JSType type = f.getAnnotation(JSType.class);
      this.commentName = type.value();
      String ss = JSImports.getImports().get(type.value());
      if (ss != null) {
        this.staticImport = ss;
        this.needsImport = true;
      }
      else
        this.needsImport = false;
    }
    else {
      this.commentName = type.getSimpleName();
      this.importName = type.getSimpleName();
      this.needsImport = !(classNames.contains(importName) || primitiveNames.contains(importName));
      if (this.needsImport) {
        String newImport = tree.createImport(currentClass, this.importName);
        if (newImport != null) {
          this.importPath = newImport;
          this.importName = type.getSimpleName();
        }
        else {
          this.needsImport = false;
          this.commentName = "Object";
        }
      }
    }
  }

  public String getStaticImport() {
    return staticImport;
  }

  public String getImportName() {
    return importName;
  }

  public String getImportPath() {
    if (this.importPath != null)
      return this.importPath;
    return importName;
  }

  String toJSString(String start) {
    StringBuilder sb = new StringBuilder();
    sb.append(generateSetterComment(start, this));
    sb.append(start).append("set").append(S).append(this.name).append("(").append(this.name).append(")").append(S).append("{")//.append(LB)
        //.append(start).append(start)
        .append("this._").append(this.name).append(" = ").append(this.name).append(";")//.append(LB)
        //.append(start)
        .append("}").append(LB)
        .append(generateGetterComment(start, this))
        .append(start).append("get").append(S).append(this.name).append("()").append(S).append("{").append(S)//.append(LB)
        //.append(start).append(start)
        .append("return").append(S).append("this._").append(this.name).append(C)//.append(LB)
        //.append(start)
        .append("}").append(LB);
    return sb.toString();

  }

  String getCommentType() {
    if (Number.class.isAssignableFrom(this.type) || numberNames.contains(this.commentName)) {
      return "number";
    }
    else if (this.type == String.class) {
      return "string";
    }
    else {
      return this.commentName;
    }
  }

  static String generateSetterComment(String start, JSField field) {
    StringBuilder sb = new StringBuilder();
    sb
        .append(start).append("/**").append(LB)
        .append(start).append(S).append("*").append(S).append("@param").append(S).append("{").append(field.getCommentType()).append("}").append(S).append(field.name).append(LB)
        .append(start).append(S).append("*/").append(LB);
    return sb.toString();
  }

  static String generateGetterComment(String start, JSField field) {
    StringBuilder sb = new StringBuilder();
    sb
        .append(start).append("/**").append(LB)
        .append(start).append(S).append("*").append(S).append("@returns").append(S).append("{").append(field.getCommentType()).append("}").append(LB)
        .append(start).append(S).append("*/").append(LB);
    return sb.toString();
  }


}