/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.im.lac.camel.chemaxon.processor;

import chemaxon.standardizer.Standardizer;
import chemaxon.struc.Molecule;

import java.io.File;
import java.io.InputStream;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 *
 * @author timbo
 */
public class StandardizerProcessor implements Processor {

    private final Standardizer standardizer;

    public StandardizerProcessor(String config) {
        standardizer = new Standardizer(config);
    }

   public StandardizerProcessor(File file) {
        standardizer = new Standardizer(file);
    }

    public StandardizerProcessor(InputStream input) {
        standardizer = new Standardizer(input);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Iterable<Molecule> mols = exchange.getIn().getBody(Iterable.class);
        for (Molecule mol : mols) {
            standardizer.standardize(mol);
        }
    }

}
