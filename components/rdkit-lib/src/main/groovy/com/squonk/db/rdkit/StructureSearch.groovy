package com.squonk.db.rdkit

import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 * Created by timbo on 30/11/2015.
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
class StructureSearch {

    int limit = -1
}
