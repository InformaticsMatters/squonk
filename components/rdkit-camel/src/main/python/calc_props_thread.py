from com.im.lac.types import MoleculeObject, MoleculeObjectIterable
from com.im.lac.util import CloseableMoleculeObjectQueue

from java import lang
import sys
#Place the python module on the path
sys.path.append('/lac/components/rdkit-camel/src/main/python')
from java.util import ArrayList
from find_props.find_props import calc_props
from java.lang import Thread, InterruptedException



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
        java_mol.putValue(my_funct, val)
        request.body.add(java_mol)
    request.body.close()


class ObjPropThread(Thread):
    def run(self):
        calc_my_props()
        self.stop()

mols = request.getBody(MoleculeObjectIterable)
my_funct = request.getHeader("function")
request.body = CloseableMoleculeObjectQueue(100)
my_thread = ObjPropThread()
my_thread.start()

