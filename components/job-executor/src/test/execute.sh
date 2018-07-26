#!/usr/bin/env bash

curl -X POST \
  -F "ExecutionParameters=@ExecutionParametersSdf.json;type=application/json;filename=ExecutionParameters.json"\
  -F "input=@../../../../data/testfiles/Kinase_inhibs.sdf.gz;type=chemical/x-mdl-sdfile;filename=input"\
  -H "Content-Type: multipart/mixed"\
  -H "SquonkUsername: user1"\
  http://localhost:8080/job-executor/rest/v1/jobs


curl -X POST \
  -F "ExecutionParameters=@ExecutionParametersDataset.json;type=application/json;filename=ExecutionParameters.json"\
  -F "input_data=@../../../../data/testfiles/Kinase_inhibs.json.gz;type=application/x-squonk-molecule-object+json;filename=input_data"\
  -F "input_metadata=@../../../../data/testfiles/Kinase_inhibs.metadata;type=application/x-squonk-dataset-metadata+json;filename=input_metadata"\
  -H "Content-Type: multipart/mixed"\
  -H "SquonkUsername: user1"\
  http://localhost:8080/job-executor/rest/v1/jobs


curl -H "SquonkUsername: user1" http://localhost:8080/job-executor/rest/v1/jobs