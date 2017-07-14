select count(*) from vendordbs.emolecules_order_bb_molfps where m@>'c1cccc2c1nncc2' ;
select count(*) from vendordbs.emolecules_order_sc_molfps where m@>'c1cccc2c1nncc2' ;
select count(*) from vendordbs.chembl_23_molfps where m@>'c1cccc2c1nncc2' ;
select count(*) from vendordbs.pdb_ligand_molfps where m@>'c1cccc2c1nncc2' ;

select count(*) from vendordbs.emolecules_order_bb_molfps  where mfp2%morganbv_fp('Cc1ccc2nc(-c3ccc(NC(C4N(C(c5cccs5)=O)CCC4)=O)cc3)sc2c1');
select count(*) from vendordbs.emolecules_order_bb_molfps  where ffp2%featmorganbv_fp('Cc1ccc2nc(-c3ccc(NC(C4N(C(c5cccs5)=O)CCC4)=O)cc3)sc2c1');
select count(*) from vendordbs.emolecules_order_bb_molfps  where rdk%rdkit_fp('Cc1ccc2nc(-c3ccc(NC(C4N(C(c5cccs5)=O)CCC4)=O)cc3)sc2c1');

select count(*) from vendordbs.emolecules_order_sc_molfps  where mfp2%morganbv_fp('Cc1ccc2nc(-c3ccc(NC(C4N(C(c5cccs5)=O)CCC4)=O)cc3)sc2c1');
select count(*) from vendordbs.emolecules_order_sc_molfps  where ffp2%featmorganbv_fp('Cc1ccc2nc(-c3ccc(NC(C4N(C(c5cccs5)=O)CCC4)=O)cc3)sc2c1');
select count(*) from vendordbs.emolecules_order_sc_molfps  where rdk%rdkit_fp('Cc1ccc2nc(-c3ccc(NC(C4N(C(c5cccs5)=O)CCC4)=O)cc3)sc2c1');

select count(*) from vendordbs.chembl_23_molfps  where mfp2%morganbv_fp('Cc1ccc2nc(-c3ccc(NC(C4N(C(c5cccs5)=O)CCC4)=O)cc3)sc2c1');
select count(*) from vendordbs.chembl_23_molfps  where ffp2%featmorganbv_fp('Cc1ccc2nc(-c3ccc(NC(C4N(C(c5cccs5)=O)CCC4)=O)cc3)sc2c1');
select count(*) from vendordbs.chembl_23_molfps  where rdk%rdkit_fp('Cc1ccc2nc(-c3ccc(NC(C4N(C(c5cccs5)=O)CCC4)=O)cc3)sc2c1');
