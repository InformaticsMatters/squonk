from com.im.lac.types import MoleculeObjectIterable
from java import lang
import sys
from java.util import ArrayList
from java.lang import Thread, InterruptedException
from java.lang import Class
lang.System.loadLibrary('GraphMolWrap')
# Pull it in as a stream of string
from org.RDKit import *
import time

def parse_mol_simple(my_type, txt):
    """Function to parse individual mols given a type"""
    if my_type == "mol":
        mol = RWMol.MolFromMolBlock(txt.strip())
    elif my_type == "smiles":
        # Assumes that smiles is the first column
        mol = RWMol.MolFromSmiles(txt.split()[0])
    elif my_type == "inchi":
        # Assumes that INCHI is the first column
        my_vals = ExtraInchiReturnValues()
        mol = RDKFuncs.InchiToMol(my_txt.split()[0], my_vals)
    return mol

def count_in(mols):
    # this gets the body converted to a MoleculeObjectIterable
    counter = 0
    while mols.hasNext():
        molobj = mols.next()
    # gets as string and is usually OK, but potentially could be a binary format 
    # such as cdx so getSourceAsBytes() which returns a byte array is safer, but 
    # as long as we only handle smiles, inchi, sdf, mol then strings are OK
        molstr = molobj.getSource()
    # Get the format and use this as a starting poitn to work out 
        molformat = molobj.getFormat()
        my_val = molobj.getValue("my_test")
        if my_val == "true":
            pass
        else:
            continue
#    rdmol = parse_mol_simple(molformat, molstr)
#        rdmol = molobj.getRepresentation("rdkit.mol")
        counter += 1
# for simplicity we return the count, but most likely we want to return another MoleculeObjectIterable 
    return counter
# Now just call this function as the last one

mols = request.getBody(MoleculeObjectIterable)
my_count = count_in(mols)
request.body = my_count
