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
public class ConstantTest extends ProgramEvaluationTest {

	@Override
	public Collection<String> createExpressions() {
		Collection<String> expressions = new ArrayList<String>();

		// Create facts.
		expressions.add("p('A').");
		expressions.add("q('C').");
		expressions.add("r('C', '1', 'A').");

		// Create rules.
		expressions.add("'A' = 'B' :- q('C').");

		return expressions;
	}

	@Test
	public void testP() throws Exception {
		// The result should be: a, b

		IRelation relation = evaluate("?- p(?X).");

		Assert.assertTrue("A not in relation.",
				relation.contains(Helper.createConstantTuple("A")));
		Assert.assertTrue("B not in relation.",
				relation.contains(Helper.createConstantTuple("B")));

		Assert.assertEquals("Relation does not have correct size", 2,
				relation.size());
	}

	@Test
	public void testR() throws Exception {
		// The result should be: (C, 1, A), (C, 1, B)

		IRelation relation = evaluate("?- r(?X, ?Y, ?Z).");

		Assert.assertTrue("(C, 1, A) not in relation.",
				relation.contains(Helper.createConstantTuple("C", "1", "A")));
		Assert.assertTrue("(C, 1, B) not in relation.",
				relation.contains(Helper.createConstantTuple("C", "1", "B")));

		Assert.assertEquals("Relation does not have correct size", 2,
				relation.size());
	}

}
