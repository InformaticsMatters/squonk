package org.squonk.io;


import java.awt.*;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/** Parameters for molecule depiction.
 * Note that exact interpretation is likely to be implementation dependent
 * e.g. with CDK the size parameters for raster formats like png are interpreted as pixels but
 * vector formats like svg are interpreted as millimetres.
 *
 *
 * Created by timbo on 17/01/2016.
 */
public class DepictionParameters implements Serializable {


    public enum OutputFormat {
        svg, png
    }

    private final Integer width;
    private final Integer height;
    private final Color backgroundColor;
    private final Boolean expandToFit;

    public DepictionParameters(Integer width, Integer height, Boolean expandToFit, Color backgroundColor) {
        this.width = width;
        this.height = height;
        this.expandToFit = expandToFit;
        this.backgroundColor = backgroundColor;
    }

    public DepictionParameters(Integer width, Integer height) {
        this(width, height, true, new Color(255, 255, 255, 0));
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

    public Boolean isExpandToFit() {
        return expandToFit;
    }

    public Map<String,String> asQueryParams() {
        Map<String,String> map = new LinkedHashMap<>();
        if (width != null) {
            map.put("w", width.toString());
        }
        if (height != null) {
            map.put("h", height.toString());
        }
        if (backgroundColor != null) {
            String hex = String.format("#%02x%02x%02x%02x",
                    backgroundColor.getAlpha(),
                    backgroundColor.getRed(),
                    backgroundColor.getGreen(),
                    backgroundColor.getBlue());
            map.put("bg", hex);
        }
        if (expandToFit != null) {
            map.put("expand", expandToFit ? "1" : "0");
        }

        return map;
    }

}
