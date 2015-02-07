from com.im.lac.types import MoleculeObjectIterable
from java import lang
import sys
sys.path.append("/RDKit/rdkit/Code/JavaWrappers/gmwrapper/org.RDKitDoc.jar")
sys.path.append("/RDKit/rdkit/Code/JavaWrappers/gmwrapper/libGraphMolWrap.so")
sys.path.append("/RDKit/rdkit/Code/JavaWrappers/gmwrapper/org.RDKit.jar")
sys.path.append("/RDKit/rdkit/lib")
sys.path.append("/RDKit/rdkit/Code/JavaWrappers/gmwrapper")
from java.util import ArrayList

from java.lang import Class
lang.System.loadLibrary('GraphMolWrap')
# Pull it in as a stream of string
from org.RDKit import *


# this gets the body converted to a MoleculeObjectIterable
mols = request.getBody(MoleculeObjectIterable)

counter = 0
while mols.hasNext():
    molobj = mols.next()
    # gets as string and is usually OK, but potentially could be a binary format 
    # such as cdx so getSourceAsBytes() which returns a byte array is safer, but 
    # as long as we only handle smiles, inchi, sdf, mol then strings are OK
    molstr = molobj.getSourceAsString()
    print molstr
    # here we simulate setting a property
    # molobj.putValue("somename", "somevalue)
    ### Make an RDKit mol
    rdmol = RWMol.MolFromSmiles(molstr)

    # here we simulate storing a molecule representation so that we do not need
    # to regenerate it later
    #molobj.putRepresentation("rdkit.mol", "<rdkit molecule>")
    # once stored it can be retrieved later using
    #my_mol = molobj.getRepresention("rdkit.mol")
    # or
    # molobj.getRepresention("some.key", ClassName)
    if rdmol:
        counter += 1   
 
# for simplicity we return the count, but most likely we want to return another MoleculeObjectIterable 
request.body = counter
