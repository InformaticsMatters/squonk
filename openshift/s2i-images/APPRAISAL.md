# CI/CD thoughts
## OpenShift s2i appraisal
Our current s2i deployment is neat but it has _issues_. Over the past few weeks
I’ve observed a number of issues that impede the build and deployment of
Squonk. They are: -

1.  **Forced to use incremental builds**. As a Java application a single s2i
    build image is not sufficient (as we build multiple application images
    from a single repository). Therefore you have to publish a main _build_
    s2i image and separate _runtime_ s2i images for every application container
    image. This is a lot of additional docker images to maintain (six at the moment)
2.  **Learning curve**The s2i principle is simple but the build scripts
    (assemble, run, save-artifacts and usage) have eccentricities
    (‘special directories’ and non-intuitive patterns) and are therefore
    cumbersome and (in the case of incremental builds) rather complex.
3.  **Off the “yellow-brick road”** s2i is clever but it’s OpenShift specific
    and is a distraction if you want to deploy to somewhere else
    (like Kubernetes/Rancher)
4.  **Duplication (project bloat)** Having to maintain gradle, docker and s2i
    builds introduces duplication and a documentation burden - we have three
    ways of doing essentially one thing - i.e. the production of container
    images. Gradle’s docker commands and s2i are indeed clever but they’re
    (from the point of view of the container community) odd. Building docker
    using gradle plug-ins is already a _learning curve_ for many who would be
    much more comfortable using gradle to compile and docker-compose files to
    deploy.
5.  **Inflexible build** It is impossible to publish images with a run-time
    declaration of image tag. You need to define the tag when you deploy the
    s2i builds (and image streams) or accept that the built images will always
    be (for example) `latest`. Basically you cannot define the tag from the
    content of the repository.
6.  **Inflexible publication** You cannot run docker commands from an s2i
    environment - there is no docker socket so you;’d have to resort to a
    base image that contains something like _buildah_. But build is not
    really mature and until recently, did not offer _arg before from_
    (an important docker file feature). And, using build scripts also
    presents its own problems - it’s _clever_ but has not really migrated to
    the general community so, for most, it’s just _odd_.
7.  **Maintenance effort** the almost constant baby-sitting of the service.
    I see now (12 Aug) that all builds have failed for some reason
    (unable to push to registry). We have enough to do and the mechanism just
    takes too much effort to keep it going.
8.  **Cluster pressure** The build puts pressure on the deployment cluster -
    i.e. you need sufficient cores and memory to execute the builds so your
    development needs resources above those required for the run-time
    application - you need spare hardware to do the builds! 
9.  **Only for simple deployments**. There’s no ability for a build to trigger
    supporting actions - basically the build runs and pushes to a pre-defined
    tag in an image stream. Great for single-pod deployments, but what if the
    new application version requires the addition of a persistent volume or
    the deployment of new secrets?
10. **Nothing new** s2i doesn’t really do anything for us that can’t already
    be done more simply and quickly with existing community-respected processes
    (like Dockerfiles and travis - see the “Alternatives” section below)

## s2i summary
It has actually been beneficial for us to explore s2i _techniques_. We need to
know how it works to advise others but I fear that the pattern
(for something like Squonk) is…

-   too complex
-   too inflexible
-   too fragile

It just doesn’t fit our project’s existing build and deployment strategy.

The incremental builds are indeed clever but they introduce layers of
documentation and processes that are unnecessary and too complex and fragile. 

It clearly works well for a “bog standard” (simple) style of _node_ or _django_
applications and works well for thinks like `microservice:latest`. But,
for a complex network of dependent containers (with associated data
configurations, secrets, volumes and post-installation actions like the
installation of users) - it just isn’t suitable.

## Alternatives
## (1) Build in the cloud, deploy with Ansible
We already have a relatively comprehensive and robust deployment mechanism
based on hardened and mature technologies - **gradle**, **docker** and
**ansible**.

Why not simply leverage our existing development effort and build upon it?

