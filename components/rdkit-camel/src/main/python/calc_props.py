from com.im.lac.types import MoleculeObject, MoleculeObjectIterable
from java import lang
import sys

sys.path.append('/lac/components/rdkit-camel/src/main/python')
sys.path.append("/RDKit/rdkit/Code/JavaWrappers/gmwrapper/org.RDKitDoc.jar")
sys.path.append("/RDKit/rdkit/Code/JavaWrappers/gmwrapper/libGraphMolWrap.so")
sys.path.append("/RDKit/rdkit/Code/JavaWrappers/gmwrapper/org.RDKit.jar")
sys.path.append("/RDKit/rdkit/lib")
sys.path.append("/RDKit/rdkit/Code/JavaWrappers/gmwrapper")
from java.util import ArrayList
from find_props.find_props import calc_props



from java.lang import Class
lang.System.loadLibrary('GraphMolWrap')
# Pull it in as a stream of string
from org.RDKit import *


my_funct = request.getHeader("function")

my_list = request.body

counter = 0
calc_props(request, my_funct)
for item in my_list:
  counter+=1




request.body = counter
