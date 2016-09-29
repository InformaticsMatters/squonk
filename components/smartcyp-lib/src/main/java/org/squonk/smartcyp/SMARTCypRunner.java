package org.squonk.smartcyp;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.io.FormatFactory;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.MDLReader;
import org.openscience.cdk.io.formats.IChemFormat;
import org.openscience.cdk.smiles.DeduceBondSystemTool;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.AtomPropertySet;
import org.squonk.types.MoleculeObject;
import smartcyp.MoleculeKU;
import smartcyp.SMARTSnEnergiesTable;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by timbo on 26/09/2016.
 */
public class SMARTCypRunner {

    private static final Logger LOG = Logger.getLogger(SMARTCypRunner.class.getName());
    private static final String ORIG_ATOM_INDEX = "__OrigAtomIndex__";

    private final FormatFactory factory = new FormatFactory();
    private final DeduceBondSystemTool dbst = new DeduceBondSystemTool();
    private final CDKHydrogenAdder hydrogenAdder = CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance());
    private final SMARTSnEnergiesTable SMARTSnEnergiesTable = new SMARTSnEnergiesTable();

    private final boolean performGeneral, perform2D6, perform2C9;

    private final Float scoreFilter;
    private final boolean addEmpiricalNitrogenOxidationCorrections;
    private final Integer maxRank;


    public SMARTCypRunner(boolean performGeneral, boolean perform2D6, boolean perform2C9, Float scoreFilter, Integer maxRank, boolean addEmpiricalNitrogenOxidationCorrections) {
        this.performGeneral = performGeneral;
        this.perform2D6 = perform2D6;
        this.perform2C9 = perform2C9;
        this.scoreFilter = scoreFilter;
        this.maxRank = maxRank;
        this.addEmpiricalNitrogenOxidationCorrections = addEmpiricalNitrogenOxidationCorrections;
    }

    public SMARTCypRunner() {
        this(true, true, true, 100f, 3, true);
    }


    public Dataset<MoleculeObject> execute(Dataset<MoleculeObject> dataset) throws Exception {


        LOG.fine("\n ************** Processing SMARTS and Energies **************");

        Stream<MoleculeObject> stream = dataset.getStream();

        stream = stream.peek((mo) -> {
            try {
                MoleculeKU moleculeKU = readMoleculeKU(mo);
                if (moleculeKU != null) {
                    moleculeKU.assignAtomEnergies(SMARTSnEnergiesTable.getSMARTSnEnergiesTable());

                    LOG.finer("\n ************** Calculating shortest distance to protonated amine **************");
                    moleculeKU.calculateDist2ProtAmine();

                    LOG.finer("\n ************** Calculating shortest distance to carboxylic acid **************");
                    moleculeKU.calculateDist2CarboxylicAcid();

                    LOG.finer("\n ************** Calculating Span2End**************");
                    moleculeKU.calculateSpan2End();

                    if (addEmpiricalNitrogenOxidationCorrections) {
                        LOG.finer("\n ************** Add Empirical Nitrogen Oxidation Corrections **************");
                        moleculeKU.unlikelyNoxidationCorrection();
                    }

                    LOG.finer("\n ************** Calculating Accessabilities and Atom Scores **************");
                    moleculeKU.calculateAtomAccessabilities();
                    //compute 2DSASA
                    double[] SASA = moleculeKU.calculateSASA();

                    if (performGeneral) moleculeKU.calculateAtomScores();
                    if (perform2D6) moleculeKU.calculate2D6AtomScores();
                    if (perform2C9) moleculeKU.calculate2C9AtomScores();


                    LOG.finer("\n ************** Identifying, sorting and ranking C, N, P and S atoms **************");
                    if (performGeneral) {
                        moleculeKU.sortAtoms();
                        moleculeKU.rankAtoms();
                    }
                    if (perform2D6) {
                        moleculeKU.sortAtoms2D6();
                        moleculeKU.rankAtoms2D6();
                    }
                    if (perform2C9) {
                        moleculeKU.sortAtoms2C9();
                        moleculeKU.rankAtoms2C9();
                    }

                    writeData(mo, moleculeKU);

                }
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to read molecule", e);
            }

        });

        DatasetMetadata<MoleculeObject> meta = new DatasetMetadata<>(MoleculeObject.class);
        return new Dataset<>(MoleculeObject.class, stream, meta);
    }

    private void writeData(MoleculeObject mo, MoleculeKU moleculeKU) {

        List<AtomPropertySet.Score> scoresGeneral = new ArrayList<>();
        List<AtomPropertySet.Score> scores2D6 = new ArrayList<>();
        List<AtomPropertySet.Score> scores2C9 = new ArrayList<>();
        for (int atomIndex = 0; atomIndex < moleculeKU.getAtomCount(); ++atomIndex) {
            IAtom currentAtom = moleculeKU.getAtom(atomIndex);
            String currentAtomType = currentAtom.getSymbol();
            if (currentAtomType.equals("C") || currentAtomType.equals("N") || currentAtomType.equals("P") || currentAtomType.equals("S")) {
                Number score = MoleculeKU.SMARTCYP_PROPERTY.Score.get(currentAtom);
                int rank = MoleculeKU.SMARTCYP_PROPERTY.Ranking.get(currentAtom).intValue();
                if (filter(score, rank)) {
                    Integer origAtomIndex = (Integer)currentAtom.getProperty(ORIG_ATOM_INDEX);
                    scoresGeneral.add(AtomPropertySet.createScore(origAtomIndex, currentAtomType, score.floatValue(), rank));
                }

                score = MoleculeKU.SMARTCYP_PROPERTY.Score2D6.get(currentAtom);
                rank = MoleculeKU.SMARTCYP_PROPERTY.Ranking2D6.get(currentAtom).intValue();
                if (filter(score, rank)) {
                    Integer origAtomIndex = (Integer)currentAtom.getProperty(ORIG_ATOM_INDEX);
                    scores2D6.add(AtomPropertySet.createScore(origAtomIndex, currentAtomType, score.floatValue(), rank));
                }

//                this.outfile.print("," + this.twoDecimalFormat.format(MoleculeKU.SMARTCYP_PROPERTY.Span2End.get(currentAtom)));
//                if (MoleculeKU.SMARTCYP_PROPERTY.Dist2ProtAmine.get(currentAtom) != null) {
//                    this.outfile.print("," + this.twoDecimalFormat.format(MoleculeKU.SMARTCYP_PROPERTY.Dist2ProtAmine.get(currentAtom)));
//                } else {
//                    this.outfile.print(",0");
//                }

                score = MoleculeKU.SMARTCYP_PROPERTY.Score2C9.get(currentAtom);
                rank = MoleculeKU.SMARTCYP_PROPERTY.Ranking2C9.get(currentAtom).intValue();
                if (filter(score, rank)) {
                    Integer origAtomIndex = (Integer)currentAtom.getProperty(ORIG_ATOM_INDEX);
                    scores2C9.add(AtomPropertySet.createScore(origAtomIndex, currentAtomType, score.floatValue(), rank));
                }

//                if (MoleculeKU.SMARTCYP_PROPERTY.Dist2CarboxylicAcid.get(currentAtom) != null) {
//                    this.outfile.print("," + this.twoDecimalFormat.format(MoleculeKU.SMARTCYP_PROPERTY.Dist2CarboxylicAcid.get(currentAtom)));
//                } else {
//                    this.outfile.print(",0");
//                }
//
//                if (MoleculeKU.SMARTCYP_PROPERTY.SASA2D.get(currentAtom) != null) {
//                    this.outfile.print("," + this.twoDecimalFormat.format(MoleculeKU.SMARTCYP_PROPERTY.SASA2D.get(currentAtom)));
//                } else {
//                    this.outfile.print(",0");
//                }

            }
        }

        if (scoresGeneral.size() > 0) {
            sortByRank(scoresGeneral);
            mo.putValue("SMARTCyp_GEN", new AtomPropertySet(scoresGeneral));
        }
        if (scores2D6.size() > 0) {
            sortByRank(scores2D6);
            mo.putValue("SMARTCyp_2D6", new AtomPropertySet(scores2D6));
        }
        if (scores2C9.size() > 0) {
            sortByRank(scores2C9);
            mo.putValue("SMARTCyp_2C9", new AtomPropertySet(scores2C9));
        }

    }

    private boolean filter(Number score, int rank) {
        return score != null &&
                (scoreFilter == null || scoreFilter > score.floatValue()) &&
                (maxRank == null || rank <= maxRank);
    }

    private void sortByRank(List<AtomPropertySet.Score> list) {
        list.sort((i, j) -> i.getRank().compareTo(j.getRank()));
    }


    private MoleculeKU readMoleculeKU(MoleculeObject mo) throws Exception {

        String format = mo.getFormat();
        String mol = mo.getSource();
        IAtomContainer iAtomContainer = null;
        boolean deducebonds = false;
        if (format != null && format.startsWith("smiles")) {
            iAtomContainer = readAsSmiles(mol);
            deducebonds = true;
        } else {
            ISimpleChemObjectReader reader = null;
            if (format != null && (format.equals("mol") || format.startsWith("mol:"))) {
                reader = new MDLReader();
            } else {
                IChemFormat chemFormat = factory.guessFormat(new StringReader(mol));
                if (chemFormat == null) {
                    // give up and assume smiles
                    iAtomContainer = readAsSmiles(mol);
                    deducebonds = true;
                } else {
                    reader = (ISimpleChemObjectReader) (Class.forName(chemFormat.getReaderClassName()).newInstance());
                }
            }
            if (reader != null) {
                reader.setReader(new ByteArrayInputStream(mol.getBytes()));
                iAtomContainer = reader.read(new Molecule());
            }
        }

        MoleculeKU moleculeKU = null;

        if (iAtomContainer != null) {

            // tag the atoms with the original atom indexes which we need later
            for (int atomIndex = 0; atomIndex < iAtomContainer.getAtomCount(); ++atomIndex) {
                IAtom atom = iAtomContainer.getAtom(atomIndex);
                atom.setProperty(ORIG_ATOM_INDEX, atomIndex);
            }

            // Remove salts or solvents... Keep only the largest molecule
            if (!ConnectivityChecker.isConnected(iAtomContainer)) {
                //System.out.println(atomContainerNr);
                IMoleculeSet fragments = ConnectivityChecker.partitionIntoMolecules(iAtomContainer);

                int maxID = 0;
                int maxVal = -1;
                for (int i = 0; i < fragments.getMoleculeCount(); i++) {
                    if (fragments.getMolecule(i).getAtomCount() > maxVal) {
                        maxID = i;
                        maxVal = fragments.getMolecule(i).getAtomCount();
                    }
                }
                iAtomContainer = fragments.getMolecule(maxID);
            }
            //end of salt removal

            iAtomContainer = AtomContainerManipulator.removeHydrogens(iAtomContainer);

            //check number of atoms, if less than 2 don't add molecule
            if (iAtomContainer.getAtomCount() > 1) {
                //System.out.println(iAtomContainer.getProperties());

                AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(iAtomContainer);

                if (deducebonds) {
                    DeduceBondSystemTool dbst = new DeduceBondSystemTool();
                    iAtomContainer = dbst.fixAromaticBondOrders((IMolecule) iAtomContainer);
                }

                hydrogenAdder.addImplicitHydrogens(iAtomContainer);
                CDKHueckelAromaticityDetector.detectAromaticity(iAtomContainer);

                moleculeKU = new MoleculeKU(iAtomContainer, SMARTSnEnergiesTable.getSMARTSnEnergiesTable());
                moleculeKU.setID(mo.getUUID().toString());
                //set the molecule title in the moleculeKU object
                if (iAtomContainer.getProperty("SMIdbNAME") != "" && iAtomContainer.getProperty("SMIdbNAME") != null) {
                    iAtomContainer.setProperty(CDKConstants.TITLE, iAtomContainer.getProperty("SMIdbNAME"));
                }
                moleculeKU.setProperty(CDKConstants.TITLE, iAtomContainer.getProperty(CDKConstants.TITLE));
                moleculeKU.setProperties(iAtomContainer.getProperties());
            }


        }
        return moleculeKU;
    }

    private IAtomContainer readAsSmiles(String smiles) throws CDKException {
        SmilesParser parser = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        IAtomContainer iAtomContainer = parser.parseSmiles(smiles);
        iAtomContainer = dbst.fixAromaticBondOrders((IMolecule) iAtomContainer);

        return iAtomContainer;
    }

}