/* Example of how to render images of structures.
 * Taken from: http://chem-bla-ics.blogspot.co.uk/2010/04/cdk-jchempaint-1-rendering-molecules.html
 *
 */
import java.util.List;

import java.awt.*;
import java.awt.image.*;

import javax.imageio.*;

import org.openscience.cdk.*;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.layout.*;
import org.openscience.cdk.renderer.*;
import org.openscience.cdk.renderer.color.CDK2DAtomColors
import org.openscience.cdk.renderer.font.*;
import org.openscience.cdk.renderer.generators.*;
import org.openscience.cdk.renderer.generators.standard.StandardGenerator
import org.openscience.cdk.renderer.visitor.*;
import org.openscience.cdk.smiles.SmilesParser
import org.openscience.cdk.templates.*;

int WIDTH = 600;
int HEIGHT = 600;

// the draw area and the image should be the same size
Rectangle drawArea = new Rectangle(WIDTH, HEIGHT);
Image image = new BufferedImage(
    WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB
);

String smiles = 'S(SC1=NC2=CC=CC=C2S1)C3=NC4=C(S3)C=CC=C4'
SmilesParser smilesParser = new SmilesParser(DefaultChemObjectBuilder.getInstance());
IAtomContainer mol = smilesParser.parseSmiles(smiles);
        
StructureDiagramGenerator sdg = new StructureDiagramGenerator();
sdg.setMolecule(mol);
sdg.generateCoordinates();
mol = sdg.getMolecule();

Font font = new Font("Verdana", Font.PLAIN, 18);

// generators make the image elements
List generators = new ArrayList();
generators.add(new BasicSceneGenerator());
//generators.add(new BasicBondGenerator());
//generators.add(new BasicAtomGenerator());
generators.add(new StandardGenerator(font));
// the renderer needs to have a toolkit-specific font manager
AtomContainerRenderer renderer = new AtomContainerRenderer(generators, new AWTFontManager());

RendererModel rendererModel = renderer.getRenderer2DModel();
//rendererModel.set(StandardGenerator.Visibility.class, SymbolVisibility.iupacRecommendations());
rendererModel.set(StandardGenerator.AtomColor.class, new CDK2DAtomColors());

// the call to 'setup' only needs to be done on the first paint
renderer.setup(mol, drawArea);

// paint the background
Graphics2D g2 = (Graphics2D)image.getGraphics();
g2.setColor(Color.WHITE);
g2.fillRect(0, 0, WIDTH, HEIGHT);

// the paint method also needs a toolkit-specific renderer
renderer.paint(mol, new AWTDrawVisitor(g2));

ImageIO.write((RenderedImage)image, "PNG", new File("/Users/timbo/tmp/mol.png"));