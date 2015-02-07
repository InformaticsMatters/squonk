from com.im.lac.types import MoleculeObjectIterable

# this gets the body converted to a MoleculeObjectIterable
mols = request.getBody(MoleculeObjectIterable)

count = 0
while mols.hasNext():
    count = count + 1
    molobj = mols.next()
    # gets as string and is usually OK, but potentially could be a binary format 
    # such as cdx so getSourceAsBytes() which returns a byte array is safer, but 
    # as long as we only handle smiles, inchi, sdf, mol then strings are OK
    molstr = molobj.getSourceAsString()

    # here we simulate setting a property
    # molobj.putValue("somename", "somevalue)

    # here we simulate storing a molecule representation so that we do not need
    # to regenerate it later
    # molobj.putRepresentation("some.key", "<rdkit molecule>")
    # once stored it can be retrieved later using
    # molobj.getRepresention("some.key")
    # or
    # molobj.getRepresention("some.key", ClassName)

    
# for simplicity we return the count, but most likely we want to return another MoleculeObjectIterable 
request.body = count