package com.im.lac.files.api.webservice;

import java.io.InputStream;
import java.io.Serializable;

/**
 * @author simetrias
 */
public interface Files extends Serializable {

    void createTempFile(String tempFolderName, String name, InputStream inputStream);

}
