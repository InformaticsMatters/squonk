package org.squonk.execution.runners;

import org.apache.camel.CamelContext;
import org.squonk.execution.steps.impl.AbstractDatasetStandardStep;
import org.squonk.io.FileDataSource;
import org.squonk.io.SquonkDataSource;
import org.squonk.types.StreamType;
import org.squonk.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class DefaultServiceRunner implements ServiceRunner {

    private static final Logger LOG = Logger.getLogger(DefaultServiceRunner.class.getName());

    private final String jobId;
    private final AbstractDatasetStandardStep step;
    private final CamelContext camelContext;

    private File workDir;
    private List<FileDataSource> results;
    private boolean resultsReady = false;

    public DefaultServiceRunner(String jobId, AbstractDatasetStandardStep step, CamelContext camelContext) {
        this.jobId = jobId;
        this.step = step;
        this.camelContext = camelContext;
    }

    public void execute(Map<String, Object> data, Map<String, Object> options) throws Exception {
        if (workDir != null) {
            throw new IllegalStateException("Already executing");
        }
        createWorkDir();
        // execute the step
        Map<String,Object> outputs = step.doExecute(data, options, camelContext == null ? null : camelContext.getTypeConverter());
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
