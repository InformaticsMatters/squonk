package com.im.lac.portal.service.api;

import java.io.Serializable;

public interface PropertyDescriptor extends Serializable {

    long STRUCTURE_PROPERTY_ID = 0;

    Long getId();

    String getDescription();

}
