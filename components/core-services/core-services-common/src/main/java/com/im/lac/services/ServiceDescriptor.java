package com.im.lac.services;

import com.im.lac.dataset.Metadata;
import com.im.lac.job.jobdef.JobDefinition;

/**
 * Descriptor of a service that can be used to parameterise a request to this service. The basic
 * process goes as follows:
 * <ol>
 * <li>The service implementation provides a URL that returns a List of ServiceDescriptors for
 * services it supports.</li>
 * <li>The administrator of the service registers this URL into the system to make the system aware
 * of the services</li>
 * <li>At runtime the system looks up the registered services, retrieves their ServiceDescriptors,
 * and makes those services available to the user</li>
 * <li>The user chooses to use a service. A UI is generated to allow them to define the appropriate
 * parameters for execution.</li>
 * <li>When user chooses to submit the appropriate JobDefintion is created and POSTed to the job
 * service</li>
 * <li>A JobStatus is immediately returned that allows the job to be monitored and handled.</li>
 * </ol>
 *
 * @author timbo
 */
interface ServiceDescriptor {

    /**
     * A license token the user must have to be able to use the service. Should this really be an
     * enum? - probably need to be able to define new token types on the fly so it might be better
     * as and interface.
     *
     */
    public enum LicenseToken {

        CHEMAXON
    }

    /**
     * The short name of this service
     *
     * @return
     */
    String getName();

    /**
     * The owner of this service
     *
     * @return
     */
    String getOwner();

    /**
     * A meaningful description of the service
     *
     * @return
     */
    String getDescription();

    /**
     * The URL to call to execute the service
     *
     * @return
     */
    String getExecutionUrl();

    /**
     * The URL to call to get documentation on the service. e.g. the services "home page"
     *
     * @return
     */
    String getResourceUrl();

    /**
     * The URL of the owner of this service. e.g. the owners "home page"
     *
     * @return
     */
    String getOwnerUrl();

    /**
     * The type(s) of object this service can process. e.g. MoleculeObject
     *
     * @return
     */
    Class[] getInputClass();

    /**
     * Often the same as the inputClass, but not always
     *
     * @return
     */
    Class[] getOutputClass();

    /**
     * Single Item or Stream of multiple Items
     *
     * @return
     */
    Metadata.Type[] getInputTypes();

    /**
     * Usually the same as the inputType, but not always
     *
     * @return
     */
    Metadata.Type[] getOutputTypes();

    /**
     * One of mode modes through which this service can be accessed. e.g. a service can support
     * direct and batch modes.
     *
     * @return
     */
    Mode[] getModes();

    interface Mode {

        /**
         * The short name of this mode
         *
         * @return
         */
        String getName();

        /**
         * A meaningful description of the mode
         *
         * @return
         */
        String getDescription();

        /**
         * The JobDefinition class for this mode. This is used to submit a job to this service.
         *
         * @return
         */
        Class<? extends JobDefinition> getJobType();

        /**
         * The minimum number of items that can be sent to this service mode. Usually this would be
         * 1.
         *
         * @return
         */
        int getMinSize();

        /**
         * The maximum number of items that can be sent to this service mode. Usually this would be
         * Integer.MAX_VALUE, but this allows certain service modes to be limited in terms of input
         * e.g. for slow running jobs only a small number of records can be sent in "direct" mode
         * with a batch mode supporting larger, or unlimited number of records.
         *
         * @return
         */
        int getMaxSize();

        /**
         * The cost of using this service for a single item. e.g. for processing 1000 items is costs
         * 1000 * cost. Many services will have a cost of zero. Cost is in arbitrary units and
         * deducted from the users account.
         * <p>
         * <b>Important</b>: ensure negative costs are not permitted!
         *
         * @return
         */
        float getCost();

        /**
         * To use this service mode you must possess ALL of these tokens.
         *
         * @return
         */
        LicenseToken[] getRequiredLienceTokens();

        /**
         * Get the parameters accepted by this service mode. This is used to build the UI for the
         * user to fill, and those parameters submitted with the JobDefintion. These should be
         * modelled as name/value pairs that can handled as HTTP header parameters (not a Map as
         * HTTP supports multiple parameters with the same name)
         * <b>
         * TODO - spec this out in more detail
         *
         * @return
         */
        Object[] getParameters();

    }

}
