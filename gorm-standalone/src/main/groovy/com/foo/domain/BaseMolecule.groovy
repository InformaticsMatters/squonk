package com.foo.domain

import grails.persistence.Entity

@Entity
class BaseMolecule {
    Long id
    String structure
    static constraints = {
        id blank:false
        structure blank:false
    }

    BaseMolecule(Long id, String structure) {
        this.id = id
        this.structure = structure
    }
}
