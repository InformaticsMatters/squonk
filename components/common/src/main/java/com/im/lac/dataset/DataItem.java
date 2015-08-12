package com.im.lac.dataset;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 *
 * @author timbo
 */
public class DataItem implements Serializable {

    private Long id;
    private String name;
    private Long ownerId;
    private String ownerUsername;
    private Metadata metadata;
    private Timestamp created;
    private Timestamp updated;
    private Long loid;

    public DataItem() {

    }

    public DataItem(
            Long id,
            String name,
            Long ownerId,
            String ownerUsername,
            Metadata metadata,
            Timestamp created,
            Timestamp updated,
            Long loid) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.ownerUsername = ownerUsername;
        this.metadata = metadata;
        this.created = created;
        this.updated = updated;
        this.loid = loid;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    
    public Long getOwnerId() {
        return ownerId;
    }
    
    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public Timestamp getUpdated() {
        return updated;
    }

    public void setUpdated(Timestamp updated) {
        this.updated = updated;
    }

    public Long getLoid() {
        return loid;
    }

    public void setLoid(Long loid) {
        this.loid = loid;
    }

    @Override
    public String toString() {
        return "DataItem{" + "id=" + id + ", name=" + name + ", owner=" + ownerUsername + ", created=" + created + ", updated=" + updated + ", loid=" + loid + ", metadata=" + metadata + '}';
    }

}
