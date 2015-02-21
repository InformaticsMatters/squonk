# Code to find the MMPs from a list of molecules
#
#     * Redistributions of source code must retain the above copyright
#       notice, this list of conditions and the following disclaimer.
#     * Redistributions in binary form must reproduce the above
#       copyright notice, this list of conditions and the following
#       disclaimer in the documentation and/or other materials provided
#       with the distribution.
#     * Neither the name of GlaxoSmithKline Research & Development Ltd.
#       nor the names of its contributors may be used to endorse or promote
#       products derived from this software without specific prior written
#       permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
# "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
# LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
# A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
# OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
# SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
# LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
# DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
# THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#
# Created by Jameed Hussain, July 2013
from java import lang
import sys
from java.lang import Class
lang.System.loadLibrary('GraphMolWrap')
# Pull it in as a stream of string
from org.RDKit import *
sys.path.append("/usr/lib/python2.7/")
import re

def find_correct(f_array):

    core = ""
    side_chains = ""

    for f in f_array:
        attachments = f.count("*")
        if (attachments == 1):
            side_chains = "%s.%s" % (side_chains,f)
        else:
            core = f

    side_chains = side_chains.lstrip('.')

    #cansmi the side chains
    temp = RWMol.MolFromSmiles(side_chains)
    side_chains = RWMol.MolToSmiles( temp, True )

    #and cansmi the core
    temp = RWMol.MolFromSmiles(core)
    core = RWMol.MolToSmiles( temp, True )

    return core,side_chains

def delete_bonds(smi,id,mol,bonds,out):
    #use the same parent mol object and create editable mol
# In jython a mol is editable...
    em = RWMol.MolFromSmiles(RWMol.MolToSmiles(mol, True))
    #loop through the bonds to delete
    isotope = 0
    isotope_track = {};
    for my_vect in bonds:
        isotope += 1
        #remove the bond
        i = my_vect

        em.removeBond(i.get(0).second,i.get(1).second)
        # Now add attachement points
        newAtomA = em.addAtom(Atom(0))
        em.addBond(i.get(0).second,newAtomA,Bond.BondType.SINGLE)
        # Now add the atom
        newAtomB = em.addAtom(Atom(0))
        em.addBond(i.get(1).second,newAtomB,Bond.BondType.SINGLE)
        # Keep track of where to put isotopes
        isotope_track[newAtomA] = isotope
        isotope_track[newAtomB] = isotope


    #should be able to get away without sanitising mol
    #as the existing valencies/atoms not changed
    modifiedMol = em #RWMol.MolFromSmiles(RWMol.MolToSmiles(em, True))
    #canonical smiles can be different with and without the isotopes
    #hence to keep track of duplicates use fragmented_smi_noIsotopes
    fragmented_smi_noIsotopes = RWMol.MolToSmiles(modifiedMol,True)

    valid = True
    fragments = fragmented_smi_noIsotopes.split(".")

    #check if its a valid triple cut
    if(isotope == 3):
        valid = False
        for f in fragments:
            matchObj = re.search( '\*.*\*.*\*', f)
            if matchObj:
                valid = True
                break

    if valid:
        if(isotope == 1):
            fragmented_smi_noIsotopes = re.sub('\[\*\]', '[*:1]', fragmented_smi_noIsotopes)

            fragments = fragmented_smi_noIsotopes.split(".")

            #print fragmented_smi_noIsotopes
            s1 = RWMol.MolFromSmiles(fragments[0])
            s2 = RWMol.MolFromSmiles(fragments[1])

            #need to cansmi again as smiles can be different
            output = '%s,%s,,%s.%s' % (smi,id,RWMol.MolToSmiles(s1,True),RWMol.MolToSmiles(s2,True) )
            if( (output in out) == False):
                out.add(output)

        elif (isotope >= 2):
            #add the isotope labels
            for key in isotope_track:
                #to add isotope lables
                modifiedMol.getAtomWithIdx(key).setIsotope(isotope_track[key])
            fragmented_smi = RWMol.MolToSmiles(modifiedMol,True)

            #change the isotopes into labels - currently can't add SMARTS or labels to mol
            fragmented_smi = re.sub('\[1\*\]', '[*:1]', fragmented_smi)
            fragmented_smi = re.sub('\[2\*\]', '[*:2]', fragmented_smi)
            fragmented_smi = re.sub('\[3\*\]', '[*:3]', fragmented_smi)

            fragments = fragmented_smi.split(".")

            #identify core/side chains and cansmi them
            core,side_chains = find_correct(fragments)

            #now change the labels on sidechains and core
            #to get the new labels, cansmi the dot-disconnected side chains
            #the first fragment in the side chains has attachment label 1, 2nd: 2, 3rd: 3
            #then change the labels accordingly in the core
            #this is required by the indexing script, as the side-chains are "keys" in the index
            #this ensures the side-chains always have the same numbering

            isotope_track = {}
            side_chain_fragments = side_chains.split(".")

            for s in xrange( len(side_chain_fragments) ):
                matchObj = re.search( '\[\*\:([123])\]', side_chain_fragments[s] )
                if matchObj:
                    #add to isotope_track with key: old_isotope, value:
                    isotope_track[matchObj.group(1)] = str(s+1)

            #change the labels if required
            if(isotope_track['1'] != '1'):
                core = re.sub('\[\*\:1\]', '[*:XX' + isotope_track['1'] + 'XX]' , core)
                side_chains = re.sub('\[\*\:1\]', '[*:XX' + isotope_track['1'] + 'XX]' , side_chains)
            if(isotope_track['2'] != '2'):
                core = re.sub('\[\*\:2\]', '[*:XX' + isotope_track['2'] + 'XX]' , core)
                side_chains = re.sub('\[\*\:2\]', '[*:XX' + isotope_track['2'] + 'XX]' , side_chains)

            if(isotope == 3):
                if(isotope_track['3'] != '3'):
                    core = re.sub('\[\*\:3\]', '[*:XX' + isotope_track['3'] + 'XX]' , core)
                    side_chains = re.sub('\[\*\:3\]', '[*:XX' + isotope_track['3'] + 'XX]' , side_chains)

            #now remove the XX
            core = re.sub('XX', '' , core)
            side_chains = re.sub('XX', '' , side_chains)

            output = '%s,%s,%s,%s' % (smi,id,core,side_chains)
            if( (output in out) == False):
                out.add(output)

