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
package it.geosolutions.hdf.object.h4;

import it.geosolutions.hdf.object.IHObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.hdflib.HDFLibrary;

/**
 * Class representing a HDF VGroup.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class H4VGroup extends H4Variable implements IHObject,
        IH4Object {

    /** TODO: need to add locking as used in H4SDS objects */
    
    /** Logger. */
    private final static Logger LOGGER = Logger
            .getLogger("it.geosolutions.hdf.object.h4");

    private AbstractH4Object attributesHolder;

    private class H4VGroupAttributesManager extends AbstractH4Object {

        public H4VGroupAttributesManager(final int identifier,
                final int numAttributes) {
            super(identifier, numAttributes);
        }

        /**
         * @see {@link AbstractH4Object#readAttribute(int, Object)}
         */
        protected boolean readAttribute(int index, Object values)
                throws HDFException {
            return HDFLibrary.Vgetattr(getIdentifier(), index, values);
        }

        /**
         * @see {@link AbstractH4Object#getAttributeInfo(int, String[])}
         */
        protected int[] getAttributeInfo(int index, String[] attrName)
                throws HDFException {
            int[] attrInfo = new int[] { 0, 0, 0 };
            boolean done = HDFLibrary.Vattrinfo(getIdentifier(), index,
                    attrName, attrInfo);
            if (done)
                return attrInfo;
            else
                return null;
        }

        /**
         * @see {@link AbstractH4Object#getAttributeIndexByName(String)}
         */
        protected int getAttributeIndexByName(String attributeName)
                throws HDFException {
            return HDFLibrary.Vfindattr(getIdentifier(), attributeName);
        }
    }

    /**
     * The list of TAG/REF couples referred by this VGroup
     */
    private List tagRefList;

    /**
     * the number of pairs TAG/REF referred by this group
     */
    private int numObjects;

    /**
     * the tag of this group
     */
    private int tag;

    /**
     * the reference of this group
     */
    private H4ReferencedObject reference;

    /**
     * the class name of this group
     */
    private String className = "";

    // ////////////////////////////////////////////////////////////////////////
    //
    // SET of Getters
    // 
    // ////////////////////////////////////////////////////////////////////////

    /**
     * getter of <code>reference</code>
     * 
     * @return the reference of this group.
     */
    int getReference() {
        return reference.getReference();
    }

    /**
     * getter of <code>tag</code>
     * 
     * @return the tag of this group
     */
    public int getTag() {
        return tag;
    }

    /**
     * getter of <code>className</code>
     * 
     * @return the class name of this group
     */
    public String getClassName() {
        return className;
    }

    /**
     * getter of <code>numObjects</code>
     * 
     * @return the number of pairs TAG/REF referred by this group
     */
    public int getNumObjects() {
        return numObjects;
    }

    /**
     * getter of <code>h4VGroupCollectionOwner</code>
     * 
     * @return the {@link H4VGroupCollection} to which this {@link H4VGroup}
     *         belongs.
     */
    H4VGroupCollection getH4VGroupCollectionOwner() {
        return h4VGroupCollectionOwner;
    }

    /**
     * The {@link H4VGroupCollection} to which this {@link H4VGroup} belongs.
     * 
     * @uml.associationEnd inverse="H4VGroupCollection:it.geosolutions.hdf.object.h4.H4VGroupCollection"
     */
    H4VGroupCollection h4VGroupCollectionOwner = null;

    /**
     * Main Constructor which builds a <code>H4Vgroup</code> given its ref.
     * This constructor is called by the {@link H4VGroupCollection} during the
     * initialization fase. After this call, the {@link H4VGroupCollection}
     * check if the built object is a real VGroup. If affirmative, it call the
     * {@link #init()} method.
     * 
     * @param h4VgroupCollection
     *                the parent collection
     * @param ref
     *                the reference of this VGroup
     */
    public H4VGroup(H4VGroupCollection h4VgroupCollection, final int ref) {
        h4VGroupCollectionOwner = h4VgroupCollection;
        final int fileID = h4VGroupCollectionOwner.getH4File().getIdentifier();
        try {
            reference = new H4ReferencedObject(ref);
            int identifier = HDFLibrary.Vattach(fileID, ref, "r");
            if (identifier != HDFConstants.FAIL) {
                setIdentifier(identifier);
                final String[] vgroupClass = { "" };
                HDFLibrary.Vgetclass(identifier, vgroupClass);
                className = vgroupClass[0];
                // NOTE that this version does not call the init method since we
                // need to check if the just built object is a vgroup
            } else {
                // XXX
            }
        } catch (HDFException e) {
            throw new RuntimeException(
                    "HDFException occurred while creating a new H4VGroup instance ",
                    e);
        }
    }

    /**
     * Constructor which builds a new <code>H4Vgroup</code> starting from a
     * parent one.
     * 
     * @param parentGroup
     *                the parent VGroup
     * @param ref
     *                the reference of the needed VGroup
     * 
     */
    public H4VGroup(H4VGroup parentGroup, final int ref) {
        this.h4VGroupCollectionOwner = parentGroup.h4VGroupCollectionOwner;
        final int fileID = h4VGroupCollectionOwner.getH4File().getIdentifier();
        if (fileID == HDFConstants.FAIL)
            throw new IllegalArgumentException("Invalid file identifier");
        try {
            reference = new H4ReferencedObject(ref);
            int identifier = HDFLibrary.Vattach(fileID, ref, "r");
            if (identifier != HDFConstants.FAIL) {
                setIdentifier(identifier);
                final String[] vgroupClass = { "" };
                HDFLibrary.Vgetclass(identifier, vgroupClass);
                className = vgroupClass[0];
                initialize();
            } else {
                throw new IllegalStateException(
                        "Failing to get an identifier for the VGroup element");
            }
        } catch (HDFException e) {
            throw new IllegalStateException(
                    "HDFException occurred while creating a new H4VGroup instance ",
                    e);
        }
    }

    /**
     * Initializes the VGroup
     * 
     * @throws HDFException
     */
    public void initialize() throws HDFException {
        final String[] vgroupName = { "" };
        int identifier = getIdentifier();
        HDFLibrary.Vgetname(identifier, vgroupName);
        setName(vgroupName[0]);
        tag = HDFLibrary.VQuerytag(identifier);
        numObjects = HDFLibrary.Vntagrefs(identifier);
        attributesHolder = new H4VGroupAttributesManager(identifier,
                HDFLibrary.Vnattrs(identifier));
    }

    protected void finalize() throws Throwable {
        try {
            dispose();
        } catch (Throwable e) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(Level.WARNING,
                        "Catched exception during dimension finalization: "
                                + e.getLocalizedMessage());
        }
    }

    /**
     * close this {@link H4VGroup} and dispose allocated objects.
     */
    public synchronized void dispose() {
        final int identifier = getIdentifier();
        if (identifier != HDFConstants.FAIL) {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.log(Level.FINE, "disposing VGroup with ID = ");
            try {
                HDFLibrary.Vdetach(identifier);
                if (attributesHolder != null) {
                    attributesHolder.dispose();
                    attributesHolder = null;
                }
            } catch (HDFException e) {
                if (LOGGER.isLoggable(Level.WARNING))
                    LOGGER.log(Level.WARNING,
                            "Error detaching the VGroup with ID = "
                                    + identifier);
            }
        }
        super.dispose();
    }

    /**
     * Returns a <code>List</code> containing TAG/REF couples referred by this
     * VGroup. A TAG/REF couple is stored as an int array having 2 as size.
     * 
     * @return a <code>List</code> of TAG/REF couples.
     */
    public synchronized List getTagRefList() throws HDFException {
        if (tagRefList == null) {
            tagRefList = new ArrayList(numObjects);
            for (int i = 0; i < numObjects; i++) {
                final int tagRef[] = { 0, 0 };
                HDFLibrary.Vgettagref(getIdentifier(), i, tagRef);
                tagRefList.add(i, tagRef);
            }
        }
        return Collections.unmodifiableList(tagRefList);
    }

    /**
     * @see {@link IH4Object#getAttribute(int)}
     */
    public H4Attribute getAttribute(int attributeIndex) throws HDFException {
        return attributesHolder.getAttribute(attributeIndex);
    }

    /**
     * @see {@link IH4Object#getAttribute(String)}
     */
    public H4Attribute getAttribute(String attributeName) throws HDFException {
        return attributesHolder.getAttribute(attributeName);
    }

    /**
     * @see {@link IH4Object#getNumAttributes()}
     */
    public int getNumAttributes() {
        return attributesHolder.getNumAttributes();
    }
}
