package org.squonk.execution.runners;

import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Volume;
import nextflow.cli.CmdRun;
import nextflow.cli.Launcher;
import org.squonk.util.IOUtils;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by timbo on 01/08/17.
 */
public class NextflowRunner extends AbstractRunner {

    private static final Logger LOG = Logger.getLogger(NextflowRunner.class.getName());


    public NextflowRunner() {
        this(null, null);
    }

    public NextflowRunner(String hostBaseWorkDir, String jobId) {
        super(hostBaseWorkDir, jobId);
    }

    protected String getDefaultWorkDir() {
        return IOUtils.getConfiguration("SQUONK_NEXTFLOW_WORK_DIR", "/squonk/work/nextflow");
    }

    @Override
    public int execute(String... cmd) {
        throw new UnsupportedOperationException("NextflowRunner does not support this execute");
    }

    @Override
    public String getLog() {
        throw new UnsupportedOperationException("NextflowRunner does not support getLog");
    }

    @Override
    public Volume addVolume(String mountAs) {
        throw new UnsupportedOperationException("NextflowRunner does not support addVolume");
    }

    @Override
    public Bind addBind(String hostDir, Volume volume, AccessMode mode) {
        throw new UnsupportedOperationException("NextflowRunner does not support addBind");
    }

    @Override
    public String getLocalWorkDir() {
        throw new UnsupportedOperationException("NextflowRunner does not support getLocalWorkDir");
    }


    public int execute(List<String> args, Map<String,String> params) {

        // create runner and launcher
        CmdRun run = new CmdRun();
        run.setLauncher(new Launcher());
        String workdir = getHostWorkDir().getAbsolutePath() + "/work";
        LOG.info("Using nextflow work dir of " + workdir);
        run.setWorkDir(workdir);

        // define args and params
        run.setArgs(args);
        run.setParams(params);

        // execute nextflow
        run.run();

        return 0;
    }


    public void stop() {
        // TODO - implement

    }
}
