package com.im.lac.portal.service.api;

import java.io.Serializable;
import java.util.List;

public interface RowDescriptor extends Serializable {

    Long getId();

    String getDescription();

    List<PropertyDescriptor> listAllPropertyDescriptors();

    PropertyDescriptor findPropertyDescriptorById(Long id);

}
