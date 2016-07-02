package org.squonk.mqueue;

import org.squonk.util.IOUtils;

/**
 *
 * @author timbo
 */
public class MessageQueueCredentials {

    public static final String MQUEUE_JOB_STEPS_EXCHANGE_NAME = "jobs.direct";
    public static final String MQUEUE_JOB_STEPS_EXCHANGE_PARAMS = "&autoDelete=false&durable=true&queue=jobs.steps&routingKey=jobs.steps";
    public static final String MQUEUE_JOB_STEPS_QUEUE_NAME = "jobs.steps";

    public static final String MQUEUE_USERS_EXCHANGE_NAME = "users.topic";
    public static final String MQUEUE_USERS_EXCHANGE_PARAMS = "&exchangeType=topic&autoDelete=false&durable=true&queue=allusers&routingKey=users.#";

    public static final String MQUEUE_JOB_METRICS_EXCHANGE_NAME = "metrics.topic";
    public static final String MQUEUE_JOB_METRICS_EXCHANGE_PARAMS = "&exchangeType=topic&autoDelete=false&durable=true&queue=metrics";


    private String hostname;
    private String username;
    private String password;
    private String virtualHost;

    /**
     * Default credentials pointing to prod environment. Default setting scan be overridden using
     * the RABBITMQ_HOST, RABBITMQ_USER and RABBITMQ_PASSWORD environment variables.
     */
    public MessageQueueCredentials() {
        this(null, null);
    }

    /**
     * Specify the MQ credentials. If null then defaults for the prod environment are used, but can
     * be overridden using the RABBITMQ_HOST, RABBITMQ_SQUONK_PASS environment
     * variables.
     *
     * @param hostname
     * @param password
     */
    public MessageQueueCredentials(String hostname, String password) {

        this.hostname = (hostname != null ? hostname : IOUtils.getConfiguration("RABBITMQ_HOST", "localhost"));
        this.username = "squonk";
        this.password = (password != null ? password : IOUtils.getConfiguration("RABBITMQ_SQUONK_PASS", "squonk"));
        this.virtualHost = "/squonk";
    }

    public String getHostname() {
        return hostname;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public String generateUrl(String exchange, String params) {
        return "rabbitmq://" + getHostname()
                + "/" + exchange
                + "?vhost=" + getVirtualHost()
                + "&username=" + getUsername()
                + "&password=" + getPassword()
                + params;
    }


}
