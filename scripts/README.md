# Scripts (for building)
Scripts for use (mainly) by Travis.

Unlike GitLab, Travis does not permit the execution of multiple commands in
a Job. So, to provide custom (and complex) builds and deployments - especially
those that require the execution of more than one command - the 'steps' for
each job are written in scripts that we neatly collect together in this
directory and refer to from the project's `.travis.yml`.

As a user you should be able to run any script from the project root
of a local checkout, i.e.: -

    $ ./scripts/compile.sh
