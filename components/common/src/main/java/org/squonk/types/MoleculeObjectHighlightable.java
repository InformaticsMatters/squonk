package org.squonk.types;

import org.squonk.io.DepictionParameters;

import java.awt.*;

/**
 * Created by timbo on 03/10/16.
 */
public interface MoleculeObjectHighlightable {

    void highlight(DepictionParameters dp,
            Color startColor, Color endColor,
            float startValue, float endValue,
            DepictionParameters.HighlightMode mode, boolean highlightBonds);
}
