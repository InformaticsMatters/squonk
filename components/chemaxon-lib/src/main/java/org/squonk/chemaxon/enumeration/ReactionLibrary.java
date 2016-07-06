package org.squonk.chemaxon.enumeration;

import org.squonk.util.IOUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by timbo on 05/07/16.
 */
public class ReactionLibrary {

    private static final String PREFIX = "chemaxon_reaction_library/";
    private static final String EXTENSION = ".mrv";

    private final ZipFile zipFile;

    public ReactionLibrary(String path) throws IOException {
        this.zipFile = new ZipFile(path);
    }

    public List<String> getReactionNames() {
        Stream<? extends ZipEntry> entries = zipFile.stream();
        List<String> names = entries.map((e) -> e.getName())
                .filter((s) -> s.endsWith(EXTENSION) && s.startsWith(PREFIX))
                .map((s) -> s.substring(26, s.length() - 4))
                .collect(Collectors.toList());
        return names;
    }

    public String getReaction(String name) throws IOException {
        ZipEntry entry = zipFile.getEntry(PREFIX + name + EXTENSION);
        return entry == null ? null : IOUtils.convertStreamToString(zipFile.getInputStream(entry));
    }
}
