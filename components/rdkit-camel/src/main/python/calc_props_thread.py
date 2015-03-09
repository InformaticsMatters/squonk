from com.im.lac.types import MoleculeObject, MoleculeObjectIterable
from com.im.lac.util import CloseableMoleculeObjectQueue

from java import lang
import sys
#Place the python module on the path
#sys.path.append('/lac/components/rdkit-camel/src/main/python')
from java.util import ArrayList
from find_props.find_props import calc_props
from java.lang import Thread, InterruptedException
import time


from java.lang import Class
lang.System.loadLibrary('GraphMolWrap')
# Pull it in as a stream of string
from org.RDKit import *
import sys
from mol_parsing.rdkit_parse import get_or_create_rdmol


def calc_my_props():
    """Function to caluclate properties on a molecule"""
    while mols.hasNext():
        java_mol = mols.next()
        # Now get the RDMol and the java_mol  updated
        rdmol, java_mol = get_or_create_rdmol(java_mol)
        if rdmol is None:
            continue
        # Now calculate the prop
        val = calc_props(rdmol, my_funct)
        if val is None:
            continue
        # Set the value of this function to the molecule object
        java_mol.putValue(my_funct, val)
        # To make sure the molecules in the end viewer have been through this stage
        java_mol.putValue("my_test", "true")
        # Add to the list
        calc_mols.add(java_mol) 
    #Close the loop
    calc_mols.close()


class ObjPropThread(Thread):
    """Thread to calculate molecule properties"""
    def run(self):
        # Try and catch this exception - if we get it just to kill the thread
        try:
            calc_my_props()
            self.stop()
        except:
            sys.stderr.write('Error in molecule thread making - shutting down')
            calc_mols.close()
            self.stop()

# Get the mols from the previous process 
mols = request.getBody(MoleculeObjectIterable)
# Find the function we're running
my_funct = request.getHeader("function")
# Create the closeable queue
calc_mols = CloseableMoleculeObjectQueue(40)
# Set this as the body
request.setBody(calc_mols)
# Set up the thread
my_thread = ObjPropThread()
# Get it going 
my_thread.start()
