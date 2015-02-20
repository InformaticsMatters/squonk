# File to do the molecular filtering
from com.im.lac.types import MoleculeObject, MoleculeObjectIterable
from java import lang
import sys
from java.util import ArrayList
from find_props.find_props import calc_props
from find_props.filter_props import filter_prop
from com.im.lac.util import CloseableMoleculeObjectQueue
from java.lang import Thread, InterruptedException

from java.lang import Class
lang.System.loadLibrary('GraphMolWrap')
# Pull it in as a stream of string
from org.RDKit import *


def filter_props():
    """Function to caluclate properties on a molecule"""
    while mols.hasNext():
        java_mol = mols.next()
        rdmol = java_mol.getRepresentation("rdkit.mol")
        if not rdmol:
            # Need a function here to recreate the RDMol if needed
            continue
        val = calc_props(rdmol, my_funct)
        if val < min_ans or val > max_ans:
            java_mol.putValue("my_test", "fail")
        else:
            rdmol.setProp(my_funct, str(val))
            java_mol.putValue("my_test", "true")
        java_mol.putValue(my_funct, val) 
        filter_mols.add(java_mol)
# Now close this body
    filter_mols.close()


class ObjFiltThread(Thread):
    def run(self):
        filter_props()
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

filter_mols = CloseableMoleculeObjectQueue(100)
request.body = filter_mols
my_thread = ObjFiltThread()
my_thread.start()
