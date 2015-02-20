from com.im.lac.types import MoleculeObject, MoleculeObjectIterable
from com.im.lac.util import CloseableMoleculeObjectQueue

from java import lang
import sys
#Place the python module on the path
sys.path.append('/lac/components/rdkit-camel/src/main/python')
from java.util import ArrayList
from find_props.find_props import calc_props
from java.lang import Thread, InterruptedException
import time


from java.lang import Class
lang.System.loadLibrary('GraphMolWrap')
# Pull it in as a stream of string
from org.RDKit import *



def calc_my_props():
    """Function to caluclate properties on a molecule"""
    while mols.hasNext():
        java_mol = mols.next()
        rdmol = java_mol.getRepresentation("rdkit.mol")
        val = calc_props(rdmol, my_funct)
        rdmol.setProp(my_funct, str(val))
        #java_mol.putValue(my_funct, val)
#        java_mol.putValue("my_test", "pass")
        counter = str(java_mol.getValue("me")) + "DONE"
        java_mol.putValue("my_test", "true")
        out_mols.add(java_mol) 
    out_mols.close()


class ObjPropThread(Thread):
    """Thread to calculate molecule properties"""
    def run(self):
        calc_my_props()


# Get the mols from the previous process 
mols = request.getBody(MoleculeObjectIterable)
my_funct = request.getHeader("function")
out_mols = CloseableMoleculeObjectQueue(40)
request.setBody(out_mols)
#calc_my_props()
my_thread = ObjPropThread()
my_thread.start()
