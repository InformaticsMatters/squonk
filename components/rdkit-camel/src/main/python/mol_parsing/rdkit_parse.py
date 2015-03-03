#Series of functions to parse molecules based on RDKit

from java import lang
lang.System.loadLibrary('GraphMolWrap')
# Pull it in as a stream of string
from org.RDKit import *


def parse_mol_simple(my_type, txt):
    """Function to parse individual mols given a type"""
    if my_type == "mol":
        try:
            mol = RWMol.MolFromMolBlock(txt.strip())
        except:
            mol = RWMol.MolFromMolBlock(txt)
    elif my_type == "smiles":
        # Assumes that smiles is the first column
        mol = RWMol.MolFromSmiles(txt.split()[0])
    elif my_type == "inchi":
        # Assumes that INCHI is the first column
        my_vals = ExtraInchiReturnValues()
        mol = RDKFuncs.InchiToMol(my_txt.split()[0], my_vals)
    return mol


def parse_mol_obj(molobj):
    """Function to get the RDKit mol for a java mol obj"""
    molstr = molobj.getSource()
    # Get the format and use this as a starting poitn to work out 
    molformat = molobj.getFormat()
    # Now parse it with RDKit
    return parse_mol_simple(molformat, molstr)


def get_or_create_rdmol(molobj):
    """Function to either make or get an RDKit molecule"""
    # First check if it exists
    rdmol = molobj.getRepresentation("rdkit.mol")
    if not rdmol:
        rdmol = parse_mol_obj(molobj)
        molobj.putRepresentation(rdmol, "rdkit.mol")
    else:
        pass
    return rdmol, molobj
