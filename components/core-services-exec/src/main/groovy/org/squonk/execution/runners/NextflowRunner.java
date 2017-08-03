package org.squonk.execution.runners;

import nextflow.cli.CmdRun;
import nextflow.cli.Launcher;
import org.squonk.util.IOUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by timbo on 01/08/17.
 */
public class NextflowRunner extends AbstractRunner {

    private static final Logger LOG = Logger.getLogger(NextflowRunner.class.getName());


    public NextflowRunner() {
        this(null);
    }

    public NextflowRunner(String hostBaseWorkDir) {
        super(hostBaseWorkDir);
    }

    protected String getDefaultWorkDir() {
        return IOUtils.getConfiguration("SQUONK_NEXTFLOW_WORK_DIR", "/tmp/work/squonk_test/nextflow");
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
