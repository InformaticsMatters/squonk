# File to calculate properties for a molecule and add these properties back to the molecules
# property to be calculate will be put in using a request.header string
from java import lang
lang.System.loadLibrary('GraphMolWrap')
from org.RDKit import *

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

def mol_logp(mol, ret_val=False):
    """Function for calculating mol log p
    Takes an RDKit molecule
    Returns a int"""
    return RDKFuncs.calcMolLogP(mol)

# A dictionary to relate functioons tostrings
funct_dict = {"num_hba": num_hba,
"num_hbd": num_hbd,
"num_rings": num_rings,
"mol_logp": mol_logp}


def calc_props(request, function):
    for mol in request.body:
        val = funct_dict[function](mol)
        mol.setProp(function, str(val))
# Request will comprise two parts
## 1) Stream of molecuels
## 2) String relating to property

if __name__ == "__main__":
    print "calculating properties"
    calc_props(request)
