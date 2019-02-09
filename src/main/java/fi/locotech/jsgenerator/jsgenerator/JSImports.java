package fi.locotech.jsgenerator.jsgenerator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JSImports {

  private static Map<String, String> imports;

  public static Map<String, String> getImports() {
    if(imports == null) {
      synchronized (JSImports.class) {
        if(imports == null) {
          imports = new HashMap<>();
          for(String[] im : getTheImports()) {
            imports.put(im[0],im[1]);
          }
        }
      }
    }
    return imports;
  }



  private static List<String[]> getTheImports() {
    List<String[]> imports = new LinkedList<>();

    imports.add(new String[]{"moment", "import moment from 'moment';"});

    return imports;
  }
}
