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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.openscience.cdk.Atom;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.atomtype.IAtomTypeMatcher;
import org.openscience.cdk.atomtype.SybylAtomTypeMatcher;
import org.openscience.cdk.config.AtomTypeFactory;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.PathTools;
import org.openscience.cdk.graph.invariant.EquivalentClassPartitioner;
import org.openscience.cdk.graph.matrix.AdjacencyMatrix;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IRing;
import org.openscience.cdk.interfaces.IRingSet;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.ringsearch.SSSRFinder;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;

public class MoleculeKU extends AtomContainer implements IMolecule {
	
	public enum SMARTCYP_PROPERTY {
		SymmetryNumber,
		IsSymmetric,
		NrofSymmetricSites,
		AtomIsTertiaryAlkylAmine,
		Score {
			@Override
			public String getLabel() {
				return "S";
			}
		},
		Score2D6 {
			@Override
			public String getLabel() {
				return "S2D6";
			}
		},
		Score2C9 {
			@Override
			public String getLabel() {
				return "S2C9";
			}
		},
		Ranking {
			@Override
			public String getLabel() {
				return "R";
			}
		},
		Ranking2D6 {
			@Override
			public String getLabel() {
				return "R";
			}
		},
		Ranking2C9 {
			@Override
			public String getLabel() {
				return "R";
			}
		},
		Energy {
			@Override
			public String getLabel() {
				return "E";
			}
		},
		Accessibility {
			@Override
			public String getLabel() {
				return "A";
			}
		},
		Span2End {
			@Override
			public String getLabel() {
				return "S";
			}
		},
		Dist2CarboxylicAcid {
			@Override
			public String getLabel() {
				return "DC";
			}
		},
		Dist2ProtAmine {
			@Override
			public String getLabel() {
				return "DP";
			}
		},
		SASA2D{
			@Override
			public String getLabel() {
				return "S2";
			}
		};

		public String  getLabel()  { return "";};

		public void set(IAtom atom, Number value) {
			atom.setProperty(toString(), value);
		}

		public Number get(IAtom atom) {
			Object o = atom.getProperty(toString());
			return (o==null)?null:(Number)o;
		}

		public String atomProperty2String(IAtom atom) {
			return String.format("%s:%s",getLabel(),get(atom));
		}

	}
	// Local variables
	private static final long serialVersionUID = 1L;	
	AtomComparator atomComparator = new AtomComparator();
	private TreeSet<Atom> atomsSortedByEnA = new TreeSet<Atom>(atomComparator);
	AtomComparator2D6 atomComparator2D6 = new AtomComparator2D6();
	private TreeSet<Atom> atomsSortedByEnA2D6 = new TreeSet<Atom>(atomComparator2D6);
	AtomComparator2C9 atomComparator2C9 = new AtomComparator2C9();
	private TreeSet<Atom> atomsSortedByEnA2C9 = new TreeSet<Atom>(atomComparator2C9);
	private int HighestSymmetryNumber = 0;


	// Constructor
	// This constructor also calls the methods that calculate MaxTopDist, Energies and sorts C, N, P and S atoms
	// This constructor is the only way to create MoleculeKU and Atom objects, -there is no add() method
	public MoleculeKU(IAtomContainer iAtomContainer, HashMap<String, Double> SMARTSnEnergiesTable) throws CloneNotSupportedException
	{
		// Calls the constructor in org.openscience.cdk.AtomContainer
		// Atoms are stored in the array atoms[] and accessed by getAtom() and setAtom()
		super(iAtomContainer);			
		int number = 1;
		for (int atomIndex=0; atomIndex < iAtomContainer.getAtomCount(); atomIndex++) {
			iAtomContainer.getAtom(atomIndex).setID(String.valueOf(number));
			number++;
		}
	}



	public void assignAtomEnergies(HashMap<String, Double> SMARTSnEnergiesTable) throws CDKException {

		// Variables
		int numberOfSMARTSmatches = 0;															// Number of SMARTS matches = number of metabolic sites

		// Iterate over the SMARTS in SMARTSnEnergiesTable
		Set<String> keySetSMARTSnEnergies = (Set<String>) SMARTSnEnergiesTable.keySet();
		Iterator<String> keySetIteratorSMARTSnEnergies = keySetSMARTSnEnergies.iterator();

		String currentSMARTS = "C";
		SMARTSQueryTool querytool = new SMARTSQueryTool(currentSMARTS);					// Creates the Query Tool


		while(keySetIteratorSMARTSnEnergies.hasNext()){

			try {
				currentSMARTS = keySetIteratorSMARTSnEnergies.next();
				querytool.setSmarts(currentSMARTS);

				// Check if there are any SMARTS matches
				boolean status = querytool.matches(this);
				if (status) {


					numberOfSMARTSmatches = querytool.countMatches();		// Count the number of matches				
					List<List<Integer>> matchingAtomsIndicesList_1;				// List of List objects each containing the indices of the atoms in the target molecule
					List<Integer> matchingAtomsIndicesList_2 = null;						// List of atom indices
					double energy = SMARTSnEnergiesTable.get(currentSMARTS);		// Energy of currentSMARTS

					//					System.out.println("\n The SMARTS " + currentSMARTS + " has " + numberOfSMARTSmatches + " matches in the molecule " + this.getID());

					matchingAtomsIndicesList_1 = querytool.getMatchingAtoms();													// This list contains the C, N, P and S atom indices

					for(int listObjectIndex = 0; listObjectIndex < numberOfSMARTSmatches; listObjectIndex++){						

						matchingAtomsIndicesList_2 = matchingAtomsIndicesList_1.get(listObjectIndex);							// Contains multiple atoms

						// System.out.println("How many times numberOfSMARTSmatches: " + numberOfSMARTSmatches);							
						// System.out.println("atomID " +this.getAtom(atomNr).getID()+ ", energy " + energy);


						// Set the Energies of the atoms
						int indexOfMatchingAtom;
						Atom matchingAtom;
						for (int atomNr = 0; atomNr < matchingAtomsIndicesList_2.size(); atomNr++){								// Contains 1 atom
							indexOfMatchingAtom = matchingAtomsIndicesList_2.get(atomNr);

							// An atom can be matched by several SMARTS and thus assigned several energies
							// The if clause assures that atoms will getServiceDescriptors the lowest possible energy
							matchingAtom = (Atom) this.getAtom(indexOfMatchingAtom);

							if(SMARTCYP_PROPERTY.Energy.get(matchingAtom) == null 
									|| energy < SMARTCYP_PROPERTY.Energy.get(matchingAtom).doubleValue())
								SMARTCYP_PROPERTY.Energy.set(matchingAtom,energy);
						}
					}
				}
			}	
			catch (CDKException e) {System.out.println("There is something fishy with the SMARTS: " + currentSMARTS); e.printStackTrace();}
		}
		//assign energy 999 to all atoms not matching a SMARTS
		for (int testAtomNr=0; testAtomNr < this.getAtomCount(); testAtomNr++){
			IAtom testAtom;
			testAtom = this.getAtom(testAtomNr);
			if(SMARTCYP_PROPERTY.Energy.get(testAtom) == null) {
				SMARTCYP_PROPERTY.Energy.set(testAtom,999);
			}
		}
	}

