# Function to find the MMPs from a mol object queue
# Use multithreading to do the fragmentation - set off a new core every 300 mols up to maximum of 10
from com.im.lac.types import MoleculeObjectIterable
from java import lang
import sys
from java.util import ArrayList
from java.lang import Thread, InterruptedException
from java.lang import Class
lang.System.loadLibrary('GraphMolWrap')
# Pull it in as a stream of string
from org.RDKit import *
from mmp_code.mmp_make import find_mmps
from mol_parsing.rdkit_parse import get_or_create_rdmol


def make_mmps():
    """Function to count the number of molecules making ito the end of the test"""
    # Now loop through the mols
    my_mols = []
    counter = 0
    while mols.hasNext():
        molobj = mols.next()
        counter += 1
        rdmol, molobj = get_or_create_rdmol(molobj)
        my_mols.append([RWMol.MolToSmiles(rdmol, True), counter])
    return find_mmps(my_mols)
# Now just call this function as the last one

mols = request.getBody(MoleculeObjectIterable)

my_mmps = make_mmps()
request.body = my_mmps

