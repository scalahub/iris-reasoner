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
package org.deri.iris.builtins.numeric;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.factory.Factory;

/**
 */
public class NumericMultiplyBuiltinTest extends AbstractNumericTest {

	public NumericMultiplyBuiltinTest(String name) {
		super(name);
	}
	
	public void testBuiltin() throws EvaluationException {
		ITerm term1 = Factory.CONCRETE.createDecimal(24.4);
		ITerm term2 = Factory.CONCRETE.createDecimal(3.0);
		ITerm result = Factory.CONCRETE.createDecimal(73.2);

		NumericMultiplyBuiltin builtin = new NumericMultiplyBuiltin(X, Y, Z);
		args = Factory.BASIC.createTuple(term1, term2, result);
		actual = builtin.evaluate(args);
		assertEquals(EMPTY_TUPLE, actual);
		
		term1 = Factory.CONCRETE.createInteger(13);
		term2 = Factory.CONCRETE.createInteger(4);
		result = Factory.CONCRETE.createInteger(52);

		builtin = new NumericMultiplyBuiltin(X, Y, Z);
		args = Factory.BASIC.createTuple(term1, term2, result);
		actual = builtin.evaluate(args);

		assertEquals(EMPTY_TUPLE, actual);

		term1 = Factory.CONCRETE.createInteger(10);
		term2 = Factory.CONCRETE.createInteger(3);
		result = Factory.CONCRETE.createInteger(30);

		builtin = new NumericMultiplyBuiltin(X, Y, Z);
		args = Factory.BASIC.createTuple(term1, term2, result);
		actual = builtin.evaluate(args);

		assertEquals(EMPTY_TUPLE, actual);

		term1 = Factory.CONCRETE.createDecimal(10.0);
		term2 = Factory.CONCRETE.createInteger(3);
		result = Factory.CONCRETE.createInteger(30);

		builtin = new NumericMultiplyBuiltin(X, Y, Z);
		args = Factory.BASIC.createTuple(term1, term2, result);
		actual = builtin.evaluate(args);

		assertEquals(EMPTY_TUPLE, actual);
		
		term1 = Factory.CONCRETE.createDecimal(23.0);
		term2 = Factory.CONCRETE.createInteger(3);
		result = Factory.CONCRETE.createInteger(2);

		builtin = new NumericMultiplyBuiltin(X, Y, Z);
		args = Factory.BASIC.createTuple(term1, term2, result);
		actual = builtin.evaluate(args);

		assertEquals(null, actual);
	}

}