	// Calculates the Accessibilities of all atoms
	public void calculateAtomAccessabilities() throws CloneNotSupportedException{


		int[][] adjacencyMatrix = AdjacencyMatrix.getMatrix(this);

		// Calculate the maximum topology distance
		// Takes an adjacency matrix and outputs and MaxTopDist matrix of the same size
		int[][] minTopDistMatrix = PathTools.computeFloydAPSP(adjacencyMatrix);


		// Find the longest Path of all, "longestMaxTopDistInMolecule"
		double longestMaxTopDistInMolecule = 0;
		double currentMaxTopDist = 0;
		for(int x = 0; x < this.getAtomCount(); x++){
			for(int y = 0; y < this.getAtomCount(); y++){
				currentMaxTopDist =  minTopDistMatrix[x][y];
				if(currentMaxTopDist > longestMaxTopDistInMolecule) longestMaxTopDistInMolecule = currentMaxTopDist;
			}
		}


		// Find the Accessibility value ("longest shortestPath") for each atom

		// ITERATE REFERENCE ATOMS
		for (int refAtomNr=0; refAtomNr < this.getAtomCount(); refAtomNr++){

			// ITERATE COMPARISON ATOMS
			double highestMaxTopDistInMatrixRow = 0;
			IAtom refAtom;
			for (int compAtomNr = 0; compAtomNr < this.getAtomCount(); compAtomNr++){
				if(highestMaxTopDistInMatrixRow < minTopDistMatrix[refAtomNr][compAtomNr]) highestMaxTopDistInMatrixRow = minTopDistMatrix[refAtomNr][compAtomNr];
			}	

			refAtom = this.getAtom(refAtomNr);
			// Set the Accessibility of the Atom
			SMARTCYP_PROPERTY.Accessibility.set(refAtom,(highestMaxTopDistInMatrixRow / longestMaxTopDistInMolecule));
		}
	}

	// Compute the score of all atoms
	public void calculateAtomScores() throws CloneNotSupportedException{

		// ITERATE ATOMS
		for (int refAtomNr=0; refAtomNr < this.getAtomCount(); refAtomNr++){
			IAtom refAtom;
			refAtom = this.getAtom(refAtomNr);
			// Calculate the Atom scores
			if(SMARTCYP_PROPERTY.Accessibility.get(refAtom)!=null) {
				if(SMARTCYP_PROPERTY.Energy.get(refAtom) != null){
					double score = SMARTCYP_PROPERTY.Energy.get(refAtom).doubleValue() - 8 * SMARTCYP_PROPERTY.Accessibility.get(refAtom).doubleValue() - 0.04 * SMARTCYP_PROPERTY.SASA2D.get(refAtom).doubleValue();
					SMARTCYP_PROPERTY.Score.set(refAtom,score);
				}
			}
		}
	}

	// Compute the 2D6 score of all atoms
	public void calculate2D6AtomScores() throws CloneNotSupportedException{

		// ITERATE ATOMS
		for (int refAtomNr=0; refAtomNr < this.getAtomCount(); refAtomNr++){
			IAtom refAtom;
			refAtom = this.getAtom(refAtomNr);
			double CorrectionDist2ProtAmine;
			double CorrectionSpan2End;
			//double x;
			CorrectionDist2ProtAmine = 0;
			int span2end; 
			int cutoff = 8;
			int s2endcutoff = 4;
			double constant = 6.7;
			// Calculate the Atom scores
			if(SMARTCYP_PROPERTY.Accessibility.get(refAtom)!=null) {
				if(SMARTCYP_PROPERTY.Energy.get(refAtom) != null){
					if(SMARTCYP_PROPERTY.Dist2ProtAmine.get(refAtom) != null){
						//x = SMARTCYP_PROPERTY.Dist2ProtAmine.getServiceDescriptors(refAtom).intValue();
						//if(SMARTCYP_PROPERTY.Dist2ProtAmine.getServiceDescriptors(refAtom).intValue()>4 && SMARTCYP_PROPERTY.Dist2ProtAmine.getServiceDescriptors(refAtom).intValue()<10) Correction2D6 = 30 * Math.exp(-0.5 * (x - 7.5) * (x - 7.5)) + 15 * Math.exp(-0.05 * (x - 7.5) * (x - 7.5));
						//x = Math.abs(SMARTCYP_PROPERTY.Dist2ProtAmine.getServiceDescriptors(refAtom).doubleValue() - 7.5);
						double ProtAmineDist = SMARTCYP_PROPERTY.Dist2ProtAmine.get(refAtom).doubleValue();
						CorrectionDist2ProtAmine = 0;
						if(ProtAmineDist < cutoff) CorrectionDist2ProtAmine = constant*(cutoff - ProtAmineDist);
					}
					else CorrectionDist2ProtAmine = 0;
					span2end = SMARTCYP_PROPERTY.Span2End.get(refAtom).intValue();
					if(span2end < s2endcutoff){
						CorrectionSpan2End = constant*span2end;
					}
					else CorrectionSpan2End = constant*s2endcutoff + 0.01*span2end;
					double score = SMARTCYP_PROPERTY.Energy.get(refAtom).doubleValue() + CorrectionDist2ProtAmine + CorrectionSpan2End  - 0.04 * SMARTCYP_PROPERTY.SASA2D.get(refAtom).doubleValue();
					SMARTCYP_PROPERTY.Score2D6.set(refAtom,score);
				}
			}
		}
	}

	// Compute the 2C9 score of all atoms
	public void calculate2C9AtomScores() throws CloneNotSupportedException{

		// ITERATE ATOMS
		for (int refAtomNr=0; refAtomNr < this.getAtomCount(); refAtomNr++){
			IAtom refAtom;
			refAtom = this.getAtom(refAtomNr);
			double CorrectionDist2Carboxylicacid;
			double CorrectionSpan2End;
			//double x;
			CorrectionDist2Carboxylicacid = 0;
			int span2end; 
			int cutoff = 8;
			int s2endcutoff = 4;
			double constant = 5.9;
			// Calculate the Atom scores
			if(SMARTCYP_PROPERTY.Accessibility.get(refAtom)!=null) {
				if(SMARTCYP_PROPERTY.Energy.get(refAtom) != null){
					if(SMARTCYP_PROPERTY.Dist2CarboxylicAcid.get(refAtom) != null){
						double CarboxylicAcidDist = SMARTCYP_PROPERTY.Dist2CarboxylicAcid.get(refAtom).doubleValue();
						CorrectionDist2Carboxylicacid = 0;
						if(CarboxylicAcidDist < cutoff) CorrectionDist2Carboxylicacid = constant*(cutoff - CarboxylicAcidDist);
					}
					else CorrectionDist2Carboxylicacid = 0;
					span2end = SMARTCYP_PROPERTY.Span2End.get(refAtom).intValue();
					if(span2end < s2endcutoff){
						CorrectionSpan2End = constant*span2end;
					}
					else CorrectionSpan2End = constant*s2endcutoff + 0.01*span2end;
					double score = SMARTCYP_PROPERTY.Energy.get(refAtom).doubleValue() + CorrectionDist2Carboxylicacid + CorrectionSpan2End - 0.04 * SMARTCYP_PROPERTY.SASA2D.get(refAtom).doubleValue();
					SMARTCYP_PROPERTY.Score2C9.set(refAtom,score);
				}
			}
		}
	}
	
	// Calculates the Span to end of molecule
	public void calculateSpan2End() throws CloneNotSupportedException{


		int[][] adjacencyMatrix = AdjacencyMatrix.getMatrix(this);

		// Calculate the maximum topology distance
		// Takes an adjacency matrix and outputs and MaxTopDist matrix of the same size
		int[][] minTopDistMatrix = PathTools.computeFloydAPSP(adjacencyMatrix);


		// Find the longest Path of all, "longestMaxTopDistInMolecule"
		double longestMaxTopDistInMolecule = 0;
		double currentMaxTopDist = 0;
		for(int x = 0; x < this.getAtomCount(); x++){
			for(int y = 0; y < this.getAtomCount(); y++){
				currentMaxTopDist =  minTopDistMatrix[x][y];
				if(currentMaxTopDist > longestMaxTopDistInMolecule) longestMaxTopDistInMolecule = currentMaxTopDist;
			}
		}


		// Find the Span2End (maxtopdist - currenttopdist) for each atom

		// ITERATE REFERENCE ATOMS
		for (int refAtomNr=0; refAtomNr < this.getAtomCount(); refAtomNr++){

			// ITERATE COMPARISON ATOMS
			double highestMaxTopDistInMatrixRow = 0;
			IAtom refAtom;
			for (int compAtomNr = 0; compAtomNr < this.getAtomCount(); compAtomNr++){
				if(highestMaxTopDistInMatrixRow < minTopDistMatrix[refAtomNr][compAtomNr]) highestMaxTopDistInMatrixRow = minTopDistMatrix[refAtomNr][compAtomNr];
			}	

			refAtom = this.getAtom(refAtomNr);
			// Set the Accessibility of the Atom
			SMARTCYP_PROPERTY.Span2End.set(refAtom,(longestMaxTopDistInMolecule - highestMaxTopDistInMatrixRow));

		}
	}

