package org.eclipse.imagen.media.codecimpl.util;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;

public class MinMaxSingleBandColorModel extends ColorModel {

  private static final String NOT_IMPLEMENTED = "Not implemented by MinMaxSingleBandColorModel"; //$NON-NLS-1$
  private final double minValue;
  private final double maxValue;
  private final double noDataValue;

  public MinMaxSingleBandColorModel(
      final double minValue,
      final double maxValue,
      final double noData,
      final boolean hasAlpha,
      final ColorSpace colorSpace,
      final int transferType) {
    super(DataBuffer.getDataTypeSize(transferType),
        new int[] {
            DataBuffer.getDataTypeSize(transferType) - 8,
            8 },
        colorSpace,
        hasAlpha,
        hasAlpha,
        Transparency.TRANSLUCENT,
        transferType);

    if (transferType == DataBuffer.TYPE_UNDEFINED) {
      throw new IllegalArgumentException(
          "Unsupported data type value. transferType: " //$NON-NLS-1$
              + transferType);
    }
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.noDataValue = noData;
  }

  /**
   * Throws an <code>IllegalArgumentException</code>, since pixel values for this <code>ColorModel</code> are not
   * conveniently representable as a single <code>int</code>.
   */
  @Override
  public int getRed(final int pixel) {
    throw new IllegalArgumentException(NOT_IMPLEMENTED);
  }

  /**
   * Throws an <code>IllegalArgumentException</code>, since pixel values for this <code>ColorModel</code> are not
   * conveniently representable as a single <code>int</code>.
   */
  @Override
  public int getGreen(final int pixel) {
    throw new IllegalArgumentException(NOT_IMPLEMENTED);
  }

  /**
   * Throws an <code>IllegalArgumentException</code>, since pixel values for this <code>ColorModel</code> are not
   * conveniently representable as a single <code>int</code>.
   */
  @Override
  public int getBlue(final int pixel) {
    throw new IllegalArgumentException(NOT_IMPLEMENTED);
  }

  /**
   * Throws an <code>IllegalArgumentException</code>, since pixel values for this <code>ColorModel</code> are not
   * conveniently representable as a single <code>int</code>.
   */
  @Override
  public int getAlpha(final int pixel) {
    throw new IllegalArgumentException(NOT_IMPLEMENTED);
  }

  /**
   * Throws an <code>IllegalArgumentException</code>, since pixel values for this <code>ColorModel</code> are not
   * conveniently representable as a single <code>int</code>.
   */
  @Override
  public int getRGB(final int pixel) {
    throw new IllegalArgumentException(NOT_IMPLEMENTED);
  }

  private final int clamp(final double value) {
    // Ensure NaN maps to 0
    return (value >= 0.0) ? ((value > 255.0) ? 255 : (int) value) : 0;
  }

  private final int convert(final double value) {
    if (value == this.noDataValue) {
      return 0;
    }
    return clamp(((value - this.minValue) / (this.maxValue - this.minValue) * 255));
  }

  private int getSample(final Object inData) {
    return convert(getValue(inData));
  }

  private double getValue(final Object inData) {
    if (this.transferType == DataBuffer.TYPE_BYTE) {
      final byte[] fdata = (byte[]) inData;
      final int i = fdata[0];
      return i;
    }
    if (this.transferType == DataBuffer.TYPE_INT) {
      final int[] fdata = (int[]) inData;
      final int i = fdata[0];
      return i;
    }
    if (this.transferType == DataBuffer.TYPE_USHORT || this.transferType == DataBuffer.TYPE_SHORT) {
      final short[] fdata = (short[]) inData;
      final short s = fdata[0];
      return s;
    }
    if (this.transferType == DataBuffer.TYPE_FLOAT) {
      final float[] fdata = (float[]) inData;
      return fdata[0];
    }
    final double[] ddata = (double[]) inData;
    return ddata[0];
  }

  /**
   * Returns the red color component for the specified pixel, scaled from 0 to 255 in the default RGB
   * <code>ColorSpace</code>, sRGB. A color conversion is done if necessary. The <code>pixel</code> value is specified
   * by an array of data elements of type <code>transferType</code> passed in as an object reference. The returned value
   * will be a non pre-multiplied value. If the alpha is premultiplied, this method divides it out before returning the
   * value (if the alpha value is 0, the red value will be 0).
   *
   * @param inData The pixel from which to get the red color component, specified by an array of data elements of type
   *               <code>transferType</code>.
   *
   * @return The red color component for the specified pixel, as an int.
   *
   * @throws ClassCastException             If <code>inData</code> is not a primitive array of type
   *                                        <code>transferType</code>.
   * @throws ArrayIndexOutOfBoundsException if <code>inData</code> is not large enough to hold a pixel value for this
   *                                        <code>ColorModel</code>.
   */
  @Override
  public int getRed(final Object inData) {
    return getSample(inData);
  }

