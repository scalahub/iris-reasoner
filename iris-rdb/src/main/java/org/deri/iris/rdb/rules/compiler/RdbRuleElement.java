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
package org.deri.iris.rdb.rules.compiler;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.facts.IFacts;
import org.deri.iris.rdb.storage.IRdbRelation;

public abstract class RdbRuleElement {

	/**
	 * Default constructor.
	 */
	public RdbRuleElement() {
	}

	/**
	 * Called to process tuples from previous literals.
	 * 
	 * @param previous
	 *            The relation of tuples from the previous rule element. This
	 *            should be null if this element represents the first literal.
	 * @return The output relation for this literal.
	 * @throws EvaluationException
	 */
	public abstract IRdbRelation process(IRdbRelation input)
			throws EvaluationException;

	public abstract RdbRuleElement getDeltaSubstitution(IFacts deltas);

	public abstract ITuple getOutputTuple();
	
	public abstract void dispose();

}
