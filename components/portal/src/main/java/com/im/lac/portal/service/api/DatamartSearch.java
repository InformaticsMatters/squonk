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

        private List<PropertyDefinition> propertyDefinitionList;

        public List<PropertyDefinition> getPropertyDefinitionList() {
            return propertyDefinitionList;
        }

        public void setPropertyDefinitionList(List<PropertyDefinition> propertyDefinitionList) {
            this.propertyDefinitionList = propertyDefinitionList;
        }
    }
}

