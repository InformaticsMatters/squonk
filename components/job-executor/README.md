# External Job Execution

Provides a REST web service for executing Squonk services.
The job is initialised by POSTing a job description and the data to process in Multipart MIME format.
Simple examples can be found in this [example](src/test/execute.sh).

Then key class is org.squonk.services.camel.routes.JobExecutorRouteBuilder which itself uses classes from
the core-services-exec module.

This service is packaged up in the `squonk/jobexecutor` Docker image, and where Keycloak authentication is used the 
`squonk/jobexecutor-keycloak` image, and deployed to the running Squonk environment.

## Dev mode

When running in the 'dev' mode in Docker the SQUONK_JOBEXECUTOR_ALLOW_UNAUTHENTICATED
environment variable is set to 'true' so no authentication is in place. When you can access the API you do so as the user
`nobody`. That user may not exist in the database, but in this unauthenticated mode you can specify a user to delegate to
with the `SquonkUsername` header.

### Using curl

This is a good approach for testing.

Note: you may need to add the `-k` flag to curl to allow use of self-signed certificates and/or the `-L` flag to allow
curl to follow redirects.

_Listing services_

You can get a summary of the services that are available:


```
curl -H "SquonkUsername: user1" http://<server:port>/jobexecutor/rest/v1/services/
```

Replace `<server:port>` with the appropriate hostname and port e.g. 172.20.0.2:8080. You might need to use https as the
protocol depending on your environment.

What is returned is an array of the services, with the id, name and description of those services.
Identify the service you want to use. e.g. `core.dataset.filter.slice.v1` which is a simple services that let's you extract
a subset of a dataset. To see the full details of the service use the service ID to fetch the full service descriptor:

```
curl -H "SquonkUsername: user1" http://<server:port>/jobexecutor/rest/v1/services/core.dataset.filter.slice.v1
``` 

The most important things to look for are the `inputDescriptors` and `outputDescriptors` which identify the expected inputs 
and outputs (and their formats), as well as the `optionDescriptors` which define what options can be specified when executing 
the service. For instance, in this case there are 2 options, the number of records to skip (`skip` option) and the number
of records to include (`count` option).

```json
"optionDescriptors": [
        {
          "@class": "org.squonk.options.OptionDescriptor",
          "typeDescriptor": {
            "@class": "org.squonk.options.SimpleTypeDescriptor",
            "type": "java.lang.Integer"
          },
          "key": "skip",
          "label": "Number to skip",
          "description": "The number of records to skip",
          "visible": true,
          "editable": true,
          "minValues": 1,
          "modes": [
            "User"
          ]
        },
        {
          "@class": "org.squonk.options.OptionDescriptor",
          "typeDescriptor": {
            "@class": "org.squonk.options.SimpleTypeDescriptor",
            "type": "java.lang.Integer"
          },
          "key": "count",
          "label": "Number to include",
          "description": "The number of records to include after skipping",
          "visible": true,
          "editable": true,
          "minValues": 1,
          "modes": [
            "User"
          ]
        }
      ]
```

Values for those options needs to be supplied when executing the service. For instance:

```json
{"skip":5,"count":5}
```

_Listing jobs_

Display a list of your current jobs:

```
curl -H "SquonkUsername: user1" http://<server:port>/jobexecutor/rest/v1/jobs/
```

Initially this will be an empty array as you have not yet submitted any.

_Posting a new Job_

From the root directory of the squonk project directory execute something like this:

```
curl -kL -X POST \
  -H "SquonkUsername: user1"\
  -F 'options={"skip":5,"count":5}'\
  -F 'input_data=@data/testfiles/Kinase_inhibs.json.gz;type=application/x-squonk-molecule-object+json;filename=input_data'\
  -F 'input_metadata=@data/testfiles/Kinase_inhibs.metadata;type=application/x-squonk-dataset-metadata+json;filename=input_metadata'\
  -H "Content-Type: multipart/mixed"\
  http://<server:port>/jobexecutor/rest/v1/jobs/core.dataset.filter.slice.v1
```

Notice how the service ID is included as the last part of the URL, and the options are specified as the first form parameter.
The inputs are specified as the second and third form field.

The result is some JSON that includes the job ID, and hopefully says that the job status is `RUNNING`.
You can then monitor the job's status like this:

```
curl -H "SquonkUsername: user1" http://<server:port>/jobexecutor/rest/v1/jobs/<job-id>/status/
```

Substitute in your job ID. 
Once the status is `RESULTS_READY` you can retrieve the results using:

```
curl -H "SquonkUsername: user1" http://<server:port>/jobexecutor/rest/v1/jobs/<job-id>/results/
```

Once you have the results you must delete the job using:

```
curl -X DELETE -H "SquonkUsername: user1" http://<server:port>/jobexecutor/rest/v1/jobs/<job-id>/
```

## Authentication

