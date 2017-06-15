/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.dataset;

import java.sql.Timestamp;

/**
 *
 * @author timbo
 */
public class DatasetDescriptor {

    private Long id;
    private String name;
    private Long ownerId;
    private String ownerUsername;
    private DatasetMetadata metadata;
    private Timestamp created;
    private Timestamp updated;
    private Long loid;

    public DatasetDescriptor() {

    }

    public DatasetDescriptor(
            Long id,
            String name,
            Long ownerId,
            String ownerUsername,
            DatasetMetadata metadata,
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

    /**
     * Get the value of metadata
     *
     * @return the value of metadata
     */
    public DatasetMetadata getMetadata() {
        return metadata;
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

    public Long getOwnerId() {
        return ownerId;
    }

    public String getOwnerUsername() {
        return ownerUsername;
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
        return "DatasetDescriptor" + "id=" + id + ", name=" + name + ", owner=" + ownerUsername + ", created=" + created + ", updated=" + updated + ", loid=" + loid + ", metadata=" + metadata + '}';
    }

}
