/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2009, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    either version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.plugins.hdf4;

import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.resources.TestData;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class HDF4Test extends AbstractHDF4TestCase {
    // Actually, HDF on Linux is not tested. Test are disabled.
    private final static boolean isLinux = ((String) System
            .getProperty("os.name")).equalsIgnoreCase("Linux") ? true : false;

    public HDF4Test(String name) {
        super(name);
    }

    /**
     * This test method uses an HDF4 file containing several subdatasets
     */
    public void testSubDataset() throws FileNotFoundException, IOException {
        if (!isLinux && isDriverAvailable)
            return;
        try {
            final int startIndex = 0;
            final int loopLength = 5;

            for (int i = startIndex; i < startIndex + loopLength; i++) {
                final ImageReadParam irp = new ImageReadParam();
                irp.setSourceSubsampling(1, 1, 0, 0);
                final String fileName = "TOVS_DAILY_AM_870330_NG.HDF";
                final File file = TestData.file(this, fileName);
                ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
                        "ImageRead");
                final ImageReader mReader = new HDF4ImageReaderSpi()
                        .createReaderInstance();
                pbjImageRead.setParameter("Input", file);
                pbjImageRead.setParameter("Reader", mReader);
                pbjImageRead.setParameter("readParam", irp);
                pbjImageRead.setParameter("ImageChoice", new Integer(i));
                final ImageLayout l = new ImageLayout();
                l.setTileGridXOffset(0).setTileGridYOffset(0)
                        .setTileHeight(256).setTileWidth(256);
                // get a RenderedImage
                RenderedOp image = JAI.create("ImageRead", pbjImageRead,
                        new RenderingHints(JAI.KEY_IMAGE_LAYOUT, l));
                image.getTiles();
                if (TestData.isInteractiveTest())
                    ImageIOUtilities.visualize(image, fileName);
                mReader.dispose();
            }
        } catch (FileNotFoundException fnfe) {
            warningMessage();
        }
    }

    public void testManualRead() throws FileNotFoundException, IOException {
        if (!isLinux && isDriverAvailable)
            return;
        try {
            ImageReader mReader = new HDF4ImageReaderSpi()
                    .createReaderInstance();
            final String fileName = "TOVS_DAILY_AM_870330_NG.HDF";
            final File file = TestData.file(this, fileName);
            mReader.setInput(file);
            RenderedImage ri = mReader.read(0);
            if (TestData.isInteractiveTest())
                ImageIOUtilities.visualize(ri);
            else {
                assertNotNull(ri);
                mReader.dispose();
            }
        } catch (FileNotFoundException fnfe) {
            warningMessage();
        }
    }

    /**
     * This test method retrieves properties from each band of the sample
     * dataset
     */
    public void testRasterBandsProperties() throws FileNotFoundException,
            IOException {
        if (!isLinux && isDriverAvailable)
            return;
        try {
            ImageReader reader = new HDF4ImageReaderSpi()
                    .createReaderInstance();
            String fileName = "TOVS_DAILY_AM_870330_NG.HDF";
            File file = TestData.file(this, fileName);
            reader.setInput(file);
            final int numImages = 3;
            final int startIndex = 2;
            int bands;
            Iterator<ImageTypeSpecifier> it;
            SampleModel sm;
            for (int i = startIndex; i < startIndex + numImages; i++) {
                it = reader.getImageTypes(i);
                ImageTypeSpecifier its;
                if (it.hasNext()) {
                    its = (ImageTypeSpecifier) it.next();
                    sm = its.getSampleModel();
                    bands = sm.getNumBands();
                    double d;
                    StringBuffer sb = new StringBuffer(
                            "RasterBands properties retrieval").append(
                            " Image: ").append(i);
                    HDF4ImageReader mReader = (HDF4ImageReader) reader;
                    for (int j = 0; j < bands; j++) {
                        sb.append(" \n\t Band: ").append(j).append(" --- ");
                        try {
                            d = mReader.getNoDataValue(i, j);
                            sb.append("NoDataV=").append(d);
                        } catch (IllegalArgumentException iae) {
                            sb.append(" NoDataV=NotAvailable");
                        }
                        try {
                            d = mReader.getOffset(i, j);
                            sb.append(" Off=").append(d);
                        } catch (IllegalArgumentException iae) {
                            sb.append(" Off=NotAvailable");
                        }
                        try {
                            d = mReader.getScale(i, j);
                            sb.append(" Scale=").append(d);
                        } catch (IllegalArgumentException iae) {
                            sb.append(" Scale=NotAvailable");
                        }
                        try {
                            d = mReader.getMinimum(i, j);
                            sb.append(" Min=").append(d);
                        } catch (IllegalArgumentException iae) {
                            sb.append(" Min=NotAvailable");
                        }
                        try {
                            d = mReader.getMaximum(i, j);
                            sb.append(" MAX=").append(d);
                        } catch (IllegalArgumentException iae) {
                            sb.append(" MAX=NotAvailable");
                        }
                    }
                    LOGGER.info(sb.toString());
                }
            }
            reader.dispose();
        } catch (FileNotFoundException fnfe) {
            warningMessage();
        }
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();

        // Test reading of several subdatasets
        suite.addTest(new HDF4Test("testSubDataset"));

        // Test read without exploiting JAI
        suite.addTest(new HDF4Test("testManualRead"));

        // Test reading of several subdatasets
        suite.addTest(new HDF4Test("testRasterBandsProperties"));

        return suite;
    }

    public static void main(java.lang.String[] args) {
        if (!isLinux && isDriverAvailable)
            return;
        junit.textui.TestRunner.run(suite());
    }
}
