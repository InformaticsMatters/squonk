package org.squonk.types;

import java.io.IOException;
import java.io.InputStream;

/**
 * TODO - make Dataset, SDFile and CSVFile implement this.
 *
 * Created by timbo on 30/01/17.
 */
public interface SquonkDataType {


    String getMediaType();

    InputStream getInputStream(boolean gzip) throws IOException;
}
