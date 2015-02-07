package com.im.lac.demo.model

import java.sql.Timestamp
import groovy.transform.Canonical


/**
 *
 * @author timbo
 */
@Canonical
class DataItem {
    
    Long id
    String name
    Integer size
    Timestamp created
    Timestamp updated
    Long loid
}

