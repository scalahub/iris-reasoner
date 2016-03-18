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
package org.deri.iris.rdb.evaluation;

import java.util.ArrayList;
import java.util.Collection;

import org.deri.iris.storage.IRelation;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for correct evaluation of examples with rule head equality.
 */
public class BuiltinTest extends ProgramEvaluationTest {

	@Override
	public Collection<String> createExpressions() {
		Collection<String> expressions = new ArrayList<String>();

		// Create facts.
		expressions.add("lower('foobar').");
		expressions.add("lower('noob').");
		expressions.add("lower('pwnage').");

		expressions.add("upper('FOOBAR').");
		expressions.add("upper('NOOB').");
		expressions.add("upper('PWNAGE').");

		expressions.add("foo('foobar').");

		// Create rules.
		expressions
				.add("?X = ?Y :- lower(?X), upper(?Y), STRING_TO_UPPER(?X, ?Y).");

		return expressions;
	}

	@Test
	public void testString() throws Exception {
		// The result should be: foobar, noob, pwnage, FOOBAR, NOOB, PWNAGE

		IRelation relation = evaluate("?- lower(?X).");

		Assert.assertTrue("foobar not in relation.",
				relation.contains(Helper.createConstantTuple("foobar")));
		Assert.assertTrue("noob not in relation.",
				relation.contains(Helper.createConstantTuple("noob")));
		Assert.assertTrue("pwnage not in relation.",
				relation.contains(Helper.createConstantTuple("pwnage")));
		Assert.assertTrue("FOOBAR not in relation.",
				relation.contains(Helper.createConstantTuple("FOOBAR")));
		Assert.assertTrue("NOOB not in relation.",
				relation.contains(Helper.createConstantTuple("NOOB")));
		Assert.assertTrue("PWNAGE not in relation.",
				relation.contains(Helper.createConstantTuple("PWNAGE")));

		Assert.assertEquals("Relation does not have correct size", 6, relation.size());
	}

	@Test
	public void testFoo() throws Exception {
		// The result should be: foobar, FOOBAR

		IRelation relation = evaluate("?- foo(?X).");

		Assert.assertTrue("foobar not in relation.",
				relation.contains(Helper.createConstantTuple("foobar")));
		Assert.assertTrue("FOOBAR not in relation.",
				relation.contains(Helper.createConstantTuple("FOOBAR")));
		
		Assert.assertEquals("Relation does not have correct size", 2,
				relation.size());
	}

}
