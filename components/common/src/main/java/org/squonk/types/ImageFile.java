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

import org.squonk.io.SquonkDataSource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/** Wrapper around an image file
 *
 * @author timbo
 */
public class ImageFile extends AbstractStreamType {


    public ImageFile(SquonkDataSource input) {
        super(input);
    }

    public ImageFile(InputStream input, String mediaType) {
        super(input, mediaType, false);
    }



    /** Generate and return a BufferedImage corresponding to this ImageFile.
     * NOTE: this consumes the InputStream so that it cannot be read again.
     * If you need access to this you can use one of the IOUtils.write() methods.
     *
     * @return
     * @throws IOException
     */
    public BufferedImage getBufferedImage() throws IOException {
        return ImageIO.read(getGunzipedInputStream());
    }
    
}