  /**
   * Returns the green color component for the specified pixel, scaled from 0 to 255 in the default RGB
   * <code>ColorSpace</code>, sRGB. A color conversion is done if necessary. The <code>pixel</code> value is specified
   * by an array of data elements of type <code>transferType</code> passed in as an object reference. The returned value
   * will be a non pre-multiplied value. If the alpha is premultiplied, this method divides it out before returning the
   * value (if the alpha value is 0, the green value will be 0).
   *
   * @param inData The pixel from which to get the green color component, specified by an array of data elements of type
   *               <code>transferType</code>.
   *
   * @return The green color component for the specified pixel, as an int.
   *
   * @throws ClassCastException             If <code>inData</code> is not a primitive array of type
   *                                        <code>transferType</code>.
   * @throws ArrayIndexOutOfBoundsException if <code>inData</code> is not large enough to hold a pixel value for this
   *                                        <code>ColorModel</code>.
   */
  @Override
  public int getGreen(final Object inData) {
    return getSample(inData);
  }

  /**
   * Returns the blue color component for the specified pixel, scaled from 0 to 255 in the default RGB
   * <code>ColorSpace</code>, sRGB. A color conversion is done if necessary. The <code>pixel</code> value is specified
   * by an array of data elements of type <code>transferType</code> passed in as an object reference. The returned value
   * will be a non pre-multiplied value. If the alpha is premultiplied, this method divides it out before returning the
   * value (if the alpha value is 0, the blue value will be 0).
   *
   * @param inData The pixel from which to get the blue color component, specified by an array of data elements of type
   *               <code>transferType</code>.
   *
   * @return The blue color component for the specified pixel, as an int.
   *
   * @throws ClassCastException             If <code>inData</code> is not a primitive array of type
   *                                        <code>transferType</code>.
   * @throws ArrayIndexOutOfBoundsException if <code>inData</code> is not large enough to hold a pixel value for this
   *                                        <code>ColorModel</code>.
   */
  @Override
  public int getBlue(final Object inData) {
    return getSample(inData);
  }

  @Override
  public int getTransparency() {
    return super.getTransparency();
  }

  @Override
  public int getAlpha(final Object inData) {
    return alpha(getValue(inData));
  }

  /**
   * Returns the color/alpha components for the specified pixel in the default RGB color model format. A color
   * conversion is done if necessary. The pixel value is specified by an array of data elements of type
   * <code>transferType</code> passed in as an object reference. The returned value is in a non pre-multiplied format.
   * If the alpha is premultiplied, this method divides it out of the color components (if the alpha value is 0, the
   * color values will be 0).
   *
   * @param inData The pixel from which to get the color/alpha components, specified by an array of data elements of
   *               type <code>transferType</code>.
   *
   * @return The color/alpha components for the specified pixel, as an int.
   *
   * @throws ClassCastException             If <code>inData</code> is not a primitive array of type
   *                                        <code>transferType</code>.
   * @throws ArrayIndexOutOfBoundsException if <code>inData</code> is not large enough to hold a pixel value for this
   *                                        <code>ColorModel</code>.
   */
  @Override
  public int getRGB(final Object inData) {

    final double value = getValue(inData);
    final int alpha = alpha(value);
    final int valueResult = convert(value);

    return (alpha << 24) | (valueResult << 16) | (valueResult << 8) | valueResult;
  }

  protected int alpha(final double value) {
    if (value < this.minValue) {
      return 0;
    }
    if (value > this.maxValue) {
      return 0;
    }
    if (value == this.noDataValue) {
      return 0;
    }
    return 255;
  }

  /**
   * Returns a data element array representation of a pixel in this <code>ColorModel</code>, given an integer pixel
   * representation in the default RGB color model. This array can then be passed to the <code>setDataElements</code>
   * method of a <code>WritableRaster</code> object. If the <code>pixel</code> parameter is null, a new array is
   * allocated. If the colorSpaceType is of TYPE_GRAY then the rgb components are converted to gray using appropriate
   * weights
   *
   * @param rgb   An ARGB value packed into an int.
   * @param pixel The float or double array representation of the pixel.
   *
   * @throws ClassCastException             If <code>pixel</code> is not null and is not a primitive array of type
   *                                        <code>transferType</code>.
   *
   * @throws ArrayIndexOutOfBoundsException If <code>pixel</code> is not large enough to hold a pixel value for this
   *                                        <code>ColorModel</code>.
   */
  @Override
  public Object getDataElements(final int rgb, final Object pixel) {
    throw new IllegalArgumentException(NOT_IMPLEMENTED);
  }

