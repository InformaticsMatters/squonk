from java.lang import Class
from com.im.lac.types import MoleculeObject, MoleculeObjectIterable


# TODO - count the lines in txt
#txt = request.getBody(Class.forName("java.lang.String"))

#mols = request.getBody(MoleculeObjectIterable)

mols = request.body
counter = 0

for mol in mols:
    counter += 1
# Now count the number of mols
#while mols.hasNext():
#    molobj = mols.next()
#    counter +=1


request.body = counter
