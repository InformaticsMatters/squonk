package files.api.domain;

import toolkit.jpa.AbstractLongIdEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author simetrias
 */
@Entity
@XmlRootElement
public class CatalogItem extends AbstractLongIdEntity {
    private String name;
    private String properties;

    @Column(unique = true, nullable = false, length = 255)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Lob
    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

}
