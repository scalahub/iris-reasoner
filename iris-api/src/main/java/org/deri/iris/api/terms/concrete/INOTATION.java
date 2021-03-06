/*
 * Integrated Rule Inference System (IRIS):
 * An extensible rule inference system for datalog with extensions.
 * 
 * Copyright (C) 2009 Semantic Technology Institute (STI) Innsbruck, 
 * University of Innsbruck, Technikerstrasse 21a, 6020 Innsbruck, Austria.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 */
package org.deri.iris.api.terms.concrete;

import org.deri.iris.api.terms.IConcreteTerm;

/**
 * <p>
 * Represents the XML Schema datatype xsd:NOTATION.
 * </p>
 * <p>
 * According to the XML Schema specificaiton, the value space of xsd:NOTATION is
 * the set of QNames of notations declared in a schema.
 * </p>
 * 
 * @author Adrian Marte
 */
public interface INOTATION extends IConcreteTerm {

	/**
	 * Returns the namespace name of this NOTATION.
	 * 
	 * @return The namespace name of this NOTATION.
	 */
	public String getNamespaceName();

	/**
	 * Returns the local part of this NOTATION.
	 * 
	 * @return The local part of this NOTATION.
	 */
	public String getLocalPart();

	/**
	 * Returns an array containing the namespace name (first element) and the
	 * local part (second element).
	 * 
	 * @return An array containing the namespace name (first element) and the
	 *         local part (second element).
	 */
	public String[] getValue();
}
