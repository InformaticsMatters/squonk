package org.squonk.types;

import java.util.Map;

/**
 * Created by timbo on 13/10/2016.
 */
public interface CompositeType {

    Map<String,Object> getSimpleTypes();

    Map<String, Class> getSimpleTypeDefinitions();
}
