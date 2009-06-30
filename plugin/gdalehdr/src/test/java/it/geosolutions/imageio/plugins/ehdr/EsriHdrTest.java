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
package it.geosolutions.imageio.plugins.ehdr;

import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.resources.TestData;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageReader;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class EsriHdrTest extends AbstractEsriHdrTestCase {

    public EsriHdrTest(String name) {
        super(name);
    }

    /**
     * Test Read without exploiting JAI-ImageIO Tools
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void testRead() throws IOException, FileNotFoundException {
        if (!isGDALAvailable) {
            return;
        }
        final String fileName = "elggll.bil";
        final File file = TestData.file(this, fileName);
        ImageReader reader = new EsriHdrImageReaderSpi().createReaderInstance();
        reader.setInput(file);
        final RenderedImage image = reader.read(0);
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image);
        else
            assertNotNull(image);
        assertEquals(image.getWidth(), 600);
        assertEquals(image.getHeight(), 600);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();

        // Test Read without exploiting JAI-ImageIO tools capabilities
        suite.addTest(new EsriHdrTest("testRead"));

        return suite;
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
