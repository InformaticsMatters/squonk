from java.lang import Class
from com.im.lac.types import MoleculeObject, MoleculeObjectIterable


mols = request.body
counter = 0

for mol in mols:
    counter += 1


request.body = counter