	// Calculates the distance to the most distant possibly protonated amine / guanidine nitrogen
	public void calculateDist2ProtAmine() throws CDKException{

		//locate amine nitrogens which could be protonated
		// Variables
		int numberOfSMARTSmatches = 0;	// Number of SMARTS matches = number of protonated amine sites
		
		// new matching 2.2
		String [] SMARTSstrings = {"[$([N][CX3](=[N])[N])" + //guanidine like fragment
				                   ",$([N^3X3H0]([#6^3])([#6^3])[#6^3]),$([N^3X3H1]([#6^3])[#6^3]),$([N^3X3H2][#6^3])]"};   // primary, secondary, tertiary amines bound to only carbon and hydrogen atoms, not next to sp2 carbon
				                
		
		/* old matching 2.1
		String [] SMARTSstrings = {"[$([N][CX3](=[N])[N])" + //guanidine like fragment
				                   ",$([N^3X3H0]([#6^3])([#6^3])[#6^3]),$([N^3X3H1]([#6^3])[#6^3]),$([N^3X3H2][#6^3])" +  // primary, secondary, tertiary amines bound to only carbon and hydrogen atoms, not next to sp2 carbon
				                   ",$([nD2]1[c][nH][c][c]1)" +  //imidazole nitrogen 1
								   ",$([nH]1[c][nD2][c][c]1)" +  //imidazole nitrogen 2
		                           ",$([NX2]([#6,H])=[CX3]([#6,H])[#6])]"}; //imine nitrogen
		*/ //end old matching 2.1
		
		/*start old matching 2.0
		String [] SMARTSstrings = {"[$([N][CX3](=[N])[N]);!$([NX3][S](=[O])=[O])]", 
                "[$([NX3]);!$([NX3][#6X3]);!$([NX3][N]=[O]);!$([NX3][S](=[O])=[O])]"}; 
		*/ //end old matching 2.0
		
		for (String currentSMARTS : SMARTSstrings){
			SMARTSQueryTool querytool = new SMARTSQueryTool(currentSMARTS);		// Creates the Query Tool
	
			querytool.setSmarts(currentSMARTS);
	
			// Check if there are any SMARTS matches
			boolean status = querytool.matches(this);
			if (status) {
	
				numberOfSMARTSmatches = querytool.countMatches();		// Count the number of matches				
				List<List<Integer>> matchingAtomsIndicesList_1;			// List of List objects each containing the indices of the atoms in the target molecule
				List<Integer> matchingAtomsIndicesList_2 = null;		// List of atom indices
	
				matchingAtomsIndicesList_1 = querytool.getMatchingAtoms();	// This list contains the atom indices of protonated amine nitrogens
	
				for(int listObjectIndex = 0; listObjectIndex < numberOfSMARTSmatches; listObjectIndex++){						
	
					matchingAtomsIndicesList_2 = matchingAtomsIndicesList_1.get(listObjectIndex);	// Contains multiple atoms
	
					// Compute distance for all atoms to the matching atoms
					int indexOfMatchingAtom;
					for (int atomNr = 0; atomNr < matchingAtomsIndicesList_2.size(); atomNr++){		// Contains 1 atom
						indexOfMatchingAtom = matchingAtomsIndicesList_2.get(atomNr);
						//System.out.println("\n" + indexOfMatchingAtom);
						int[][] adjacencyMatrix = AdjacencyMatrix.getMatrix(this);
						int[][] minTopDistMatrix = PathTools.computeFloydAPSP(adjacencyMatrix);
						//iterate over all atoms
						for (int refAtomNr=0; refAtomNr < this.getAtomCount(); refAtomNr++){
							Atom refAtom;
							refAtom = (Atom) this.getAtom(refAtomNr);
							int thisdist2protamine;
							thisdist2protamine = minTopDistMatrix[refAtomNr][indexOfMatchingAtom];
							if(SMARTCYP_PROPERTY.Dist2ProtAmine.get(refAtom) != null){
								if(thisdist2protamine > SMARTCYP_PROPERTY.Dist2ProtAmine.get(refAtom).intValue()){
									SMARTCYP_PROPERTY.Dist2ProtAmine.set(refAtom,thisdist2protamine);
								}
							}
							else SMARTCYP_PROPERTY.Dist2ProtAmine.set(refAtom,thisdist2protamine);
						}
					}
				}
			}
		}
	}

	// Calculates the distance to the furthest carboxylic acid
	public void calculateDist2CarboxylicAcid() throws CDKException{
		
		//locate carboxylic acid groups
		// Variables
		int numberOfSMARTSmatches = 0;	// Number of SMARTS matches = number of carboxylic acid sites
					       
		String [] SMARTSstrings = {"[$([O]=[C^2][OH1])" + // carboxylic acid oxygen 
				   ",$([O]=[C^2][C^2]=[C^2][OH1]),$([O]=[C^2][c][c][OH1])" + // vinylogous carboxylic acids (e.g. ascorbic acid)
				   ",$([n]1:[n]:[n]:[n]:[c]1)" + // tetrazole 1
				   ",$([n]1:[n]:[n]:[c]:[n]1)" + // tetrazole 2
				   ",$([O]=[C^2][N][OH1])" + // hydroxamic acid
				   ",$([O]=[C^2]([N])[N])" + // urea
				   ",$([O]=[S][OH1])" + // sulfinic and sulfonic acids 
				   ",$([O]=[PD4][OH1])" + // phosphate esters and phosphoric acids
				   ",$([O]=[S](=[O])(c)[C][C]=[O]),$([O]=[C][C][S](=[O])(=[O])[c])" + // sulfones next to phenyls with carbonyl two bonds away
				   ",$([O]=[S](=[O])[NH1][C]=[O]),$([O]=[C][NH1][S](=[O])=[O])" + // sulfones bound to nitrogen with carbonyl next to it
				   ",$([O]=[C^2][NH1][O]),$([O]=[C^2][NH1][C]#[N])" + // peptide with oxygen or cyano group next to nitrogen
				   ",$([OH1][c]1[n][o,s][c,n][c]1),$([OH1][n]1[n][c,n][c][c]1),$([OH1][n]1[c][n][c][c]1)" + // alcohol on aromatic five membered ring
				   ",$([O]=[C]1[N][C](=O)[O,S][C,N]1)" + // carbonyl oxygen on almost conjugated five membered ring
				   ",$([O]=[C]1[NH1,O][N]=[N,C][N]1)" + // carbonyl oxygen on fully conjugated five membered ring
				   ",$([nD2]1[nD2][c]([S]=[O])[nD2][c]1),$([nD2]1[c]([S]=[O])[nD2][c][nD2]1),$([nD2]1[c]([S]=[O])[nD2][nD2][c]1)" + // nitrogens in histidine-like 5-ring with sulfoxide/sulfone next to it
			       ",$([O]=[SX4](=[O])[NX3])]"}; // sulfonamides
		

		for (String currentSMARTS : SMARTSstrings){
			SMARTSQueryTool querytool = new SMARTSQueryTool(currentSMARTS);		// Creates the Query Tool
			
			querytool.setSmarts(currentSMARTS);
			
			// Check if there are any SMARTS matches
			boolean status = querytool.matches(this);
			if (status) {
				
				numberOfSMARTSmatches = querytool.countMatches();		// Count the number of matches				
				List<List<Integer>> matchingAtomsIndicesList_1;			// List of List objects each containing the indices of the atoms in the target molecule
				List<Integer> matchingAtomsIndicesList_2 = null;		// List of atom indices
				
				matchingAtomsIndicesList_1 = querytool.getMatchingAtoms();	// This list contains the atom indices of protonated amine nitrogens
				
				for(int listObjectIndex = 0; listObjectIndex < numberOfSMARTSmatches; listObjectIndex++){						
				
					matchingAtomsIndicesList_2 = matchingAtomsIndicesList_1.get(listObjectIndex);	// Contains multiple atoms
				
					// Compute distance for all atoms to the matching atoms
					int indexOfMatchingAtom;
					for (int atomNr = 0; atomNr < matchingAtomsIndicesList_2.size(); atomNr++){		// Contains 1 atom
						indexOfMatchingAtom = matchingAtomsIndicesList_2.get(atomNr);
						//System.out.println("\n" + indexOfMatchingAtom);
						int[][] adjacencyMatrix = AdjacencyMatrix.getMatrix(this);
						int[][] minTopDistMatrix = PathTools.computeFloydAPSP(adjacencyMatrix);
						//iterate over all atoms
						for (int refAtomNr=0; refAtomNr < this.getAtomCount(); refAtomNr++){
							Atom refAtom;
							refAtom = (Atom) this.getAtom(refAtomNr);
							int thisdist2carboxylicacid;
							thisdist2carboxylicacid = minTopDistMatrix[refAtomNr][indexOfMatchingAtom];
							if(SMARTCYP_PROPERTY.Dist2CarboxylicAcid.get(refAtom) != null){
								if(thisdist2carboxylicacid > SMARTCYP_PROPERTY.Dist2CarboxylicAcid.get(refAtom).intValue()){
									SMARTCYP_PROPERTY.Dist2CarboxylicAcid.set(refAtom,thisdist2carboxylicacid);
								}
							}
							else SMARTCYP_PROPERTY.Dist2CarboxylicAcid.set(refAtom,thisdist2carboxylicacid);
						}
					}
				}
			}
		}
	}


