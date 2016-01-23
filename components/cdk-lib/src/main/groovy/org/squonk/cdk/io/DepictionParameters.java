package org.squonk.cdk.io;

import java.awt.*;

/**
 * Created by timbo on 17/01/2016.
 */
public class DepictionParameters {
    private final Dimension size;
    private final Color backgroundColor;
    private final boolean expandToFit;

    public DepictionParameters(
            Dimension size,
            boolean expandToFit,
            Color backgroundColor
    ) {
        this.size = size;
        this.expandToFit = expandToFit;
        this.backgroundColor = backgroundColor;
    }

    public Dimension getSize() {
        return size;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public boolean isExpandToFit() {
        return expandToFit;
    }
}
