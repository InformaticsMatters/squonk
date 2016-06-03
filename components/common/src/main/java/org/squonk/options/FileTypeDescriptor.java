package org.squonk.options;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.options.types.Structure;

import java.io.File;

/** Type descriptor for a File that has to be uploaded
 * Created by timbo on 03/02/16.
 */
public class FileTypeDescriptor<T> extends SimpleTypeDescriptor<File> {

    private final String[] fileTypes;

    public FileTypeDescriptor(@JsonProperty("fileTypes") String[] fileTypes) {
        super(File.class);
        this.fileTypes = fileTypes;
    }

    public  String[] getFileTypes() {
        return fileTypes;
    }


}
