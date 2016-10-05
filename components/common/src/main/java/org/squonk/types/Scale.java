package org.squonk.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.io.DepictionParameters;
import org.squonk.util.Colors;

import java.awt.Color;
import java.io.Serializable;

/**
 * Created by timbo on 03/10/16.
 */
public class Scale implements Serializable {

    private final String name;
    private final Color fromColor, toColor;
    private final float fromValue, toValue;
    private final DepictionParameters.HighlightMode highlightMode;
    private final boolean highlightBonds;

    public Scale(
            @JsonProperty("name") String name,
            @JsonProperty("fromColor") Color fromColor,
            @JsonProperty("toColor") Color toColor,
            @JsonProperty("fromValue") float fromValue,
            @JsonProperty("toValue") float toValue,
            @JsonProperty("highlightMode") DepictionParameters.HighlightMode highlightMode,
            @JsonProperty("highlightBonds") boolean highlightBonds) {
        this.name = name;
        this.fromColor = fromColor;
        this.toColor = toColor;
        this.fromValue = fromValue;
        this.toValue = toValue;
        this.highlightMode = highlightMode;
        this.highlightBonds = highlightBonds;
    }

    public String getName() {
        return name;
    }

    public Color getFromColor() {
        return fromColor;
    }

    public Color getToColor() {
        return toColor;
    }

    public float getFromValue() {
        return fromValue;
    }

    public float getToValue() {
        return toValue;
    }

    public DepictionParameters.HighlightMode getHighlightMode() {
        return highlightMode;
    }

    public boolean isHighlightBonds() {
        return highlightBonds;
    }

    @Override
    public String toString() {
        return name + ": " + fromValue + " -> " + Colors.rgbaColorToHex(fromColor)
                + ", " + toValue + " -> " + Colors.rgbaColorToHex(toColor);
    }

}
