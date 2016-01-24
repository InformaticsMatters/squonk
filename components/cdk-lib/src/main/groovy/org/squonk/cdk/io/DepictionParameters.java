package org.squonk.cdk.io;


import java.awt.Color;

/** Parameters for molecule depiction.
 * Note that exact interpretation is likely to be implementation dependent
 * e.g. with CDK the size parameters for raster formats like png are interpreted as pixels but
 * vector formats like svg are interpreted as millimetres.
 *
 *
 * Created by timbo on 17/01/2016.
 */
public class DepictionParameters {


    public enum OututFormat {
        svg, png
    }

    private final Integer width;
    private final Integer height;
    private final Color backgroundColor;
    private final boolean expandToFit;

    public DepictionParameters(
            Integer width,
            Integer height,
            boolean expandToFit,
            Color backgroundColor
    ) {
        this.width = width;
        this.height = height;
        this.expandToFit = expandToFit;
        this.backgroundColor = backgroundColor;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public boolean isExpandToFit() {
        return expandToFit;
    }

}
