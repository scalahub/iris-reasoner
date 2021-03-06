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
package org.deri.iris.builtins.date;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.factory.Factory;

/**
 */
public class DayTimeDurationGreaterEqualBuiltinTest extends
		AbstractDateBuiltinTest {

	public DayTimeDurationGreaterEqualBuiltinTest(String name) {
		super(name);
	}

	public void testBuiltin() throws EvaluationException {
		ITerm date1 = Factory.CONCRETE.createDayTimeDuration(true, 15, 23, 19,
				12.0);
		ITerm date2 = Factory.CONCRETE.createDayTimeDuration(true, 15, 23, 19,
				12.0);
		ITerm date3 = Factory.CONCRETE.createDayTimeDuration(true, 10, 3, 2,
				0.9);
		ITerm date4 = Factory.CONCRETE.createDayTimeDuration(false, 4, 9, 56,
				47.3);
		ITerm date5 = Factory.CONCRETE.createDayTimeDuration(false, 20, 19, 6,
				3.3);

		DayTimeDurationGreaterEqualBuiltin builtin = new DayTimeDurationGreaterEqualBuiltin(
				X, Y);
		args = Factory.BASIC.createTuple(date1, date1);
		actual = builtin.evaluate(args);
		// (date1 = date1) -> iTuple()
		assertEquals(EMPTY_TUPLE, actual);

		builtin = new DayTimeDurationGreaterEqualBuiltin(X, Y);
		args = Factory.BASIC.createTuple(date1, date2);
		actual = builtin.evaluate(args);
		// (date1 = date2) -> null
		assertEquals(EMPTY_TUPLE, actual);

		builtin = new DayTimeDurationGreaterEqualBuiltin(X, Y);
		args = Factory.BASIC.createTuple(date3, date4);
		actual = builtin.evaluate(args);
		// (date3 > date4) -> iTuple()
		assertEquals(EMPTY_TUPLE, actual);

		builtin = new DayTimeDurationGreaterEqualBuiltin(X, Y);
		args = Factory.BASIC.createTuple(date4, date3);
		actual = builtin.evaluate(args);
		// (date4 < date3) -> null
		assertEquals(null, actual);

		builtin = new DayTimeDurationGreaterEqualBuiltin(X, Y);
		args = Factory.BASIC.createTuple(date4, date5);
		actual = builtin.evaluate(args);
		// TODO mp: StackOberflowError why?
		// (date4 < date5) -> null
		assertEquals(null, actual);
	}

}