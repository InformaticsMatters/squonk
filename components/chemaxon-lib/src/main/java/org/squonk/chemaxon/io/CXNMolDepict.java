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

package org.squonk.chemaxon.io;

import chemaxon.formats.MolExporter;
import chemaxon.formats.MolImporter;
import chemaxon.marvin.MolPrinter;
import chemaxon.struc.Molecule;
import org.squonk.io.AbstractMolDepict;
import org.squonk.io.DepictionParameters;

import java.awt.*;
import java.awt.image.*;

/**
 * Created by timbo on 24/01/2016.
 */
public class CXNMolDepict extends AbstractMolDepict<Molecule> {

    public CXNMolDepict() {
        super();
    }

    public CXNMolDepict(DepictionParameters params) {
        super(params);
    }

    @Override
    public Molecule smilesToMolecule(String molecule) throws Exception {
        return MolImporter.importMol(molecule, "smiles");
    }

    @Override
    public Molecule v2000ToMolecule(String molecule) throws Exception {
        return MolImporter.importMol(molecule, "mol");
    }

    @Override
    public Molecule v3000ToMolecule(String molecule) throws Exception {
        return MolImporter.importMol(molecule, "mol:V3");
    }

    @Override
    public Molecule stringToMolecule(String molecule) throws Exception {
        return MolImporter.importMol(molecule);
    }

    @Override
    public Molecule stringToMolecule(String molecule, String molFormat) throws Exception {
        return MolImporter.importMol(molecule, molFormat);
    }

    @Override
    public BufferedImage moleculeToImage(Molecule molecule, DepictionParameters params) throws Exception {

        DepictionParameters p = depictionParameters(params);
        Rectangle rect = new Rectangle(p.getWidth(), p.getHeight());

        MolPrinter molPrinter = new MolPrinter();
        //molPrinter.setBackgroundColor(p.getBackgroundColor());
        molPrinter.setMol(molecule);
        molPrinter.setDisplayQuality(1);
        molPrinter.setTransparent(true);

        BufferedImage image = new BufferedImage(p.getWidth(), p.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = image.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        graphics2D.setColor(p.getBackgroundColor());
        graphics2D.fillRect(0, 0, p.getWidth(), p.getHeight());
        double scale = molPrinter.maxScale(rect);
        molPrinter.setScale(scale);
        molPrinter.paint(graphics2D, rect);

//        BufferedImage filteredImage = imageToBufferedImage(image);
//        return filteredImage;

        return image;
    }

//    public Image makeWhiteTransparent(BufferedImage bufferedImage) {
//        ImageFilter filter = new RGBImageFilter() {
//
//            private int markerRGB = 0xFFFFFFFF;
//
//            @Override
//            public int filterRGB(final int x, final int y, final int rgb) {
//                if ((rgb | 0xFF000000) == markerRGB) {
//                    // mark the alpha bits as zero - transparent
//                    return 0x00FFFFFF & rgb;
//                } else {
//                    // nothing to do
//                    return rgb;
//                }
//            }
//        };
//
//        ImageProducer ip = new FilteredImageSource(bufferedImage.getSource(), filter);
//        return Toolkit.getDefaultToolkit().createImage(ip);
//    }

    private BufferedImage imageToBufferedImage(Image image) {
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bufferedImage.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return bufferedImage;
    }


    @Override
    public String moleculeToSVG(Molecule molecule, DepictionParameters params) throws Exception {
        String opts = "svg:nosource," + buildStandardOptions(params);
        System.out.println("Exporting mol "+ molecule + " with options " + opts);
        byte[] bytes = MolExporter.exportToBinFormat(molecule, opts);
        return new String(bytes);
    }

    private String buildStandardOptions(DepictionParameters params) {

        DepictionParameters p = depictionParameters(params);

        Color col = p.getBackgroundColor();
        StringBuilder b = new StringBuilder()
                .append("w").append(p.getWidth())
                .append(",h").append(p.getHeight());
        if (col == null) {
            b.append(",#FFFFFF");
        } else {
          b.append(",#").append(Integer.toHexString(col.getRGB()));
        }

        return b.toString();
    }

}
