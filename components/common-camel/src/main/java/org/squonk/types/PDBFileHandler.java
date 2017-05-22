package org.squonk.types;

import org.squonk.api.HttpHandler;
import org.squonk.api.VariableHandler;
import org.squonk.http.RequestResponseExecutor;
import org.squonk.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by timbo on 23/03/2016.
 */
public class PDBFileHandler implements HttpHandler<PDBFile>, VariableHandler<PDBFile> {

    @Override
    public Class<PDBFile> getType() {
        return PDBFile.class;
    }

    @Override
    public void prepareRequest(PDBFile pdb, RequestResponseExecutor executor, boolean gzip) throws IOException {
        if (pdb != null) {
            executor.prepareRequestBody(gzip ? IOUtils.getGzippedInputStream(pdb.getInputStream()) : pdb.getInputStream());
        }
    }

    @Override
    public void writeResponse(PDBFile pdb, RequestResponseExecutor executor, boolean gzip) throws IOException {
        if (pdb == null) {
            executor.setResponseBody(null);
        } else {
            executor.setResponseBody(gzip ? IOUtils.getGzippedInputStream(pdb.getInputStream()) : pdb.getInputStream());
        }
    }

    @Override
    public PDBFile readResponse(RequestResponseExecutor executor, boolean gunzip) throws IOException {
        InputStream is = executor.getResponseBody();
        if (is != null) {
            return new PDBFile(gunzip ? IOUtils.getGunzippedInputStream(is) : is);
        }
        return null;
    }

    @Override
    public void writeVariable(PDBFile pdb, WriteContext context) throws Exception {;
        context.writeSingleStreamValue(pdb.getInputStream(), "pdb.gz");
    }

    @Override
    public PDBFile readVariable(ReadContext context) throws Exception {
        InputStream is = context.readSingleStreamValue("pdb.gz");
        return new PDBFile(is);
    }
}
