package org.eclipse.imagen.media.codecimpl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.color.ColorSpace;
import java.awt.image.DataBuffer;

import org.eclipse.imagen.media.codecimpl.util.MinMaxSingleBandColorModel;
import org.junit.jupiter.api.Test;

public class MinMaxSingleBandColorModelTest {

  @Test
  public void testFloat() {
    final MinMaxSingleBandColorModel colorModel = new MinMaxSingleBandColorModel(
        0,
        100,
        -1,
        true,
        ColorSpace.getInstance(ColorSpace.CS_GRAY),
        DataBuffer.TYPE_FLOAT);

    test(colorModel, 255, 0, new float[] { 0 });
    test(colorModel, 255, 63, new float[] { 25 });
    test(colorModel, 255, 127, new float[] { 50 });
    test(colorModel, 255, 255, new float[] { 100 });

    test(colorModel, 0, 0, new float[] { -1 });

    test(colorModel, 0, 0, new float[] { -16 });
    test(colorModel, 0, 255, new float[] { 103 });
  }

  @Test
  public void testDouble() {
    final MinMaxSingleBandColorModel colorModel = new MinMaxSingleBandColorModel(
        0,
        100,
        -1,
        true,
        ColorSpace.getInstance(ColorSpace.CS_GRAY),
        DataBuffer.TYPE_DOUBLE);

    test(colorModel, 255, 0, new double[] { 0 });
    test(colorModel, 255, 63, new double[] { 25 });
    test(colorModel, 255, 127, new double[] { 50 });
    test(colorModel, 255, 255, new double[] { 100 });

    test(colorModel, 0, 0, new double[] { -1 });

    test(colorModel, 0, 0, new double[] { -16 });
    test(colorModel, 0, 255, new double[] { 103 });
  }

  @Test
  public void testRange() {
    final MinMaxSingleBandColorModel colorModel = new MinMaxSingleBandColorModel(
        -255,
        255,
        -999,
        true,
        ColorSpace.getInstance(ColorSpace.CS_GRAY),
        DataBuffer.TYPE_FLOAT);

    test(colorModel, 255, 0, new float[] { -255 });
    test(colorModel, 255, 64, new float[] { -127 });
    test(colorModel, 255, 127, new float[] { 0 });
    test(colorModel, 255, 191, new float[] { 127 });
    test(colorModel, 255, 255, new float[] { 255 });

    test(colorModel, 0, 0, new float[] { -999 });
  }

  private void test(
      final MinMaxSingleBandColorModel colorModel,
      final int expectedAlpha,
      final int expectedColor,
      final Object value) {
    assertEquals(expectedAlpha, colorModel.getAlpha(value));
    assertEquals(expectedColor, colorModel.getRed(value));
    assertEquals(expectedColor, colorModel.getGreen(value));
    assertEquals(expectedColor, colorModel.getBlue(value));

    final int rgb = colorModel.getRGB(value);
    assertEquals(expectedColor, rgb & 0x000000FF);
    assertEquals(expectedColor, (rgb >> 8) & 0x000000FF);
    assertEquals(expectedColor, (rgb >> 16) & 0x000000FF);
    assertEquals(expectedAlpha, (rgb >> 24) & 0x000000FF);
  }
}
