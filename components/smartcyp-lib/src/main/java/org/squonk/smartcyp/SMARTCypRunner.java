package org.squonk.smartcyp;

import org.openscience.cdk.*;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.io.*;
import org.openscience.cdk.io.formats.IChemFormat;
import org.openscience.cdk.smiles.DeduceBondSystemTool;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.MoleculeObject;
import smartcyp.*;

import java.io.*;;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by timbo on 26/09/2016.
 */
public class SMARTCypRunner {

    private static final Logger LOG = Logger.getLogger(SMARTCypRunner.class.getName());

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

        DatasetMetadata<MoleculeObject> meta = new DatasetMetadata(MoleculeObject.class);
        Dataset<MoleculeObject> results = new Dataset<>(MoleculeObject.class, stream, meta);
        return results;
    }

    private void writeData(MoleculeObject mo, MoleculeKU moleculeKU) {

        List<CypScore> scoresGeneral = new ArrayList<>();
        List<CypScore> scores2D6 = new ArrayList<>();
        List<CypScore> scores2C9 = new ArrayList<>();

        for (int atomIndex = 0; atomIndex < moleculeKU.getAtomCount(); ++atomIndex) {
            Atom currentAtom = (Atom) moleculeKU.getAtom(atomIndex);
            String currentAtomType = currentAtom.getSymbol();
            if (currentAtomType.equals("C") || currentAtomType.equals("N") || currentAtomType.equals("P") || currentAtomType.equals("S")) {
                Number score = MoleculeKU.SMARTCYP_PROPERTY.Score.get(currentAtom);
                int rank = MoleculeKU.SMARTCYP_PROPERTY.Ranking.get(currentAtom).intValue();
                if (filter(score, rank)) {
                    scoresGeneral.add(new CypScore(atomIndex + 1, currentAtomType, score, rank));
                }

                score = MoleculeKU.SMARTCYP_PROPERTY.Score2D6.get(currentAtom);
                rank = MoleculeKU.SMARTCYP_PROPERTY.Ranking2D6.get(currentAtom).intValue();
                if (filter(score, rank)) {
                    scores2D6.add(new CypScore(atomIndex + 1, currentAtomType, score, rank));
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
                    scores2C9.add(new CypScore(atomIndex + 1, currentAtomType, score, rank));
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
            mo.putValue("SMARTCyp_general", scoresGeneral);
        }
        if (scores2D6.size() > 0) {
            sortByRank(scores2D6);
            mo.putValue("SMARTCyp_2D6", scores2D6);
        }
        if (scores2C9.size() > 0) {
            sortByRank(scores2C9);
            mo.putValue("SMARTCyp_2C9", scores2C9);
        }

    }

    private boolean filter(Number score, int rank) {
        return score != null &&
                (scoreFilter == null || scoreFilter.floatValue() > score.floatValue()) &&
                (maxRank == null || rank <= maxRank.intValue());
    }

    private void sortByRank(List<CypScore> list) {
        list.sort((i, j) -> i.ranking.compareTo(j.ranking));
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
                //iAtomContainer = reader.read(new AtomContainer());
                iAtomContainer = reader.read(new Molecule());
            }
        }

        MoleculeKU moleculeKU = null;

        if (iAtomContainer != null) {
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


    // Reads the molecule infiles
    // Stores MoleculeKUs and AtomKUs
    public static MoleculeSet readInStructures(String[] inFileNames, HashMap<String, Double> SMARTSnEnergiesTable) throws CloneNotSupportedException, CDKException {

        MoleculeSet moleculeSet = new MoleculeSet();


        List<IAtomContainer> moleculeList;
        IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
        ISimpleChemObjectReader reader;

        File inputFile;
        String infileName;
        ReaderFactory readerFactory;
        IChemFile emptyChemFile;
        IChemFile chemFile;


        // Iterate over all molecule infiles (it can be a single file)
        int moleculeFileNr;
        int highestMoleculeID = 1;
        for (moleculeFileNr = 0; moleculeFileNr < inFileNames.length; moleculeFileNr++) {

            infileName = inFileNames[moleculeFileNr];
            inputFile = new File(infileName);

            readerFactory = new ReaderFactory();

            boolean deducebonds = false;

            try {

                if (infileName.endsWith(".sdf")) {
                    /*//commented away because the V2000 reader seem to fail on many structures in sdf files
                    //check if it is V2000or V3000 sdf format
					boolean isV2000 = false;
					boolean isV3000 = false;
					FileInputStream fs= new FileInputStream(infileName);
					BufferedReader br = new BufferedReader(new InputStreamReader(fs));
					for(int i = 0; i < 3; ++i)
					  br.readLine();
					String linefour = br.readLine();
					if (linefour.contains("V2000")) {
						isV2000=true;
					}
					else if (linefour.contains("V3000")) {
						isV3000=true;
					}
					fs.close();
					//now we got the correct format
					if (isV2000){
						reader = new MDLV2000Reader(new FileReader(infileName));
					}
					else if (isV3000){
						reader = new MDLV3000Reader(new FileReader(infileName));
					}
					else {
						reader = new MDLReader(new FileReader(infileName));
					}
					*/
                    reader = new MDLReader(new FileReader(infileName));

                } else if (infileName.endsWith(".smi") || infileName.endsWith(".smiles")) {
                    reader = new SMILESReader(new FileReader(infileName));
                    deducebonds = true;
                } else reader = readerFactory.createReader(new FileReader(inputFile));


                emptyChemFile = builder.newInstance(IChemFile.class);
                chemFile = (IChemFile) reader.read(emptyChemFile);

                if (chemFile == null) continue;

                //System.out.println(chemFile.toString());

                // Get Molecules
                moleculeList = ChemFileManipulator.getAllAtomContainers(chemFile);

                // Iterate Molecules
                MoleculeKU moleculeKU;
                IAtomContainer iAtomContainerTmp;
                IAtomContainer iAtomContainer;
                CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance());
                for (int atomContainerNr = 0; atomContainerNr < moleculeList.size(); atomContainerNr++) {
                    iAtomContainerTmp = moleculeList.get(atomContainerNr);

                    // Remove salts or solvents... Keep only the largest molecule
                    if (!ConnectivityChecker.isConnected(iAtomContainerTmp)) {
                        //System.out.println(atomContainerNr);
                        IMoleculeSet fragments = ConnectivityChecker.partitionIntoMolecules(iAtomContainerTmp);

                        int maxID = 0;
                        int maxVal = -1;
                        for (int i = 0; i < fragments.getMoleculeCount(); i++) {
                            if (fragments.getMolecule(i).getAtomCount() > maxVal) {
                                maxID = i;
                                maxVal = fragments.getMolecule(i).getAtomCount();
                            }
                        }
                        iAtomContainerTmp = fragments.getMolecule(maxID);
                    }
                    //end of salt removal

                    iAtomContainer = AtomContainerManipulator.removeHydrogens(iAtomContainerTmp);

                    //check number of atoms, if less than 2 don't add molecule
                    if (iAtomContainer.getAtomCount() > 1) {
                        //System.out.println(iAtomContainer.getProperties());

                        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(iAtomContainer);


                        if (deducebonds) {
                            DeduceBondSystemTool dbst = new DeduceBondSystemTool();
                            iAtomContainer = dbst.fixAromaticBondOrders((IMolecule) iAtomContainer);
                        }


                        adder.addImplicitHydrogens(iAtomContainer);
                        CDKHueckelAromaticityDetector.detectAromaticity(iAtomContainer);

                        moleculeKU = new MoleculeKU(iAtomContainer, SMARTSnEnergiesTable);
                        moleculeSet.addMolecule(moleculeKU);
                        moleculeKU.setID(Integer.toString(highestMoleculeID));
                        //set the molecule title in the moleculeKU object
                        if (iAtomContainer.getProperty("SMIdbNAME") != "" && iAtomContainer.getProperty("SMIdbNAME") != null) {
                            iAtomContainer.setProperty(CDKConstants.TITLE, iAtomContainer.getProperty("SMIdbNAME"));
                        }
                        moleculeKU.setProperty(CDKConstants.TITLE, iAtomContainer.getProperty(CDKConstants.TITLE));
                        moleculeKU.setProperties(iAtomContainer.getProperties());
                        highestMoleculeID++;
                    }

                }
                System.out.println(moleculeList.size() + " molecules were read from the file " + inFileNames[moleculeFileNr]);

            } catch (FileNotFoundException e) {
                System.out.println("File " + inFileNames[moleculeFileNr] + " not found");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return moleculeSet;
    }

    class CypScore {

        int atomNumber;
        String atomSymbol;
        Number score;
        Integer ranking;

        CypScore(int atomNumber, String atomSymbol, Number score, Integer ranking) {
            this.atomNumber = atomNumber;
            this.atomSymbol = atomSymbol;
            this.score = score;
            this.ranking = ranking;
        }

        @Override
        public String toString() {
            return atomSymbol + "." + atomNumber + " " + score + " [" + ranking + "]";
        }
    }

}