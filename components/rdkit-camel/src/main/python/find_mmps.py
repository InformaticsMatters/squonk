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
from mmp_code.mmp_make import fragment_mol
from mol_parsing.rdkit_parse import get_or_create_rdmol
from mmp_code.index import index_out_lines

def find_mmps(my_mols):
    out_mols = []
    counter = 0
    #read the mo
    for line in my_mols:
        smiles = line[0]
        cmpd_id = line[1]
        #returns a set containing the output
        o = fragment_mol(smiles,cmpd_id)
        for l in o:
           out_mols.append(l)
    return index_out_lines(out_mols)

def make_mmps():
    """Function to count the number of molecules making ito the end of the test"""
    # Now loop through the mols
    my_mols = []
    counter = 0
    while mols.hasNext():
        molobj = mols.next()
        counter += 1
        rdmol, molobj = get_or_create_rdmol(molobj)
        if rdmol is None:
            continue
        my_mols.append([RWMol.MolToSmiles(rdmol, True), counter])
        if counter == 20:
            break
    return find_mmps(my_mols)
# Now just call this function as the last one

mols = request.getBody(MoleculeObjectIterable)

my_mmps = make_mmps()
request.body = len(my_mmps)

