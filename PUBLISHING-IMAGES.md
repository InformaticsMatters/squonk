# Publishing images

>   Refer to the [main documentation]
    for general process and development advice.

## The automated build (CI/CD process)
The automated build (for public GitHub) relies [Travis] and is
controlled by the project's `.travis.yml` file in order to continually
compile, test (and deploy to [Docker Hub]) the application container images.

-   Feel free to build Squonk for your own purposes as you see fit.
-   Official images (those deployed to active sites) **MUST**
    be those published automatically, from the CI/CD build process
    and deposited in Docker Hub.

>   Always consult `.travis.yml` for the up-to-date details of the
    automated build.

Travis runs the scripts in the Squonk `scripts` directory. There are 6 scripts
but the scripts chosen to run depend on whether it's a branch and/or 
whether the repo's been tagged: -

1.  `compile`. **Runs for every commit**. A deliberately fast stage in
    order to catch compilation (build) problems.
1.  `test`.  **Runs for every commit**. Compiles and runs the standard unit
    tests.
1.  `docker`. **Runs on branches, if not tagged**. This simply ensures
    that container images can be constructed.
1.  `pulish-latest`. **Runs on master (when not tagged)**. This stage builds
    and pushes `latest` images to Docker Hub if the build is not in response
    to a **tag**.
1.  `publish-tag-master`. **Runs on master (when tagged)**. This stage builds
    and pushes images to Docker Hub. It pushes tagged and new `latest` images.
1.  `publish-tag-branch`. **Runs on non-master branches (when tagged)**.
    This stage builds and pushes images to Docker Hub. It pushes new
    tagged images but *does not* publish new `latest` images.
    
In summary:

-   All changes to master result in new images on Docker Hub
-   All tags, whether on master or not, result in a new tagged image

A typical build and publish sequence takes approximately 15 minutes.

## Validating the travis file
If you've made changes to the `.travis.yml` file it's valuable to [validate]
the changes before committing. With the command-line utility installed
run: -

    $ gem install travis
    $ travis lint

>   The linter is suffering from [bugs] so, for now, ignore things like
    **unexpected key ???, dropping**. But it can find serious flaws.

---

[bugs]: https://github.com/travis-ci/travis.rb/issues/571
[docker hub]: https://hub.docker.com/u/squonk/
[main documentation]: https://gitlab.com/informaticsmatters/documentation
[travis]: https://travis-ci.org/InformaticsMatters/squonk
[validate]: https://support.travis-ci.com/hc/en-us/articles/115002904174-Validating-travis-yml-files
