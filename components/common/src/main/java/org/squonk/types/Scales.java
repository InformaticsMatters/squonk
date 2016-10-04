package org.squonk.types;

import org.squonk.io.DepictionParameters;
import org.squonk.util.Colors;

import java.awt.*;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by timbo on 03/10/16.
 */
public class Scales {

    public static final Scales DEFAULT = new Scales();

    public static final Scale SMARTCyp = new Scale("SMARTCyp", Colors.BROWN, Colors.STEELBLUE, 25f, 100f, DepictionParameters.HighlightMode.region, false);

    private final Map<String,Scale> scales = new TreeMap(String.CASE_INSENSITIVE_ORDER);

    public Scales() {
        scales.put("SMARTCyp", SMARTCyp);
    }

    public Map<String, Scale> getScales() {
        return Collections.unmodifiableMap(scales);
    }

    public void register(
            String key,
            Color fromColor, Color toColor,
            float fromValue, float toValue,
            DepictionParameters.HighlightMode mode, boolean highlightBonds) {
        scales.put(key, new Scale(key, fromColor, toColor, fromValue, toValue, mode,  highlightBonds));
    }
}
