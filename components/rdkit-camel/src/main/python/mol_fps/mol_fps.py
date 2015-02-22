# Functions to calculate molecular fingerprints, find similarities and do clustering
from java import lang
from java.lang import Class
lang.System.loadLibrary('GraphMolWrap')
# Pull it in as a stream of string
from org.RDKit import *



class GetFP():
    def __init__(self):
        self.description = "Class to find Fingerprints using the RDKit - only compulsory variable is RDMol"
   
    def maccs(self, rdmol):
        """Function to find the MACCS keys for a molecule"""  
        return RDKFuncs.MACCSFingerprintMol(rdmol)
    
    def morgan(self, rdmol, rad=2):
        """Function find the morgan fingerprint for a molecule"""
        return RDKFuncs.MorganFingerprintMol(rdmol, rad)

    def rdkit(self, rdmol):
        """Function to find the RDKit fingerpritn for a molecule"""
        return RDKFuncs.RDKFingerprintMol(rdmol)

class GetClusts():
    """Class to cluster a list of molecule fingerprints"""
    def __init__(self):
        self.description = "Class to cluster molecules based on a distance matrix"
    
    def hierarchical_clusters(self, dist_mat):
        """Function to take a distane matrix and return a cluster"""
        return None

class GetSim():
    """Class of functions to find the similarity between two moelcuels"""
    def __init__(self):
        self.description = "Class to find the similarity between two molecules using different metrics"
    
    def tanimoto(self, rdmol, other_mol):
        """Function to find the tanimoto distance between two fingerprints"""
        return RDKFuncs.TanimotoSimilarity(rdmol, other_mol)

    def dice(self, rdmol, other_mol):
        """Function to find the dice distance between two fingerprints"""
        return RDKFuncs.DiceSimilarity(rdmol, other_mol)