For public GitHub repositories the _community_ is already familiar with
services like [Travis CI](https://travis-ci.org). It’s free and usually fast.
We could save a significant amount of development effort if we just replicate
our current build process using Travis. I’ve already set this up to see how
difficult the mechanism is (although we already use travis for publishing our
Python utilities so the learning curve is shallow).

I have already setup our ci/cd builds and deployments to docker using
[Travis](https://travis-ci.org/InformaticsMatters/squonk) so most of the work
may already be covered.

### Why travis?
Several benefits. It’s…

-   mature
-   well documented
-   easy to define (yaml)
-   respected
-   fast
-   simple to support multiple branches
-   able to publish (to PyPi, Docker etc.)
-   able to 'skip ci' (might be important)
-   build badges

Above all, it’s _managed by someone else_ so there’s no on-going maintenance
costs for us - i.e. no project to maintain or builds to baby-sit. It just works.

### Build stats (main image)
-   s2i build time **22 minutes**
    (includes time of longest runtime build of about 13 minutes)
	-   although the current build has now broken - Travis has not
-   Travis **7 minutes** (actual build uses multiple stages but it’s 7
    minutes to build and push to docker-hub)

### Travis drawbacks?
#### No binary repository
Travis does not offer binary repositories (to store things like container
images out of the public gaze) so building and storing private docker images
relies on external registries/repositories

#### Costly private repo support
Building _private_ repositories is not free. It’s an _option_ but it is
(for us) prohibitively expensive, costing at least $69 per month
(for one concurrent build)

>   Incidentally, if we want a cloud-based ci/cd strategy for private 
    repositories the best solution is to move the repository to **GitLab**
    where the ci/cd framework there is free and where a docker registry
    is also free for each project.

## Playbooks need to improve
The complexities of Squonk deployment (upgrading versions etc.) are not all
satisfied by the existing playbooks so, to have a a more streamlined automated
roll-out we need to capture all the actions of _upgrading_ in our exiting
playbooks.

### Step 1 - Convert Squonk playbooks to Ansible operators
This is actually easier than it sounds. We already use **Roles** so one
([OpenShift recommended](https://blog.openshift.com/reaching-for-the-stars-with-ansible-operator/))
approach is simply to _promote_ our roles to Ansible Galaxy and then create a
Docker operator image that uses that Role. Once a few concepts are under your
belt it’s actually quite simple and the main advance is that all the business
logic is basically deferred to Ansible scripts, which we already know very well.

I’ve already created several operators (for OpenSHift and Kubernetes)
including an Operator for PySimple and the process is relatively straightforward.

### Step 2 - Enhance the operators
Extend the Ansible playbooks to properly handle upgrades and _changes_ to
Squonk’s deployment. We need to do this anyway, doing them as an operator
isn’t any extra work. 

## Open issues
### (A) What do we do with sensitive build data?
Currently we cannot run basic tests without ChemAxon license files and data.
And it’s possible that we also need to integrate _sensitive_ date into running
Pods (as embedded files in the build or config-specs layers into the Pod).

1.  We could avoid build problems by restructuring the build so licence-based
    tests are (like infrastructure testing) not part of the tests by removing
    them and instead relaying on `black-box` style testing by simply running
    jobs vis the cell executor (or vis selenium and notebook creation - something
    that would be of great benefit anyway).
2.  A common approach is to encrypt and embed the sensitive data in the repo
    so we bundle the data into the build. When building we simply decrypt
    on-the-fly in CI/CD and on the desktop.

### (B) What do we do with private repos?
Move these to **GitLab**: -

1.  Like Travs it has its own ci/cd process  - except a lot more powerful
2.  They provide docker registries for each project - so built images can be
    safely (privately) published. You just need to provide a pull-secret for
    your deployment so that you can pull the images from it

----

## Recommendations
### Short term (immediately)
1.  Build
	1.  Adopt **Travis** as the continuous _build_ process
	2.  Identify a solution for **sensitive build data** (required for some tests)
2.  Deployment
	1.  Ensure existing **Ansible** playbooks can deploy a version
	    (i.e. not latest)
	2.  **Playbook enhancements**. Make sure playbooks allow us to deploy a
	    new version over a prior version
3.  Document the process
4.  Move private repos to GitLab to allow automated Portal and Signature
    pipeline builds for example

### Medium term
1.  Enhance `validate pipelines` so we test more job types
    (remember the test & integration tests may not be working).
    High-level _black-box_ testing may negate the need for new
    functional/integration testing. If we can run a pipeline that tests a
    heck of a lot fo the engine.
2.  Identify options for Portal testing

### Long term
1.  Adopt selenium as an automated test strategy for the **Portal** to fill
    large gaps in application testing - which currently take time and are
    error-prone to execute _by hand_.
