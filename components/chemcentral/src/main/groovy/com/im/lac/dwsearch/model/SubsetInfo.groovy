package com.im.lac.dwsearch.model

import groovy.transform.Canonical



@Canonical
class SubsetInfo {
    
    enum Status {
        OK, Pending, Error
    }

    Object id
    String name
    Date created
    String owner
    String resource
    Status status
    int size
    List items
}
