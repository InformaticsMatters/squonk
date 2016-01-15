package org.squonk.notebook.api;

import java.util.List;

/**
 * Created by timbo on 15/01/16.
 */
@Deprecated
public interface IOptionDefinition<T> {

    String getName();

    String getDisplayName();

    List<T> getPicklistValueList();

    T getDefaultValue();

    OptionType getOptionType();
}
