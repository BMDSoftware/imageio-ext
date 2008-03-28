/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *	  https://imageio-ext.dev.java.net/
 *    (C) 2007, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.hdf.object.h4;

import it.geosolutions.hdf.object.IHObject;

/**
 * Abstract class representing a HDF variable
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public abstract class H4Variable extends H4DecoratedObject implements IHObject {

	/**
	 * The name of this Variable
	 * 
	 * @uml.property name="name"
	 */
	protected String name = "";

	/**
	 * Getter of the property <code>name</code>
	 * 
	 * @return the name of this Variable.
	 * @uml.property name="name"
	 */
	public String getName() {
		return name;
	}
}
