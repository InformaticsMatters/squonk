# File to calculate properties for a molecule and add these properties back to the molecules
# property to be calculate will be put in using a request.header string
from java import lang
from com.im.lac.types import MoleculeObject, MoleculeObjectIterable
lang.System.loadLibrary('GraphMolWrap')
from org.RDKit import *
import sys


def num_hba(mol):
    """Function for calculating number of H-bond acceptors
    Takes an RDKit molecule
    Returns an int"""
    return RDKFuncs.calcNumHBA(mol)

def num_hbd(mol):
    """Function for calculating number of H-bond donors
    Takes an RDKit molecule
    Returns an int"""
    return RDKFuncs.calcNumHBD(mol)

def num_rings(mol):
    """Function for calculating number of rings
    Takes an RDKit molecule
    Returns an int"""
    return RDKFuncs.calcNumRings(mol)

def mol_logp(mol):
    """Function for calculating mol log p
    Takes an RDKit molecule
    Returns a int"""
    return RDKFuncs.calcMolLogP(mol)

def mol_mr(mol):
    """Function to find the mass of a molecule
    Takes an RDKit molecule
    Returns a float"""
    return RDKFuncs.calcMolMR(mol)
# A dictionary to relate functioons tostrings
funct_dict = {"num_hba": num_hba,
"num_hbd": num_hbd,
"num_rings": num_rings,
"mol_logp": mol_logp,
"mol_mr": mol_mr}


def calc_props(rdmol, function):
    try:
        val = funct_dict[function](rdmol)
    except:
        val = None
        sys.stderr.write("ERROR  CALCULATNG PROPERTY -> " + function)
    return val
## 1) Stream of molecuels
## 2) String relating to property
