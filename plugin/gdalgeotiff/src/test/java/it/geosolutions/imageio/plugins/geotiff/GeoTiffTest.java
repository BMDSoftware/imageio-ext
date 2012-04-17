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
package it.geosolutions.imageio.plugins.geotiff;

import it.geosolutions.imageio.gdalframework.Viewer;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;
import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.resources.TestData;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.sun.media.jai.operator.ImageWriteDescriptor;

/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class GeoTiffTest extends AbstractGeoTiffTestCase {

    public GeoTiffTest(String name) {
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
        final ImageReadParam irp = new ImageReadParam();

        // Reading a simple GrayScale image
        String fileName = "utm.tif";
        final File inputFile = TestData.file(this, fileName);
        irp.setSourceSubsampling(2, 2, 0, 0);
        ImageReader reader = new GeoTiffImageReaderSpi().createReaderInstance();
        reader.setInput(inputFile);
        final RenderedImage image = reader.readAsRenderedImage(0, irp);
        if (TestData.isInteractiveTest())
            Viewer.visualizeAllInformation(image, fileName);
        assertEquals(256, image.getWidth());
        assertEquals(256, image.getHeight());
        reader.dispose();
    }

    /**
     * Test Read exploiting JAI-ImageIO tools capabilities
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void testRead() throws FileNotFoundException, IOException {
        if (!isGDALAvailable) {
            return;
        }
        final ParameterBlockJAI pbjImageRead;
        String fileName = "utm.tif";
        final File file = TestData.file(this, fileName);

        pbjImageRead = new ParameterBlockJAI("ImageRead");
        pbjImageRead.setParameter("Input", new FileImageInputStreamExtImpl(file));
        pbjImageRead.setParameter("Reader", new GeoTiffImageReaderSpi().createReaderInstance());
        RenderedOp image = JAI.create("ImageRead", pbjImageRead);
        if (TestData.isInteractiveTest())
                Viewer.visualize(image);
        else
            assertNotNull(image.getTiles());
    }

    /**
     * Test Writing capabilities.
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void testWrite() throws IOException, FileNotFoundException {
        if (!isGDALAvailable) {
            return;
        }
        final File outputFile = TestData.temp(this, "writetest.tif", false);
        outputFile.deleteOnExit();
        final File inputFile = TestData.file(this, "utm.tif");

        ImageReadParam rparam = new ImageReadParam();
        rparam.setSourceRegion(new Rectangle(1, 1, 300, 500));
        rparam.setSourceSubsampling(1, 2, 0, 0);
        ImageReader reader = new GeoTiffImageReaderSpi().createReaderInstance();
        reader.setInput(inputFile);
        final IIOMetadata metadata = reader.getImageMetadata(0);

        final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI("ImageRead");
        pbjImageRead.setParameter("Input", inputFile);
        pbjImageRead.setParameter("reader", reader);
        pbjImageRead.setParameter("readParam", rparam);

        final ImageLayout l = new ImageLayout();
        l.setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(256).setTileWidth(256);

        RenderedOp image = JAI.create("ImageRead", pbjImageRead,new RenderingHints(JAI.KEY_IMAGE_LAYOUT, l));

        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image,inputFile.getAbsolutePath(),true);

        // ////////////////////////////////////////////////////////////////
        // preparing to write
        // ////////////////////////////////////////////////////////////////
        final ParameterBlockJAI pbjImageWrite = new ParameterBlockJAI("ImageWrite");
        ImageWriter writer = new GeoTiffImageWriterSpi().createWriterInstance();
        pbjImageWrite.setParameter("Output", outputFile);
        pbjImageWrite.setParameter("writer", writer);
        pbjImageWrite.setParameter("ImageMetadata", metadata);
        pbjImageWrite.setParameter("Transcode", false);
        ImageWriteParam param = new ImageWriteParam(Locale.getDefault());
        param.setSourceRegion(new Rectangle(10, 10, 100, 100));
        param.setSourceSubsampling(2, 1, 0, 0);
        pbjImageWrite.setParameter("writeParam", param);

        pbjImageWrite.addSource(image);
        final RenderedOp op = JAI.create("ImageWrite", pbjImageWrite);
        final ImageWriter writer2 = (ImageWriter) op.getProperty(ImageWriteDescriptor.PROPERTY_NAME_IMAGE_WRITER);
        writer2.dispose();

        // ////////////////////////////////////////////////////////////////
        // preparing to read again
        // ////////////////////////////////////////////////////////////////
        final ParameterBlockJAI pbjImageReRead = new ParameterBlockJAI("ImageRead");
        pbjImageReRead.setParameter("Input", outputFile);
        pbjImageReRead.setParameter("Reader", new GeoTiffImageReaderSpi() .createReaderInstance());
        final RenderedOp image2 = JAI.create("ImageRead", pbjImageReRead);
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image2,outputFile.getAbsolutePath(),true);
        else
            assertNotNull(image2.getTiles());
    }

    /**
     * Test Read on a Paletted Image
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void testPaletted() throws FileNotFoundException, IOException {
        if (!isGDALAvailable) {
            return;
        }
        final File outputFile = TestData.temp(this, "writetest.tif", false);
        outputFile.deleteOnExit();
        final File inputFile = TestData.file(this, "paletted.tif");

        ImageReader reader = new GeoTiffImageReaderSpi().createReaderInstance();
        reader.setInput(inputFile);
        final IIOMetadata metadata = reader.getImageMetadata(0);

        final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI("ImageRead");
        pbjImageRead.setParameter("Input", inputFile);
        pbjImageRead.setParameter("reader", reader);

        final ImageLayout l = new ImageLayout();
        l.setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(256).setTileWidth(256);

        RenderedOp image = JAI.create("ImageRead", pbjImageRead, new RenderingHints(JAI.KEY_IMAGE_LAYOUT, l));

        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "Paletted image read");

        // ////////////////////////////////////////////////////////////////
        // preparing to write
        // ////////////////////////////////////////////////////////////////
        final ParameterBlockJAI pbjImageWrite = new ParameterBlockJAI("ImageWrite");
        ImageWriter writer = new GeoTiffImageWriterSpi().createWriterInstance();
        pbjImageWrite.setParameter("Output", outputFile);
        pbjImageWrite.setParameter("writer", writer);
        pbjImageWrite.setParameter("ImageMetadata", metadata);
        pbjImageWrite.setParameter("Transcode", false);
        pbjImageWrite.addSource(image);
        final RenderedOp op = JAI.create("ImageWrite", pbjImageWrite);
        final ImageWriter writer2 = (ImageWriter) op.getProperty(ImageWriteDescriptor.PROPERTY_NAME_IMAGE_WRITER);
        writer2.dispose();

        // ////////////////////////////////////////////////////////////////
        // preparing to read again
        // ////////////////////////////////////////////////////////////////
        final ParameterBlockJAI pbjImageReRead = new ParameterBlockJAI("ImageRead");
        pbjImageReRead.setParameter("Input", outputFile);
        pbjImageReRead.setParameter("Reader", new GeoTiffImageReaderSpi().createReaderInstance());
        final RenderedOp image2 = JAI.create("ImageRead", pbjImageReRead);
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image2,
                    "Paletted image read back after writing");
        else
            assertNotNull(image2.getTiles());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();

        // Test Read exploiting JAI-ImageIO tools capabilities
        suite.addTest(new GeoTiffTest("testRead"));

        // Test Read without exploiting JAI-ImageIO tools capabilities
        suite.addTest(new GeoTiffTest("testManualRead"));

        // Test Write
        suite.addTest(new GeoTiffTest("testWrite"));

        // Test read and write of a paletted image
        suite.addTest(new GeoTiffTest("testPaletted"));

        return suite;
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
