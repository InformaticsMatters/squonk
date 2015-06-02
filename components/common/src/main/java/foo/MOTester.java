/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package foo;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.im.lac.types.MoleculeObject;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author timbo
 */
public class MOTester {

    static String jsonObject = "{\"source\": \"c1ccc2sc(SSc3nc4ccccc4s3)nc2c1\", \"values\": {\"mamma\": 1234, \"banana\": \"yellow\"}, \"format\": \"smiles\"}";

    static String jsonArray = "[{\"source\": \"c1ccc2sc(SSc3nc4ccccc4s3)nc2c1\", \"values\": {\"mamma\": 999}, \"format\": \"smiles\"}\n"
            + ",{\"source\": \"O=[N+]([O-])c1cc(Cl)c(O)c([N+](=O)[O-])c1\", \"values\": {}, \"format\": \"smiles\"}\n"
            + ",{\"source\": \"N=c1[nH]cc([N+](=O)[O-])s1\", \"values\": {}, \"format\": \"smiles\"}\n"
            + ",{\"source\": \"Nc1ccc2c(c1)C(=O)c1ccccc1C2=O\", \"values\": {}, \"format\": \"smiles\"}\n"
            + ",{\"source\": \"O=C(O)c1ccccc1-c1c2ccc(O)c(Br)c2oc2c(Br)c(=O)ccc1-2\", \"values\": {}, \"format\": \"smiles\"}\n"
            + ",{\"source\": \"CN(C)C1=C(Cl)C(=O)c2ccccc2C1=O\", \"values\": {\"mamma\": 1234, \"banana\": \"green\"}, \"format\": \"smiles\"}\n"
            + "]";

    public static void main(String[] args) throws Exception {
        long t0 = System.currentTimeMillis();
        ObjectMapper mapper = new ObjectMapper();
        long t1 = System.currentTimeMillis();
        SimpleModule module = new SimpleModule();
        long t2 = System.currentTimeMillis();

        Map<String, Class> mappings = new HashMap<>();
        mappings.put("mamma", BigInteger.class);
        mappings.put("banana", String.class);
        module.addDeserializer(MoleculeObject.class, new MoleculeObjectDeserializer());
        //module.addDeserializer(PropertyHolder.class, new PropertyHolderDeserializer(mappings));

        mapper.registerModule(module);
        module.addDeserializer(PropertyHolder.class, new PropertyHolderDeserializer(mappings));
        long t3 = System.currentTimeMillis();

        System.out.println("JSON: " + jsonObject);
        JsonParser parser1 = mapper.getFactory().createParser(jsonObject);
        MoleculeObject mol = parser1.readValueAs(MoleculeObject.class);
        System.out.println("MO: " + mol);

        System.out.println("JSON: " + jsonArray);
        JsonParser parser2 = mapper.getFactory().createParser(jsonArray);
        Iterator<MoleculeObject> mols = parser2.readValuesAs(MoleculeObject.class);
        System.out.println("Mols: " + mols);
        while (mols.hasNext()) {
            MoleculeObject mo = mols.next();
            System.out.println("MO: " + mo);
        }

        long t4 = System.currentTimeMillis();

        System.out.println("ObjectMapper Took " + (t1 - t0));
        System.out.println("Module Took " + (t3 - t1));
        System.out.println("Parsing Took " + (t4 - t3));
    }

}
