package files.api.localservice;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

/**
 * @author simetrias
 */
@XmlRootElement
public class UpdateReferencesData implements Serializable {

    private String referenceTag;
    private List<Long> catalogItemIdList;

    public String getReferenceTag() {
        return referenceTag;
    }

    public void setReferenceTag(String referenceTag) {
        this.referenceTag = referenceTag;
    }

    public List<Long> getCatalogItemIdList() {
        return catalogItemIdList;
    }

    public void setCatalogItemIdList(List<Long> catalogItemIdList) {
        this.catalogItemIdList = catalogItemIdList;
    }
}
