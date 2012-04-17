/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2008, GeoSolutions
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
package it.geosolutions.imageio.plugins.envihdr;

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
public class ENVIHdrTest extends AbstractENVIHdrTestCase {

    public ENVIHdrTest(String name) {
        super(name);
    }

    /**
     * Test Read without exploiting JAI-ImageIO Tools
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void testManualRead() throws IOException, FileNotFoundException {
        if (!isGDALAvailable) {
            return;
        }
        final String fileName = "small_world.img";
        final File file = TestData.file(this, fileName);
        ImageReader reader = new ENVIHdrImageReaderSpi().createReaderInstance();
        reader.setInput(file);
        final RenderedImage image = reader.read(0);
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image);
        else
            assertNotNull(image);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();

        // Test Read without exploiting JAI-ImageIO tools capabilities
        suite.addTest(new ENVIHdrTest("testManualRead"));

        return suite;
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
