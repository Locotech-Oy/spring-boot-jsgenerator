package fi.locotech.jsgenerator.jsgenerator;

import fi.locotech.jsgenerator.filetree.FileTree;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * JSGenerator main class.
 *
 * This is a class that provides transpiling from Java classes to JS (ES6) classes.
 *
 * The main purpose for this is to generate JS controller classes for all {@link RestController}s so that
 * they do not need to be written multiple times.
 *
 * This also provides cool way for a developer to keep the Java and JS classes up to date so the developer does not
 * need to implement every new method twice.
 *
 * Note: This is not final and does not provide all the functionality yet but this can be used on rudimentary classes
 * and {@link RestController}s.
 *
 *
 */
public class JSGenerator {

  private static final Logger log = LoggerFactory.getLogger(JSGenerator.class);

  /** Line brake */
  private static final String LB = "\r\n";
  /** Space 1*/
  private static final String S = " ";
  /** Space 2*/
  private static final String S2 = S + S;
  /** Space 4*/
  private static final String S4 = S + S + S + S;
  /** Semi-colon */
  private static final String C = ";";
  /** Java primitives, mostly.*/
  private static final Class[] primitives = {Long.class, Integer.class, Short.class, Boolean.class, Date.class, Float.class, Double.class, Character.class, Byte.class, String.class};
  /** Java primitives that are considered as numbers in JS. */
  private static final String[] numbers = {"long", "int", "short", "float", "double"};
  /** Custom code block starting comment. */
  private static final String CUSTOME_CODE_COMMENT_START = "// --- ### --- ### Custom code START ### --- ### ---";
  /** Custom code block ending comment. */
  private static final String CUSTOME_CODE_COMMENT_END = "// ### --- ### --- Custom code END --- ### --- ###";
  /** Bracket open. */
  private static final String BO = "{";
  /** Bracket close. */
  private static final String BC = "}";
  /** */
  private static Set<String> classNames;
  private static Set<String> primitiveNames;
  private static Set<String> numberNames;


  /** toJSON method for all JS classes */
  private static final String TO_JSON = "  toJSON(){\r\n" +
      "    let rtn = {};\r\n" +
      "    for(let p in this){\r\n" +
      "      if(p.charAt(0) == '_'){\r\n" +
      "        // private member according to convention, test for getter\r\n" +
      "        let pp = p.substring(1);\r\n" +
      "        if(typeof this[pp] != 'undefined'){\r\n" +
      "          rtn[pp] = this[pp];\r\n" +
      "        }\r\n" +
      "      }\r\n" +
      "    }\r\n" +
      "    return rtn;\r\n" +
      "  }";

  private final String packageToScan;

  private final String outputFolder;



  public JSGenerator(String outputFolder, String packageToScan) {
    if (outputFolder == null) {
      File output = new File("src/main/js/");
      outputFolder = output.getAbsolutePath();
    }
    this.packageToScan = packageToScan;
    this.outputFolder = outputFolder;
  }

  /**
   * Generates new JSGenerator instance with the package to scan.
   *
   * @param packageToScan package to scan for classes to transpile.
   */
  public JSGenerator(String packageToScan) {
    this(null, packageToScan);
  }

  private FileTree tree;

