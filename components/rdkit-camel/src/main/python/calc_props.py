from com.im.lac.types import MoleculeObject, MoleculeObjectIterable
from java import lang
import sys
#Place the python module on the path
#sys.path.append('/lac/components/rdkit-camel/src/main/python')
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
