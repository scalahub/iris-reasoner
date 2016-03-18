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

public class ObviousTest extends ProgramEvaluationTest {

	@Override
	public Collection<String> createExpressions() {
		Collection<String> expressions = new ArrayList<String>();

		// Create facts.
		expressions.add("person('A1').");
		expressions.add("person('B1').");
		expressions.add("person('B2').");
		expressions.add("person('C3').");

		expressions.add("hasName('A1', 'anne').");
		expressions.add("hasName('B1', 'barry').");
		expressions.add("hasName('B2', 'barry').");
		expressions.add("hasName('C3', 'charlie').");

		expressions.add("atOffsite('B1').");
		expressions.add("atOffsite('A1').");

		expressions.add("atEswc('B2').");
		expressions.add("atEswc('C3').");

		expressions.add("number(1).");

		// Create rules.
		expressions
				.add("same(?X, ?Y) :- person(?X), person(?Y), hasName(?X, ?N1), hasName(?Y, ?N2), ?N1 = ?N2.");
		expressions.add("atOffsite(?X) :- same(?X, ?Y), atOffsite(?Y).");
		expressions.add("notAtOffsite(?X) :- not atOffsite(?X).");
		expressions.add("foo(?X) :- ?Y + 1 = ?X, number(?Y).");

		return expressions;
	}

	@Test
	public void testSame() throws Exception {
		IRelation relation = evaluate("?- same(?X, ?Y).");

		Assert.assertTrue("(B1, B2) not in relation.",
				relation.contains(Helper.createConstantTuple("B1", "B2")));

		Assert.assertEquals("Relation does not have correct size", 6,
				relation.size());
	}

	@Test
	public void testAtOffSite() throws Exception {
		IRelation relation = evaluate("?- atOffsite(?X).");

		Assert.assertEquals("Relation does not have correct size", 3,
				relation.size());

		Assert.assertTrue("(A1) not in relation.",
				relation.contains(Helper.createConstantTuple("A1")));
		Assert.assertTrue("(B1) not in relation.",
				relation.contains(Helper.createConstantTuple("B1")));
		Assert.assertTrue("(B2) not in relation.",
				relation.contains(Helper.createConstantTuple("B2")));
	}

	@Test
	public void testNotAtOffsite() throws Exception {
		IRelation relation = evaluate("?- notAtOffsite(?X).");

		Assert.assertEquals("Relation does not have correct size", 9,
				relation.size());
	}

	@Test
	public void testFoo() throws Exception {
		IRelation relation = evaluate("?- foo(2).");

		Assert.assertEquals("Relation does not have correct size", 1,
				relation.size());
	}

}
