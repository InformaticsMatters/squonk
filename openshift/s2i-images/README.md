# Squonk Source-to-Image (CI/CD) files
The `s2i-images` directory contains files for building
OpenShift [Source-to-Image] docker images. There is a _main_ s2i build
and separate _runtime_ builds for each application image.

The _main_ image (the `squonk-build`) acts as an initial build stage
and is triggered by commits to the squonk repository's _master_ branch.
The `squonk-build` builds all the artefacts for each runtime and it is used
as a _source_ for each _runtime_ build.

Once the `squonk-build` has completed it writes its image to the
`squonk-build` **ImageStream**. Changes to this cause the _runtime_ builds
to launch. The _runtime_ builds copy their runtime material from
`squonk_build:latest` before writing their own images to their
image streams.

The application containers (CellExecutor, CoreServices etc.) monitor the
their corresponding _runtime_ image streams and re-deploy as they change.

## Deploying the s2i framework
Deployment of the s2i build images and their respective image streams is
handled by the squonk ansible playbook `squonk-cicd`. Refer to the
[Ansible README](../ansible/README.md) and some additional setup instructions
in the [CICD README](../ansible/README-CICD.md).

## Docker Hub
The s2i images are expected to be available on the public Docker Hub.
You need to have built and pushed these or, ideally, setup automatic builds
in Docker Hub so that the s2i images build with each GitHub commit

There should be an image for the `squonk-build` and each `*-runtime`.

When these images are pushed they will induce a rebuild of their corresponding
s2i image in the Squonk CI/CD project.

---

[source-to-image]: https://docs.openshift.com/container-platform/3.11/creating_images/s2i.html#creating-images-s2i
