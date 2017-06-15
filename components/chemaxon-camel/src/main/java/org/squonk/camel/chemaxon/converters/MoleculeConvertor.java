/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.camel.chemaxon.converters;

import chemaxon.formats.MolFormatException;
import chemaxon.struc.Molecule;
import org.apache.camel.Converter;
import org.squonk.chemaxon.molecule.MoleculeIterable;
import org.squonk.chemaxon.molecule.MoleculeUtils;
import org.squonk.types.MoleculeObject;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Iterator;
import org.apache.camel.Exchange;

/**
 * Created by timbo on 21/04/2014.
 */
@Converter
public class MoleculeConvertor {

    @Converter
    public static Molecule convertToMolecule(MoleculeObject mo, Exchange exchange) throws MolFormatException {
        return MoleculeUtils.fetchMolecule(mo, false);
    }

//    @Converter
//    public static MoleculeObject convertToMoleculeObject(String s, Exchange exchange) {
//        return new MoleculeObject(s);
//    }

    @Converter
    public static Molecule convertToMolecule(String s, Exchange exchange) throws MolFormatException {
        return MoleculeUtils.convertToMolecule(s);
    }

    @Converter
    public static Molecule convertToMolecule(byte[] bytes, Exchange exchange) throws MolFormatException {
        return MoleculeUtils.convertToMolecule(bytes);
    }

    @Converter
    public static Molecule convertToMolecule(Blob blob, Exchange exchange)
            throws MolFormatException, SQLException {
        return MoleculeUtils.convertToMolecule(blob);
    }

    @Converter
    public static Molecule convertToMolecule(Clob clob, Exchange exchange)
            throws MolFormatException, SQLException {
        return MoleculeUtils.convertToMolecule(clob);
    }

    @Converter
    public static Molecule convertToMolecule(InputStream is, Exchange exchange)
            throws IOException {
        try (InputStream input = is) {
            Iterator<Molecule> mols = createMoleculeIterator(input, exchange);
            if (mols != null && mols.hasNext()) {
                return mols.next();
            }
            return null;
        }
    }

    @Converter
    public static Iterator<Molecule> createMoleculeIterator(InputStream is, Exchange exchange)
            throws IOException {
        return createMoleculeIterable(is, exchange).iterator();
    }

    @Converter
    public static MoleculeIterable createMoleculeIterable(InputStream is, Exchange exchange)
            throws IOException {
        return MoleculeUtils.createIterable(is);
    }
}
