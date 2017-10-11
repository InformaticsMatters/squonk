# OpenShift template coding standards
A set of brief standards for the production of OpenShift templates.
An existing template will provide a good example (i.e. `chemservices.yaml`).
Importantly, if in doubt, don't guess, ask.

## Naming convention
-   Template files are written in YAMl
-   The filename is all lower-case and end `.yaml`

## Labels
-   All templates must provide a `template` label.
-   For Squonk-related templates the label should start `squonk-`.
    The postgres template would be called `squonk-postgres` for example.
-   All objects within the template must provide an `app` label
    whose value is equal to the `APP_NAME` `property`.

In this way all objects belonging to a specific template can be
deleted with the command:

    $ oc delete all,cm,pvc --selector template=${APP_NAME}
    
And all instances of a particular processing of a template can be deleted
with the command:

    $ oc delete all,cm,pvc --selector app=${APP_NAME}
    
## Properties
-   An `APP_NAME` property must be provided. The default value for
    squonk-related templates will begin `squonk-` followed by the
    type of application (i.e. `postgres`).
-   Avoid adding a description for the obvious. `APP_NAME` is unlikely
    to benefit further from a `desacription` like `The application name`.
    (DRY).
-   Unless there are very good reasons you should strive to add a set of
    CPU and memory requests and limits that are used . Limits are more important than
    requests as they provide a hard-limit that a _Pod_ cannot exceed.
    
    - name: CPU_REQUEST
      value: 500m
    - name: CPU_LIMIT
      value: 1000m
    - name: MEM_REQUEST
      value: 500Mi
    - name: MEM_LIMIT
      value: 1Gi
       
## Tags
-   Employ tags in the template to permit searching.
-   Squonk-related templates should always contain a `squonk` tag,
    i.e. `"squonk,postgres"`
 
---

October 2017
