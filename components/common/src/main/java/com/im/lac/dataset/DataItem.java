package com.im.lac.dataset;

import java.sql.Timestamp;

/**
 *
 * @author timbo
 */
public class DataItem {
    
    public static final String HEADER_DATA_ITEM_NAME = "DataItemName";

    private Long id;
    private String name;
    private Metadata metadata;
    private Timestamp created;
    private Timestamp updated;
    private Long loid;

    public DataItem() {

    }

    public DataItem(
            Long id,
            String name,
            Metadata metadata,
            Timestamp created,
            Timestamp updated,
            Long loid) {
        this.id = id;
        this.name = name;
        this.metadata = metadata;
        this.created = created;
        this.updated = updated;
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

}
