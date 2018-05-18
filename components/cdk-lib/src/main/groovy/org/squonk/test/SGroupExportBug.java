/*
 * Copyright (c) 2018 Informatics Matters Ltd.
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

package org.squonk.test;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.silent.AtomContainer;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetUtils;
import org.squonk.types.MoleculeObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** This class is for debugging purposes only. It is not used in Squonk.
 *
 */
public class SGroupExportBug {

    public static final void main(String[] args) throws Exception {

        Dataset<MoleculeObject> dataset = DatasetUtils.createDataset(
                new FileInputStream("../data/testfiles/Building_blocks_GBP.data.gz"),
                new FileInputStream("../data/testfiles/Building_blocks_GBP.metadata"));

        final SDFWriter writer = new SDFWriter(new OutputStreamWriter(new ByteArrayOutputStream()));
        final AtomicInteger size = new AtomicInteger(0);

        dataset.getStream().forEachOrdered((mo) -> {
            if (size.intValue() < 4) {
                System.out.println(mo.getSource());
            }
            try {
                MDLV2000Reader v2000Parser = new MDLV2000Reader(new ByteArrayInputStream(mo.getSource().getBytes()));
                IAtomContainer mol = v2000Parser.read(new AtomContainer());
                writer.write(mol);
            } catch (Exception e) {
                throw new RuntimeException("FAIL", e);
            }
            size.incrementAndGet();
        });
        writer.close();

        assert size.intValue() == 7003;


    }

}
