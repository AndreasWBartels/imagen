package org.eclipse.imagen.media.codecimpl.util;

import java.awt.color.ColorSpace;

public class PseudoColorSpace extends ColorSpace {

  private static final long serialVersionUID = 1L;

  private static int getType(final int numComponents) {
    if (numComponents < 1) {
      throw new IllegalArgumentException("numComponents < 1!");
    }
    switch (numComponents) {
      case 1: {
        return ColorSpace.TYPE_GRAY;
      }
      default: {
        return numComponents + 10;
      }
    }
  }

  public PseudoColorSpace(final int numComponents) {
    super(getType(numComponents), numComponents);
  }

  private float[] copyTo(final float[] source, final float[] target, final int size) {
    System.arraycopy(source, 0, target, 0, Math.min(3, size));
    return target;
  }

  @Override
  public float[] toRGB(final float[] colorvalue) {
    if (colorvalue.length < getNumComponents()) {
      throw new ArrayIndexOutOfBoundsException("colorvalue.length < getNumComponents()");
    }
    return copyTo(colorvalue, new float[3], getNumComponents());
  }

  @Override
  public float[] fromRGB(final float[] rgbvalue) {
    if (rgbvalue.length < 3) {
      throw new ArrayIndexOutOfBoundsException("rgbvalue.length < 3");
    }
    return copyTo(rgbvalue, new float[getNumComponents()], getNumComponents());
  }

  @Override
  public float[] toCIEXYZ(final float[] colorvalue) {
    if (colorvalue.length < getNumComponents()) {
      throw new ArrayIndexOutOfBoundsException("colorvalue.length < getNumComponents()");
    }
    return copyTo(colorvalue, new float[3], getNumComponents());
  }

  @Override
  public float[] fromCIEXYZ(final float[] xyzvalue) {
    if (xyzvalue.length < 3) {
      throw new ArrayIndexOutOfBoundsException("xyzvalue.length < 3");
    }
    return copyTo(xyzvalue, new float[getNumComponents()], getNumComponents());
  }
}
