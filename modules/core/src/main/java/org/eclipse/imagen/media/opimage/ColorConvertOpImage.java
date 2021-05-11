/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.eclipse.imagen.media.opimage;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.imagen.ColorSpaceJAI;
import org.eclipse.imagen.ImageLayout;
import org.eclipse.imagen.PointOpImage;
import org.eclipse.imagen.RasterFactory;

/**
 * An <code>OpImage</code> implementing the "ColorConvert" operation as described in
 * <code>org.eclipse.imagen.operator.ColorConvertDescriptor</code>.
 *
 * @since EA4
 *
 * @see org.eclipse.imagen.PointOpImage
 * @see org.eclipse.imagen.operator.ColorConvertDescriptor
 *
 */
final class ColorConvertOpImage extends PointOpImage {
  /** Cache a rgb color space */
  private static final ColorSpace rgbColorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);

  private static SoftReference softRef = null;

  /** The source image parameters */
  private ImageParameters srcParam = null;

  /** The source image parameters */
  private ImageParameters dstParam = null;

  /** The intermediate image parameters */
  private ImageParameters tempParam = null;

  /** The Java 2D ColorConvertOp instance for converting integer type */
  private ColorConvertOp colorConvertOp = null;

  /** case number */
  private int caseNumber;

  /**
   * Retrive/cache the ColorConvertOp. Because instantiate a ColorConvertOp is a time-consuming step, create a hashtable
   * referred to by a SoftReference to cache the ColorConvertOp for using repeatedly.
   *
   * @param src the color space of the source image dst the color space of the destination image
   * @return The ColorConvertOp to convert from the source color space to the destination color space.
   */
  private static synchronized ColorConvertOp
      getColorConvertOp(final ColorSpace src, final ColorSpace dst) {
    HashMap colorConvertOpBuf = null;

    if (softRef == null ||
        ((colorConvertOpBuf = (HashMap) softRef.get()) == null)) {

      colorConvertOpBuf = new HashMap();
      softRef = new SoftReference(colorConvertOpBuf);
    }

    ArrayList hashcode = new ArrayList(2);
    hashcode.add(0, src);
    hashcode.add(1, dst);
    ColorConvertOp op = (ColorConvertOp) colorConvertOpBuf.get(hashcode);

    if (op == null) {
      op = new ColorConvertOp(src, dst, null);
      colorConvertOpBuf.put(hashcode, op);
    }

    return op;
  }

  /**
   * Retrieve the minimum value of a data type.
   *
   * @param dataType The data type as in DataBuffer.TYPE_*.
   * @return The minimum value of the specified data type.
   */
  private static float getMinValue(final int dataType) {
    float minValue = 0;
    switch (dataType) {
      case DataBuffer.TYPE_BYTE:
        minValue = 0;
        break;
      case DataBuffer.TYPE_SHORT:
        minValue = Short.MIN_VALUE;
        break;
      case DataBuffer.TYPE_USHORT:
        minValue = 0;
        break;
      case DataBuffer.TYPE_INT:
        minValue = Integer.MIN_VALUE;
        break;
      default:
        minValue = 0;
    }

    return minValue;
  }

  /**
   * Retrieve the range of a data type.
   *
   * @param dataType The data type as in DataBuffer.TYPE_*.
   * @return The range of the specified data type.
   */
  private static float getRange(final int dataType) {
    float range = 1;
    switch (dataType) {
      case DataBuffer.TYPE_BYTE:
        range = 255;
        break;
      case DataBuffer.TYPE_SHORT:
        range = Short.MAX_VALUE - Short.MIN_VALUE;
        break;
      case DataBuffer.TYPE_USHORT:
        range = Short.MAX_VALUE - Short.MIN_VALUE;
        break;
      case DataBuffer.TYPE_INT:
        range = Integer.MAX_VALUE - (long) Integer.MIN_VALUE;
        break;
      default:
        range = 1;
    }

    return range;
  }

  /**
   * Constructor.
   *
   * @param source     The source image.
   * @param config     Configurable attributes of the image including configuration variables indexed by
   *                   <code>RenderingHints.Key</code>s and image properties indexed by <code>String</code>s or
   *                   <code>CaselessStringKey</code>s. This is simply forwarded to the superclass constructor.
   * @param layout     The destination image layout.
   * @param colorModel The destination color model.
   */
  public ColorConvertOpImage(final RenderedImage source,
      final Map config,
      final ImageLayout layout,
      final ColorModel colorModel) {
    super(source, layout, config, true);
    this.colorModel = colorModel;

    // Cache the ColorModels.
    this.srcParam = new ImageParameters(source.getColorModel(),
        source.getSampleModel());
    this.dstParam = new ImageParameters(colorModel, this.sampleModel);

    ColorSpace srcColorSpace = this.srcParam.getColorModel().getColorSpace();
    ColorSpace dstColorSpace = this.dstParam.getColorModel().getColorSpace();

    // for each case, define the case number; create tempParam
    // and/or ColorConvertOp if necessary
    if (srcColorSpace instanceof ColorSpaceJAI &&
        dstColorSpace instanceof ColorSpaceJAI) {

      // when both are ColorSpaceJAI, convert via RGB
      this.caseNumber = 1;
      this.tempParam = createTempParam();
    } else if (srcColorSpace instanceof ColorSpaceJAI) {

      // when source is ColorSpaceJAI, 1. convert via RGB if
      // the dest isn't RGB; 2. convert to RGB
      if (dstColorSpace != rgbColorSpace) {
        this.caseNumber = 2;
        this.tempParam = createTempParam();
        this.colorConvertOp = getColorConvertOp(rgbColorSpace,
            dstColorSpace);
      } else {
        this.caseNumber = 3;
      }
    } else if (dstColorSpace instanceof ColorSpaceJAI) {

      // when destination is ColorSpaceJAI, 1. convert via RGB if
      // source isn't RGB; 2. convert from RGB
      if (srcColorSpace != rgbColorSpace) {
        this.caseNumber = 4;
        this.tempParam = createTempParam();
        this.colorConvertOp = getColorConvertOp(srcColorSpace,
            rgbColorSpace);
      } else {
        this.caseNumber = 5;
      }
    } else {

      // if all the color space are not ColorSpaceJAI
      this.caseNumber = 6;
      this.colorConvertOp = getColorConvertOp(srcColorSpace, dstColorSpace);
    }

    // Set flag to permit in-place operation.
    permitInPlaceOperation();
  }

  /**
   * Computes a tile of the destination image in the destination color space.
   *
   * @param sources  Cobbled sources, guaranteed to provide all the source data necessary for computing the rectangle.
   * @param dest     The tile containing the rectangle to be computed.
   * @param destRect The rectangle within the tile to be computed.
   */
  @Override
  protected void computeRect(final Raster[] sources,
      final WritableRaster dest,
      final Rectangle destRect) {
    WritableRaster tempRas = null;

    // Save a reference to the source Raster.
    Raster source = sources[0];

    // Ensure the source Raster has the same bounds as the destination.
    if (!destRect.equals(source.getBounds())) {
      source = source.createChild(destRect.x,
          destRect.y,
          destRect.width,
          destRect.height,
          destRect.x,
          destRect.y,
          null);
    }

    switch (this.caseNumber) {
      // 1. When source and destination color spaces are all ColorSpaceJAI,
      // convert via RGB color space
      case 1:
        tempRas = computeRectColorSpaceJAIToRGB(source,
            this.srcParam,
            null,
            this.tempParam);
        computeRectColorSpaceJAIFromRGB(tempRas,
            this.tempParam,
            dest,
            this.dstParam);
        break;
      // when only the source color space is ColorSpaceJAI,
      // 2. if the destination is not RGB, convert to RGB using
      // ColorSpaceJAI; then convert RGB to the destination
      // 3. if the destination is RGB, convert using ColorSpaceJAI
      case 2:
        tempRas = computeRectColorSpaceJAIToRGB(source,
            this.srcParam,
            null,
            this.tempParam);
        computeRectNonColorSpaceJAI(tempRas,
            this.tempParam,
            dest,
            this.dstParam,
            destRect);
        break;
      case 3:
        computeRectColorSpaceJAIToRGB(source,
            this.srcParam,
            dest,
            this.dstParam);
        break;
      // 4, 5. When only the destination color space is ColorSpaceJAI,
      // similar to the case above.
      case 4:
        tempRas = createTempWritableRaster(source);
        computeRectNonColorSpaceJAI(source,
            this.srcParam,
            tempRas,
            this.tempParam,
            destRect);
        computeRectColorSpaceJAIFromRGB(tempRas,
            this.tempParam,
            dest,
            this.dstParam);
        break;
      case 5:
        computeRectColorSpaceJAIFromRGB(source,
            this.srcParam,
            dest,
            this.dstParam);
        break;
      // 6. If all the color space are not ColorSpaceJAI
      case 6:
        computeRectNonColorSpaceJAI(source,
            this.srcParam,
            dest,
            this.dstParam,
            destRect);
      default:
        break;
    }
  }

  // when the source color space is ColorSpaceJAI, convert it to RGB.
  // 1. If the source data type is short/int, shift the data to [0,
  // MAX-MIN]
  // 2. Convert to RGB.
  // 3. Shift back to [MIN, MAX]
  private WritableRaster computeRectColorSpaceJAIToRGB(Raster src,
      final ImageParameters srcParam,
      WritableRaster dest,
      final ImageParameters dstParam) {
    src = convertRasterToUnsigned(src);

    ColorSpaceJAI colorSpaceJAI = (ColorSpaceJAI) srcParam.getColorModel().getColorSpace();
    dest = colorSpaceJAI.toRGB(src,
        srcParam.getComponentSize(),
        dest,
        dstParam.getComponentSize());

    dest = convertRasterToSigned(dest);
    return dest;
  }

  // when the source color space is ColorSpaceJAI, convert it from RGB.
  // 1. If the source data type is short/int, shift the data to [0,
  // MAX-MIN]
  // 2. Convert from RGB.
  // 3. Shift back to [MIN, MAX]
  private WritableRaster computeRectColorSpaceJAIFromRGB(Raster src,
      final ImageParameters srcParam,
      WritableRaster dest,
      final ImageParameters dstParam) {
    src = convertRasterToUnsigned(src);
    ColorSpaceJAI colorSpaceJAI = (ColorSpaceJAI) dstParam.getColorModel().getColorSpace();
    dest = colorSpaceJAI.fromRGB(src,
        srcParam.getComponentSize(),
        dest,
        dstParam.getComponentSize());

    dest = convertRasterToSigned(dest);
    return dest;
  }

  // When the source and destination color spaces are not ColorSpaceJAI,
  // convert using ColorConvertOp of Java 2D for integer type. For the
  // floating point, use the following method.
  private void computeRectNonColorSpaceJAI(final Raster src,
      final ImageParameters srcParam,
      final WritableRaster dest,
      final ImageParameters dstParam,
      final Rectangle destRect) {
    if (!srcParam.isFloat() && !dstParam.isFloat()) {
      // Create a ColorConvertOp if there are only integral data.
      // Integral type: use the ColorConvertOp.

      // Ensure that the Rasters are the same size as apparently
      // required by ColorConvertOp although not so documented.
      Raster s = src;
      if (s.getMinX() != destRect.x ||
          s.getMinY() != destRect.y ||
          s.getWidth() != destRect.width ||
          s.getHeight() != destRect.height) {
        s = s.createChild(destRect.x,
            destRect.y,
            destRect.width,
            destRect.height,
            destRect.x,
            destRect.y,
            null);
      }
      WritableRaster d = dest;
      if (d.getMinX() != destRect.x ||
          d.getMinY() != destRect.y ||
          d.getWidth() != destRect.width ||
          d.getHeight() != destRect.height) {
        d = d.createWritableChild(destRect.x,
            destRect.y,
            destRect.width,
            destRect.height,
            destRect.x,
            destRect.y,
            null);
      }

      // Perform the color conversion on the (possible child) Rasters.
      synchronized (this.colorConvertOp.getClass()) {
        // Lock on the class to prevent crash in non-re-entrant
        // native code on MP systems (jai-core issue 21).
        this.colorConvertOp.filter(s, d);
      }
    } else {
      // For the floating point data types, convert via CIEXYZ color space.
      // Do it pixel-by-pixel (slow!).
      ColorSpace srcColorSpace = srcParam.getColorModel().getColorSpace();
      ColorSpace dstColorSpace = dstParam.getColorModel().getColorSpace();
      boolean srcFloat = srcParam.isFloat();
      float srcMinValue = srcParam.getMinValue();
      float srcRange = srcParam.getRange();

      boolean dstFloat = dstParam.isFloat();
      float dstMinValue = dstParam.getMinValue();
      float dstRange = dstParam.getRange();

      int rectYMax = destRect.y + destRect.height;
      int rectXMax = destRect.x + destRect.width;
      int numComponents = srcColorSpace.getNumComponents();
      float[] srcPixel = new float[numComponents];
      float[] xyzPixel;
      float[] dstPixel;
      for (int y = destRect.y; y < rectYMax; y++) {
        for (int x = destRect.x; x < rectXMax; x++) {
          srcPixel = src.getPixel(x, y, srcPixel);
          if (!srcFloat) {
            // Normalize the source samples.
            for (int i = 0; i < numComponents; i++) {
              srcPixel[i] = (srcPixel[i] - srcMinValue) / srcRange;
            }
          }

          // Convert src to dst via CIEXYZ.
          xyzPixel = srcColorSpace.toCIEXYZ(srcPixel);
          dstPixel = dstColorSpace.fromCIEXYZ(xyzPixel);

          if (!dstFloat) {
            // Scale the destination samples.
            for (int i = 0; i < numComponents; i++) {
              dstPixel[i] = (dstPixel[i] * dstRange + dstMinValue);
            }
          }
          dest.setPixel(x, y, dstPixel);
        }
      }
    }
  }

  // Back up the destination parameters. Set the destination to the
  // bridge color space RGB.
  private ImageParameters createTempParam() {
    ColorModel cm = null;
    SampleModel sm = null;

    if (this.srcParam.getDataType() > this.dstParam.getDataType()) {
      cm = this.srcParam.getColorModel();
      sm = this.srcParam.getSampleModel();
    } else {
      cm = this.dstParam.getColorModel();
      sm = this.dstParam.getSampleModel();
    }

    cm = new ComponentColorModel(rgbColorSpace,
        cm.getComponentSize(),
        cm.hasAlpha(),
        cm.isAlphaPremultiplied(),
        cm.getTransparency(),
        sm.getDataType());
    return new ImageParameters(cm, sm);
  }

  // Create an WritableRaster with the same SampleModel and location
  // as the passed Raster parameter.
  private WritableRaster createTempWritableRaster(final Raster src) {
    Point origin = new Point(src.getMinX(), src.getMinY());
    return RasterFactory.createWritableRaster(src.getSampleModel(),
        origin);
  }

  // Shift the sample value to [0, MAX-MIN]
  private Raster convertRasterToUnsigned(final Raster ras) {
    int type = ras.getSampleModel().getDataType();
    WritableRaster tempRas = null;

    if ((type == DataBuffer.TYPE_INT
        || type == DataBuffer.TYPE_SHORT)) {
      int minX = ras.getMinX(), minY = ras.getMinY();
      int w = ras.getWidth(), h = ras.getHeight();

      int[] buf = ras.getPixels(minX, minY, w, h, (int[]) null);
      convertBufferToUnsigned(buf, type);

      tempRas = createTempWritableRaster(ras);
      tempRas.setPixels(minX, minY, w, h, buf);
      return tempRas;
    }
    return ras;
  }

  // Shift the sample value back to [MIN, MAX]
  private WritableRaster convertRasterToSigned(final WritableRaster ras) {
    int type = ras.getSampleModel().getDataType();
    WritableRaster tempRas = null;

    if ((type == DataBuffer.TYPE_INT
        || type == DataBuffer.TYPE_SHORT)) {
      int minX = ras.getMinX(), minY = ras.getMinY();
      int w = ras.getWidth(), h = ras.getHeight();

      int[] buf = ras.getPixels(minX, minY, w, h, (int[]) null);
      convertBufferToSigned(buf, type);

      if (ras instanceof WritableRaster) {
        tempRas = ras;
      } else {
        tempRas = createTempWritableRaster(ras);
      }
      tempRas.setPixels(minX, minY, w, h, buf);
      return tempRas;
    }
    return ras;
  }

  // Shift the value to [MIN, MAX]
  private void convertBufferToSigned(final int[] buf, final int type) {
    if (buf == null) {
      return;
    }

    if (type == DataBuffer.TYPE_SHORT) {
      for (int i = 0; i < buf.length; i++) {
        buf[i] += Short.MIN_VALUE;
      }
    } else if (type == DataBuffer.TYPE_INT) {
      for (int i = 0; i < buf.length; i++) {
        buf[i] = (int) ((buf[i] & 0xFFFFFFFFl) + Integer.MIN_VALUE);
      }
    }
  }

  // Shift the value to [0, MAX-MIN]
  private void convertBufferToUnsigned(final int[] buf, final int type) {
    if (buf == null) {
      return;
    }

    if (type == DataBuffer.TYPE_SHORT) {
      for (int i = 0; i < buf.length; i++) {
        buf[i] -= Short.MIN_VALUE;
      }
    } else if (type == DataBuffer.TYPE_INT) {
      for (int i = 0; i < buf.length; i++) {
        buf[i] = (int) ((buf[i] & 0xFFFFFFFFl) - Integer.MIN_VALUE);
      }
    }
  }

// define a class to cache the parameters
  private final class ImageParameters {
    private final boolean isFloat;
    private final ColorModel colorModel;
    private final SampleModel sampleModel;
    private final float minValue;
    private final float range;
    private final int[] componentSize;
    private final int dataType;

    ImageParameters(final ColorModel cm, final SampleModel sm) {
      this.colorModel = cm;
      this.sampleModel = sm;
      this.dataType = sm.getDataType();
      this.isFloat = this.dataType == DataBuffer.TYPE_FLOAT
          || this.dataType == DataBuffer.TYPE_DOUBLE;
      this.minValue = ColorConvertOpImage.getMinValue(this.dataType);
      this.range = ColorConvertOpImage.getRange(this.dataType);
      this.componentSize = cm.getComponentSize();
    }

    public boolean isFloat() {
      return this.isFloat;
    }

    public ColorModel getColorModel() {
      return this.colorModel;
    }

    public SampleModel getSampleModel() {
      return this.sampleModel;
    }

    public float getMinValue() {
      return this.minValue;
    }

    public float getRange() {
      return this.range;
    }

    public int[] getComponentSize() {
      return this.componentSize;
    }

    public int getDataType() {
      return this.dataType;
    }
  }
}
