package org.squonk.notebook.api2;

import com.im.lac.job.jobdef.JobStatus;

public interface CellExecutor {

    JobStatus execute(CellExecutionData data) throws Exception;
}