When used in `basic` mode with a Docker deployment or in an OpenShift deployment authentication is required.
You must obtain a token from Keycloak and include that in your requests.
The token is short lived (5 mins by default). In this case it is the `squonk/jobexecutor-keycloak` docker image
that is deployed.

### Using curl

_Obtain a token_:

```
token=$(curl --data "grant_type=password&client_id=squonk-jobexecutor&username=user1&password=user1"\
  https://<server:port>/auth/realms/squonk/protocol/openid-connect/token |\
  grep -Po '(?<="access_token":")[^"]*')
```
Adjust the username, password and hostname accordingly. In `basic` mode in the docker environment the hostname is likely `nginx`.
In an OpenShift environment it is the public hostname of the jobexecutor route e.g. `squonk-jobexecutor.openshift.example.org`.

_Inspect the token_:

`echo $token`

_List jobs_:

`curl -H "Authorization: bearer $token" http://<server:port>/jobexecutor/rest/v1/jobs`

The result will be the JobStatus objects for all jobs you have submitted. Most likely and empty array.

## Using a service account

When accessing from an application better than using individual user accounts you can use a service account.
We use the `squonk-jobexecutor` client in the Keycloak realm to authenticate against.
That client must be set up to support service accounts. In the Keycloak admin console go to the `squonk-jobexecutor`
client in the appropriate realm (e.g. `squonk`) and:

1. Make sure the `Access type` is set to `confidential`
3. Enable the `Service accounts` option
4. On the `Service Account Roles` tab add the appropriate roles (e.g. `standard-user`)
5. Make a record of the client secret from the `Credentials` tab

This means you must also specify the client secret in your requests.

_Obtain a token_:

Use a POST request to get the token. With curl the command would be:

```
token=$(curl\
  -H 'Content-Type: application/x-www-form-urlencoded'\
  -d 'grant_type=client_credentials'\
  -d 'client_id=squonk-jobexecutor'\
  -d 'client_secret=<client-secret>'\
  https://<server:port>/auth/realms/squonk/protocol/openid-connect/token|\
  grep -Po '(?<="access_token":")[^"]*')
```
Substitute the appropriate values for `<client-id>`, `<client-secret>` and `<server:port>`

_Inspect the token_:

`echo $token`

_Access job executor_:

Access the application using the token.  With curl the command would be:

`curl -H "Authorization: bearer $token" http://<server:port>/jobexecutor/rest/v1/jobs`

When using a service account you can also specify a username that will be used instead of the service
account by adding a header like this: `-H "SquonkUsername: someuser"`.


### Executing a service

Examples can be found in this [example](src/test/execute.sh).

```
curl\ 
  -F 'options={"skip":5,"count":5}'\
  -F 'input_data=@data/testfiles/Kinase_inhibs.json.gz;type=application/x-squonk-molecule-object+json;filename=input_data'\
  -F 'input_metadata=@data/testfiles/Kinase_inhibs.metadata;type=application/x-squonk-dataset-metadata+json;filename=input_metadata'\
  -H "Content-Type: multipart/mixed"\
  -H "Authorization: bearer $token"\
  -H "SquonkUsername: user1" http://<server:port>/jobexecutor/rest/v1/jobs/core.dataset.filter.slice.v1
```

### Working behind reverse proxy servers.

When the service is behind a reverse proxy server (e.g. nginx on the Dockerised setup or a route in OpenShift)
your initial request will get a 301 or 302 response indicating a redirect. You need to ensure that your HTTP client handles 
this correctly. If you have problems check the initial response carefully.

With curl you need to add the `-L` option to tell curl to follow the redirect.

When using a POST operation you might also need to tell curl to use the `--post301` or `--post302` option to tell curl to use 
POST for the redirect, otherwise you will end up with an empty body.

Also, if using untrusted certificates with https you must also specify the `-k` option.

Thus the command for submitting a job that is listed above need to be turned into:

```
curl -kL --post301\ 
  -F 'options={"skip":5,"count":5}'\
  -F 'input_data=@data/testfiles/Kinase_inhibs.json.gz;type=application/x-squonk-molecule-object+json;filename=input_data'\
  -F 'input_metadata=@data/testfiles/Kinase_inhibs.metadata;type=application/x-squonk-dataset-metadata+json;filename=input_metadata'\
  -H "Content-Type: multipart/mixed"\
  -H "Authorization: bearer $token"\
  -H "SquonkUsername: user1" http://<server:port>/jobexecutor/rest/v1/jobs/core.dataset.filter.slice.v1
```

See [here](https://curl.haxx.se/docs/manpage.html#--post301) for more info on those options.


## THIS IS WORK IN PROGRESS

The following need attention:

1. Tomcat currently runs as the root user as executing jobs needs access to the docker socket.
1. Handle type conversions - accept a SDFile and send it to a service that handles Dataset
