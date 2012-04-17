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
package it.geosolutions.hdf.object.h4;

import it.geosolutions.hdf.object.AbstractHObject;

import java.awt.image.DataBuffer;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.hdflib.HDFLibrary;

/**
 * Utility abstract class for retrieving datatype information and building
 * properly typed and properly sized data array where to load data values.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class H4Utilities {

    private static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.hdf.object.H4Utilities");

    private static boolean init = false;

    /** A lock to be used to grant thread-safety while calling the HDFLibrary */
    private static ReentrantLock lock = new ReentrantLock();

    static void lock() {
        lock.lock();
    }

    static void unlock() {
        lock.unlock();
    }

    /** Set to <code>true</code> in case the JHDF lib is available */
    private static boolean available;

    /** Forces loading of JHDF lib. */
    public static void loadJHDFLib() {
        if (init == false)
            init = true;
        else
            return;
        try {
            System.loadLibrary("jhdf");
            available = true;
        } catch (UnsatisfiedLinkError e) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.warning(new StringBuffer("Native library load failed.")
                        .append(e.toString()).toString());
            available = false;
        }
    }

    /**
     * Returns <code>true</code> if the JHDF library has been loaded.
     * <code>false</code> otherwise.
     * 
     * @return <code>true</code> only if the JHDF library has been loaded.
     */
    public synchronized static boolean isJHDFLibAvailable() {
        loadJHDFLib();
        return available;
    }

    /** predefined attributes */
    public static final String SDS_PREDEF_ATTR_LONG_NAME = "long_name";

    public static final String SDS_PREDEF_ATTR_UNITS = "units";

    public static final String SDS_PREDEF_ATTR_FORMAT = "format";

    public static final String SDS_PREDEF_ATTR_CALIBRATED_NT = "calibrated_nt";

    public static final String SDS_PREDEF_ATTR_SCALE_FACTOR = "scale_factor";

    public static final String SDS_PREDEF_ATTR_SCALE_FACTOR_ERR = "scale_factor_err";

    public static final String SDS_PREDEF_ATTR_ADD_OFFSET = "add_offset";

    public static final String SDS_PREDEF_ATTR_ADD_OFFSET_ERR = "add_offset_err";

    public static final String SDS_PREDEF_ATTR_FILL_VALUE = "_FillValue";

    public static final String SDS_PREDEF_ATTR_COORDINATE_SYSTEM = "cordsys";

    public static final String SDS_PREDEF_ATTR_VALID_RANGE_MIN = "valid_min";

    public static final String SDS_PREDEF_ATTR_VALID_RANGE_MAX = "valid_max";

    public static final String SDS_PREDEF_ATTR_VALID_RANGE = "valid_range";

    /** predefined dimension attributes */
    public static final String PREDEF_ATTR_LABEL = "long_name";

    public static final String PREDEF_ATTR_UNIT = "units";

    public static final String PREDEF_ATTR_FORMAT = "format";

    /**
     * Ensure non instantiability
     */
    private H4Utilities() {

    }

    /**
     * Utility method for raising an exception in case
     * 
     * @param o
     * @param objectName
     */
    public static void checkNonNull(final Object o, final String objectName) {
        if (o == null)
            throw new NullPointerException(objectName != null ? "Object "
                    + objectName + " cannot be null"
                    : "The provided object cannot be null");
    }

    /**
     * Builds a properly typed and properly sized array to store a specific
     * amount of data, given the type of data and its size.
     * 
     * @param datatype
     *                the datatype of the data which will be stored in the
     *                array.
     * @param size
     *                the size of the required array
     * @return the allocated array
     */
    public static Object allocateArray(final int datatype, final int size) {
        if (size <= 0)
            return null;

        // //
        // 
        // Allocating a buffer of the required type and size.
        // 
        // //
        Object data = null;

        switch (datatype) {

        // Byte array
        case HDFConstants.DFNT_CHAR:
        case HDFConstants.DFNT_UCHAR8:
        case HDFConstants.DFNT_UINT8:
        case HDFConstants.DFNT_INT8:
            data = new byte[size];
            break;

        // short array
        case HDFConstants.DFNT_INT16:
        case HDFConstants.DFNT_UINT16:
            data = new short[size];
            break;

        // int array
        case HDFConstants.DFNT_INT32:
        case HDFConstants.DFNT_UINT32:
            data = new int[size];
            break;

        // long array
        case HDFConstants.DFNT_INT64:
        case HDFConstants.DFNT_UINT64:
            data = new long[size];
            break;

        // float array
        case HDFConstants.DFNT_FLOAT32:
            data = new float[size];
            break;

        // double array
        case HDFConstants.DFNT_FLOAT64:
            data = new double[size];
            break;

        // unrecognized datatype!!
        default:
            data = null;
            break;
        }

        return data;
    }

    /**
     * Returns the size (in bytes) of a given datatype
     * 
     * @param datatype
     *                the input datatype
     * @return the size (in bytes) of a given datatype
     */
    public static int getDataTypeSize(final int datatype) {
        switch (datatype) {
        case HDFConstants.DFNT_CHAR:
        case HDFConstants.DFNT_UCHAR8:
        case HDFConstants.DFNT_INT8:
        case HDFConstants.DFNT_UINT8:
            return 1;
        case HDFConstants.DFNT_INT16:
        case HDFConstants.DFNT_UINT16:
            return 2;
        case HDFConstants.DFNT_INT32:
        case HDFConstants.DFNT_UINT32:
        case HDFConstants.DFNT_FLOAT32:
            return 4;
        case HDFConstants.DFNT_INT64:
        case HDFConstants.DFNT_UINT64:
        case HDFConstants.DFNT_FLOAT64:
            return 8;
        default:
            return 0;
        }
    }

    /**
     * Returns <code>true</code> if the provided datatype is unsigned;
     * <code>false</code> otherwise.
     * 
     * @param datatype
     *                the given datatype
     * @return <code>true</code> if the provided datatype is unsigned;
     *         <code>false</code> otherwise.
     */
    public static final boolean isUnsigned(final int datatype) {
        boolean unsigned = false;
        switch (datatype) {
        case HDFConstants.DFNT_UCHAR8:
        case HDFConstants.DFNT_UINT8:
        case HDFConstants.DFNT_UINT16:
        case HDFConstants.DFNT_UINT32:
        case HDFConstants.DFNT_UINT64:
            unsigned = true;
            break;
        }
        return unsigned;
    }

    /**
     * Given a HDF datatype, returns a proper databuffer type depending on the
     * datatype properties.
     * 
     * @param datatype
     *                the input datatype
     * @return the proper databuffer type
     */
    public static int getBufferTypeFromDataType(final int datatype) {
        int bufferType = DataBuffer.TYPE_UNDEFINED;

        switch (datatype) {
        case HDFConstants.DFNT_INT8:
        case HDFConstants.DFNT_UINT8:
        case HDFConstants.DFNT_CHAR8:
        case HDFConstants.DFNT_UCHAR:
            bufferType = DataBuffer.TYPE_BYTE;
            break;
        case HDFConstants.DFNT_INT16:
            bufferType = DataBuffer.TYPE_SHORT;
            break;
        case HDFConstants.DFNT_UINT16:
            bufferType = DataBuffer.TYPE_USHORT;
            break;
        case HDFConstants.DFNT_INT32:
        case HDFConstants.DFNT_UINT32:
            bufferType = DataBuffer.TYPE_INT;
            break;
        case HDFConstants.DFNT_FLOAT32:
            bufferType = DataBuffer.TYPE_FLOAT;
            break;
        case HDFConstants.DFNT_FLOAT64:
            bufferType = DataBuffer.TYPE_DOUBLE;
            break;
        // TODO: Handle more cases??

        }
        return bufferType;
    }

    /**
     * Return values contained in the provided data buffer of the specified
     * datatype, as <code>String</code>.
     * 
     * @param datatype
     *                the data type of values
     * @param buf
     *                a buffer containing data values of a specific type
     */
    public static String getValuesAsString(int datatype, Object buf) {
        StringBuffer sb = new StringBuffer();
        int i;
        if (datatype == HDFConstants.DFNT_FLOAT32
                || datatype == HDFConstants.DFNT_FLOAT) {
            float[] ff = (float[]) buf;
            final int size = ff.length;
            for (i = 0; i < size - 1; i++) {
                sb.append(ff[i]).append(" ");
            }
            sb.append(ff[i]);
        } else if (datatype == HDFConstants.DFNT_DOUBLE
                || datatype == HDFConstants.DFNT_FLOAT64) {
            double[] dd = (double[]) buf;
            final int size = dd.length;
            for (i = 0; i < size - 1; i++) {
                sb.append(dd[i]).append(" ");
            }
            sb.append(dd[i]);
        } else if (datatype == HDFConstants.DFNT_INT8) {
            byte[] bb = (byte[]) buf;
            final int size = bb.length;
            for (i = 0; i < size - 1; i++) {
                sb.append(bb[i]).append(" ");
            }
            sb.append(bb[i]);
        } else if (datatype == HDFConstants.DFNT_UINT8) {
            byte[] bb = (byte[]) buf;
            final int size = bb.length;
            for (i = 0; i < size - 1; i++) {
                int myByte = (0x000000FF & ((int) bb[i]));
                short anUnsignedByte = (short) myByte;
                sb.append(anUnsignedByte).append(" ");
            }
            int myByte = (0x000000FF & ((int) bb[i]));
            short anUnsignedByte = (short) myByte;
            sb.append(anUnsignedByte);
        } else if (datatype == HDFConstants.DFNT_INT16
                || datatype == HDFConstants.DFNT_UINT16) {
            short[] ss = (short[]) buf;
            final int size = ss.length;
            for (i = 0; i < size - 1; i++) {
                sb.append(ss[i]).append(" ");
            }
            sb.append(ss[i]);
        } else if (datatype == HDFConstants.DFNT_INT32
                || datatype == HDFConstants.DFNT_UINT32) {
            int[] ii = (int[]) buf;
            final int size = ii.length;
            for (i = 0; i < size - 1; i++) {
                sb.append(ii[i]).append(" ");
            }
            sb.append(ii[i]);
        } else if (datatype == HDFConstants.DFNT_CHAR
                || datatype == HDFConstants.DFNT_UCHAR8) {

            byte[] bb = (byte[]) buf;
            final int size = bb.length;
            sb = new StringBuffer(size);
            for (i = 0; i < size && bb[i] != 0; i++) {
                sb.append(new String(bb, i, 1));
            }
        }
        return sb.toString();
    }

    /**
     * Checks if the object specified by the reference parameter is a VGroup
     * children of another VGroup
     * 
     * @param parentGroup
     *                the candidate parent VGroup
     * @param ref
     *                the reference of the candidate children VGroup
     * @return <code>true</code> if the VGroup is children of a parent VGroup
     */
    //TODO: SYNC
    public static boolean isAVGroup(H4VGroup parentGroup, final int ref) {
        boolean isAvGroup = false;
        final int fileID = parentGroup.h4VGroupCollectionOwner.getH4File()
                .getIdentifier();
        final int parentGroupID = parentGroup.getIdentifier();
        try {
            final int id = HDFLibrary.Vattach(fileID, ref, "r");
            if (id != HDFConstants.FAIL) {
                final String[] vgroupClass = { "" };
                HDFLibrary.Vgetclass(id, vgroupClass);
                final String name = vgroupClass[0];
                isAvGroup = HDFLibrary.Visvg(parentGroupID, ref)
                        && isAVGroupClass(name);
                // TODO: Need to detach?
            }

        } catch (HDFException e) {
            throw new IllegalStateException(
                    "Error accessing the VGroup Routines", e);
        }
        return isAvGroup;
    }

    protected static boolean isAVGroupClass(final String vGroupClassName) {
        if (vGroupClassName.equalsIgnoreCase(HDFConstants.HDF_ATTRIBUTE)
                || vGroupClassName.equalsIgnoreCase(HDFConstants.HDF_VARIABLE)
                || vGroupClassName.equalsIgnoreCase(HDFConstants.HDF_DIMENSION)
                || vGroupClassName
                        .equalsIgnoreCase(HDFConstants.HDF_UDIMENSION)
                || vGroupClassName.equalsIgnoreCase(HDFConstants.DIM_VALS)
                || vGroupClassName.equalsIgnoreCase(HDFConstants.DIM_VALS01)
                || vGroupClassName.equalsIgnoreCase(HDFConstants.HDF_CHK_TBL)
                || vGroupClassName.equalsIgnoreCase(HDFConstants.HDF_CDF)
                || vGroupClassName.equalsIgnoreCase(HDFConstants.GR_NAME)
                || vGroupClassName.equalsIgnoreCase(HDFConstants.RI_NAME)
                || vGroupClassName.equalsIgnoreCase(HDFConstants.RIGATTRNAME)
                || vGroupClassName.equalsIgnoreCase(HDFConstants.RIGATTRCLASS))
            return false;
        else
            return true;
    }

    /**
     * Given a HDF Attribute, builds a String containing comma separated values
     * related to the attribute. Some Attribute may have a int array as value.
     * 
     * @param att
     *                a HDF <code>Attribute</code>.
     * @return the built <code>String</code>
     * @throws HDFException
     */
    public static String buildAttributeString(H4Attribute att)
            throws HDFException {
        if (att == null)
            throw new IllegalArgumentException("Null attribute provided");
        final int datatype = att.getDatatype();
        Object buf = att.getValues();
        final StringBuffer sb = new StringBuffer();
        int i = 0;
        String attributeValue = "";
        if (buf != null) {
            if (datatype == HDFConstants.DFNT_FLOAT32
                    || datatype == HDFConstants.DFNT_FLOAT) {
                float[] ff = (float[]) buf;
                final int size = ff.length;
                for (i = 0; i < size - 1; i++) {
                    sb.append(ff[i]).append(",");
                }
                sb.append(ff[i]);
            } else if (datatype == HDFConstants.DFNT_DOUBLE
                    || datatype == HDFConstants.DFNT_FLOAT64) {
                double[] dd = (double[]) buf;
                final int size = dd.length;
                for (i = 0; i < size - 1; i++) {
                    sb.append(dd[i]).append(",");
                }
                sb.append(dd[i]);
            } else if (datatype == HDFConstants.DFNT_INT8) {
                byte[] bb = (byte[]) buf;
                final int size = bb.length;
                for (i = 0; i < size - 1; i++) {
                    sb.append(bb[i]).append(",");
                }
                sb.append(bb[i]);
            } else if (datatype == HDFConstants.DFNT_UINT8) {
                byte[] bb = (byte[]) buf;
                final int size = bb.length;
                for (i = 0; i < size - 1; i++) {
                    int myByte = (0x000000FF & ((int) bb[i]));
                    short anUnsignedByte = (short) myByte;
                    sb.append(anUnsignedByte).append(",");
                }
                int myByte = (0x000000FF & ((int) bb[i]));
                short anUnsignedByte = (short) myByte;
                sb.append(anUnsignedByte);
            } else if (datatype == HDFConstants.DFNT_INT16
                    || datatype == HDFConstants.DFNT_UINT16) {
                short[] ss = (short[]) buf;
                final int size = ss.length;
                for (i = 0; i < size - 1; i++) {
                    sb.append(ss[i]).append(",");
                }
                sb.append(ss[i]);
            } else if (datatype == HDFConstants.DFNT_INT32
                    || datatype == HDFConstants.DFNT_UINT32) {
                int[] ii = (int[]) buf;
                final int size = ii.length;
                for (i = 0; i < size - 1; i++) {
                    sb.append(ii[i]).append(",");
                }
                sb.append(ii[i]);
            } else if (datatype == HDFConstants.DFNT_CHAR
                    || datatype == HDFConstants.DFNT_UCHAR8) {
                byte[] bb = (byte[]) buf;
                final int size = bb.length;
                for (i = 0; i < size && bb[i] != 0; i++) {
                    sb.append(new String(bb, i, 1));
                }

            }
            attributeValue = sb.toString();
        } else {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(Level.WARNING,
                        "No values were found for the specified attribute");
        }
        return attributeValue;
    }

    /**
     * Attempts to build a new {@link H4SDS} given its index within the file. A
     * new {@link H4SDS} is returned only if the underlying SDS does not
     * represents a dimension scale. Otherwise, <code>null</code> will
     * returned.
     * 
     * @param h4SDSCollection
     *                the collection owner.
     * @param index
     *                the index of the required SDS within the file
     * @return a new {@link H4SDS} if the underlying SDS does not represents a
     *         dimension scale. <code>null</code> otherwise.
     * @throws HDFException
     */
    protected static H4SDS buildH4SDS(H4SDSCollection h4SDSCollection,
            final int index) throws HDFException {
        H4SDS sds = null;
        final int interfaceID = h4SDSCollection.getIdentifier();
        if (interfaceID == HDFConstants.FAIL) {
            if (H4SDS.LOGGER.isLoggable(Level.WARNING))
                H4SDS.LOGGER.log(Level.WARNING,
                        "undefined SDInterface identifier ");
            return sds;
        }
        try {
            H4Utilities.lock.lock();
            try {
                final int identifier = HDFLibrary.SDselect(interfaceID, index);
                if (identifier != HDFConstants.FAIL) {
                    if (!HDFLibrary.SDiscoordvar(identifier)) {
                        sds = new H4SDS(h4SDSCollection, index, identifier);
                    } else
                        HDFLibrary.SDendaccess(identifier);
                } else {
                    if (H4SDS.LOGGER.isLoggable(Level.WARNING))
                        H4SDS.LOGGER.log(Level.WARNING,
                                "undefined SD identifier ");
                }
            } finally {
                H4Utilities.lock.unlock();
            }

        } catch (HDFException e) {
            throw new RuntimeException("Error while creating a new H4SDS", e);
        }
        return sds;
    }

    /**
     * Static utility method which build a new {@link H4Attribute} given the
     * object to which the attribute is attached and the index of the attribute.
     * 
     * @param objectWithAttribute
     *                The owner {@link AbstractHObject} to which the attribute
     *                is attached
     * @param index
     *                The index of the required attribute.
     * @return the {@link H4Attribute} just built.
     * @throws HDFException
     */
    protected static H4Attribute buildAttribute(
            AbstractH4Object objectWithAttribute, final int index)
            throws HDFException {
        if (objectWithAttribute == null)
            throw new IllegalArgumentException("Input object is null");
        H4Attribute attribute = null;
        String[] attrName = new String[] { "" };

        // get various info about this attribute from the proper interface,
        // depending on the subclass of the owner object
        int[] attrInfo = objectWithAttribute.getAttributeInfo(index, attrName);

        if (attrInfo != null) {
            // build a new attribute
            attribute = new H4Attribute(objectWithAttribute, index,
                    attrName[0], attrInfo);
        }
        return attribute;
    }
}
