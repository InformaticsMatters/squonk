# External Job Execution

Provides a REST web service for executing Squonk services.
The job is initialised by POSTing a job description and the data to process in Multipart MIME format.
Simple examples can be found in this [example](src/test/execute.sh).

Then key class is org.squonk.services.camel.routes.JobExecutorRouteBuilder which itself uses classes from
the core-services-exec module.

This service is packages up in the `squonk/jobexecutor` Docker image and deployed to the running Squonk
environment (currently only the Dokerised version - OpenShift will be added).

## Authentication

When used in Squonk authenticaction is required. You must obtain a token from Keycloak and include that in your 
requests. The token is short lived (5 mins by default).

### Using curl

This is a good approach for testing. A `curl` client is automatically added to the `squonk` realm, but
is restricted to running from `localhost`.

_Obtain a token_:

```token=$(curl -kL --data "grant_type=password&client_id=curl&username=user1&password=user1"\
 https://172.20.0.1/auth/realms/squonk/protocol/openid-connect/token |\
  grep -Po '(?<="access_token":")[^"]*')
```
Adjust the username, password and hostname accordingly.

_Inspect the token_:

`echo $token`

_Access job executor_:

`curl -kL -H "Authorization: bearer $token" http://172.20.0.1/jobexecutor/rest/v1/jobs`

The result will be the JobStatus objects for all jobs you have submitted. Most likely and empty array.

### Using an application

The client application must be added to the `squonk` realm in Keycloak:

1. Create the new client and specify the appropriate `Redirect URIs` for your application
2. Set the `Access type` to be `confidential`
3. Enable the `Service accounts` option
4. On the `Service Account Roles` tab add the appropriate roles (e.g. `standard-user`)
5. Make a record of the client secret from the `Credentials` tab

_Obtain a token_:

Use a POST request to get the token. With curl the command would be:

```
token=$(curl -kL \
  -H 'Content-Type: application/x-www-form-urlencoded'\
  -d 'grant_type=client_credentials'\
  -d 'client_id=<client-id>'\
  -d 'client_secret=<client-secret>'\
  https://1<hostname>/auth/realms/squonk/protocol/openid-connect/token|\
  grep -Po '(?<="access_token":")[^"]*')
```
Substitute the appropriate values for `<client-id>`, `<client-secret>` and `<hostname>`

_Inspect the token_:

`echo $token`

_Access job executor_:

Access the application using the token.  With curl the command would be:

`curl -kL -H "Authorization: bearer $token" http://172.20.0.1/jobexecutor/rest/v1/jobs`

When using a service account you can also specify a username that will be used instead of the service
account by adding a header like this: `-H "SquonkUsername: someuser"`.


## THIS IS WORK IN PROGRESS

The following need attention:

1. Check content types and gzip encoding.
1. Tomcat currently runs as the root user as executing jobs needs access to the docker socket.
1. Only plain Docker services (those defined with a DockerServiceDescriptor) are supported.
Other types will soon be added.
1. Support for accessing 'thin' services needs to be added.
1. Handle type conversions - accept a SDFile and send it to a service that handles Dataset 
