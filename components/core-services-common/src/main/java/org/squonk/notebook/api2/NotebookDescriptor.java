package org.squonk.notebook.api2;

import java.util.Date;

/**
 * Created by timbo on 29/02/16.
 */
public class NotebookDescriptor {

    private Long id;
    private String name;
    private String description;
    private String owner;
    private Date createdDate;
    private Date lastUpdatedDate;

    public NotebookDescriptor() {

    }

    public NotebookDescriptor(Long id, String name, String description, String owner, Date createdDate, Date lastUpdatedDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
    }


    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getOwner() {
        return owner;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }
}
