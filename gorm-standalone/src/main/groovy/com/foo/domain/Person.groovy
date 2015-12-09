package com.foo.domain

import grails.persistence.Entity

@Entity
class Person {
    String name
    static constraints = {
        name blank:false
    }
}
