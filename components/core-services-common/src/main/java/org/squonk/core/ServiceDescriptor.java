package org.squonk.core;

/** Interface that defined an executable service. From an instance of this class you can obtain a @{link ServiceConfig} instance
 * that defines the properties that are needed by a client of this service.
 * Implementations of this interface define the details of how the service is to be executed.
 *
 *
 * Created by timbo on 04/01/17.
 */
public interface ServiceDescriptor {

    String getId();

    /** The descriptor that defines the client side properties of the service
     *
     * @return
     */
    ServiceConfig getServiceConfig();

}
