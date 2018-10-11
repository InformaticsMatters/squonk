# External Job Execution

Provides a REST web service for executing Squonk services.
The job is initialised by POSTing a job description and the data to process in Multipart MIME format.
Simple examples can be found in this [example](src/test/execute.sh).

Then key class is org.squonk.services.camel.routes.JobExecutorRouteBuilder which itself uses classes from
the core-services-exec module.

This service is packaged up in the `squonk/jobexecutor` Docker image, and where Keycloak authentication is used the 
`squonk/jobexecutor-keycloak` image, and deployed to the running Squonk
environment.

## Dev mode

When running in the 'dev' mode in Docker the SQUONK_JOBEXECUTOR_ALLOW_UNAUTHENTICATED
environment variable is set to 'true' so no authentication is in place. You can access the API directly as the user
`nobody`. That user may not exist in the database, but in this unauthenticated mode you can specify a user to delegate to
with the `SquonkUsername` header.

### Using curl

This is a good approach for testing.

_Listing jobs_

```
curl -H "SquonkUsername: user1" http://172.20.0.2:8080/jobexecutor/rest/v1/jobs
```

_Posting a new Job_

From the `src/test` directory execute something like this:

```
curl -kL -X POST \
  -H "SquonkUsername: user1"\
  -F "ExecutionParameters=@ExecutionParametersSdf.json;type=application/json;filename=ExecutionParameters.json"\
  -F "input=@../../../../data/testfiles/Kinase_inhibs.sdf;type=chemical/x-mdl-sdfile;filename=input"\
  -H "Content-Type: multipart/mixed"\
  http://172.20.0.2:8080/jobexecutor/rest/v1/jobs/
```

The result is some JSON that includes the job ID, and hopefully says that the job status is `RUNNING`.
You can then monitor the job's status like this:

```
curl -H "SquonkUsername: user1" http://172.20.0.2:8080/jobexecutor/rest/v1/jobs/<job-id>/status
```

Substitute in your job ID. 
Once the status is `RESULTS_READY` you can retrieve the results using:

```
curl -H "SquonkUsername: user1" http://172.20.0.2:8080/jobexecutor/rest/v1/jobs/<job-id>/results
```

Once you have the results delete the job using:

```
curl -X DELETE -H "SquonkUsername: user1" http://172.20.0.2:8080/jobexecutor/rest/v1/jobs/<job-id>
```

## Authentication

When used in `basic` mode or in OpenShift authentication is required. You must obtain a token from Keycloak and include that in your 
requests. The token is short lived (5 mins by default). In this case it is the `squonk/jobexecutor-keycloak` docker image
that is deployed.

### Using curl

_Obtain a token_:

```token=$(curl --data "grant_type=password&client_id=squonk-portal&username=user1&password=user1"\
 https://hostname/auth/realms/squonk/protocol/openid-connect/token |\
  grep -Po '(?<="access_token":")[^"]*')
```
Adjust the username, password and hostname accordingly. In `basic` mode in the docker environment the hostname is likely `nginx`.
In an OpenShift environment it is the public hostname of the jobexecutor route e.g. `squonk-jobexecutor.openshift.example.org`.

_Inspect the token_:

`echo $token`

_List job_:

`curl -H "Authorization: bearer $token" http://hostname/jobexecutor/rest/v1/jobs`

The result will be the JobStatus objects for all jobs you have submitted. Most likely and empty array.

## Using a service account

When accessing from an application better than using individual user accounts you can use a service account.
We currently use the `squonk-portal` client in the Keycloak realm to authenticate against (this may change in future).
That client must be set up to support service accounts. In the Keycloak admin console go to the appropriate client 
(e.g. `squonk-portal`) in the appropriate realm (e.g. `squonk`) and:

1. Make sure the `Access type` to be `confidential`
3. Enable the `Service accounts` option
4. On the `Service Account Roles` tab add the appropriate roles (e.g. `standard-user`)
5. Make a record of the client secret from the `Credentials` tab

_Obtain a token_:

Use a POST request to get the token. With curl the command would be:

```
token=$(curl\
  -H 'Content-Type: application/x-www-form-urlencoded'\
  -d 'grant_type=client_credentials'\
  -d 'client_id=squonk-portal'\
  -d 'client_secret=<client-secret>'\
  https://<hostname>/auth/realms/squonk/protocol/openid-connect/token|\
  grep -Po '(?<="access_token":")[^"]*')
```
Substitute the appropriate values for `<client-id>`, `<client-secret>` and `<hostname>`

_Inspect the token_:

`echo $token`

_Access job executor_:

Access the application using the token.  With curl the command would be:

`curl -H "Authorization: bearer $token" http://172.20.0.1/jobexecutor/rest/v1/jobs`

When using a service account you can also specify a username that will be used instead of the service
account by adding a header like this: `-H "SquonkUsername: someuser"`.


### Executing a service

Examples can be found in this [example](src/test/execute.sh).

```
curl\ 
  -F "ExecutionParameters=@ExecutionParametersSdf.json;type=application/json;filename=ExecutionParameters.json" \
  -F "input=@../../../../data/testfiles/Kinase_inhibs.sdf;type=chemical/x-mdl-sdfile;filename=input"\
  -H "Content-Type: multipart/mixed"\
  -H "Authorization: bearer $token"\
  -H "SquonkUsername: user1" http://nginx/jobexecutor/rest/v1/jobs/
```

### Working behind reverse proxy servers.

Then the services is behind a reverse proxy server (e.g. nginx on the Dockerised setup or a route in OpenShift)
your initial request will get a 301 or 302 response indicting a redirect. You need to ensure that your HTTP client handles 
this correctly. If you have problems check the initial response carefully.

With curl you need to add the `-L` option to tell curl to follow the redirect.

When using a POST operation you also need to tell curl to use the `--post301` or `--post302` option to tell curl to use 
POST for the redirect, otherwise you will end up with an empty body.

Also, if using untrusted certificates with https you must also specify the `-k` option.

Thus the command for submitting a job that is listed above need to be turned into:

```
curl -kL --post301\ 
  -F "ExecutionParameters=@ExecutionParametersSdf.json;type=application/json;filename=ExecutionParameters.json" \
  -F "input=@../../../../data/testfiles/Kinase_inhibs.sdf;type=chemical/x-mdl-sdfile;filename=input"\
  -H "Content-Type: multipart/mixed"\
  -H "Authorization: bearer $token"\
  -H "SquonkUsername: user1" http://nginx/jobexecutor/rest/v1/jobs/
```

See [here](https://curl.haxx.se/docs/manpage.html#--post301) for more info on those options.

## THIS IS WORK IN PROGRESS

The following need attention:

1. Tomcat currently runs as the root user as executing jobs needs access to the docker socket.
1. Docker services (those defined with a DockerServiceDescriptor or NextflowServiceDescriptor) and internal services are supported.
HTTP services (mostly for property prediction with ChemAxon and RDKit) are not yet supported.
1. Support for accessing 'thin' services needs to be added.
1. Handle type conversions - accept a SDFile and send it to a service that handles Dataset
1. Allow to specify just the service descriptor ID not the entire service descriptor in the job description