  /**
   * Throws an <code>IllegalArgumentException</code>, since pixel values for this <code>ColorModel</code> are not
   * conveniently representable as a single <code>int</code>.
   */
  @Override
  public int[] getComponents(final int pixel, final int[] components, final int offset) {
    throw new IllegalArgumentException(NOT_IMPLEMENTED);
  }

  /**
   * Throws an <code>IllegalArgumentException</code> since the pixel values cannot be placed into an <code>int</code>
   * array.
   */
  @Override
  public int[] getComponents(final Object pixel, final int[] components, final int offset) {
    throw new IllegalArgumentException(NOT_IMPLEMENTED);
  }

  /**
   * Throws an <code>IllegalArgumentException</code>, since pixel values for this <code>ColorModel</code> are not
   * conveniently representable as a single <code>int</code>.
   */
  @Override
  public int getDataElement(final int[] components, final int offset) {
    throw new IllegalArgumentException(NOT_IMPLEMENTED);
  }

  /**
   * Returns a data element array representation of a pixel in this <code>ColorModel</code>, given an array of
   * unnormalized color/alpha components. This array can then be passed to the <code>setDataElements</code> method of a
   * <code>WritableRaster</code> object.
   *
   * @param components An array of unnormalized color/alpha components.
   * @param offset     The integer offset into the <code>components</code> array.
   * @param obj        The object in which to store the data element array representation of the pixel. If
   *                   <code>obj</code> variable is null, a new array is allocated. If <code>obj</code> is not null, it
   *                   must be a primitive array of type <code>transferType</code>. An
   *                   <code>ArrayIndexOutOfBoundsException</code> is thrown if <code>obj</code> is not large enough to
   *                   hold a pixel value for this <code>ColorModel</code>.
   *
   * @return The data element array representation of a pixel in this <code>ColorModel</code>.
   *
   * @throws IllegalArgumentException       If the components array is not large enough to hold all the color and alpha
   *                                        components (starting at offset).
   * @throws ClassCastException             If <code>obj</code> is not null and is not a primitive array of type
   *                                        <code>transferType</code>.
   * @throws ArrayIndexOutOfBoundsException If <code>obj</code> is not large enough to hold a pixel value for this
   *                                        <code>ColorModel</code>.
   */
  @Override
  public Object getDataElements(final int[] components, final int offset, final Object obj) {
    throw new IllegalArgumentException(NOT_IMPLEMENTED);
  }

  /**
   * Checks whether or not the specified <CODE>SampleModel</CODE> is compatible with this <CODE>ColorModel</CODE>.
   *
   * @param sm The <CODE>SampleModel</CODE> to test for compatibility.
   *
   * @return <CODE>true</CODE> if the <CODE>SampleModel</CODE> is compatible with this <CODE>ColorModel</CODE>,
   *         <CODE>false</CODE> if it is not.
   *
   * @see SampleModel
   */
  @Override
  public boolean isCompatibleSampleModel(final SampleModel sm) {
    if (!(sm instanceof ComponentSampleModel)) {
      return false;
    }

    // Must have one component
    if (1 != sm.getNumBands()) {
      return false;
    }

    if (sm.getTransferType() != this.transferType) {
      return false;
    }

    return true;
  }

  /**
   * Returns true if <CODE>raster</CODE> is compatible with this <CODE>ColorModel</CODE>; false if it is not.
   *
   * @param raster The <CODE>Raster</CODE> object to test for compatibility.
   *
   * @return <CODE>true</CODE> if <CODE>raster</CODE> is compatible with this <CODE>ColorModel</CODE>,
   *         <CODE>false</CODE> if it is not.
   */
  @Override
  public boolean isCompatibleRaster(final Raster raster) {

    final SampleModel sm = raster.getSampleModel();

    if (sm instanceof ComponentSampleModel) {
      if (sm.getNumBands() != 1) {
        return false;
      }
      if (sm.getSampleSize(0) != DataBuffer.getDataTypeSize(this.transferType)) {
        return false;
      }
      return (raster.getTransferType() == this.transferType);
    }
    return false;
  }

  public double getMinValue() {
    return this.minValue;
  }

  public double getMaxValue() {
    return this.maxValue;
  }

  public double getNoDataValue() {
    return this.noDataValue;
  }
}
