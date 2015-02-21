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
from mol_fps.mol_fps import GetFP, GetSim, GetClusts 

def cluster_mols():
    """Function to count the number of molecules making ito the end of the test"""
    out_array = []
    # The functions to do the clustering
    get_clusts = GetClusts()
    get_sim = GetSim()
    get_fp = GetFP()
    while mols.hasNext():
        molobj = mols.next()
        rdmol, molobj = get_or_create_rdmol(molobj)
        # Add this mol to that vector
        out_fp = getattr(get_fp, my_fp)(rdmol)
        out_array.append(out_fp)
    # Now find the differences between
    dist_mat = ArrayList()
    for i, fp in enumerate(out_array):
        for j,fp_other in enumerate(out_array[i:]):
            dist = getattr(get_sim, my_sim)(fp,fp_other)
            dist_mat.add(dist)
    # Now just retoun the count
    return len(dist_mat)
# Now just call this function as the last one

mols = request.getBody(MoleculeObjectIterable)

my_fp = request.getHeader("fingerprint")
my_clust = request.getHeader("clustering")
my_sim = request.getHeader("similarity")

my_clusts = cluster_mols()
request.body = my_clusts

