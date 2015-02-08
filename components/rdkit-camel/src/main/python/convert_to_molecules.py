
from com.im.lac.types import MoleculeObject, MoleculeObjectIterable
from java import lang
import sys
from java.util import ArrayList

from java.lang import Class
lang.System.loadLibrary('GraphMolWrap')
# Pull it in as a stream of string
from org.RDKit import *


def check_stream_type(in_stream):
    """Function to check what the input is"""
    # File types, SMILES, SD, MOL, INCHI
    print "Checking stream"
    # First check - is this a file, or a stream
    # If it is a file, then we want to convert to stream, everrything should be stream
    print "Checking type"
    # Now check what file format we havea
    my_mols = in_stream.split("$$$$")
    if len(my_mols) > 1:
        # Read the first mol
        rdmol = RWMol.MolFromMolBlock(my_mols[0])
        # If this is none - this may not be a MOL file so we need to test the others
        if rdmol is None:
            # Check if it can read any of the others
            if len([x for x in my_mols if RWMol.MolFromMolBlock(x)]) == 0:
                return None, None, None, None
            else:
                # IF it reads as an - set the flags accordingly
                file_flag = "sdf"
                delim = "$$$$"
                return file_flag, delim, 0, None
        else:
            # IF it reads as an - set the flags accordingly
            file_flag = "sdf"
            delim = "$$$$"
            return file_flag, delim, 0, None
    # Now split the file on lines
    my_mols = in_stream.split("\n")
    # IF there's only one line assume there isn't a header
    if len(my_mols) == 1:
        test_line = my_mols[0]
    # Otherwise assume the first line MIGHT be a header
    else:
        test_line = my_mols[1]
    header = my_mols[0]
    # Check for tab,  comma and space seperatin
    if len(header.split("\t")) > 1:
        delim = "\t"
    elif len(header.split(",")) > 1:
        delim = ","
    elif len(header.split(" ")) > 1:
        delim = " "
    else:
        print "Assuming only one column"
        delim = " "
    # Check for whitespace delimeter
    rdmol = [i for i in range(len(test_line.split(delim))) if None != RWMol.MolFromSmiles(test_line.split(delim)[i])]
    # If we get any ols - this is a smiles file
    if rdmol:
         file_flag = "smiles"
         return file_flag, delim, 0, True ##DEL ME
         if RWMol.MolFromSmiles(header.split(delim)[rdmol[0]]):
             return file_flag, delim, rdmol[0], True
         else:
             return file_flag, delim, rdmol[0], False
    elif rdmol is None:

        pass
    # Needed to get the InChI reading correctly
    my_vals = ExtraInchiReturnValues()
    # Check if there are Inchis
    rdmol = [i for i in range(len(test_line.split(delim))) if RDKFuncs.InchiToMol(test_line.split(delim)[i], my_vals) != None ]
    # If there are Inchis - assign that as the flag
    if rdmol:
        file_flag = "inchi"
        if RDKFuncs.InchiToMol(header.split(delim)[rdmol[0]], my_vals):
            return file_flag, delim, rdmol[0], True
        else:
            return file_flag, delim, rdmol[0], False
    elif rdmol is None:
        pass
    print "UNKNOWN FILE TYPE"
    return None, None, None, None


def read_mols(file_flag, delim, col_ind, header, in_stream):
    """Function to actually read the mols
    Takes in a file_flag - indicating the type of file
    delim - string indicating what the delimiter is
    col_ind - an int indicating the column
    header - a bool indicating if there is a header
    in_stream - the stream to read
    Returns - a Pythonlist of RDKit molecules"""
    out_l = ArrayList()
    if file_flag == "sdf":
        # Read the SD file
        suppl = in_stream.split("$$$$")
        # Loop through the mols 
        for item in suppl:
            # Append to the list
            my_stream = item.strip()
            mol = RWMol.MolFromMolBlock(my_stream)
            if not mol:
                continue
            out_l.add(mol)
        # Return the list
        return out_l
    elif file_flag == "smiles":
        me =  """Need to add header identifier etc"""
        suppl = in_stream.split("\n")
        for item in suppl:
            mol = RWMol.MolFromSmiles(item.split(delim)[col_ind])
            if mol is None:
                continue
            out_l.add(mol)
        return out_l
    elif file_flag == "inchi":
        my_vals = ExtraInchiReturnValues()
        in_mols = in_stream.split("\n")
        if header:
            out_vals = [x for x in in_mols[0].split(delim)]
            for mol in in_mols:
                o_mol = RDKFuncs.InchiToMol(mol.split(delim)[col_ind], my_vals)
                if o_mol:
                    out_l.add(RDKFuncs.InchiToMol(mol.split(delim)[col_ind], my_vals))
            return out_l
        else:
            [out_l.add(RDKFuncs.InchiToMol(mol.split(delim)[col_ind], my_vals)) for mol in in_mols if RDKFuncs.InchiToMol(mol.split(delim)[col_ind], my_vals)] 
            return out_l       

def parse_mols(in_stream):# Make the request
    # Check the type
    print request
    file_flag, delim, col_ind, header = check_stream_type(in_stream)
    print "FILE TYPE: ",file_flag
    print "DELIMITER: ",delim
    print "COLUMN IND: ",col_ind
    print "COLUMN HEADER: ",header
    # Now read the files and pass out as a stream of molecule
    out_mols = read_mols(file_flag, delim, col_ind, header, in_stream)
    return out_mols
# Just replace this with parse_mols and pss in the request and we've got it
txt = request.getBody(Class.forName("java.lang.String"))
out_mols = parse_mols(txt)
request.body = out_mols
