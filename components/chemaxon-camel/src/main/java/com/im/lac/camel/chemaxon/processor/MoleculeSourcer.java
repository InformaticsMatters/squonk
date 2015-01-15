/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.im.lac.camel.chemaxon.processor;

import chemaxon.struc.Molecule;
import com.im.lac.chemaxon.molecule.MoleculeIterable;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.Exchange;

/**
 *
 * @author timbo
 */
public abstract class MoleculeSourcer {

    private static final Logger LOG = Logger.getLogger(MoleculeSourcer.class.getName());

    void handle(Exchange exchange) throws Exception {
        Molecule mol = exchange.getIn().getBody(Molecule.class);
        if (mol != null) {
            handleSingle(exchange, mol);
        } else {
            // try as iterable of molecules
            Iterable<Molecule> iterable = exchange.getIn().getBody(MoleculeIterable.class);
            if (iterable == null) {
                iterable = exchange.getIn().getBody(Iterable.class);
            }
            if (iterable != null) {
                handleMultiple(exchange, iterable.iterator());
            } else {
                // give up
                handleOther(exchange, exchange.getIn().getBody());
            }
        }
    }

    abstract void handleSingle(Exchange exchange, Molecule mol) throws Exception;

    abstract void handleMultiple(Exchange exchange, Iterator<Molecule> mols) throws Exception;

    protected void handleOther(Exchange exchange, Object o) {
        LOG.log(Level.WARNING, "Can't find molecules from {0}", o.getClass().getName());
        throw new IllegalArgumentException("No valid Molecule content could be found");
    }

}
