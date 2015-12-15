package com.squonk.db

import groovy.transform.Canonical

/**
 * Created by timbo on 08/12/2015.
 */
@Canonical
class DbColumn {

    final DbTable table
    final String name
    final String definition
    final String defaultValue
    final boolean notNull

    DbColumn(DbTable table, String name, String definition) {
        this.table = table
        this.name = name
        this.definition = definition
    }


}
