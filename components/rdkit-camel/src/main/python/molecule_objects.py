from com.im.lac.types import MoleculeObjectIterable
from com.im.lac.util import MoleculeObjectStreamProvider
from com.im.lac.util import CloseableMoleculeObjectQueue
from com.im.lac.types import MoleculeObject
from java import lang
import sys
from java.util import ArrayList
from java.util.stream import Stream
from java.lang import Thread, InterruptedException
from java.lang import Class
lang.System.loadLibrary('GraphMolWrap')
# Pull it in as a stream of string
from org.RDKit import *

from mol_parsing.rdkit_parse import get_or_create_rdmol, parse_mol_obj
# this gets the body converted to a Iterator of MoleculeObjects
def read_in():
    counter = 0
    while mols.hasNext():
        counter +=1
        molobj = mols.next()
        # Now get the molecule
        rdmol, molobj = get_or_create_rdmol(molobj)
        if not rdmol:
            print molobj.getSource()
            continue 
        # Put this representation to the function
        molobj.putValue("me", counter)
        # Add to the queue
        out_mols_here.add(molobj)
    # Close the queue to stop the blocking 
    out_mols_here.close()

class ObjReadThread(Thread):
    def run(self):
        try:
            read_in()
            self.stop()
        except:
            out_mols_here.close()
            self.stop()
#            raise

# Get the previous body and set the next one
provider = request.getBody(MoleculeObjectStreamProvider)
if provider:
    print "found a provider"
    mols = provider.getStream().iterator()
else:
    mols = request.getBody(MoleculeObjectIterable)
if not mols:
    provider = request.getBody(Stream)
if provider:
    mols = provider.iterator()
if mols:
    out_mols_here = CloseableMoleculeObjectQueue(40)
    request.setBody(out_mols_here)
    my_thread = ObjReadThread()
    my_thread.start()
else:
    print "can't convert. found " + request.getBody().getClass().getName()