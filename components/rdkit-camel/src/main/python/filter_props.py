# File to do the molecular filtering
from com.im.lac.types import MoleculeObject, MoleculeObjectIterable
from java import lang
import sys

#sys.path.append('/lac/components/rdkit-camel/src/main/python')
#sys.path.append("/RDKit/rdkit/Code/JavaWrappers/gmwrapper/org.RDKitDoc.jar")
#sys.path.append("/RDKit/rdkit/Code/JavaWrappers/gmwrapper/libGraphMolWrap.so")
#sys.path.append("/RDKit/rdkit/Code/JavaWrappers/gmwrapper/org.RDKit.jar")
#sys.path.append("/RDKit/rdkit/lib")
#sys.path.append("/RDKit/rdkit/Code/JavaWrappers/gmwrapper")
from java.util import ArrayList
from find_props.find_props import calc_props
from find_props.filter_props import filter_prop


from java.lang import Class
lang.System.loadLibrary('GraphMolWrap')
# Pull it in as a stream of string
from org.RDKit import *

# Get the header
my_head = request.getHeader("function").split("<")

if len(my_head) == 1:
    my_funct = my_head[0]
    min_ans == None
    max_ans == None
else:
    my_funct = my_head[1]
    min_ans = float(my_head[0])
    max_ans = float(my_head[2])

my_list = request.body

counter = 0
my_mols = filter_prop(request, my_funct, min_ans, max_ans)

# Now do the counting to check it's worked
for item in my_mols:
  counter+=1

request.body = counter


