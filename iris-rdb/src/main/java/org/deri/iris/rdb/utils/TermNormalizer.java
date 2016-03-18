/*
 * Integrated Rule Inference System (IRIS):
 * An extensible rule inference system for datalog with extensions.
 * 
 * Copyright (C) 2011 Semantic Technology Institute (STI) Innsbruck, 
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
package org.deri.iris.rdb.utils;

import java.math.BigDecimal;

import org.deri.iris.api.terms.IConcreteTerm;
import org.deri.iris.api.terms.INumericTerm;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.concrete.IDateTerm;
import org.deri.iris.api.terms.concrete.IDateTime;
import org.deri.iris.api.terms.concrete.IDuration;
import org.deri.iris.builtins.datatype.ToDateTimeBuiltin;
import org.deri.iris.builtins.datatype.ToDurationBuiltin;

public class TermNormalizer {

	public String createString(ITerm term) {
		if (term == null || !(term instanceof IConcreteTerm)) {
			return null;
		}

		String result = null;

		if (term instanceof IDateTime) {
			// We only have 2 implementations of IDateTime: DateTime and
			// DateTimeStamp which both use the same toCanonicalString method.
			IDateTime dateTime = (IDateTime) term;
			result = dateTime.toCanonicalString();
		}

		if (term instanceof IDateTerm) {
			// Cast a date to a date time and return the canonical
			// representation of date time.
			IDateTerm date = (IDateTerm) term;
			IDateTime dateTime = ToDateTimeBuiltin.toDateTime(date);
			result = dateTime.toCanonicalString();
		}

		if (term instanceof IDuration) {
			// Cast the duration to duration in order to have a "full" duration
			// for a year month and day time duration.
			IDuration duration = ToDurationBuiltin.toDuration(term);

			if (duration != null) {
				result = duration.toCanonicalString();
			}
		}

		if (term instanceof INumericTerm) {
			// All numeric terms are represented as a decimal number.
			INumericTerm numeric = (INumericTerm) term;
			BigDecimal value = numeric.getValue();
			return value.toString();
		}

		if (result == null) {
			result = ((IConcreteTerm) term).toCanonicalString();
		}

		if (result != null) {
			return result.trim();
		}

		return null;
	}

}
