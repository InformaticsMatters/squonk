package org.squonk.db.rdkit.dsl;

import java.util.List;

/**
 * Created by timbo on 14/12/2015.
 */
public interface IProjectionPart {
    int appendToProjections(StringBuilder builder, List bindVars);

    String getProjectionName();
}
