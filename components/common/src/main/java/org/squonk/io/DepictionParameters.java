package org.squonk.io;


import org.squonk.util.Colors;
import org.squonk.util.Utils;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parameters for molecule depiction.
 * Note that exact interpretation is likely to be implementation dependent
 * e.g. with CDK the size parameters for raster formats like png are interpreted as pixels but
 * vector formats like svg are interpreted as millimetres.
 * <p>
 * <p>
 * Created by timbo on 17/01/2016.
 */
public class DepictionParameters implements Serializable {

    public static final String PROP_WIDTH = "w";
    public static final String PROP_HEIGHT = "h";
    public static final String PROP_BG_COL = "bg";
    public static final String PROP_EXPAND_TO_FIT = "expand";
    public static final String PROP_MARGIN = "margin";
    public static final String PROP_IMG_FORMAT = "imgFormat";
    public static final String PROP_MOL_FORMAT = "molFormat";
    public static final String PROP_MOL = "mol";

    public static final String IMG_FORMAT_SVG = "svg";
    public static final String IMG_FORMAT_PNG = "png";


    private static final Logger LOG = Logger.getLogger(DepictionParameters.class.getName());

    private static final Color DEFAULT_BACKGROUND = new Color(255, 255, 255, 0);

    public enum OutputFormat {
        svg, png
    }

    public enum HighlightMode {
        direct, region
    }

    public enum ColorScheme {
        toolkit_default, black, white, cpk
    }

    private Integer width;
    private Integer height;
    private double margin = 0;
    private ColorScheme colorScheme;
    private Color backgroundColor;
    private Boolean expandToFit;
    private final List<Highlight> highlights = new ArrayList<>();

    /** With defaults for all key values
     *
     */
    public DepictionParameters() {
        this(200, 150, true, DEFAULT_BACKGROUND);
    }

    public DepictionParameters(Integer width, Integer height, Boolean expandToFit, Color backgroundColor, ColorScheme colorScheme) {
        this.width = width;
        this.height = height;
        this.expandToFit = expandToFit;
        this.backgroundColor = backgroundColor;
        this.colorScheme = colorScheme;
    }

    public DepictionParameters(Integer width, Integer height, Boolean expandToFit, Color backgroundColor) {
        this(width, height, expandToFit, backgroundColor, ColorScheme.toolkit_default);
    }

