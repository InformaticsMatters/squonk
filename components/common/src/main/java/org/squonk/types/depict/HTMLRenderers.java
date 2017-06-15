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

package org.squonk.types.depict;

import org.squonk.types.Scale;
import org.squonk.util.Colors;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by timbo on 05/10/16.
 */
public class HTMLRenderers {

    private static HTMLRenderers INSTANCE = new HTMLRenderers();

    private final Map<Class,HTMLRenderer<? extends Object>> renderers = new HashMap<>();

    private HTMLRenderers() {
        renderers.put(Scale.class, new ScaleHTMLRenderer());
    }

    public static HTMLRenderers getInstance() {
        return INSTANCE;
    }

    public boolean canRender(Class cls) {
        return renderers.containsKey(cls);
    }

    public String render(Object o) {
        HTMLRenderer renderer = renderers.get(o.getClass());
        return renderer == null ? null : renderer.renderAsHTML(o);
    }

    class ScaleHTMLRenderer implements HTMLRenderer<Scale> {

        private static final String HTML = "<div>\n" +
                "<div style=\"float:left;padding:3px;\">%s:</div>\n" +
                "<div style=\"float:left;text-align: center;width:50px;background:%s;border-style:solid;border-width:1px;padding:2px;\">%s</div>\n" +
                "<div style=\"float:left;padding:3px;\">-</div>\n" +
                "<div style=\"float:left;text-align: center;width:50px;background:%s;border-style:solid;border-width:1px;padding:2px;\">%s</div>\n" +
                "</div>";

        @Override
        public String renderAsHTML(Scale o) {
            return String.format(HTML, o.getName(), Colors.rgbColorToHex(o.getFromColor()), o.getFromValue(),
                    Colors.rgbColorToHex(o.getToColor()), o.getToValue());
        }
    }

}
