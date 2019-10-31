# Development procedure
>   This is a guide for the Squonk development process ...
    It is not a guide on how to use git.

## All changes require an issue
1.  Development is backed and tracked by GitHub issues.
1.  If you’re going to modify the code whether it’s a feature,
    bug or critical fix, start by creating an issue.
1.  The issue **comment** should have sufficient content to allow the audience
    to know what’s wrong and what changes the issue will introduce.

## All changes require a branch
1.  Development must take place on a branch.
    Branch from a suitable source (quite often this will be the `master` branch).
1.  For simple issue correlation the branch name must begin with the issue
    number. It only needs the issue number but if you would prefer to add
    additional descriptive text this should follow the number and be hyphenated.
1.  the following are valid branch names: `68` and `68-inconsistent-role-var-names`

>   Concentrate on fixing the issue at hand. It is all too tempting to
    fix other problems while you happen to have a development branch available.
    **DON'T**. For the purpose of tracking changes and generating respectable
    release notes restrict the changes in your development branch to fixing
    the issue it belongs to. If you do find other faults on your journey
    create issues and move on - concentrate on fixing the issue for which the
    branch was created. 

## Avoid skip ci
1.  Travis omits the build process when a commit message contains the
    text `[skip ci]`. This is often used to omit a potentially long and
    time-consuming build if all that's been changed is a README. The trouble
    with this approach is that if your last commit on a branch has `[skip ci]`
    in the comment the subsequent pull-request build may also be omitted.

>   It's all about a _continuous_ build and deployment process - just let the
    machines do their work, it's free, even if it turns out it's pointless
    - one day it might not be!
     
## Submit to master via a pull request
1.  You should avoid clicking **Merge pull request** until the
    corresponding build has successfully completed - which typically takes
    around 15 minutes.
1.  The repository is setup to automatically remove the source branch
    when the pull-request is merged.

## Tag master regularly
1.  Fell free to let changes accumulate in `master` as you see fit, but you
    should consider creating tags at the earliest opportunity so...
1.  As changes are accepted into the `master` branch consider whether they
    are significant enough to warrant a new tag.
1.  Tags follow the [Semantic Versioning 2.0.0] standard - it's basically
    `MAJOR`.`MINOR`.`PATCH`. The tag **must not** begin with `v` or `V`,
    it's just _digits and dots_.
1.  As a side-effect of the way Travis works, tags on non-master branches also
    result in an image build and push to Docker. For this reason, for now,
    please avoid tagging non-master branches.

## Prune docker.io regularly
1.  As each tag results in a new container image on docker.io we need
    to be aware that the registry may fill up. preventing new images
    from beign pushed. To satisfy the Docker image quota you will need
    to optionally archive and then delete old images.
1.  **DO NOT** delete any tagged images that are expected to exist in
    current deployments.

# The CI/CD process (basics)
1.  We rely on Travis (and the project's `.travis.yml` file) to continually
    compile, test (and deploy) the application container images.
1.  A build sequence takes approximately 15 minutes.
1.  When you create a development branch the tests for your branch will start.
    The CI/CD process will compile and test code as you commit your changes.
1.  Changes on the `master` branch will result in new `:latest` container
    images pushed to docker.io.
1.  When a branch is tagged new container images will be pushed with the
    tag `:<tag>`.

---

[Semantic Versioning 2.0.0]: https://semver.org