package org.squonk.data

/**
 * Created by timbo on 24/01/2016.
 */
class Molecules {

    static def ethanol = [

        v2000 : '''
  Mrv0541 02231512112D

  3  2  0  0  0  0            999 V2000
   -4.1987    0.0884    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
   -3.4842    0.5009    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
   -2.9008   -0.0825    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0
  1  2  1  0  0  0  0
  2  3  1  0  0  0  0
M  END
''',

        v3000 : '''
  Mrv0541 01191615492D

  0  0  0     0  0            999 V3000
M  V30 BEGIN CTAB
M  V30 COUNTS 3 2 0 0 0
M  V30 BEGIN ATOM
M  V30 1 C 2.31 -1.3337 0 0
M  V30 2 C 3.6437 -2.1037 0 0
M  V30 3 O 4.9774 -1.3337 0 0
M  V30 END ATOM
M  V30 BEGIN BOND
M  V30 1 1 1 2
M  V30 2 1 2 3
M  V30 END BOND
M  V30 END CTAB
M  END
''',

        smiles : 'CCO',

        inchi : 'InChI=1S/C2H6O/c1-2-3/h3H,2H2,1H3'
    ]
}
