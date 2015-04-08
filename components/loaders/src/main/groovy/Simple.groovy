
import com.im.lac.chemaxon.molecule.MoleculeUtils

import chemaxon.marvin.io.MRecord
import com.im.lac.chemaxon.molecule.MRecordIterator


Iterator<MRecord> iter = new MRecordIterator(new FileInputStream("/home/timbo/data/structures/drugbank/2015_04/all.sdf"))
int c = 0
try {
    while (iter.hasNext()) {
        MRecord rec = iter.next()
        println "$rec $c"
        c++
    }
} finally {
    iter.close()
}

//Iterator<MRecord> iter = new MRecordIterator(new FileInputStream("/home/timbo/data/structures/drugbank/2015_04/all.sdf"))
//int c = 0
//try {
//    while (true) {
//        Object o = iter.next()
//        println "$o $c"
//        c++
//        if (c > 10000) {
//            break
//        }
//    }
//} finally {
//    iter.close()
//}