    public DepictionParameters(Integer width, Integer height) {
        this(width, height, true, DEFAULT_BACKGROUND);
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Boolean isExpandToFit() {
        return expandToFit;
    }

    public void setExpandToFit(Boolean expandToFit) {
        this.expandToFit = expandToFit;
    }

    public ColorScheme getColorScheme() {
        return colorScheme;
    }

    public void setColorScheme(ColorScheme colorScheme) {
        this.colorScheme = colorScheme;
    }

    public List<Highlight> getHighlights() {
        return highlights;
    }

    public double getMargin() {
        return margin;
    }

    public void setMargin(double margin) {
        this.margin = margin;
    }

    /**
     * Highlight the specified atoms
     *
     * @param atomIndexes
     * @param color
     * @param mode
     * @param highlightBonds Whether to highlight the connecting bonds
     * @return
     */
    public DepictionParameters addAtomHighlight(int[] atomIndexes, Color color, HighlightMode mode, boolean highlightBonds) {
        highlights.add(new AtomHighlight(atomIndexes, color, mode, highlightBonds));
        return this;
    }

    public QueryParams asQueryParams() {
        QueryParams params = new QueryParams();
        if (width != null) {
            params.add(PROP_WIDTH, width.toString());
        }
        if (height != null) {
            params.add(PROP_HEIGHT, height.toString());
        }
        if (backgroundColor != null) {
            params.add(PROP_BG_COL, Colors.rgbaColorToHex(backgroundColor));
        }
        if (expandToFit != null) {
            params.add(PROP_EXPAND_TO_FIT, expandToFit ? "1" : "0");
        }
        if (margin > 0) {
            params.add(PROP_MARGIN, "" + margin);
        }
        highlights.stream().forEachOrdered((h) -> h.append(params));
        return params;
    }

    private static String getHttpParameter(String name, Map<String, String[]> params) {
        String[] values = params.get(name);
        if (values == null) {
            return null;
        }
        if (values.length == 1) {
            return values[0];
        } else {
            LOG.log(Level.WARNING, "Invalid number of params for " + name + ". Expected 1, found " + values.length);
            return null;
        }
    }

    public static DepictionParameters fromHttpParams(Map<String, String[]> params) {
        String paramWidth = getHttpParameter(PROP_WIDTH, params);
        String paramHeight = getHttpParameter(PROP_HEIGHT, params);
        String paramExpand = getHttpParameter(PROP_EXPAND_TO_FIT, params);
        String paramBg = getHttpParameter(PROP_BG_COL, params);
        String paramColorScheme = getHttpParameter("colorScheme", params);
        String paramMargin = getHttpParameter(PROP_MARGIN, params);

        // size
        Integer width = null;
        Integer height = null;
        if (paramWidth != null && paramHeight != null) {
            try {
                width = Integer.parseInt(paramWidth);
                height = Integer.parseInt(paramHeight);

            } catch (NumberFormatException ex) {
                LOG.log(Level.INFO, "Can't interpret expand parameters: " + paramWidth + " " + paramHeight, ex);
            }
        }

        // margin
        Double margin = null;
        if (paramMargin != null) {
            try {
                margin = new Double(paramMargin);
            } catch (NumberFormatException ex) {
                LOG.log(Level.INFO, "Can't interpret expand parameters: " + paramWidth + " " + paramHeight, ex);
            }
        }

        // background
        Color col = null;
        if (paramBg != null) {
            try {
                col = Colors.rgbaHexToColor(paramBg);
            } catch (NumberFormatException ex) {
                LOG.log(Level.INFO, "Can't interpret color parameters: " + paramBg, ex);
            }
        }

        // expand to fit
        Boolean expand = true;
        if (paramExpand != null) {
            try {
                expand = Boolean.parseBoolean(paramExpand);
            } catch (Exception ex) {
                LOG.log(Level.INFO, "Can't interpret expand parameter: " + paramExpand, ex);
            }
        }

        // colorScheme
        ColorScheme colorScheme = ColorScheme.toolkit_default;
        if (paramColorScheme != null) {
            try {
                colorScheme = ColorScheme.valueOf(paramColorScheme);
            } catch (Exception e) {
                LOG.warning("Bad ColorSchem enum value: " + paramColorScheme);
            }
        }

        DepictionParameters dps = new DepictionParameters(width, height, expand, col, colorScheme);

        // margin
        if (margin != null) {
            dps.setMargin(margin);
        }

        // highlights
        for (Map.Entry<String,String[]> e : params.entrySet()) {
            String k = e.getKey();
            DepictionParameters.HighlightMode mode = null;
            boolean fragment = false;
            if (k.startsWith("atom_highlight_")) {
                if (k.startsWith("atom_highlight_region_")) {
                    mode = DepictionParameters.HighlightMode.region;
                } else if (k.startsWith("atom_highlight_direct_")) {
                    mode = DepictionParameters.HighlightMode.direct;
                }
            } else if (k.startsWith("frag_highlight_")) {
                fragment = true;
                if (k.startsWith("frag_highlight_region_")) {
                    mode = DepictionParameters.HighlightMode.region;
                } else if (k.startsWith("frag_highlight_direct_")) {
                    mode = DepictionParameters.HighlightMode.direct;
                }
            }
            if (mode != null) {
                String hex = k.substring(22);
                Color c = new Color(Long.decode(hex).intValue(), true);
                String[] values = e.getValue();
                for (String value : values) {
                    int[] atomIndexes = Utils.stringToIntArray(value);
                    dps.addAtomHighlight(atomIndexes, c, mode, fragment);
                }
            }
        }

        return dps;
    }


    public interface Highlight {
        void append(QueryParams queryParams);
    }

    public class AtomHighlight implements Highlight {

        private final int[] atomIndexes;
        private final Color color;
        private final HighlightMode mode;
        private final boolean highlightBonds;

        AtomHighlight(int[] atomIndexes, Color color, HighlightMode mode, boolean highlightBonds) {
            this.atomIndexes = atomIndexes;
            this.color = color;
            this.mode = mode;
            this.highlightBonds = highlightBonds;
        }

        public int[] getAtomIndexes() {
            return atomIndexes;
        }

        public Color getColor() {
            return color;
        }

        public HighlightMode getMode() {
            return mode;
        }

        public boolean isHighlightBonds() {
            return highlightBonds;
        }

        @Override
        public void append(QueryParams queryParams) {
            queryParams.add(createKey(), createValue());
        }

        private String createKey() {
            if (highlightBonds) {
                return "frag_highlight_" + mode + "_" + Colors.rgbaColorToHex(color);
            } else {
                return "atom_highlight_" + mode + "_" + Colors.rgbaColorToHex(color);
            }
        }

        private String createValue() {
            return Utils.intArrayToString(atomIndexes, ",");
        }

        @Override
        public String toString() {
            return createKey() + "=" + createValue();
        }
    }

}
