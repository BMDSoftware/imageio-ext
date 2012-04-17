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
package it.geosolutions.imageio.plugins.mrsid;

import it.geosolutions.imageio.gdalframework.GDALUtilities;

import java.util.logging.Logger;

import javax.media.jai.JAI;

import junit.framework.TestCase;

/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class AbstractMrSIDTestCase extends TestCase {

    protected static final String fileName = "n13250i.sid";

    /** A simple flag set to true in case the MrSID driver is available */
    protected final static boolean isDriverAvailable = GDALUtilities
            .isDriverAvailable("MRSID");

    private final static String msg = "MRSID Tests are skipped due to missing Driver.\n"
            + "Be sure GDAL has been built against MRSID and the required"
            + " lib is in the classpath";

    protected static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.plugins.mrsid");

    public AbstractMrSIDTestCase(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        if (!isDriverAvailable) {
            LOGGER.warning(msg);
            return;
        }
        // general settings
        JAI.getDefaultInstance().getTileScheduler().setParallelism(10);
        JAI.getDefaultInstance().getTileScheduler().setPriority(4);
        JAI.getDefaultInstance().getTileScheduler().setPrefetchPriority(2);
        JAI.getDefaultInstance().getTileScheduler().setPrefetchParallelism(5);
        JAI.getDefaultInstance().getTileCache().setMemoryCapacity(
                128 * 1024 * 1024);
        JAI.getDefaultInstance().getTileCache().setMemoryThreshold(1.0f);
    }

    protected void warningMessage() {
        LOGGER.info("Test file not available. Tests are skipped.");
    }
}
