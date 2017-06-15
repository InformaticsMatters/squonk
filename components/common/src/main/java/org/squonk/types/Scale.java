/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
