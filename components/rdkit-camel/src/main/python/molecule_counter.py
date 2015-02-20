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


def count_in(mols):
    """Function to count the number of molecules making ito the end of the test"""
    counter = 0
    while mols.hasNext():
        molobj = mols.next()
        my_val = molobj.getValue("my_test")
        if my_val == "true":
            pass
        else:
            continue
        counter += 1
    # Now just retoun the count
    return counter
# Now just call this function as the last one

mols = request.getBody(MoleculeObjectIterable)
my_count = count_in(mols)
request.body = my_count
