#!/usr/bin/env bash

# Some Job API usage examples
# (automated/simplified with the use of 'jq')
#
# You must enable the unauthenticated user and prevent sending status updates
# by defining tomcat with these environment variables:
# `SQUONK_JOBEXECUTOR_ALLOW_UNAUTHENTICATED=true`
# `SQUONK_JOBEXECUTOR_SEND_STATUS_UPDATES=false`
#
# You can start the server with the tomcatRunWar task from the job-executor
# directory. That task blocks.

# Run a couple of jobs...

curl -X POST \
  -F "ExecutionParameters=@ExecutionParametersSdf.json;type=application/json;filename=ExecutionParameters.json"\
  -F "input=@../../../../data/testfiles/Kinase_inhibs.sdf.gz;type=chemical/x-mdl-sdfile;filename=input"\
  -H "Content-Type: multipart/mixed"\
  -H "SquonkUsername: user1"\
  http://localhost:8888/jobexecutor/rest/v1/jobs/submit-async

curl -X POST \
  -F "ExecutionParameters=@ExecutionParametersDataset.json;type=application/json;filename=ExecutionParameters.json"\
  -F "input_data=@../../../../data/testfiles/Kinase_inhibs.json.gz;type=application/x-squonk-molecule-object+json;filename=input_data"\
  -F "input_metadata=@../../../../data/testfiles/Kinase_inhibs.metadata;type=application/x-squonk-dataset-metadata+json;filename=input_metadata"\
  -H "Content-Type: multipart/mixed"\
  -H "SquonkUsername: user1"\
  http://localhost:8888/jobexecutor/rest/v1/jobs/submit-async


curl -X POST \
  -F "ExecutionParameters=@ExecutionParametersClusterButina.json;type=application/json;filename=ExecutionParameters.json"\
  -F "input_data=@../../../../data/testfiles/Kinase_inhibs.json.gz;type=application/x-squonk-molecule-object+json;filename=input_data"\
  -F "input_metadata=@../../../../data/testfiles/Kinase_inhibs.metadata;type=application/x-squonk-dataset-metadata+json;filename=input_metadata"\
  -H "Content-Type: multipart/mixed"\
  -H "SquonkUsername: user1"\
  http://localhost:8888/jobexecutor/rest/v1/jobs/submit-async


sleep 2

# Inspect the job queue...
curl -H "SquonkUsername: user1" http://localhost:8888/jobexecutor/rest/v1/jobs 2> /dev/null | jq

# Get job status strings...
JOB_1_STATUS=$(curl -H "SquonkUsername: user1" http://localhost:8888/jobexecutor/rest/v1/jobs | jq -r .[0].status)
echo $JOB_1_STATUS
JOB_2_STATUS=$(curl -H "SquonkUsername: user1" http://localhost:8888/jobexecutor/rest/v1/jobs | jq -r .[1].status)
echo $JOB_2_STATUS

# Get the status of specific job...
JOB_1_ID=$(curl -H "SquonkUsername: user1" http://localhost:8888/jobexecutor/rest/v1/jobs | jq -r .[0].jobId)
curl -H "SquonkUsername: user1" http://localhost:8888/jobexecutor/rest/v1/jobs/$JOB_1_ID/status | jq -r .status

sleep 2

# Delete both jobs...
JOB_1_ID=$(curl -H "SquonkUsername: user1" http://localhost:8888/jobexecutor/rest/v1/jobs | jq -r .[0].jobId)
JOB_2_ID=$(curl -H "SquonkUsername: user1" http://localhost:8888/jobexecutor/rest/v1/jobs | jq -r .[1].jobId)
curl -H "SquonkUsername: user1" http://localhost:8888/jobexecutor/rest/v1/jobs/$JOB_1_ID/terminate | jq -r .status
curl -H "SquonkUsername: user1" http://localhost:8888/jobexecutor/rest/v1/jobs/$JOB_2_ID/terminate | jq -r .status

# Run a nextflow job (without a user)
# wand wait until READY

NF_JOB_ID=$(curl -X POST \
  -F "ExecutionParameters=@NextflowExecutionParametersDataset.json;type=application/json;filename=NextflowExecutionParametersDataset.json"\
  -F "input_data=@../../../../data/testfiles/Kinase_inhibs.json.gz;type=application/x-squonk-molecule-object+json;filename=input_data"\
  -H "Content-Type: multipart/mixed"\
  http://localhost:8888/jobexecutor/rest/v1/jobs/submit-async 2> /dev/null | jq -r .jobId)

NF_JOB_STATUS=$(curl http://localhost:8888/jobexecutor/rest/v1/jobs/$NF_JOB_ID/status 2> /dev/null | jq -r .status)
while [ $NF_JOB_STATUS != "RESULTS_READY" ]; do
    echo "Waiting for Ready..."
    sleep 2
    NF_JOB_STATUS=$(curl http://localhost:8888/jobexecutor/rest/v1/jobs/$NF_JOB_ID/status 2> /dev/null | jq -r .status)
done
echo $NF_JOB_STATUS

# Delete the (userless) job...
curl http://localhost:8888/jobexecutor/rest/v1/jobs/$NF_JOB_ID/terminate | jq -r .status

# Delete all jobs...
JOB_ID=$(curl -H "SquonkUsername: user1" http://localhost:8888/jobexecutor/rest/v1/jobs | jq -r .[0].jobId)
while [ $JOB_ID != "null" ]; do
    echo "Deleting job $JOB_ID..."
    curl http://localhost:8888/jobexecutor/rest/v1/jobs/$JOB_ID/terminate 2> /dev/null | jq -r .status
    sleep 2
    JOB_ID=$(curl -H "SquonkUsername: user1" http://localhost:8888/jobexecutor/rest/v1/jobs 2> /dev/null | jq -r .[0].jobId)
done
echo "No more jobs."

# Finally, display the job queue...
curl http://localhost:8888/jobexecutor/rest/v1/jobs 2> /dev/null | jq
