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
package org.deri.iris.rdb.rules.optimization;

import java.util.Collections;
import java.util.List;

import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.rdb.facts.RdbFacts;
import org.deri.iris.rules.IRuleOptimiser;

/**
 * A {@link IRuleOptimiser} that adds a literal "true" to the body of a rule,
 * that has no literals in it's body.
 */
public class EmptyRuleBodyOptimizer implements IRuleOptimiser {

	@Override
	public IRule optimise(IRule rule) {
		if (rule == null || rule.getBody().size() > 0) {
			return rule;
		}

		IPredicate truePredicate = RdbFacts.TRUE_PREDICATE;
		ITuple tuple = Factory.BASIC.createTuple();
		ILiteral literal = Factory.BASIC.createLiteral(true, truePredicate,
				tuple);

		List<ILiteral> newBody = Collections.singletonList(literal);

		return Factory.BASIC.createRule(rule.getHead(), newBody);
	}
	
}
