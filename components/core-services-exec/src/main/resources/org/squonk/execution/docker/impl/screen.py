import sys
from rdkit import Chem
from rdkit.Chem import AllChem
from rdkit.Chem import MACCSkeys
from rdkit import DataStructs
from rdkit.Chem.Fingerprints import FingerprintMols
import gzip, json
import argparse

sim_field = "similarity"

descriptors = {
    #'atompairs':   lambda m: Pairs.GetAtomPairFingerprint(m),
    'maccs':       lambda m: MACCSkeys.GenMACCSKeys(m),
    'morgan2':     lambda m: AllChem.GetMorganFingerprint(m,2),
    'morgan3':     lambda m: AllChem.GetMorganFingerprint(m,3),
    'rdkit':       lambda m: FingerprintMols.FingerprintMol(m),
    #'topo':        lambda m: Torsions.GetTopologicalTorsionFingerprint(m)
}

metrics = {'asymmetric':DataStructs.AsymmetricSimilarity,
           'braunblanquet':DataStructs.BraunBlanquetSimilarity,
           'cosine':DataStructs.CosineSimilarity,
           'dice': DataStructs.DiceSimilarity,
           'kulczynski':DataStructs.KulczynskiSimilarity,
           'mcconnaughey':DataStructs.McConnaugheySimilarity,
           #'onbit':DataStructs.OnBitSimilarity,
           'rogotgoldberg':DataStructs.RogotGoldbergSimilarity,
           'russel':DataStructs.RusselSimilarity,
           'sokal':DataStructs.SokalSimilarity,
           'tanimoto': DataStructs.TanimotoSimilarity
           #'tversky': DataStructs.TverskySimilarity
           }

parser = argparse.ArgumentParser(description='RDKit screen')
parser.add_argument('query', help='query structure as smiles')
parser.add_argument('threshold', type=float, help='similarity threshoild (1.0 means identical)')
parser.add_argument('-d', '--descriptor', choices=['maccs','morgan2','morgan3','rdkit'], default='rdkit', help='descriptor or fingerprint type (default rdkit)')
parser.add_argument('-m', '--metric',
                    choices=['asymmetric','braunblanquet','cosine','dice','kulczynski','mcconnaughey','rogotgoldberg','russel','sokal','tanimoto'],
                    default='tanimoto', help='similarity metric (default tanimoto)')
parser.add_argument('-i', '--input', default='input.sdf.gz', help="input SD file, Default 'input.sdf.gz'")
parser.add_argument('-o', '--output', default='output', help="base name for output file (no extension). Default 'output'")

args = parser.parse_args()

descriptor = descriptors[args.descriptor]
metric = metrics[args.metric.lower()]

gz_in = gzip.open(args.input)
suppl = Chem.ForwardSDMolSupplier(gz_in)
#gz_in = gzip.GzipFile("", "rb", fileobj=sys.stdin)
#suppl = Chem.ForwardSDMolSupplier(sys.stdin)

query_rdkitmol = Chem.MolFromSmiles(args.query)
query_fp = descriptor(query_rdkitmol)

gz_out=gzip.open(args.output + '.sdf.gz','w+')
writer = Chem.SDWriter(gz_out)

# OK, all looks good so we can hope that things will run OK.
# But before we start lets write the metadata so that the results can be handled.
m = open(args.output + '_metadata.json', 'w')
meta = {'type':'org.squonk.types.MoleculeObject','size':0,'valueClassMappings' :{sim_field:'java.lang.Float'}}
m.write(json.dumps(meta))
m.flush()
m.close()

i=0
count = 0
for mol in suppl:
    i +=1
    target_fp = descriptor(mol)
    sim = metric(query_fp, target_fp)

    if sim > args.threshold:
        count +=1
        #print i,sim
        for name in mol.GetPropNames():
            mol.ClearProp(name)
        mol.SetDoubleProp(sim_field, sim)
        writer.write(mol)

writer.flush()
writer.close()
gz_out.close()

m = open(args.output  + '_metrics.txt', 'w')
m.write('__InputCount__=' + str(i) + "\n")
m.write('__OutputCount__=' + str(count) + "\n")
m.write('RDKitScreen=' + str(i) + "\n")
m.flush()
m.close()

print "Found",count,"similar molecules"
