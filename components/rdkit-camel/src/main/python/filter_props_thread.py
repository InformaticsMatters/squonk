# File to do the molecular filtering
from com.im.lac.types import MoleculeObject, MoleculeObjectIterable
from java import lang
import sys
from java.util import ArrayList
from find_props.find_props import calc_props
from find_props.filter_props import filter_prop
from com.im.lac.util import CloseableMoleculeObjectQueue
from java.lang import Thread, InterruptedException
import sys
from java.lang import Class
lang.System.loadLibrary('GraphMolWrap')
# Pull it in as a stream of string
from org.RDKit import *

from mol_parsing.rdkit_parse import get_or_create_rdmol

def filter_props():
    """Function to caluclate properties on a molecule"""
    while mols.hasNext():
        java_mol = mols.next()
        # Get or make the RDKit molecule
        rdmol, java_mol = get_or_create_rdmol(java_mol)
        if rdmol is None:
            print java_mol.getSource()
            continue
        # Work out the function
        val = calc_props(rdmol, my_funct)
        if val is None:
            continue
        # Dp the logic and go past if it passes the test
        if val < min_ans or val > max_ans:
            continue
        else:
            pass
        # Now add the value
        java_mol.putValue(my_funct, val)
        # Now to make sure it's been through the test
        java_mol.putValue("my_test", "true")
        filter_mols.add(java_mol)
    # Now close this body
    filter_mols.close()


class ObjFiltThread(Thread):
    def run(self):
        try:
            filter_props()
            self.stop()
        except:
            sys.stderr.write("Exception in molecule filtering")
            filter_mols.close()
            self.stop()


mols = request.getBody(MoleculeObjectIterable)
my_head = request.getHeader("function").split("<")
# Now parse the header
if len(my_head) == 1:
    my_funct = my_head[0]
    min_ans == None   
    max_ans == None
else:
    my_funct = my_head[1]
    min_ans = float(my_head[0])
    max_ans = float(my_head[2])

# Create the closeable qeuue to leave
filter_mols = CloseableMoleculeObjectQueue(40)
# Set filter mols to the body
request.body = filter_mols
# Create the thread
my_thread = ObjFiltThread()
# Star the thread
my_thread.start()
