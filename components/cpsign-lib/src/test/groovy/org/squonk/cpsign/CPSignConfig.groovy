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

package org.squonk.cpsign

import org.squonk.util.IOUtils

/**
 * Created by timbo on 21/10/2016.
 */
class CPSignConfig {

    static final File workDir = new File(IOUtils.getConfiguration("CPSIGN_MODEL_DIR", null))
    static final File license = new File(IOUtils.getConfiguration("CPSIGN_LICENSE_URL", null))
}