	//  This method makes atomsSortedByEnA
	public void sortAtoms() throws CDKException{

		Atom currentAtom;
		String currentAtomType;					// Atom symbol i.e. C, H, N, P or S

		// The Symmetry Numbers are needed to compare the atoms (Atom class and the compareTo method) before adding them below
		this.setSymmetryNumbers();
		int[] AddedSymmetryNumbers = new int[this.HighestSymmetryNumber];

		for (int atomNr = 0; atomNr < this.getAtomCount(); atomNr++){

			currentAtom = (Atom) this.getAtom(atomNr);
			int currentSymmetryNumber = SMARTCYP_PROPERTY.SymmetryNumber.get(currentAtom).intValue();

			// Match atom symbol
			currentAtomType = currentAtom.getSymbol();
			if(currentAtomType.equals("C") || currentAtomType.equals("N") || currentAtomType.equals("P") || currentAtomType.equals("S")) {
				if (FindInArray(AddedSymmetryNumbers,currentSymmetryNumber) == 0) {
					atomsSortedByEnA.add(currentAtom);
					AddedSymmetryNumbers[currentSymmetryNumber - 1] = currentSymmetryNumber;
				}
			}
		}
	}
	
	public void sortAtoms2D6() throws CDKException{

		Atom currentAtom;
		String currentAtomType;					// Atom symbol i.e. C, H, N, P or S

		// The Symmetry Numbers are needed to compare the atoms (Atom class and the compareTo method) before adding them below
		this.setSymmetryNumbers();
		int[] AddedSymmetryNumbers = new int[this.HighestSymmetryNumber];
 
		for (int atomNr = 0; atomNr < this.getAtomCount(); atomNr++){

			currentAtom = (Atom) this.getAtom(atomNr);
			int currentSymmetryNumber = SMARTCYP_PROPERTY.SymmetryNumber.get(currentAtom).intValue();

			// Match atom symbol
			currentAtomType = currentAtom.getSymbol();
			if(currentAtomType.equals("C") || currentAtomType.equals("N") || currentAtomType.equals("P") || currentAtomType.equals("S")) {
				if (FindInArray(AddedSymmetryNumbers,currentSymmetryNumber) == 0) {
					atomsSortedByEnA2D6.add(currentAtom);
					AddedSymmetryNumbers[currentSymmetryNumber - 1] = currentSymmetryNumber;
				}
			}
		}
	}

	public void sortAtoms2C9() throws CDKException{

		Atom currentAtom;
		String currentAtomType;					// Atom symbol i.e. C, H, N, P or S

		// The Symmetry Numbers are needed to compare the atoms (Atom class and the compareTo method) before adding them below
		this.setSymmetryNumbers();
		int[] AddedSymmetryNumbers = new int[this.HighestSymmetryNumber];
 
		for (int atomNr = 0; atomNr < this.getAtomCount(); atomNr++){

			currentAtom = (Atom) this.getAtom(atomNr);
			int currentSymmetryNumber = SMARTCYP_PROPERTY.SymmetryNumber.get(currentAtom).intValue();

			// Match atom symbol
			currentAtomType = currentAtom.getSymbol();
			if(currentAtomType.equals("C") || currentAtomType.equals("N") || currentAtomType.equals("P") || currentAtomType.equals("S")) {
				if (FindInArray(AddedSymmetryNumbers,currentSymmetryNumber) == 0) {
					atomsSortedByEnA2C9.add(currentAtom);
					AddedSymmetryNumbers[currentSymmetryNumber - 1] = currentSymmetryNumber;
				}
			}
		}
	}

	// Symmetric atoms have identical values in the array from getTopoEquivClassbyHuXu
	public void setSymmetryNumbers() throws CDKException{
		Atom atom;
		//set charges so that they are not null
		for(int atomIndex = 0; atomIndex < this.getAtomCount(); atomIndex++){
			atom = (Atom) this.getAtom(atomIndex);
			atom.setCharge((double) atom.getFormalCharge());
		}
		//compute symmetry
		EquivalentClassPartitioner symmtest = new EquivalentClassPartitioner((AtomContainer) this);
		int[] symmetryNumbersArray = symmtest.getTopoEquivClassbyHuXu((AtomContainer) this);
		symmetryNumbersArray[0]=0;//so we can count the number of symmetric sites for each atom without double counting for the ones with the highest symmetrynumber
		int symmsites;
		for(int atomIndex = 0; atomIndex < this.getAtomCount(); atomIndex++){
			symmsites = 0;
			atom = (Atom) this.getAtom(atomIndex);
			SMARTCYP_PROPERTY.SymmetryNumber.set(atom,symmetryNumbersArray[atomIndex+1]);
			// Compute how many symmetric sites the atom has, 1=only itself
			symmsites = FindInArray(symmetryNumbersArray,symmetryNumbersArray[atomIndex+1]);
			SMARTCYP_PROPERTY.NrofSymmetricSites.set(atom,symmsites);

			if (symmetryNumbersArray[atomIndex+1] > HighestSymmetryNumber) HighestSymmetryNumber = symmetryNumbersArray[atomIndex+1];
		}
	}



	// This method makes the ranking
	public void rankAtoms() throws CDKException{

		// Iterate over the Atoms in this sortedAtomsTreeSet
		int rankNr = 1;
		int loopNr = 1;
		Atom previousAtom = null;
		Atom currentAtom;
		Iterator<Atom> atomsSortedByEnAiterator = this.getAtomsSortedByEnA().iterator();
		while(atomsSortedByEnAiterator.hasNext()){

			currentAtom = atomsSortedByEnAiterator.next();

			// First Atom
			if(previousAtom == null){}				// Do nothing												

			// Atoms have no score, compare Accessibility instead
			else if(SMARTCYP_PROPERTY.Score.get(currentAtom) == null){
				if(SMARTCYP_PROPERTY.Accessibility.get(currentAtom) != SMARTCYP_PROPERTY.Accessibility.get(previousAtom)) rankNr = loopNr;
			} 

			// Compare scores
			else if(SMARTCYP_PROPERTY.Score.get(currentAtom).doubleValue() > SMARTCYP_PROPERTY.Score.get(previousAtom).doubleValue()) rankNr = loopNr;

			// Else, Atoms have the same score
			SMARTCYP_PROPERTY.Ranking.set(currentAtom,rankNr);
			previousAtom = currentAtom;	
			loopNr++;
		}

		this.rankSymmetricAtoms();
	}

