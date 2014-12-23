package com.im.lac.portal.service.api;

import java.util.List;

public class DatamartSearch {

    private StructureSearch structureSearch;
    private PropertySearch propertySearch;

    public static class StructureSearch {

        private SearchType type;
        private String structure;

        public enum SearchType {

            SUBSTRUCTURE

        }
    }

    class PropertySearch {

        private List<PropertyDescriptor> propertyDescriptorList;

        public List<PropertyDescriptor> getPropertyDescriptorList() {
            return propertyDescriptorList;
        }

        public void setPropertyDescriptorList(List<PropertyDescriptor> propertyDescriptorList) {
            this.propertyDescriptorList = propertyDescriptorList;
        }
    }
}