def fragment_mol(smi,id):

    mol = RWMol.MolFromSmiles(smi)

    #different cuts can give the same fragments
    #to use outlines to remove them
    outlines = set()

    if(mol == None):
        sys.stderr.write("Can't generate mol for: %s\n" % (smi) )
    else:
        #SMARTS for "acyclic and not in a functional group"
        smarts = RWMol.MolFromSmarts("[#6+0;!$(*=,#[!#6])]!@!=!#[*]")

        #finds the relevant bonds to break
        #find the atoms maches
        matching_atoms = mol.getSubstructMatches(smarts)
        
        total = matching_atoms.size()

        #catch case where there are no bonds to fragment
        if(total == 0):
            output = '%s,%s,,' % (smi,id)
            if( (output in outlines) == False ):
                outlines.add(output)

        bonds_selected = []

        #loop to generate every single, double and triple cut in the molecule
        for x in xrange(total):
            #print matches[x]
            bonds_selected.append(matching_atoms.get(x))
            delete_bonds(smi,id,mol,bonds_selected,outlines)
            bonds_selected = []
            for y in xrange(x+1,total):
                #print matching_atoms[x],matching_atoms[y]
                bonds_selected.append(matching_atoms.get(x))
                bonds_selected.append(matching_atoms.get(y))
                delete_bonds(smi,id,mol,bonds_selected,outlines)
                bonds_selected = []
                for z in xrange(y+1, total):
                    #print matching_atoms[x],matching_atoms[y],matching_atoms[z]
                    bonds_selected.append(matching_atoms.get(x))
                    bonds_selected.append(matching_atoms.get(y))
                    bonds_selected.append(matching_atoms.get(z))
                    delete_bonds(smi,id,mol,bonds_selected,outlines)
                    bonds_selected = []
            #right, we are done.
    return outlines

def find_mmps(mols):
    out_mols = []
    #read the mols
    for line in mols:
        smiles = line[0]
        cmpd_id = line[1]
        #returns a set containing the output
        o = fragment_mol(smiles,cmpd_id)
        for l in o:
           out_mols.append(l)
    return out_mols
