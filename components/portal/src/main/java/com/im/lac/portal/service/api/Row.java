package com.im.lac.portal.service.api;

import java.io.Serializable;
import java.util.List;

public interface Row extends Serializable {

    Long getId();

    RowDescriptor getDescriptor();

    Object getProperty(PropertyDescriptor propertyDescriptor);

    List<Row> getChildren();

}


