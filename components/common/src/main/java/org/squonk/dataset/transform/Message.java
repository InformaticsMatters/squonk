package org.squonk.dataset.transform;

/**
 * Created by timbo on 07/08/16.
 */
public class Message {

    public enum Severity {Info, Warning, Error}

    private String message;
    private Severity level;

    public Message(String message, Severity level) {
        this.message = message;
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public Severity getLevel() {
        return level;
    }
}
