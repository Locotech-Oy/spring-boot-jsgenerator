package fi.locotech.jsgenerator.jsgenerator;

import java.util.List;

public class JSControllerCodeGenerator {

  private static final String LB = "\r\n";
  private static final String S = " ";
  private static final String BO = "{";
  private static final String BC = "}";
  private static final String ADD_TO_ARRAY = "push";
  private static final String ADD_TO_SET = "add";
  private static final String ADD_TO_MAP = "set";

  private static final String RETURN_OBJECT_COLLECTION_REQUEST =
      "    let request = new Request(method, $$$PARAMS, path);\r\n" +
      "    return new Promise((resolve, reject) => {\r\n" +
      "      request.doRequest().then((res) => {\r\n" +
      "        let data = new $$$COLLECTION();\r\n" +
      "        for(let row of res.body) {\r\n" +
      "          data.$$$ADD_METHOD(new $$$OBJECT(row));\r\n" +
      "        }\r\n" +
      "        resolve(data);\r\n" +
      "      }).catch((err) => {\r\n" +
      "        reject(err);\r\n" +
      "      })\r\n" +
      "    });";

  private static final String RETURN_PRIMITIVE_COLLECTION_REQUEST =
      "    let request = new Request(method, $$$PARAMS, path);\r\n" +
          "    return new Promise((resolve, reject) => {\r\n" +
          "      request.doRequest().then((res) => {\r\n" +
          "        let data = new $$$COLLECTION();\r\n" +
          "        for(let row of res.body) {\r\n" +
          "          data.$$$ADD_METHOD(row);\r\n" +
          "        }\r\n" +
          "        resolve(data);\r\n" +
          "      }).catch((err) => {\r\n" +
          "        reject(err);\r\n" +
          "      })\r\n" +
          "    });";

  private static final String RETURN_OBJECT_REQUEST =
      "    let request = new Request(method, $$$PARAMS, path);\r\n" +
      "    return new Promise((resolve, reject) => {\r\n" +
      "      request.doRequest().then((res) => {\r\n" +
      "        if(res.body == null)\r\n" +
      "          resolve(null);\r\n" +
      "        else\r\n" +
      "          resolve(new $$$OBJECT(res.body));\r\n" +
      "      }).catch((err) => {\r\n" +
      "        reject(err);\r\n" +
      "      })\r\n" +
      "    });";

  private static final String RETURN_PRIMITIVE_REQUEST =
      "    let request = new Request(method, $$$PARAMS, path);\r\n" +
          "    return new Promise((resolve, reject) => {\r\n" +
          "      request.doRequest().then((res) => {\r\n" +
          "        resolve(res.body);\r\n" +
          "      }).catch((err) => {\r\n" +
          "        reject(err);\r\n" +
          "      })\r\n" +
          "    });";

  private static final String VOID_REQUEST =
      "    let request = new Request(method, $$$PARAMS, path);\r\n" +
      "    return new Promise((resolve, reject) => {\r\n" +
      "      request.doRequest().then((res) => {\r\n" +
      "        resolve(true);\r\n" +
      "      }).catch((err) => {\r\n" +
      "        reject(err);\r\n" +
      "      })\r\n" +
      "    });";


  public static String createMethodBody(List<JSObjectType> returnChain, List<JSMethodParameter> parameters) {
    String param = generateParamObject(parameters);
    if(returnChain.size() == 0) {
      // we have void method
      return VOID_REQUEST.replace("$$$PARAMS", param);
    } else if (returnChain.get(0).isCollection()) {
      String addMethod = null;
      if(returnChain.get(0).getName().equals("Array"))
        addMethod = ADD_TO_ARRAY;
      else if (returnChain.get(0).getName().equals("Set"))
        addMethod = ADD_TO_SET;

      if(returnChain.get(1).getType() == JSObjectType.JSO.OBJECT) {
        // we have collection return
        // TODO THIS WILL WORK WITH SET AND ARRAY BUT NOT WITH MAP, THIS NEEDS TO BE EXTRAPOLATED FOR IT
        // TODO THIS DOES NOT WORK WITH INNERTYPES, THAT NEEDS MORE WORK STILL

        return RETURN_OBJECT_COLLECTION_REQUEST
            .replace("$$$PARAMS", param)
            .replace("$$$COLLECTION", returnChain.get(0).getName())
            .replace("$$$ADD_METHOD", addMethod)
            .replace("$$$OBJECT", returnChain.get(1).getName());
      } else if (returnChain.get(1).getType() == JSObjectType.JSO.PRIMITIVE) {

          // we have collection return
          // TODO THIS WILL WORK WITH SET AND ARRAY BUT NOT WITH MAP, THIS NEEDS TO BE EXTRAPOLATED FOR IT
          // TODO THIS DOES NOT WORK WITH INNERTYPES, THAT NEEDS MORE WORK STILL
          return RETURN_PRIMITIVE_COLLECTION_REQUEST
              .replace("$$$PARAMS", param)
              .replace("$$$COLLECTION", returnChain.get(0).getName())
              .replace("$$$ADD_METHOD", addMethod);

      } else {
        throw new RuntimeException("Unsupported type");
      }
    } else {
      if(returnChain.get(0).getType() == JSObjectType.JSO.PRIMITIVE) {
        // we have primitive return
        return RETURN_PRIMITIVE_REQUEST.replace("$$$PARAMS", param);
      }
      else {
        // we have object return
        return RETURN_OBJECT_REQUEST.replace("$$$PARAMS", param).replace("$$$OBJECT", returnChain.get(0).getName());
      }
    }
  }

  private static final String generateParamObject(List<JSMethodParameter> params) {
    if(params.size() == 0)
      return "null";
    if(params.size() == 1 && params.get(0).getAnnotatedName() == null) {
      // we have just simple parameter object
      return params.get(0).getParameterName();
    }
    StringBuilder paramBuilder = new StringBuilder();
    paramBuilder.append(BO);
    int i = 0;
    for (JSMethodParameter param : params) {
      paramBuilder.append(param.getAnnotatedName()).append(":").append(param.getParameterName());
      if(++i < params.size())
        paramBuilder.append(", ");
    }
    paramBuilder.append(BC);
    return paramBuilder.toString();
  }


}
