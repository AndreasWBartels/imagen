package org.eclipse.imagen.media.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class PropertyUtilTest {

  @Test
  public void test() {
    String string = PropertyUtil.getString("org.eclipse.imagen.media.util", "Generic0");
    assertNotNull(string);
    assertEquals(string, "The input argument(s) may not be null.");
  }

}
