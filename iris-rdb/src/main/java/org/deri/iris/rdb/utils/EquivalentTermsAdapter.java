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

import java.util.Set;

import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.deri.iris.utils.equivalence.IEquivalentTerms;

public class EquivalentTermsAdapter implements IEquivalentTerms {

	private IRelation equivalenceRelation;

	public EquivalentTermsAdapter(IRelation equivalenceRelation) {
		this.equivalenceRelation = equivalenceRelation;
	}

	@Override
	public boolean areEquivalent(ITerm x, ITerm y) {
		ITuple tuple = Factory.BASIC.createTuple(x, y);
		return equivalenceRelation.contains(tuple);
	}

	@Override
	public void setEquivalent(ITerm x, ITerm y) {
		ITuple tuple = Factory.BASIC.createTuple(x, y);
		equivalenceRelation.add(tuple);
	}

	@Override
	public ITerm findRepresentative(ITerm term) {
		return null;
	}

	@Override
	public Set<ITerm> getEquivalent(ITerm term) {
		return null;
	}
	
	@Override
	public String toString() {
		return equivalenceRelation.toString();
	}

}
