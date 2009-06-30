/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
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
package it.geosolutions.imageio.plugins.ecw;

import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.resources.TestData;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Testing reading capabilities for {@link ECWImageReader}.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 */
public class ECWTest extends AbstractECWTestCase {

    private final static String ECWPSkipTest = "ecwp://Set a valid link";

    private final static String ECWP = ECWPSkipTest; // Change with a valid
                                                        // ecwp

    /** @todo optimize logic for test skipping */
    public ECWTest(String name) {
        super(name);
    }

    /**
     * Test reading of a RGB image
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void testImageRead() throws FileNotFoundException, IOException {
        if (!isDriverAvailable) {
            return;
        }
        final ParameterBlockJAI pbjImageRead;
        final ImageReadParam irp = new ImageReadParam();
        final String fileName = "sample.ecw";
        final File file = TestData.file(this, fileName);

        irp.setSourceSubsampling(2, 2, 0, 0);
        pbjImageRead = new ParameterBlockJAI("ImageRead");
        pbjImageRead.setParameter("Input", file);
        pbjImageRead.setParameter("readParam", irp);
        final ImageLayout l = new ImageLayout();
        l.setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(512)
                .setTileWidth(512);
        RenderedOp image = JAI.create("ImageRead", pbjImageRead,
                new RenderingHints(JAI.KEY_IMAGE_LAYOUT, l));
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, fileName);
        else
            image.getTiles();
        assertEquals(200, image.getWidth());
        assertEquals(100, image.getHeight());
    }

    public void testManualRead() throws FileNotFoundException, IOException {
        if (!isDriverAvailable) {
            return;
        }
        final ECWImageReaderSpi spi = new ECWImageReaderSpi();
        final ECWImageReader mReader = new ECWImageReader(spi);
        final String fileName = "sample.ecw";
        final File file = TestData.file(this, fileName);
        final ImageReadParam param = new ImageReadParam();
        final int imageIndex = 0;

        mReader.setInput(file);
        final RenderedImage image = mReader.readAsRenderedImage(imageIndex,
                param);
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, fileName);
        assertEquals(400, image.getWidth());
        assertEquals(200, image.getHeight());
        mReader.dispose();
    }

    public void testECWPRead() throws FileNotFoundException, IOException {
        if (!isDriverAvailable) {
            return;
        }
        if (ECWP.equalsIgnoreCase(ECWPSkipTest))
            return;

        final ImageReader mReader = new ECWImageReaderSpi()
                .createReaderInstance();
        final ECWPImageInputStream ecwp = new ECWPImageInputStream(ECWP);
        final ImageReadParam param = new ImageReadParam();
        param.setSourceSubsampling(1, 1, 0, 0);
        param.setSourceRegion(new Rectangle(1000, 1000, 2000, 2000));
        final int imageIndex = 0;

        mReader.setInput(ecwp);
        final RenderedImage image = mReader.readAsRenderedImage(imageIndex,
                param);
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, ECWP);
        mReader.dispose();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();

        // Test reading of a RGB image
        suite.addTest(new ECWTest("testImageRead"));

        // Test reading of a RGB image
        suite.addTest(new ECWTest("testManualRead"));

        // Test reading from ECWP
        suite.addTest(new ECWTest("testECWPRead"));

        return suite;
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

}
