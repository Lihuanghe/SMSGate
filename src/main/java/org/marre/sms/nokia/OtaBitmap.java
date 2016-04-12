/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is "SMS Library for the Java platform".
 *
 * The Initial Developer of the Original Code is Markus Eriksson.
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * ***** END LICENSE BLOCK ***** */
package org.marre.sms.nokia;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.Serializable;

/**
 * Nokia OTA Bitmap format
 * <p>
 * This class can currently only handle non-animated B/W OTA Bitmaps.
 * <p>
 * Format is:
 * 
 * <pre>
 *                     Octet 1 -&gt; 0 : Not sure what this is
 *                     Octet 2 -&gt; &lt;width&gt; : Width of image
 *                     Octet 3 -&gt; &lt;height&gt; : Height of image
 *                     Octet 4 -&gt; 1 : Number of colors?? B/W == 1?
 *                     Octet 5-n -&gt; &lt;imgdata&gt; : Image data 1 bit for each pixel
 * </pre>
 * 
 * I have only verified this class with BufferedImages of type TYPE_INT_ARGB
 * 
 * @author Markus Eriksson
 */
public class OtaBitmap implements Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -2466133864013197366L;
	private int width_;
    private int height_;

    private byte[] otaImgData_;

    /**
     * Initialise with a raw Ota Bitmap
     * 
     * @param otaBitmapData
     */
    public OtaBitmap(byte[] otaBitmapData)
    {
        if (otaBitmapData != null)
        {
            //Read info field read until no more fields left bit 7 is 0
            int infoField = otaBitmapData[0]; //assume just 1 for now
            width_ = otaBitmapData[1];
            height_ = otaBitmapData[2];
            int depth = otaBitmapData[3];

            int length = otaBitmapData.length - 4;
            otaImgData_ = new byte[length];

            System.arraycopy(otaBitmapData, 4, otaImgData_, 0, length);
        }
    }

    /**
     * Creates an OtaBitmap object from an BufferedImage.
     * <p>
     * Every pixel that is not white will be converted to black.
     * 
     * @param img
     *            Image to convert.
     */
    public OtaBitmap(BufferedImage img)
    {
        int bitOffset = 0;
        int data = 0;
        int nByte = 0;
        int nTotalBytes = 0;
        Raster raster = img.getData();

        width_ = img.getWidth();
        height_ = img.getHeight();

        nTotalBytes = (width_ * height_) / 8;
        if (((width_ * height_) % 8) > 0)
        {
            nTotalBytes++;
        }

        otaImgData_ = new byte[nTotalBytes];

        for (int y = 0; y < height_; y++)
        {
            for (int x = 0; x < width_; x++)
            {
                int color = img.getRGB(x, y);

                if (color != 0)
                {
                    data |= ((1 << (7 - bitOffset)) & 0xff);
                }

                bitOffset++;

                if (bitOffset >= 8)
                {
                    otaImgData_[nByte] = (byte) (data & 0xff);

                    bitOffset = 0;
                    data = 0x00;
                    nByte++;
                }
            }
        }

        if (bitOffset > 0)
        {
            otaImgData_[nByte] = (byte) (data & 0xff);
        }
    }

    /**
     * Returns the created image data (not including image header)
     * 
     * @return Image data
     */
    public byte[] getImageData()
    {
        return otaImgData_;
    }

    /**
     * Returns the encoded OtaBitmap
     * 
     * @return An encoded OtaBitmap
     */
    public byte[] getBytes()
    {
        byte[] otaBitmap = new byte[otaImgData_.length + 4];

        otaBitmap[0] = 0; // Not sure what this is
        otaBitmap[1] = (byte) (width_ & 0xff);
        otaBitmap[2] = (byte) (height_ & 0xff);
        otaBitmap[3] = 1; // Number of colors

        // Add image data
        System.arraycopy(otaImgData_, 0, otaBitmap, 4, otaImgData_.length);

        return otaBitmap;
    }

    /**
     * @return
     */
    public int getHeight()
    {
        return height_;
    }

    /**
     * @return
     */
    public int getWidth()
    {
        return width_;
    }

    /**
     * @param i
     */
    public void setHeight(int i)
    {
        height_ = i;
    }

    /**
     * @param i
     */
    public void setWidth(int i)
    {
        width_ = i;
    }

}