	// This method makes the ranking
	public void rankAtoms2D6() throws CDKException{

		// Iterate over the Atoms in this sortedAtomsTreeSet
		int rankNr = 1;
		int loopNr = 1;
		Atom previousAtom = null;
		Atom currentAtom;
		Iterator<Atom> atomsSortedByEnAiterator = this.getAtomsSortedByEnA2D6().iterator();
		while(atomsSortedByEnAiterator.hasNext()){

			currentAtom = atomsSortedByEnAiterator.next();

			// First Atom
			if(previousAtom == null){}				// Do nothing												

			// Atoms have no score, compare Accessibility instead
			//else if(SMARTCYP_PROPERTY.Score2D6.getServiceDescriptors(currentAtom) == null){
			//	if(SMARTCYP_PROPERTY.Accessibility.getServiceDescriptors(currentAtom) != SMARTCYP_PROPERTY.Accessibility.getServiceDescriptors(previousAtom)) rankNr = loopNr;
			//} 

			// Compare scores
			else if(SMARTCYP_PROPERTY.Score2D6.get(currentAtom).doubleValue() > SMARTCYP_PROPERTY.Score2D6.get(previousAtom).doubleValue()) rankNr = loopNr;

			// Else, Atoms have the same score
			SMARTCYP_PROPERTY.Ranking2D6.set(currentAtom,rankNr);
			previousAtom = currentAtom;	
			loopNr++;
		}

		this.rankSymmetricAtoms2D6();
	}

	// This method makes the ranking
	public void rankAtoms2C9() throws CDKException{

		// Iterate over the Atoms in this sortedAtomsTreeSet
		int rankNr = 1;
		int loopNr = 1;
		Atom previousAtom = null;
		Atom currentAtom;
		Iterator<Atom> atomsSortedByEnAiterator = this.getAtomsSortedByEnA2C9().iterator();
		while(atomsSortedByEnAiterator.hasNext()){

			currentAtom = atomsSortedByEnAiterator.next();

			// First Atom
			if(previousAtom == null){}				// Do nothing												

			// Atoms have no score, compare Accessibility instead
			//else if(SMARTCYP_PROPERTY.Score2D6.getServiceDescriptors(currentAtom) == null){
			//	if(SMARTCYP_PROPERTY.Accessibility.getServiceDescriptors(currentAtom) != SMARTCYP_PROPERTY.Accessibility.getServiceDescriptors(previousAtom)) rankNr = loopNr;
			//} 

			// Compare scores
			else if(SMARTCYP_PROPERTY.Score2C9.get(currentAtom).doubleValue() > SMARTCYP_PROPERTY.Score2C9.get(previousAtom).doubleValue()) rankNr = loopNr;

			// Else, Atoms have the same score
			SMARTCYP_PROPERTY.Ranking2C9.set(currentAtom,rankNr);
			previousAtom = currentAtom;	
			loopNr++;
		}

		this.rankSymmetricAtoms2C9();
	}

	// This method makes the ranking of symmetric atoms
	public void rankSymmetricAtoms() throws CDKException{

		Atom currentAtom;
		String currentAtomType;					// Atom symbol i.e. C, H, N, P or S

		for (int atomNr = 0; atomNr < this.getAtomCount(); atomNr++){

			currentAtom = (Atom) this.getAtom(atomNr);

			// Match atom symbol
			currentAtomType = currentAtom.getSymbol();
			if(currentAtomType.equals("C") || currentAtomType.equals("N") || currentAtomType.equals("P") || currentAtomType.equals("S")) {			

				//This clause finds symmetric atoms which have not been assigned a ranking
				if(SMARTCYP_PROPERTY.Ranking.get(currentAtom) == null){

					// AtomsSortedByEnA contains the ranked atoms
					// We just need to find the symmetric atom and use its ranking for the unranked symmetric atom
					Iterator<Atom> atomsSortedByEnAiterator = this.getAtomsSortedByEnA().iterator();
					Atom rankedAtom;
					Number rankNr;
					while(atomsSortedByEnAiterator.hasNext()){

						rankedAtom = atomsSortedByEnAiterator.next();

						if(SMARTCYP_PROPERTY.SymmetryNumber.get(currentAtom).intValue() == SMARTCYP_PROPERTY.SymmetryNumber.get(rankedAtom).intValue()){

							rankNr = SMARTCYP_PROPERTY.Ranking.get(rankedAtom);
							SMARTCYP_PROPERTY.Ranking.set(currentAtom,rankNr);
							SMARTCYP_PROPERTY.IsSymmetric.set(currentAtom,1);

						}
					}

				}

			}
		}
	}

	// This method makes the ranking of symmetric atoms
	public void rankSymmetricAtoms2D6() throws CDKException{

		Atom currentAtom;
		String currentAtomType;					// Atom symbol i.e. C, H, N, P or S

		for (int atomNr = 0; atomNr < this.getAtomCount(); atomNr++){

			currentAtom = (Atom) this.getAtom(atomNr);

			// Match atom symbol
			currentAtomType = currentAtom.getSymbol();
			if(currentAtomType.equals("C") || currentAtomType.equals("N") || currentAtomType.equals("P") || currentAtomType.equals("S")) {			

				//This clause finds symmetric atoms which have not been assigned a ranking
				if(SMARTCYP_PROPERTY.Ranking2D6.get(currentAtom) == null){

					// AtomsSortedByEnA contains the ranked atoms
					// We just need to find the symmetric atom and use its ranking for the unranked symmetric atom
					Iterator<Atom> atomsSortedByEnAiterator = this.getAtomsSortedByEnA2D6().iterator();
					Atom rankedAtom;
					Number rankNr;
					while(atomsSortedByEnAiterator.hasNext()){

						rankedAtom = atomsSortedByEnAiterator.next();

						if(SMARTCYP_PROPERTY.SymmetryNumber.get(currentAtom).intValue() == SMARTCYP_PROPERTY.SymmetryNumber.get(rankedAtom).intValue()){

							rankNr = SMARTCYP_PROPERTY.Ranking2D6.get(rankedAtom);
							SMARTCYP_PROPERTY.Ranking2D6.set(currentAtom,rankNr);
							SMARTCYP_PROPERTY.IsSymmetric.set(currentAtom,1);

						}
					}

				}

			}
		}
	}	

	// This method makes the ranking of symmetric atoms
	public void rankSymmetricAtoms2C9() throws CDKException{

		Atom currentAtom;
		String currentAtomType;					// Atom symbol i.e. C, H, N, P or S

		for (int atomNr = 0; atomNr < this.getAtomCount(); atomNr++){

			currentAtom = (Atom) this.getAtom(atomNr);

			// Match atom symbol
			currentAtomType = currentAtom.getSymbol();
			if(currentAtomType.equals("C") || currentAtomType.equals("N") || currentAtomType.equals("P") || currentAtomType.equals("S")) {			

				//This clause finds symmetric atoms which have not been assigned a ranking
				if(SMARTCYP_PROPERTY.Ranking2C9.get(currentAtom) == null){

					// AtomsSortedByEnA contains the ranked atoms
					// We just need to find the symmetric atom and use its ranking for the unranked symmetric atom
					Iterator<Atom> atomsSortedByEnAiterator = this.getAtomsSortedByEnA2C9().iterator();
					Atom rankedAtom;
					Number rankNr;
					while(atomsSortedByEnAiterator.hasNext()){

						rankedAtom = atomsSortedByEnAiterator.next();

						if(SMARTCYP_PROPERTY.SymmetryNumber.get(currentAtom).intValue() == SMARTCYP_PROPERTY.SymmetryNumber.get(rankedAtom).intValue()){

							rankNr = SMARTCYP_PROPERTY.Ranking2C9.get(rankedAtom);
							SMARTCYP_PROPERTY.Ranking2C9.set(currentAtom,rankNr);
							SMARTCYP_PROPERTY.IsSymmetric.set(currentAtom,1);

						}
					}

				}

			}
		}
	}	
	
