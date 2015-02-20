from com.im.lac.types import MoleculeObjectIterable
from com.im.lac.util import CloseableMoleculeObjectQueue
from com.im.lac.types import MoleculeObject
from java import lang
import sys
from java.util import ArrayList
from java.lang import Thread, InterruptedException

from java.lang import Class
lang.System.loadLibrary('GraphMolWrap')
# Pull it in as a stream of string
from org.RDKit import *

from mol_parsing.rdkit_parse import parse_mol_obj
# this gets the body converted to a MoleculeObjectIterable
def read_in():
    counter = 0
    while mols.hasNext():
        counter +=1
        molobj = mols.next()
    # gets as string and is usually OK, but potentially could be a binary format 
    # such as cdx so getSourceAsBytes() which returns a byte array is safer, but 
    # as long as we only handle smiles, inchi, sdf, mol then strings are OK
        rdmol = parse_mol_obj(molobj)
        if not rdmol:
            continue 
   # here we simulate setting a property
#        molobj.putRepresentation("rdkit.mol", rdmol)
        molobj.putValue("me", counter)
    ### Make an RDKit mol
# Add to the queuw
        out_mols_here.add(molobj)
    # here we simulate storing a molecule representation so that we do not need
    # to regenerate it later
#    molobj.putRepresentation("rdkit.mol", rdmol)
    # once stored it can be retrieved later using
    #my_mol = molobj.getRepresention("rdkit.mol")
    # or
    # molobj.getRepresention("some.key", ClassName)
# Close the queue to stop the blocking 
    out_mols_here.close()

class ObjReadThread(Thread):
    def run(self):
        read_in()
        self.stop()

# Get the prvevious body and set the next one
mols = request.getBody(MoleculeObjectIterable)
out_mols_here = CloseableMoleculeObjectQueue(40)
request.setBody(out_mols_here)
#read_in()
my_thread = ObjReadThread()
my_thread.start()
#import threading
#t = threading.Thread(target=read_in,)
#t.start()
