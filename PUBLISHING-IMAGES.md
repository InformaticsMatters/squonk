# Publishing images

>   Refer to the [main documentation]
    for general process and development advice.

## The automated build (CI/CD process)
The automated build (for public GitHub) relies on GitHub Actions and is
controlled by the files in the project's `.github/workflows` directory
to continually compile and test the application container images before
deploying them to [Docker Hub].

-   Feel free to build Squonk for your own purposes as you see fit.
-   Official images (those deployed to active sites) **MUST**
    be those published automatically, from the CI/CD build process
    and deposited in Docker Hub.

>   Always consult the github workflows for the up-to-date details of the
    automated build.
 
In summary:

-   All changes to master result in new images on Docker Hub
-   All tags, whether on master or not, result in a new tagged image

A typical build and publish sequence takes approximately 15 minutes.

---

[docker hub]: https://hub.docker.com/u/squonk/
[main documentation]: https://gitlab.com/informaticsmatters/documentation
