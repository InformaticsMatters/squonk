package org.squonk.execution.runners;

import org.apache.camel.CamelContext;
import org.squonk.core.HttpServiceDescriptor;
import org.squonk.io.FileDataSource;
import org.squonk.io.SquonkDataSource;
import org.squonk.types.StreamType;
import org.squonk.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

;

public class HttpServiceRunner implements ServiceRunner {

    private static final Logger LOG = Logger.getLogger(HttpServiceRunner.class.getName());

    private final String jobId;
    private final HttpServiceDescriptor serviceDescriptor;
    private final CamelContext camelContext;

    private File workDir;
    private List<FileDataSource> results;
    private boolean resultsReady = false;

    public HttpServiceRunner(String jobId, HttpServiceDescriptor serviceDescriptor, CamelContext camelContext) {
        this.jobId = jobId;
        this.serviceDescriptor = serviceDescriptor;
        this.camelContext = camelContext;
    }

    public void execute(Map<String, Object> data, Map<String, Object> options) throws Exception {

        String execClsName = serviceDescriptor.getServiceConfig().getExecutorClassName();
        LOG.info("Executor class name is " + execClsName);

        if (workDir != null) {
            throw new IllegalStateException("Already executing");
        }
        createWorkDir();
        // execute the step
        Map<String,Object> outputs = null; //step.doExecuteWithDataset(data, options, camelContext == null ? null : camelContext.getTypeConverter());
        results = new ArrayList<>();
        for (Map.Entry<String,Object> e : outputs.entrySet()) {
            String name = e.getKey();
            Object value = e.getValue();
            LOG.fine("Found value " + value + " for variable " + name);
            if (value != null) {
                if (value instanceof StreamType) {
                    StreamType streamType = (StreamType) value;
                    SquonkDataSource[] dataSources = streamType.getDataSources();
                    for (SquonkDataSource dataSource : dataSources) {
                        FileDataSource fds = dataSource.writeTo(workDir, name);
                        results.add(fds);
                    }
                }
            }
        }
        resultsReady = true;
    }

    public boolean isResultsReady() {
        return resultsReady;
    }

    private void createWorkDir() throws IOException {
        String workDirStr = AbstractRunner.getDefaultWorkDir();
        File base = new File(workDirStr);
        workDir = new File(base, jobId);
        if (!workDir.mkdir()) {
            throw new IOException("Could not create work dir " + workDir.getPath());
        }

    }

    public List<FileDataSource> getResults() {
        if (isResultsReady()) {
            return results;
        } else {
            throw new IllegalStateException("Results not ready");
        }
    }

    public void cleanup() {
        if (workDir != null) {
            IOUtils.deleteDirRecursive(workDir);
        }
    }

    public File getHostWorkDir() {
        return workDir;
    }

}
