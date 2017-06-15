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
