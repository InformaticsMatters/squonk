from com.im.lac.types import MoleculeObjectIterable
from java import lang
import sys
from java.util import ArrayList

from java.lang import Class
#lang.System.loadLibrary('GraphMolWrap')
# Pull it in as a stream of string
#from org.RDKit import *


#def parse_mol_simple(my_type, txt):
#    """Function to parse individual mols given a type"""
#    if my_type == "mol":
#        mol = RWMol.MolFromMolBlock(txt.strip())
#    elif my_type == "smiles":
#        # Assumes that smiles is the first column
#        mol = RWMol.MolFromSmiles(txt.split()[0])
#    elif my_type == "inchi":
#        # Assumes that INCHI is the first column
#        my_vals = ExtraInchiReturnValues()
#        mol = RDKFuncs.InchiToMol(my_txt.split()[0], my_vals)
#    return mol


# this gets the body converted to a MoleculeObjectIterable
mols = request.getBody(MoleculeObjectIterable)

counter = 0

while mols.hasNext():
    molobj = mols.next()
    # gets as string and is usually OK, but potentially could be a binary format 
    # such as cdx so getSourceAsBytes() which returns a byte array is safer, but 
    # as long as we only handle smiles, inchi, sdf, mol then strings are OK
    molstr = molobj.getSourceAsString()
    # Get the format and use this as a starting poitn to work out 
    molformat = molobj.getFormat()
#    rdmol = parse_mol_simple(molformat, molstr)
#    if not rdmol:
#        continue
    if molstr and molformat:
        counter += 1
    # here we simulate setting a property
    # molobj.putValue("somename", "somevalue)
    ### Make an RDKit mol

    # here we simulate storing a molecule representation so that we do not need
    # to regenerate it later
#    molobj.putRepresentation("rdkit.mol", rdmol)
    # once stored it can be retrieved later using
    #my_mol = molobj.getRepresention("rdkit.mol")
    # or
    # molobj.getRepresention("some.key", ClassName)
 
# for simplicity we return the count, but most likely we want to return another MoleculeObjectIterable 
request.body = counter
