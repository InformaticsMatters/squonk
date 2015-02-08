from java import lang
from com.im.lac.types import MoleculeObject, MoleculeObjectIterable
lang.System.loadLibrary('GraphMolWrap')
from org.RDKit import *
from find_props import funct_dict
from java.util import ArrayList


def filter_prop(request, function, max_ans, min_ans):
    """Function to filter a list of mols given a particular property
    Takes a request object with three potential attributes
    1) function - a string indicating the property (e.g. 'num_hba')
    2) Max_ans and 3) min_ans - floats indicating the upper and lower limits"""
    new_ans = ArrayList()
    # Loop through the mols
    mols = request.body
    for rdmol in mols:
        # Get the value for this property
        my_val = funct_dict[function](rdmol)
        # Add this value to the molecule
        rdmol.setProp(function, str(my_val))
        # Now do the checks
        if max_ans:
            if my_val < max_ans:
                continue
        if min_ans:
            if my_val > min_ans:
                continue
        # If it's passed these tests append to the out list
        new_ans.add(rdmol) 
    # Return the out list in the body of the request
    return new_ans
