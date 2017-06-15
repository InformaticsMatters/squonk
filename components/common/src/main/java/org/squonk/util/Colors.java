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

package org.squonk.util;

import org.squonk.io.DepictionParameters;
import org.squonk.types.AtomPropertySet;

import java.awt.Color;
import java.util.logging.Logger;

/**
 * Created by timbo on 30/09/2016.
 */
public class Colors {

    private static final Logger LOG = Logger.getLogger(Colors.class.getName());

    public static final Color STEELBLUE = Color.decode("#4682B4");
    public static final Color BROWN = Color.decode("#A52A2A");

    public static String rgbaColorToHex(Color color) {
        return String.format("#%02x%02x%02x%02x",
                color.getAlpha(),
                color.getRed(),
                color.getGreen(),
                color.getBlue());
    }

    public static String rgbColorToHex(Color color) {
        return String.format("#%02x%02x%02x",
                color.getRed(),
                color.getGreen(),
                color.getBlue());
    }

    public static Color rgbaHexToColor(String hex) {
        return new Color(Long.decode(hex).intValue(), true);
    }

    public static Color interpolateRGBLinear(Color start, Color end, float value) {
        int r = interpolateRGBLinear(start.getRed(), end.getRed(), value);
        int g = interpolateRGBLinear(start.getGreen(), end.getGreen(), value);
        int b = interpolateRGBLinear(start.getBlue(), end.getBlue(), value);
        return new Color(r, g, b);
    }

    public static int interpolateRGBLinear(int start, int end, float value) {
        float val = start + ((end - start) * value);
        int i = (int) val;
        if (i < 0) {
            return 0;
        } else if (i > 255) {
            return 255;
        } else {
            return i;
        }
    }


}
