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

package org.squonk.execution.variable.impl;

import java.io.File;

/**
 * Created by timbo on 01/01/17.
 */
public abstract class AbstractFilesystemContext {

    protected final File dir;
    protected final String baseName;

    protected AbstractFilesystemContext(File dir, String baseName) {
        this.dir = dir;
        this.baseName = baseName;
    }

    protected File generateFile(String key) {
        if (key == null) {
            return new File(dir, baseName);
        } else {
            return new File(dir, baseName + "." + key);
        }
    }

}
