package com.im.lac.wicket.inmethod;

import com.inmethod.grid.column.AbstractColumn;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.io.Serializable;

/**
 * @author simetrias
 */
public class CheckBoxColumn<T extends Serializable> extends AbstractColumn<EasyDataSource<T>, T, String> {

    private CheckBoxColumnCallbackHandler<T> checkBoxColumnCallbackHandler = new CheckBoxColumnCallbackHandler<T>();

    public CheckBoxColumn(String id) {
        super(id, new Model<String>(""));
    }

    @Override
    public String getCellCssClass(IModel<T> rowModel, int rowNum) {
        if (rowModel.getObject() instanceof CssProviderRowModel) {
            return ((CssProviderRowModel) rowModel.getObject()).getCellCssClassForColumn(this.getId());
        } else {
            return super.getCellCssClass(rowModel, rowNum);
        }
    }

    @Override
    public Component newCell(WebMarkupContainer wmc, String componentId, final IModel<T> rowModel) {
        return new CheckBoxPanel<T>(componentId, rowModel, checkBoxColumnCallbackHandler);
    }

    public void setCheckBoxColumnCallbackHandler(CheckBoxColumnCallbackHandler<T> checkBoxColumnCallbackHandler) {
        this.checkBoxColumnCallbackHandler = checkBoxColumnCallbackHandler;
    }
}
