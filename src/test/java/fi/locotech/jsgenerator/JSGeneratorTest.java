package fi.locotech.jsgenerator;


import fi.locotech.jsgenerator.jsgenerator.JSGenerator;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class JSGeneratorTest {

  @Test
  public void generateJS() {

    JSGenerator gen = new JSGenerator("fi.locotech.jsgenerator.example");
    gen.generateJS();
  }
}
