package org.squonk.io;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by timbo on 23/01/2016.
 */
public abstract class AbstractMolDepict<T extends Object> {

    protected final DepictionParameters params;
    protected static final Color DEFAULT_BACKGROUND = new Color(255, 255, 255, 0);
    protected static final int DEFAULT_WIDTH = 100;
    protected static final int DEFAULT_HEIGHT = 75;
    protected static final boolean DEFAULT_EXPAND_TO_FIT = true;

    /** Constructor with these parameters as defaults
     *
     * @param params
     */
    public AbstractMolDepict(DepictionParameters params) {
        this.params = params;
    }

    /** Constructor using default parameters of 100px x 75px image, scaled to fit, and transparent background
     *
     */
    public AbstractMolDepict() {
        this.params = new DepictionParameters(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_EXPAND_TO_FIT, DEFAULT_BACKGROUND);
    }

    public abstract T smilesToMolecule(String molecule) throws Exception;

    public abstract T v2000ToMolecule(String molecule) throws Exception;

    public abstract T v3000ToMolecule(String molecule) throws Exception;

    public abstract T stringToMolecule(String molecule) throws Exception;

    public T stringToMolecule(String s, String molFormat) throws Exception {
        if (molFormat != null) {
            if (molFormat.toLowerCase().startsWith("smiles")) {
                return smilesToMolecule(s);
            } else if (molFormat.toLowerCase().startsWith("mol:v2")) {
                return v2000ToMolecule(s);
            } else if (molFormat.toLowerCase().startsWith("mol:v3")) {
                return v3000ToMolecule(s);
            }
        }
        return stringToMolecule(s);
    }

    public abstract BufferedImage moleculeToImage(T molecule, DepictionParameters params) throws Exception;

    public BufferedImage moleculeToImage(T molecule) throws Exception {
        return moleculeToImage(molecule, params);
    }

    public abstract String moleculeToSVG(T molecule, DepictionParameters params) throws Exception;

    public String moleculeToSVG(T molecule) throws Exception {
        return moleculeToSVG(molecule, params);
    }

    public byte[] writeImage(BufferedImage img, String format) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, format, out);
        out.close();
        return out.toByteArray();
    }

    public String stringToSVG(String mol, DepictionParameters params) throws Exception {
        return moleculeToSVG(stringToMolecule(mol), params);
    }

    public String stringToSVG(String mol, String molFormat, DepictionParameters params) throws Exception {
        return moleculeToSVG(stringToMolecule(mol, molFormat), params);
    }

    public String stringToSVG(String mol) throws Exception {
        return moleculeToSVG(stringToMolecule(mol), params);
    }

    public String stringToSVG(String mol, String molFormat) throws Exception {
        return moleculeToSVG(stringToMolecule(mol, molFormat), params);
    }

    public String smilesToSVG(String mol, DepictionParameters params) throws Exception {
        return moleculeToSVG(smilesToMolecule(mol), params);
    }

    public String smilesToSVG(String mol) throws Exception {
        return moleculeToSVG(smilesToMolecule(mol), params);
    }

    public String v2000ToSVG(String mol, DepictionParameters params) throws Exception {
        return moleculeToSVG(v2000ToMolecule(mol), params);
    }

    public String v2000ToSVG(String mol) throws Exception {
        return moleculeToSVG(v2000ToMolecule(mol), params);
    }

    public String v3000ToSVG(String mol, DepictionParameters params) throws Exception {
        return moleculeToSVG(v3000ToMolecule(mol), params);
    }

    public String v3000ToSVG(String mol) throws Exception {
        return moleculeToSVG(v3000ToMolecule(mol), params);
    }

    public byte[] stringToImage(String mol, String imgFormat, DepictionParameters params) throws Exception {
        return writeImage(moleculeToImage(stringToMolecule(mol), params), imgFormat);
    }

    public byte[] stringToImage(String mol, String imgFormat) throws Exception {
        return writeImage(moleculeToImage(stringToMolecule(mol), params), imgFormat);
    }

    public byte[] smilesToImage(String mol, String imgFormat, DepictionParameters params) throws Exception {
        return writeImage(moleculeToImage(smilesToMolecule(mol), params), imgFormat);
    }

    public byte[] smilesToImage(String mol, String imgFormat) throws Exception {
        return writeImage(moleculeToImage(smilesToMolecule(mol), params), imgFormat);
    }

    public byte[] v2000ToImage(String mol, String imgFormat, DepictionParameters params) throws Exception {
        return writeImage(moleculeToImage(v2000ToMolecule(mol), params), imgFormat);
    }

    public byte[] v2000ToImage(String mol, String imgFormat) throws Exception {
        return writeImage(moleculeToImage(v2000ToMolecule(mol), params), imgFormat);
    }

    public byte[] v3000ToImage(String mol, String imgFormat, DepictionParameters params) throws Exception {
        return writeImage(moleculeToImage(v2000ToMolecule(mol), params), imgFormat);
    }

    public byte[] v3000ToImage(String mol, String imgFormat) throws Exception {
        return writeImage(moleculeToImage(v3000ToMolecule(mol), params), imgFormat);
    }

    protected DepictionParameters depictionParameters(DepictionParameters maybeNull) {
        return maybeNull == null ? this.params : maybeNull;

    }


}
