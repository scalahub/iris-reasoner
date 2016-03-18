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

import org.deri.iris.api.terms.IStringTerm;

/**
 * <p>
 * Represents the XML Schema datatype xsd:normalizedString.
 * </p>
 * <p>
 * xsd:normalizedString are strings that do not contain the carriage return
 * (#xD), line feed (#xA) nor tab (#x9) characters.
 * </p>
 * 
 * @author Adrian Marte
 */
public interface INormalizedString extends IStringTerm {

	/**
	 * Returns the string representing this xsd:normalizedString.
	 * 
	 * @return The string representing this xsd:normalizedString.
	 */
	public String getValue();

}
