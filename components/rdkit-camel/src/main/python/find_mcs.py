# Function to find the MCS from a mol object queue

from com.im.lac.types import MoleculeObjectIterable
from java import lang
import sys
from java.util import ArrayList
from java.lang import Thread, InterruptedException
from java.lang import Class
lang.System.loadLibrary('GraphMolWrap')
# Pull it in as a stream of string
from org.RDKit import *
from mol_parsing.rdkit_parse import get_or_create_rdmol

def find_mcs(mols):
    """Function to count the number of molecules making ito the end of the test"""
    out_mols = ROMol_Vect()
    while mols.hasNext():
        molobj = mols.next()
        rdmol, molobj = get_or_create_rdmol(molobj)
        # Add this mol to that vector
        out_mols.add(rdmol)
    # Now find the MCS
    mcs=RDKFuncs.findMCS(out_mols)#,True,1,60,False,False,False,False,AtomComparator.AtomCompareElements,BondComparator.BondCompareAny)
    # Now just retoun the count
    return mcs.getSmartsString()
# Now just call this function as the last one

mols = request.getBody(MoleculeObjectIterable)
my_mcs = find_mcs(mols)
request.body = my_mcs

