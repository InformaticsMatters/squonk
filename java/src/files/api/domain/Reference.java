package files.api.domain;

import toolkit.jpa.AbstractLongIdEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author simetrias
 */
@Entity
@XmlRootElement
public class Reference extends AbstractLongIdEntity {
    private String tag;
    private CatalogItem catalogItem;

    @Column(nullable = false)
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @ManyToOne
    @JoinColumn(nullable = false)
    public CatalogItem getCatalogItem() {
        return catalogItem;
    }

    public void setCatalogItem(CatalogItem catalogItem) {
        this.catalogItem = catalogItem;
    }
}
