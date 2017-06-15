/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package smartcyp;


import java.util.Comparator;

import org.openscience.cdk.Atom;

import smartcyp.MoleculeKU.SMARTCYP_PROPERTY;

public class AtomComparator2C9 implements Comparator<Atom> {

	private final int before = -1;
	private final int equal = 0;		// Only used for symmetric atoms, not atoms with same Score
	private final int after = 1;

	double currentAtomScore;
	double comparisonAtomScore;
	double currentAtomAccessibility;
	double comparisonAtomAccessibility;



	// Atoms sorted by Energy and A
	// My implementation of compare, compares E and A
	public int compare(Atom currentAtom, Atom comparisonAtom) {
		

		return this.compareScore(currentAtom, comparisonAtom);

	}



	private int compareScore(Atom currentAtom, Atom comparisonAtom){
		
		// Set Score values
		if(SMARTCYP_PROPERTY.Score2C9.get(currentAtom) != null)  currentAtomScore = SMARTCYP_PROPERTY.Score2C9.get(currentAtom).doubleValue();
		if(SMARTCYP_PROPERTY.Score2C9.get(comparisonAtom) != null)  comparisonAtomScore = SMARTCYP_PROPERTY.Score2C9.get(comparisonAtom).doubleValue();
		
		// Dual null Scores
		if (SMARTCYP_PROPERTY.Score2C9.get(currentAtom) == null && SMARTCYP_PROPERTY.Score2C9.get(comparisonAtom) == null){					
			//If scores are null the Energies are too, then compare the Accessibility
			return this.compareAccessibility(currentAtom, comparisonAtom);
		}

		// Single null scores
		else if(SMARTCYP_PROPERTY.Score2C9.get(currentAtom) == null) return after;
		else if(SMARTCYP_PROPERTY.Score2C9.get(comparisonAtom) == null) return before;

		// Compare 2 numeric scores
		else if(currentAtomScore < comparisonAtomScore) return before;
		else if(currentAtomScore > comparisonAtomScore) return after;

		// Distinguish symmetric atoms
		else return this.checksymmetry(currentAtom, comparisonAtom);

	}



	private int compareAccessibility(Atom currentAtom, Atom comparisonAtom){

		// Compare 2 numeric Accessibility values
		currentAtomAccessibility = SMARTCYP_PROPERTY.Accessibility.get(currentAtom).doubleValue();
		comparisonAtomAccessibility = SMARTCYP_PROPERTY.Accessibility.get(comparisonAtom).doubleValue();
		
		if(currentAtomAccessibility < comparisonAtomAccessibility) return after;
		else if(currentAtomAccessibility > comparisonAtomAccessibility) return before;

		// Distinguish symmetric atoms
		else return this.checksymmetry(currentAtom, comparisonAtom);

	}

	private int checksymmetry(Atom currentAtom, Atom comparisonAtom){

		// Symmetric
		if(SMARTCYP_PROPERTY.SymmetryNumber.get(currentAtom).intValue() == SMARTCYP_PROPERTY.SymmetryNumber.get(comparisonAtom).intValue()) return equal;
		
		// Non-symmetric
		else return after;
	}


}