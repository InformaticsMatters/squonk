package com.foo.domain

import grails.persistence.Entity

/**
 * Created by timbo on 09/12/2015.
 */
@Entity
class MyMolecule {

    String structure
    String name

    MyMolecule(String structure, String name) {
        this.structure = structure
        this.name = name
    }
}