  /**
   * Static constructor sets up some sets and for the generator.
   */
  static void setup() {
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

  public static void main(String... args) {

    JSGenerator generator = new JSGenerator("fi.locotech.jsgenerator.");
    generator.generateJS();


  }

  public void generateJS() {
    try {
      this.privateGenerateJS();
      this.generateControllers();
    }
    catch (IOException ioe) {
      log.error("Error while generating JS", ioe);
    }
  }

  /**
   * Generates JS (ES6) code for all controllers:
   * <ul>
   * <li>JSDoc block for with all the return types and parameters typed for Intellisense</li>
   * <li>Method body using Request.js to call the right end point with right parameters</li>
   * <li>Constructing the return objects and Arrays, as well as Sets.</li>
   * </ul>
   * <p>
   * TODO Maps and inner generics are not yet supported.
   *
   * @throws IOException in case something hits the fan.
   */
  private void generateControllers() throws IOException {
    tree.addStraightPackage(packageToScan + "api.Request");


    ClassPathScanningCandidateComponentProvider scanner =
        new ClassPathScanningCandidateComponentProvider(false);

    scanner.addIncludeFilter(new AnnotationTypeFilter(JSController.class));

    List<Class> classes = new LinkedList<>();

    for (BeanDefinition bd : scanner.findCandidateComponents(packageToScan)) {
      try {
        Class clazz = this.getClass().getClassLoader().loadClass(bd.getBeanClassName());
        tree.addPackage(clazz);
        classes.add(clazz);
      }
      catch (ClassNotFoundException e) {
        log.error("Class " + bd.getBeanClassName() + "could not be loaded", e);
      }
    }
    for (Class c : classes) {
      String path = outputFolder + tree.getPathToWriteFile(c.getSimpleName());
      File jsdir = new File(path);
      if (jsdir.exists()) {

      }
      else {
        jsdir.mkdirs();
      }
      File js = new File(path + c.getSimpleName() + ".js");
      String customCode = "";
      if (js.exists()) {
        customCode = this.readCustomCode(js);
      }
      if (customCode.equals(LB))
        customCode = "";
      try (Writer w = new FileWriter(js)) {

        w.write(generateController(c, customCode));
      }
      catch (Exception e) {
        throw new IOException(e);
      }
    }
  }

  /**
   * Generates signatures and methods for the given controller.
   *
   * @param clazz      of the Controller
   * @param customCode read custom code that was typed to insert after the controller body.
   * @return the controller code as a String.
   * @throws Exception in case something hits the fan.
   */
  private String generateController(Class clazz, String customCode) throws Exception {
    StringBuilder sb = new StringBuilder();

    List<Class> otherInputs = new LinkedList<>();
    String methods = generateGenericMethods(clazz, otherInputs);

    sb.append(generateControllerImports(clazz, otherInputs));

    sb.append(LB);
    sb.append(generateClassDeclaration(clazz));
    //sb.append(generateDeclaredMethods(clazz.getDeclaredMethods(), clazz));
    sb.append(methods);
    sb.append(LB).append(LB);


    sb.append(LB).append(LB)
        .append(S2).append(CUSTOME_CODE_COMMENT_START).append(LB)
        .append(customCode).append(LB)
        .append(S2).append(CUSTOME_CODE_COMMENT_END).append(LB)
        .append(LB).append(BC).append(LB);

    return sb.toString();
  }

  /**
   * Generates all import lines for the controller.
   *
   * @param clazz of the controller.
   * @return all needed imports in a String format.
   * @throws Exception in case shit meets the fan.
   */
  private String generateControllerImports(Class clazz, List<Class> others) throws Exception {


    if (clazz.isAnnotationPresent(JSController.class)) {
      JSController c = (JSController) clazz.getAnnotation(JSController.class);

    }
    else {
      throw new Exception("Class " + clazz.getSimpleName() + " should not be here");
    }


    StringBuilder imports = new StringBuilder();
    /*
    String importPath = "";
    String importName = "";
    String commentName;
    boolean needsImport = false;
    String newImport = tree.createImport(clazz.getSimpleName(), importClass.getSimpleName());

    if (newImport != null) {
      importPath = newImport;
      needsImport = true;
      importName = ((Class) importClass).getSimpleName();
    }
    else {
      needsImport = false;
      commentName = "Object";
    }
    if (needsImport) {
      String s = "import" + S + BO + importName + BC + S + "from" + S + "'" + importPath + "'" + C;
      imports.append(s);
    }
*/
    String importRequestPath = tree.createImport(clazz.getSimpleName(), "Request");
    imports.append(LB)
        .append("import {Request} from '").append(importRequestPath).append("';");
    imports.append(LB);

    for (Class c : others) {
      String importPath2 = "";
      String importName2 = "";
      boolean needsImport2 = false;
      String newImport2 = tree.createImport(clazz.getSimpleName(), c.getSimpleName());

      if (newImport2 != null) {
        importPath2 = newImport2;
        needsImport2 = true;
        importName2 = c.getSimpleName();
      }
      if (needsImport2) {
        String s = "import" + S + BO + importName2 + BC + S + "from" + S + "'" + importPath2 + "'" + C;
        imports.append(s);
        imports.append(LB);
      }
    }

    return imports.toString();
  }

  /**
   * Generates class declaration.
   *
   * @param clazz which's declration needs to be generated.
   * @return the declaration.
   */
  private String generateClassDeclaration(Class clazz) {
    StringBuilder sb = new StringBuilder();
    sb.append(LB);
    sb.append("export class").append(S).append(clazz.getSimpleName()).append(S).append(BO).append(LB);
    return sb.toString();
  }

  /**
   * Generates code for all generic methods.
   * <p>
   * NOTE!
   * Generic method means a method that has generally parametrized things on it.
   *
   * @param clazz which's methods needs
   * @return all generic methods for the class.
   */
  private String generateGenericMethods(Class clazz, List<Class> imports) {
    //List<Method> declaredSearch = Arrays.asList(clazz.getDeclaredMethods());
    StringBuilder methods = new StringBuilder();
    Set<String> methodNames = new HashSet<>();
    for (Method method : clazz.getMethods()) {
      //if (declaredSearch.contains(method))
      //  continue;
      if (method.isAnnotationPresent(RequestMapping.class)) {
        if(method.isAnnotationPresent(Override.class) || methodNames.contains(method.getName())) {
          // we do not want to create the method signature for overridden methods as their signatures already come
          // from the base methods (signature must be same when it is overridden) and there seems to be some funky
          // generics things going on with the methods so that their generic parameters (which are also overridden)
          // appear to be <Object, Object>

          // also the Override does not seem to be present ever so we do just simple name mapping for now... But this
          // is not good as there may be multiple same name methods, but then again JS cannot have that so it is
          // void point anyways.

          // we could probably go on a stretch and just rename the methods but that is not good either because the
          // overridden methods will still result into <Object, Object> signature.

          // though we take a leap of faith here that the actual base method will come here before the override
          // but it seems to be so all of the time, this might be due to the method resolving order in the Java
          continue;
        }
        methods.append(generateGenericMethod(method, clazz, imports));
        methods.append(LB).append(LB);
        methodNames.add(method.getName());
      }
    }
    return methods.toString();
  }

  /**
   * Generates code for all declared methods.
   *
   * @param methodArray array of methods for the class
   * @param clazz       the class which's methods needs some generating.
   * @return the generated declared methods.
   */
  private String generateDeclaredMethods(Method[] methodArray, Class clazz, List<Class> returns) {
    StringBuilder methods = new StringBuilder();
    for (Method method : methodArray) {
      if (method.isAnnotationPresent(RequestMapping.class)) {
        if(method.isAnnotationPresent(Override.class)) {
          // we do not want to create the method signature for overridden methods as their signatures already come
          // from the base methods (signature must be same when it is overridden) and there seems to be some funky
          // generics things going on with the methods so that their generic parameters (which are also overridden)
          // appear to be <Object, Object>
          continue;
        }
        methods.append(generateGenericMethod(method, clazz, returns));
        methods.append(LB).append(LB);
      }
    }
    return methods.toString();
  }

  /**
   * Generates code for one method.
   * <p>
   * This should be used for declared methods.
   *
   * @param method method that needs some generating.
   * @param clazz  the class which the method belongs into.
   * @return the generated method.
   */
  private String generateMethod(Method method, Class clazz, List<Class> imports) {
    StringBuilder sb = new StringBuilder();
    sb.append(generateGenericMethodSignature(method, clazz, imports));
    sb.append(generateMethodBody(method, clazz));
    sb.append(LB).append(S + S).append(BC);
    return sb.toString();
  }

  /**
   * Generates a generic method code.
   * <p>
   * This should be used for inherited methods.
   *
   * @param method
   * @param clazz
   * @return
   */
  private String generateGenericMethod(Method method, Class clazz, List<Class> imports) {
    StringBuilder sb = new StringBuilder();
    sb.append(generateGenericMethodSignature(method, clazz, imports));
    sb.append(generateMethodBody(method, clazz));
    sb.append(LB).append(S + S).append(BC);
    return sb.toString();
  }

  private boolean isMethodCollection(Method method) {
    return (Collection.class.isAssignableFrom(method.getReturnType()));
  }

  private String getJSCollection(Class type) {
    if (Set.class.isAssignableFrom(type)) {
      return "Set";
    }
    if (Map.class.isAssignableFrom(type)) {
      return "Map";
    }
    else {
      return "Array";
    }
  }

  private List<String> getCollectionTypeArguments(Class controller, Type... arguments) {
    List<String> jsTypes = new ArrayList<>(arguments.length);
    for (Type type : arguments) {


    }
    return jsTypes;
  }

  private Map<Type, Class> generateGenericToClassMap(Class clazz) {
    ParameterizedType pt = (ParameterizedType) clazz.getGenericSuperclass();
    Type[] actualTypes = pt.getActualTypeArguments();
    Type[] genericTypes = clazz.getSuperclass().getTypeParameters();
    Map<Type, Class> typeMap = new HashMap<>();
    for (int i = 0; i < actualTypes.length; i++) {
      typeMap.put(genericTypes[i], (Class) actualTypes[i]); // this could fail if we had controller for collections of some items but fuck it for now
    }
    return typeMap;
  }

  private String generateGenericMethodSignature(Method method, Class clazz, List<Class> imports) {
    TypeVariable<?>[] tvr = clazz.getTypeParameters();
    ParameterizedType pt = (ParameterizedType) clazz.getGenericSuperclass();
    Type[] types = pt.getActualTypeArguments();
    Type[] gT = clazz.getSuperclass().getTypeParameters();
    Map<Type, Type> genericToStandard = new HashMap<>();
    for (int k = 0; k < types.length; k++) {
      genericToStandard.put(gT[k], types[k]);
    }

    StringBuilder sb = new StringBuilder();
    RequestMapping rm = method.getAnnotation(RequestMapping.class);
    String name = method.getName();
    StringBuilder pb = new StringBuilder();
    Parameter[] params = method.getParameters();
    Type[] genericParamTypes = method.getGenericParameterTypes();
    Type returnT = method.getGenericReturnType();
    Class returnC = null;
    if (returnT != null) {
      if(returnT instanceof Class)
        returnC = (Class)returnT;
      else
        returnC = (Class) genericToStandard.get(returnT);
    }

    sb.append(S2).append("/**").append(LB);


    for (int i = 0; i < method.getParameterCount(); i++) {

      Type p = genericParamTypes[i];
      Class actualType = (Class) genericToStandard.get(p);
      pb.append(params[i].getName());
      if (i < method.getParameterCount() - 1)
        pb.append(", ");
      if (actualType == null && p != null)
        actualType = (Class) p;
      sb.append(S2).append("*").append(S).append("@param").append(S).append(BO)
          .append(getCommentName(actualType, genericToStandard, imports)).append(BC).append(S).append(params[i].getName()).append(LB);
    }
    if (returnC != null) {
      sb.append(S2).append("*").append(S).append("@returns").append(S).append(BO).append("Promise<")
          .append(getCommentName(returnC, genericToStandard, imports)).append(">").append(BC).append(LB);

    }
    else if (returnT != null) {
      // this is primitive most likely
      Type t = method.getGenericReturnType();
      if (t.getTypeName().equals("void")) {
        // do nada
      }
      else {
        if (t instanceof ParameterizedType) {
          StringBuilder cnb = new StringBuilder();
          cnb.append(getCommentName(((ParameterizedType) t).getRawType(), genericToStandard, imports));
          int o = 0;
          for (Type tt : ((ParameterizedType) t).getActualTypeArguments()) {
            cnb.append(getCommentName(tt, genericToStandard, imports));
            o++;
            if (o < ((ParameterizedType) t).getActualTypeArguments().length) {
              cnb.append(", ");
            }
          }
          cnb.append(">");
          sb.append(S2).append("*").append(S).append("@returns").append(S).append(BO).append("Promise<").append(cnb).append(">")
              .append(BC).append(LB);

        }
        else {
          sb.append(S2).append("*").append(S).append("@returns").append(S).append(BO).append("Promise<")
              .append(getCommentName(method.getReturnType(), genericToStandard, imports)).append(">").append(BC).append(LB);
        }

      }
    }
    sb.append(S2).append("*/").append(LB);
    sb.append(S).append(S).append("static").append(S).append(name).append("(").append(pb.toString()).append(")").append(BO).append(LB);
    return sb.toString();
  }

  private static String getCommentForClass(Class type, Map<Type, Type> genericToType, List<Class> imports) {
    String name = type.getSimpleName();
    StringBuilder commentName = new StringBuilder();

    if (Collection.class.isAssignableFrom(type)) {
      // we have collection, lets iterate
      if (Set.class.isAssignableFrom(type)) {
        commentName.append("Set<");
      }
      else if (Map.class.isAssignableFrom(type)) {
        commentName.append("Map<");
      }
      else {
        commentName.append("Array<");
      }
      /*Type[] arguments = type.getTypeParameters();
      int k = 0;
      for (Type t : arguments) {
        Type realType = genericToType.get(t);
        if (realType != null)
          commentName.append((Class) realType);
        else {
          // we have primitive type here which should be castable
          commentName.append((Class) t);
        }
        k++;
        if (k < arguments.length) {
          commentName.append(", ");
        }
      }
      commentName.append(">"); */
      return commentName.toString();
    }
    if (Number.class.isAssignableFrom(type) || numberNames.contains(type.getSimpleName().toLowerCase())) {
      return "number";
    }
    else if (type.getSimpleName().toLowerCase().equals("string")) {
      return "string";
    }
    else {
      if(type.isAnnotationPresent(JSClass.class) && imports != null && !imports.contains(type)) {
        imports.add(type);
      }
      return type.getSimpleName();
    }

  }

  private static String getCommentName(Type type, Map<Type, Type> genericToType, List<Class> imports) {
    StringBuilder commentName = new StringBuilder();
    if (type instanceof Class)
      commentName.append(getCommentForClass((Class) type, genericToType, imports));
    else {
      // ok we have a tye instead of a class, so we are on our last legs them
      Type realType = genericToType.get(type);
      if (realType instanceof Class)
        commentName.append(((Class) realType).getSimpleName());
      else
        commentName.append(type.getTypeName());

    }
    return commentName.toString();
  }

  /*
    private String generateMethodSignature(Method method) {
      StringBuilder sb = new StringBuilder();
      RequestMapping rm = method.getAnnotation(RequestMapping.class);
      String name = method.getName();
      StringBuilder pb = new StringBuilder();
      Parameter[] params = method.getParameters();
      for (int i = 0; i < method.getParameterCount(); i++) {
        Parameter p = params[i];
        pb.append(p.getName());
        if (i < method.getParameterCount() - 1)
          pb.append(", ");
      }
      sb.append(S).append(S).append("static").append(S).append(name).append("(").append(pb.toString()).append(")").append(BO).append(LB);
      return sb.toString();
    }
  */
  private String generateMethodBody(Method method, Class clazz) {

    if (!clazz.isAnnotationPresent(RestController.class))
      throw new RuntimeException("Class " + clazz.getSimpleName() + " is not RestController.");
    RequestMapping controllerAnnotation = (RequestMapping) clazz.getAnnotation(RequestMapping.class);
    // FUCK THIS SHIT
    // TODO build the path to the controller and then to the method
    String controllerPath = controllerAnnotation.value()[0];

    RequestMapping rm = method.getAnnotation(RequestMapping.class);

    String methodPath = rm.value()[0];

    RequestMethod[] methods = rm.method();

    RequestMethod rMethod = RequestMethod.GET;
    if (methods.length > 0)
      rMethod = methods[0];


    String path = controllerPath + methodPath;
    String pathVar = "const path = '" + path + "';";
    String methodVar = "const method = '" + rMethod.name() + "';";


    StringBuilder body = new StringBuilder();
    body.append(S4).append(pathVar).append(LB)
        .append(S4).append(methodVar).append(LB);


    List<JSMethodParameter> parameters = new LinkedList<>();
    Parameter[] params = method.getParameters();
    Type[] genericParamTypes = method.getGenericParameterTypes();
    Map<Type, Class> genericToStandard = generateGenericToClassMap(clazz);
    for (int i = 0; i < method.getParameterCount(); i++) {
      Parameter parameter = params[i];
      Type p = genericParamTypes[i];
      List<JSObjectType> chain = new LinkedList<>();
      createJSTypeChain(p, clazz, chain, false);
      String paramName = parameter.getName();
      String annotatedName = null;
      if (parameter.isAnnotationPresent(RequestParam.class)) {
        RequestParam rp = parameter.getAnnotation(RequestParam.class);
        annotatedName = rp.value();
      }
      parameters.add(new JSMethodParameter(paramName, annotatedName, chain));
    }

    // now we have all the fucking parameters, or at least I think so, even to the nth fucking type argument


    List<JSObjectType> returnTypeChain = new LinkedList<>();
    createJSTypeChain(method.getGenericReturnType(), clazz, returnTypeChain, false);

    String methodBody = JSControllerCodeGenerator.createMethodBody(returnTypeChain, parameters);
    body.append(methodBody);


      /*System.out.println("Chain:");
      for (JSObjectType s : chain)
        System.out.println("Type: " + s.getName() + ", JSO: " + s.getType().name() + ", isTypeParameter: " + s.isTypeParameter());
      System.out.println(); */

    return body.toString();
  }

  private void createJSTypeChain(Type t, Class clazz, List<JSObjectType> chain, boolean isTypeParameter) {
    if (t == null || t.getTypeName().equals("void"))
      return;
    Map<Type, Class> genericTypeToClass = generateGenericToClassMap(clazz);


    Class actualReturnType = null;
    if (t instanceof ParameterizedType) {

      // we have collection of something, so we

      Class rawType = (Class) ((ParameterizedType) t).getRawType();
      if (Set.class.isAssignableFrom(rawType)) {
        chain.add(new JSObjectType("Set", JSObjectType.JSO.COLLECTION, isTypeParameter));
      }
      else if (Map.class.isAssignableFrom(rawType)) {
        chain.add(new JSObjectType("Map", JSObjectType.JSO.COLLECTION, isTypeParameter));
      }
      else {
        chain.add(new JSObjectType("Array", JSObjectType.JSO.COLLECTION, isTypeParameter));
      }
      // and now we need to do this for all the parameters
      for (Type tt : ((ParameterizedType) t).getActualTypeArguments()) {
        // do this same for all these
        createJSTypeChain(tt, clazz, chain, true);
      }
    }
    else {
      // this is just a type, but it might be a class ar type argument, so we need to see which one this is
      Class rType = null;
      if (t instanceof Class) {
        rType = (Class) t;
      }
      else {
        Class ret = genericTypeToClass.get(t);
        if (ret != null) {
          rType = ret;
        }
        else {
          chain.add(new JSObjectType(t.getTypeName(), JSObjectType.JSO.OBJECT, isTypeParameter));
          return;
        }
      }
      if (rType != null) {
        // we need to see if this is
        chain.add(createPrimitiveJSType(rType, isTypeParameter));
        return;
      }
    }

  }

  private JSObjectType createPrimitiveJSType(Class rType, boolean isTypeParameter) {
    if (Number.class.isAssignableFrom(rType) || numberNames.contains(rType.getSimpleName().toLowerCase())) {
      return new JSObjectType("number", JSObjectType.JSO.PRIMITIVE, isTypeParameter);
    }
    else if (rType.getSimpleName().toLowerCase().equals("string")) {
      return new JSObjectType("string", JSObjectType.JSO.PRIMITIVE, isTypeParameter);
    }
    else if (rType.getSimpleName().toLowerCase().equals("object")) {
      return new JSObjectType("object", JSObjectType.JSO.PRIMITIVE, isTypeParameter);
    }
    else {
      return new JSObjectType(rType.getSimpleName(), JSObjectType.JSO.OBJECT, isTypeParameter);
    }
  }

  /**
   * Defines if method is void
   *
   * @param method to be inspected
   * @return true if method is void, false if not.
   */
  private boolean isMethodVoid(Method method) {
    Type t = method.getGenericReturnType();
    return (t.getTypeName().equals("void"));
  }

  private void privateGenerateJS() throws IOException {
    setup();
    this.tree = new FileTree(packageToScan);
    ClassPathScanningCandidateComponentProvider scanner =
        new ClassPathScanningCandidateComponentProvider(false);

    scanner.addIncludeFilter(new AnnotationTypeFilter(JSClass.class));

    List<Class> classes = new LinkedList<>();

    for (BeanDefinition bd : scanner.findCandidateComponents(packageToScan)) {
      try {
        Class clazz = this.getClass().getClassLoader().loadClass(bd.getBeanClassName());
        tree.addPackage(clazz);
        classes.add(clazz);
      }
      catch (ClassNotFoundException e) {
        log.error("Class " + bd.getBeanClassName() + "could not be loaded", e);
      }

    }
    for (Class c : classes) {
      String path = outputFolder + tree.getPathToWriteFile(c.getSimpleName());
      File jsdir = new File(path);
      if (jsdir.exists()) {
        // do some magic here
      }
      else {
        jsdir.mkdirs();
      }
      File js = new File(path + c.getSimpleName() + ".js");
      String customCode = "";
      if (js.exists()) {
        customCode = this.readCustomCode(js);
      }
      if (customCode.equals(LB))
        customCode = "";
      try (Writer w = new FileWriter(js)) {
        w.write(generate(c, customCode));
      }
      //System.out.println("FILE PATH WILL BE " + outputFolder + tree.getPathToWriteFile(c.getSimpleName()));
      //System.out.println(generate(c, ""));
    }
  }

  @Data
  static class Tuple<K, V> {
    private K k;
    private V v;

    public Tuple(K k, V v) {
      this.k = k;
      this.v = v;
    }
  }

  private Map<String, Tuple<String, String>> generateImportsForClasses(List<Class> classes) {
    Map<String, Tuple<String, String>> imports = new HashMap<>();
    for (Class c : classes) {
      String key = c.getSimpleName();
      String path = c.getPackage().getName().substring(this.packageToScan.length());
      System.out.println(path);
      if (path.equals("")) {
        // do nada
        Tuple<String, String> tuple = new Tuple<>("./" + key, "/" + key + ".js");
        imports.put(key, tuple);
      }
      else {
        // we need to split the shit up
        String[] splts = path.split("[.]");
        String impr = "./";
        String pathToFile = "/";
        for (String s : splts) {
          impr += s + "/";
          pathToFile += s + "/";
        }
        impr += key;
        pathToFile += key + ".js";
        imports.put(key, new Tuple<String, String>(impr, pathToFile));
      }
    }
    return imports;
  }

  private String readCustomCode(File f) throws IOException {
    StringBuilder sb = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new FileReader(f))) {
      String line = br.readLine();

      while (line != null) {
        line = br.readLine();
        if (line != null && line.contains(CUSTOME_CODE_COMMENT_START)) {
          // we start here
          line = br.readLine();
          while (line != null && !line.contains(CUSTOME_CODE_COMMENT_END)) {
            sb.append(line);
            line = br.readLine();
            if (line != null && !line.contains(CUSTOME_CODE_COMMENT_END))
              sb.append(LB);
          }
          break;
        }
      }
    }
    return sb.toString();
  }

  public String generate(Class clazz, String existingCode) {
    Class c = clazz;
    StringBuilder sb = new StringBuilder();
    sb.append("export class ");
    sb.append(c.getSimpleName()).append(" {").append(LB).append(LB);


    Field[] fffs = c.getDeclaredFields();
    List<JSField> ffs = new ArrayList<>(fffs.length);
    for (Field f : fffs) {
      f.setAccessible(true);
      if (f.isAnnotationPresent(JSIgnore.class))
        continue;
      ffs.add(new JSField(f.getName(), f.getType(), f, c.getSimpleName(), tree));
    }
    String start = "  ";
    for (JSField jsf : ffs) {
      sb.append(jsf.toJSString(start)).append(LB);
    }
    sb.append(LB).append(getConstructor(start));
    sb.append(LB).append(getFactoryMethod(start, c.getSimpleName(), ffs));
    //sb.append(LB).append(LB).append("}");

    StringBuilder full = new StringBuilder();
    full.append(LB).append(generateImports(ffs)).append(LB).append(LB).append(sb.toString());

    // TODO append comment, existing code and out comment and then the final "}"

    full.append(LB).append(LB)
        .append(start).append(CUSTOME_CODE_COMMENT_START).append(LB)
        .append(existingCode).append(LB)
        .append(start).append(CUSTOME_CODE_COMMENT_END).append(LB)
        .append(LB).append("}").append(LB);
    return full.toString();
  }


  static String getConstructor(String start) {
    StringBuilder sb = new StringBuilder();
    sb
        .append(start).append("constructor(props) {").append(LB)
        .append(start).append(start).append("Object.assign(this, props);").append(LB)
        .append(start).append("}").append(LB);
    return sb.toString();
  }

  static String getFactoryMethod(String start, String className, List<JSField> fields) {
    StringBuilder sb = new StringBuilder();
    sb
        .append(start).append("fromProps(props)").append(S).append("{").append(LB)
        .append(start).append(start).append("let").append(S).append("instance").append(" = ").append("new").append(S).append(className).append("()").append(C).append(LB)
        .append(start).append(start).append("Object.assign(instance,props)").append(C).append(LB)
        .append(start).append(start).append("return instance").append(C).append(LB)
        .append(start).append("}").append(LB);
    sb.append(TO_JSON).append(LB);
    return sb.toString();
  }

  static String generateImports(List<JSField> ffs) {

    StringBuilder sbb = new StringBuilder();
    for (JSField f : ffs) {
      if (f.needsImport)
        sbb.append(generateImport(f));
    }
    return sbb.toString();
  }

  static String generateImport(JSField ffs) {
    if (ffs.needsImport && ffs.staticImport != null)
      return ffs.staticImport + LB;
    StringBuilder imports = new StringBuilder();
    imports
        .append("import").append(S).append("{").append(ffs.getImportName()).append("}").append(S).append("from").append(S).append("'").append(ffs.getImportPath()).append("'").append(C).append(LB);
    return imports.toString();
  }
}
