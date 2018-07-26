# External Job Execution

Provides a REST web service for executing Squonk services.
The job is initialised by POSTing a job description and the data to process in Multipart MIME format.
Simple examples can be found in this [example](src/test/execute.sh).

Then key class is org.squonk.services.camel.routes.JobExecutorRouteBuilder which itself uses classes from
the core-services-exec module.

This service is packages up in the `squonk/jobexecutor` Docker image and deployed to the running Squonk
environment (currently only the Dokerised version - OpenShift will be added).

THIS IS WORK IN PROGRESS

The following need attention:

1. Check content types and gzip encoding.
1. Tomcat currently runs as the root user as executing jobs needs access to the docker socket.
1. Only plain Docker services (those defined with a DockerServiceDescriptor) are supported.
Other types will soon be added.
1. Support for accessing 'thin' services needs to be added.
