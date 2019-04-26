# An OpenShift (S2I) Squonk Build Image
This project is used to build the Squonk Source-To-Image (s2i) container image
that's employed in Squonk's OpenShift build process. It's based on our Gradle
Jenkins [agent], tuned for use as an s2i image.

The image contains: -

-   Custom **S2I Scripts**
-   Our **jenkins-slave-buildah-centos7** base image
    -   **buildah**
    -   **podman**
    -   **skopeo**
-   **Python 3.7**

## S2I usage instructions
Just use S2I...

    $ s2i usage squonk/s2i:latest

---

[agent]: https://github.com/InformaticsMatters/openshift-jenkins-buildah-slave