	public void unlikelyNoxidationCorrection() throws CDKException {

		ArrayList<String> EmpiricalSMARTS = new ArrayList<String>();
		//select nitrogen oxidations never or almost never seen
		EmpiricalSMARTS.add("[$([NX3H0]([CX4])([CX4])[CX4]);!$([NX3]@[C])]"); //tertiary amine nitrogen not in ring
		EmpiricalSMARTS.add("[$([NX3H0]1([CX4])[CX4][CX4][CX4][CX4][CX4]1);!$([NX3]1(@[C])[C][C][C][C][C]1);!$([NX3]1[C](@[*])[C][C][C][C]1)]"); //tertiary amine piperidine nitrogen
		EmpiricalSMARTS.add("[$([NR2H0]1([CX4])[CX4]2[CX4][CX4][CX4][CX4]1[CX4][CX4][CX4]2)]"); //tertiary amine in bridged dual 6-rings
		EmpiricalSMARTS.add("[$([NX3H0]1([CX4])[CX3][C][NX3][C][CX4]1);!$([NX3]1([C])[C][C][NX3]([#6X3])[C][C]1);!$([NX3]1([#6X3])[C][C][NX3][C][C]1)]"); //piperazine nitrogen
		EmpiricalSMARTS.add("[$([N^3H0]);$([NR2r6](@[CX4])(@[CX4])@[CX4])]"); //nitrogen bridging two six rings as in octahydroquinolizine
		
		
		// Variables
		double correction = 100.0; //the penalty (in kJ/mol) which is added to the score of the atoms chosen
		int numberOfSMARTSmatches = 0;
		String currentSMARTS = "C";
		SMARTSQueryTool querytool = new SMARTSQueryTool(currentSMARTS); // Creates the Query Tool

		// Iterate over the SMARTS in the array above
		Iterator<String> itr = EmpiricalSMARTS.iterator();
		while (itr.hasNext()){
			try {
				currentSMARTS = itr.next();
				querytool.setSmarts(currentSMARTS);

				// Check if there are any SMARTS matches
				boolean status = querytool.matches(this);
				if (status) {
					numberOfSMARTSmatches = querytool.countMatches();	// Count the number of matches				
					List<List<Integer>> matchingAtomsIndicesList_1;		// List of List objects each containing the indices of the atoms in the target molecule
					List<Integer> matchingAtomsIndicesList_2 = null;	// List of atom indices
					//System.out.println("\n The SMARTS " + currentSMARTS + " has " + numberOfSMARTSmatches + " matches in the molecule " + this.getID());
					
					matchingAtomsIndicesList_1 = querytool.getMatchingAtoms();													// This list contains the C, N, P and S atom indices

					for(int listObjectIndex = 0; listObjectIndex < numberOfSMARTSmatches; listObjectIndex++){						

						matchingAtomsIndicesList_2 = matchingAtomsIndicesList_1.get(listObjectIndex);							// Contains multiple atoms

						// Set the descriptorvalue of the atoms which should be empirically corrected
						int indexOfMatchingAtom;
						Atom matchingAtom;
						double newenergy = 0.0;
						for (int atomNr = 0; atomNr < matchingAtomsIndicesList_2.size(); atomNr++){								// Contains 1 atom
							indexOfMatchingAtom = matchingAtomsIndicesList_2.get(atomNr);
							matchingAtom = (Atom) this.getAtom(indexOfMatchingAtom);
							newenergy = SMARTCYP_PROPERTY.Energy.get(matchingAtom).doubleValue() + correction;
							SMARTCYP_PROPERTY.Energy.set(matchingAtom,newenergy);
							//System.out.println(indexOfMatchingAtom);
						}
					}
				}
			}	
			catch (CDKException e) {System.out.println("There is something fishy with the SMARTS: " + currentSMARTS); e.printStackTrace();}
		}	
	}
	
