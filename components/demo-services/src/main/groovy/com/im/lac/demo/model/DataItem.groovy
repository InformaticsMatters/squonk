package com.im.lac.demo.model

import java.sql.Timestamp
import com.im.lac.types.io.Metadata
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
    Metadata metadata
    Timestamp created
    Timestamp updated
    Long loid
}

