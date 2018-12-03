package org.squonk.cdk;

import org.squonk.util.IOUtils;

public interface CDKSettings {

    String WriteAromaticBondTypes = IOUtils.getConfiguration("CDK_WriteAromaticBondTypes", "true");
}