	// Calculates the SASA descriptor for all atoms
	public double[] calculateSASA() throws CDKException{
		int debug = 0;
		int maxLevel = 4;
		IAtomType[] atomTypes = null;
		int natoms = this.getAtomCount();
		
		double[] SASA = new double[natoms];
		
		//initiate ring perception
		SSSRFinder ringfinder = new SSSRFinder(this);
		IRingSet ringset = ringfinder.findSSSR();
		//ringset now contains all the rings in the molecule
		
		//do atom type matching
		IAtomTypeMatcher atm = SybylAtomTypeMatcher.getInstance(NoNotificationChemObjectBuilder.getInstance());
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream("org/openscience/cdk/dict/data/sybyl-atom-types.owl");
		@SuppressWarnings("deprecation")
		AtomTypeFactory factory = AtomTypeFactory.getInstance(ins,"owl",NoNotificationChemObjectBuilder.getInstance());
		atomTypes = factory.getAllAtomTypes();
		
		//map atomtypes to the atomIndex integer array
		TreeMap<String,Integer> map = new TreeMap<String,Integer>();
        for (int i = 0; i < atomTypes.length; i++) { 
            map.put(atomTypes[i].getAtomTypeName(),new Integer(i));
        }
        int[] atomIndex = new int[natoms]; //array of atom type integers
        for (int i = 0; i < natoms; i++) {
            try {
                IAtomType a = atm.findMatchingAtomType(this,this.getAtom(i));
                if ( a != null) {
                    Object mappedType = map.get(a.getAtomTypeName());
                    if (mappedType != null)	
                        atomIndex[i] = ((Integer) mappedType).intValue();
                    else {
                        //System.out.println(a.getAtomTypeName() + " not found in " + map);
                        atomIndex[i] = -1;
                    }    
                } else //atom type not found 
                	atomIndex[i] = -1;
            } catch (Exception x) {
                x.printStackTrace();
                throw new CDKException(x.getMessage() + "\ninitConnectionMatrix");
            }                
        }
        
        //compute bond distances between all atoms
        int[][] aMatrix = PathTools.computeFloydAPSP(AdjacencyMatrix.getMatrix(this));
        
        //assign values to the results arrays for all atoms
		int L = (atomTypes.length + 7) ; //7 is size of ring vector
		int [][] result = new int[natoms][L*(maxLevel+1) + 3]; //create result array, 3 is size of neighbor count vector
		
		//compute largestring for all atoms in molecule
		int [] largestrings = new int [natoms];
		for (int i = 0; i < natoms; i++) {
			try {
				largestrings[i] = calculateLargestRing(this.getAtom(i),ringset);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//construct atom type counts and ring counts
		for (int i = 0; i < natoms; i++) {
			
			//determine number of neighbours
			List<IAtom> neighbors = this.getConnectedAtomsList(this.getAtom(i));
			int degree = neighbors.size();
			
			// determine max levels depending on degree
			int AtomTypemaxLevel = maxLevel;
			if (degree == 1) AtomTypemaxLevel = 3;
			if (degree == 2 || degree == 3) AtomTypemaxLevel = 2;
			if (degree == 4) AtomTypemaxLevel = 1;
			int RingmaxLevel = maxLevel;
			if (degree == 1) RingmaxLevel = 2;
			if (degree == 2) RingmaxLevel = 1;
			if (degree== 3 || degree == 4) RingmaxLevel = 0;
			
			//ring perception for atom i
    		int [] rings;
    		try {
				rings = calculateRings(this.getAtom(i), ringset);
				//System.out.print(this.getAtom(i).getID() + ": ");
				for (int k = 0; k < 7; k++){
					result[i][L-7+k] = rings[k];
					//System.out.print(rings[k] + " ");
				}
				//System.out.println(" ");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			//for every atom, iterate through its connections to all other atoms
			for (int j=0; j < natoms; j++) {
		        //atom type perception
		        if (aMatrix[i][j] <= AtomTypemaxLevel){ //j is bonds less or equal to AtomTypemaxLevel away from i
		        	if (atomIndex[j] >= 0) //atom type defined in factory
			            result[i][L*(aMatrix[i][j])+atomIndex[j]]++;
		        	else if (atomIndex[j] == -1) //-1, unknown  type
		        		result[i][L*(aMatrix[i][j])+(L-8)]++;
		        }
	        	//add ring perception for other atoms than atom i
		        if(aMatrix[i][j] <= RingmaxLevel && aMatrix[i][j] != 0){
	        		//atom i is not atom j
	        		int largestring = 0;
					largestring = largestrings[j];
					if (largestring > 2){
						//there is a ring
						if (largestring < 9){
							//the ring is 3-8 atoms
							result[i][L*(aMatrix[i][j])+L-7+(largestring-3)]++;
						}
						else {
							//the ring is larger than 8 atoms
							result[i][L*(aMatrix[i][j])+L-1]++;
						}
					}
	        	}
			}
	        
			// add count of neighbors at levels 2, 3 and 4
	        int count2 = 0;
	        int count3 = 0;
	        int count4 = 0;
	        for (int l=0; l < natoms; l++){
	        	if (aMatrix[i][l] == 2) count2++;
	        	if (aMatrix[i][l] == 3) count3++;
	        	if (aMatrix[i][l] == 4) count4++;
	        }
	        result[i][L*(maxLevel+1)] = count2;
	        result[i][L*(maxLevel+1) + 1] = count3;
	        result[i][L*(maxLevel+1) + 2] = count4;
	        
	        //now compute the SASA
			if (degree == 1){
				SASA[i] = 69.398 + 5.78853*result[i][0] + 8.14842*result[i][1] + 10.902*result[i][3] - 3.79476*(result[i][4] + result[i][12]) - 4.72395*result[i][5] - 0.428496*result[i][6] - 7.86361*result[i][7] - 6.98044*result[i][8] - 0.360787*result[i][14] - 2.77637*result[i][23] + 19.2001*result[i][29] + 21.6887*result[i][30] - 6.90254*result[i][38] + 10.9989*result[i][40] + 21.3963*result[i][41] + 36.6827*result[i][44] + 2.05628*result[i][49] + 1.50124*result[i][61] - 2.75678*result[i][62] + 0.584144*result[i][63] + 0.513805*result[i][64] + 0.687804*(result[i][65] + result[i][73]) - 0.684153*result[i][66] + 3.60709*result[i][67] + 5.16584*result[i][68] + 0.978418*result[i][70] - 3.30868*result[i][72] - 0.474438*result[i][74] + 0.855398*result[i][75] - 0.00514406*result[i][90] - 1.34609*result[i][92] + 0.434973*result[i][93] - 1.66701*result[i][110] + 0.449726*result[i][114] + 2.79506*result[i][115] + 3.16091*result[i][116] + 3.14218*result[i][117] + 3.26049*result[i][118] + 5.21884*result[i][119] + 3.63829*result[i][120] - 0.132674*result[i][121] - 1.09042*result[i][122] - 0.148641*result[i][123] - 0.399671*result[i][124] - 0.622042*result[i][125] + 0.967894*(result[i][126] + result[i][134]) + 0.944741*result[i][127] - 0.0686273*result[i][129] + 1.65721*result[i][130] - 1.18028*result[i][131] + 0.976225*result[i][133] - 0.699007*result[i][135] - 2.00286*result[i][136] - 2.77637*result[i][145] - 1.37897*result[i][151] - 3.84404*result[i][152] - 1.85712*result[i][153] - 1.99257*result[i][154] - 1.10528*result[i][160] - 0.556981*result[i][162] - 3.44505*result[i][163] - 6.73047*result[i][166] + 3.49429*result[i][171] + 0.241962*result[i][175] + 0.444548*result[i][176] + 1.27982*result[i][177] + 1.16457*result[i][178] + 1.2906*result[i][179] + 1.98826*result[i][180] + 1.65196*result[i][181] - 0.00425184*result[i][182] - 1.08794*result[i][183] - 0.445351*result[i][184] - 1.05244*result[i][185] + 0.60601*result[i][186] - 0.550949*(result[i][187] + result[i][195]) + 0.0117083*result[i][188] + 0.5737*result[i][189] - 0.583502*result[i][190] - 0.171522*result[i][191] + 1.91683*result[i][192] - 0.837166*result[i][194] - 1.0275*result[i][196] - 1.30878*result[i][197] - 2.11989*result[i][206] - 2.307*result[i][212] - 2.17505*result[i][213] - 0.897734*result[i][214] - 3.05934*result[i][215] - 0.283271*result[i][221] - 1.56529*result[i][223] - 4.20396*result[i][224] - 14.1115*result[i][227] + 0.712954*result[i][232] + 1.0782*result[i][236] - 3.47834*result[i][305] - 2.52864*result[i][306] - 2.0899*result[i][307];
			}
			else if (degree == 2){
				SASA[i] = 36.313 + 4.6392*result[i][0] + 1.2816*result[i][1] + 0.381523*result[i][2] - 4.8009*result[i][3] - 5.74616*(result[i][4] + result[i][12]) - 5.72546*result[i][5] - 17.4968*result[i][6] - 8.6582*result[i][7] + 13.4589*result[i][9] - 9.44538*result[i][11] - 8.00637*result[i][13] - 5.87287*result[i][14] + 16.345*result[i][29] - 5.84368*result[i][49] + 15.703*result[i][53] + 10.8456*result[i][54] + 7.11435*result[i][55] + 5.62924*result[i][56] + 3.66452*result[i][57] + 3.03218*result[i][58] + 2.37037*result[i][59] + 1.15921*result[i][60] - 0.25986*result[i][61] - 0.0538884*result[i][62] - 0.198095*result[i][63] - 2.39771*result[i][64] + 0.915652*(result[i][65] + result[i][73]) + 2.05874*result[i][66] - 2.3966*result[i][67] + 1.748*result[i][68] + 3.6437*result[i][69] + 5.48383*result[i][70] + 0.760189*result[i][72] + 2.25371*result[i][74] + 1.76986*result[i][75] - 1.99391*result[i][90] - 3.66614*result[i][91] + 0.921871*result[i][92] + 2.12494*result[i][93] + 6.22166*result[i][99] - 2.16946*result[i][101] - 1.92131*result[i][102] - 5.20707*result[i][105] + 0.960373*result[i][110] - 1.39626*result[i][114] + 3.41092*result[i][115] + 2.27951*result[i][116] + 2.33036*result[i][117] + 2.30013*result[i][118] + 1.91994*result[i][119] + 1.63312*result[i][120] + 0.51774*result[i][121] - 1.33287*result[i][122] - 1.0075*result[i][123] - 0.00417948*result[i][124] + 0.425798*result[i][125] - 1.61451*(result[i][126] + result[i][134]) - 0.218171*result[i][127] + 3.47493*result[i][128] - 0.192681*result[i][129] + 0.109217*result[i][130] - 1.89443*result[i][131] - 0.162071*result[i][133] + 0.498812*result[i][135] - 0.967793*result[i][136] - 0.106596*result[i][145] - 2.1237*result[i][151] - 1.77206*result[i][152] - 1.84888*result[i][153] - 4.05364*result[i][154] + 0.386633*result[i][160] - 2.49029*result[i][162] - 3.23834*result[i][163] - 4.19951*result[i][166] - 0.329984*result[i][171] - 1.24065*result[i][175] - 2.7876*result[i][305] - 1.91567*result[i][306] - 0.824754*result[i][307];
			}
			else if (degree == 3){
				SASA[i] = 8.10362 + 4.2306*result[i][0] - 0.195801*result[i][1] - 1.20172*result[i][2] - 0.63488*(result[i][4] + result[i][12]) - 4.29911*result[i][5] + 13.9863*result[i][9] - 3.72158*result[i][11] - 4.22975*result[i][13] - 2.83839*result[i][14] + 15.3793*result[i][31] - 0.778124*result[i][49] + 15.4021*result[i][53] + 7.06802*result[i][54] + 3.59719*result[i][55] + 2.39734*result[i][56] + 2.14661*result[i][57] + 0.786478*result[i][58] - 0.620753*result[i][59] - 0.976815*result[i][60] - 0.451406*result[i][61] + 0.07849*result[i][62] - 0.422978*result[i][63] + 0.337648*result[i][64] + 0.72273*(result[i][65] + result[i][73]) + 0.820382*result[i][66] + 0.00630794*result[i][67] + 1.08627*result[i][68] + 1.12133*result[i][69] - 0.530167*result[i][70] + 0.425938*result[i][72] + 0.760863*result[i][74] + 0.731573*result[i][75] + 1.29837*result[i][84] - 0.775103*result[i][90] - 1.60884*result[i][91] - 0.452205*result[i][92] - 0.831961*result[i][93] + 2.05704*result[i][99] - 0.210129*result[i][101] - 0.541039*result[i][102] - 1.25276*result[i][105] + 0.274326*result[i][110] + 0.327228*result[i][114] - 0.435025*result[i][122] - 0.370122*result[i][123] + 0.0170519*result[i][124] + 0.146891*result[i][125] - 0.228529*(result[i][126] + result[i][134]) - 0.0871553*result[i][127] + 0.333549*result[i][128] - 0.373297*result[i][129] + 0.289118*result[i][130] - 1.03946*result[i][131] - 0.0323635*result[i][133] + 0.315932*result[i][135] - 0.604901*result[i][136] - 0.230707*result[i][145] - 0.467778*result[i][151] + 0.648047*result[i][152] - 1.14572*result[i][153] - 1.52299*result[i][154] - 0.239062*result[i][160] - 0.642037*result[i][162] - 0.121696*result[i][163] - 0.436216*result[i][166] + 0.106403*result[i][171] + 1.14091*result[i][175] - 0.594363*result[i][305] - 0.321139*result[i][306] - 0.305511*result[i][307];
			}
			else if (degree == 4){
				SASA[i] = 0.736923 - 0.336528*result[i][0] + 0.301084*result[i][9] - 0.459869*(result[i][4] + result[i][12]) + 0.345742*result[i][32] - 0.418892*result[i][53] + 0.41299*result[i][54] + 0.35276*result[i][55] + 0.254555*result[i][56] + 0.058761*result[i][57] + 0.301721*result[i][58] - 0.00457247*result[i][59] - 0.017045*result[i][60] - 0.0487005*result[i][61] - 0.039913*result[i][62] - 0.00786418*result[i][63] - 0.0342756*result[i][64] - 0.0591981*(result[i][65] + result[i][73]) - 0.0854523*result[i][66] + 0.0267218*result[i][68] + 0.191598*result[i][69] + 0.247932*result[i][70] - 0.0951786*result[i][72] - 0.0706718*result[i][74] - 0.092765*result[i][75] - 0.136653*result[i][90] - 0.48108*result[i][91] - 0.0373439*result[i][93] - 0.0245937*result[i][99] - 0.0793985*result[i][101] - 0.0572088*result[i][102] + 0.6073*result[i][110] + 0.0558154*result[i][114] - 0.0378271*result[i][305] - 0.0572892*result[i][306];
			}
			//negative SASA is not allowed
			if (SASA[i] < 0) SASA[i] = 0;
			
			SMARTCYP_PROPERTY.SASA2D.set(this.getAtom(i),SASA[i]);
		}
		
		if (debug == 1){
			for (int j=0; j < factory.getSize(); j++) {
				System.out.print(j);
				System.out.print(".");
		    	System.out.print(atomTypes[j].getAtomTypeName());
		    	System.out.print("\t");
		    }			    
		    System.out.println("");
		    for (int i = 0; i < natoms; i++)
		    	System.out.println(Arrays.toString(result[i]));
		}
		
		return SASA;
		
	}

	public int[] calculateRings(IAtom atom,IRingSet ringset) throws Exception{
		
		//member of 3,4,5,6,7,8,larger rings
		int [] result = {0,0,0,0,0,0,0};
		
		boolean isinring = atom.getFlag(CDKConstants.ISINRING); //true/false
		if(isinring){
			for(int ringindex = 0; ringindex < ringset.getAtomContainerCount(); ringindex++){
				IRing ring = (IRing) ringset.getAtomContainer(ringindex);
				int ringsize = ring.getAtomCount();
				if (ring.contains(atom)){
					if (ringsize > 2){
						if (ringsize <9) {
							result[ringsize-3]++;
						}
						else {
							result[6]++;
						}
					}
				}
			}
		}
		return result;
	}

	public int calculateLargestRing(IAtom atom, IRingSet ringset) throws Exception{
		
		int result = 0;
		
		boolean isinring = atom.getFlag(CDKConstants.ISINRING); //true/false
		if(isinring){
			for(int ringindex = 0; ringindex < ringset.getAtomContainerCount(); ringindex++){
				IRing ring = (IRing) ringset.getAtomContainer(ringindex);
				if (ring.contains(atom)){
					int ringsize = ring.getAtomCount();
					if (ringsize > result) result = ringsize;
				}
			}
		}		
		return result;
	}

	// Get the TreeSet containing the sorted C, N, P and S atoms
	public TreeSet<Atom> getAtomsSortedByEnA(){
		return this.atomsSortedByEnA;
	}

	public TreeSet<Atom> getAtomsSortedByEnA2D6(){
		return this.atomsSortedByEnA2D6;
	}

	public TreeSet<Atom> getAtomsSortedByEnA2C9(){
		return this.atomsSortedByEnA2C9;
	}
	
	public void setID(String id){
		super.setID(id);
	}

	public String toString(){
		for(int atomNr=0; atomNr < this.getAtomCount(); atomNr++) System.out.println(this.getAtom(atomNr).toString());
		return "MoleculeKU " + super.toString();
	}

	public static int FindInArray(int[] arr, int numToFind) {
		int occurence=0;
		for (int i = 0; i < arr.length; i++) { 
			if (arr[i] == numToFind) occurence++;
		}
		return occurence;
	}

	public int[] convertStringArraytoIntArray(String[] sarray) throws Exception {
		if (sarray != null) {
			int intarray[] = new int[sarray.length];
			for (int i = 0; i < sarray.length; i++) {
				intarray[i] = Integer.parseInt(sarray[i]);
			}
			return intarray;
		}
		return null;
	}

	public static int [] concatAll(int[] first, int[]... rest) {
		int totalLength = first.length;
		for (int [] array : rest) {
			totalLength += array.length;
		}
		int [] result = Arrays.copyOf(first, totalLength);
		int offset = first.length;
		for (int [] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}

	public void setFilterValue() {
		//set the minimum standard score as a filter data value of the molecule
		double smallestscore = 99;
		double thisscore;
		IAtom thisatom;
		for (int atomNr = 0; atomNr < this.getAtomCount(); atomNr++){
			thisatom = this.getAtom(atomNr);
			thisscore = (Double) SMARTCYP_PROPERTY.Score.get(thisatom);
			if(thisscore < smallestscore){
				smallestscore = thisscore;
			}
		}
		this.setProperty("FilterValue", smallestscore);
	}
}





