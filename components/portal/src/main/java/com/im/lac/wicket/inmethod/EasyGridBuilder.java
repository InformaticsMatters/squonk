package com.im.lac.wicket.inmethod;

import com.inmethod.grid.IGridColumn;
import com.inmethod.grid.SizeUnit;
import com.inmethod.grid.column.PropertyColumn;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converter.BigDecimalConverter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author simetrias
 */
public class EasyGridBuilder<T extends Serializable> implements Serializable {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private static final IConverter<BigDecimal> DECIMAL_CONVERTER = new DecimalConverter();
    private String id;
    private ArrayList<IGridColumn<EasyDataSource<T>, T, String>> columnList;
    private CssProviderHeaderModel cssProviderHeaderModel;

    public EasyGridBuilder(String id) {
        this.id = id;
        columnList = new ArrayList<IGridColumn<EasyDataSource<T>, T, String>>();
    }

    public ArrayList<IGridColumn<EasyDataSource<T>, T, String>> getColumnList() {
        return columnList;
    }

    public EasyGrid<T> build(final EasyDataSource.Delegate<T> dataDelegate) {
        EasyDataSource<T> easyDataSource = new EasyDataSource<T>(dataDelegate);
        EasyGrid<T> easyGrid = new EasyGrid<T>(id, easyDataSource, columnList);

        if (dataDelegate.getTotalCount() < 12) {
            easyGrid.setContentHeight(230, SizeUnit.PX);
        }
        easyGrid.setRowsPerPage(20);
        easyGrid.setClickRowToSelect(true);
        easyGrid.setClickRowToDeselect(true);
        easyGrid.setAllowSelectMultiple(false);

        return easyGrid;
    }

    public RowActionsColumn<T> newActionsColumn(final List<String> actionNameList, RowActionsCallbackHandler<T> rowActionsCallbackHandler) {
        return new RowActionsColumn<T>(actionNameList, rowActionsCallbackHandler);
    }

    public PropertyColumn<EasyDataSource<T>, T, String, String> newPropertyColumn(String title, String property, String sortProperty) {
        PropertyColumn<EasyDataSource<T>, T, String, String> propertyColumn;
        propertyColumn = new PropertyColumn<EasyDataSource<T>, T, String, String>(new Model<String>(title), property, sortProperty) {

            @Override
            public String getHeaderCssClass() {
                if (cssProviderHeaderModel != null) {
                    return cssProviderHeaderModel.getHeaderCssClassForColumn(this.getId());
                } else {
                    return super.getHeaderCssClass();
                }
            }

            @Override
            public String getCellCssClass(IModel<T> rowModel, int rowNum) {
                if (rowModel.getObject() instanceof CssProviderRowModel) {
                    return ((CssProviderRowModel) rowModel.getObject()).getCellCssClassForColumn(this.getId());
                } else {
                    return super.getCellCssClass(rowModel, rowNum);
                }
            }

        };
        return propertyColumn;
    }

    public PropertyColumn<EasyDataSource<T>, T, String, String> newBigDecimalPropertyColumn(String title, String property, String sortProperty) {
        PropertyColumn<EasyDataSource<T>, T, String, String> propertyColumn;
        propertyColumn = new PropertyColumn<EasyDataSource<T>, T, String, String>(new Model<String>(title), property, sortProperty) {

            @Override
            @SuppressWarnings("unchecked")
            protected <C> IConverter<C> getConverter(Class<C> type) {
                return (IConverter<C>) DECIMAL_CONVERTER;
            }

            @Override
            public String getHeaderCssClass() {
                if (cssProviderHeaderModel != null) {
                    return cssProviderHeaderModel.getHeaderCssClassForColumn(this.getId());
                } else {
                    return super.getHeaderCssClass();
                }
            }

            @Override
            public String getCellCssClass(IModel<T> rowModel, int rowNum) {
                if (rowModel.getObject() instanceof CssProviderRowModel) {
                    return ((CssProviderRowModel) rowModel.getObject()).getCellCssClassForColumn(this.getId());
                } else {
                    return super.getCellCssClass(rowModel, rowNum);
                }
            }
        };
        return propertyColumn;
    }

    public PropertyColumn<EasyDataSource<T>, T, String, String> newDatePropertyColumn(String title, String property, String sortProperty) {
        return newDatePropertyColumn(title, property, sortProperty, SIMPLE_DATE_FORMAT);
    }

    public PropertyColumn<EasyDataSource<T>, T, String, String> newDatePropertyColumn(String title, String property, String sortProperty, final DateFormat dateFormat) {
        PropertyColumn<EasyDataSource<T>, T, String, String> propertyColumn;
        propertyColumn = new PropertyColumn<EasyDataSource<T>, T, String, String>(new Model<String>(title), property, sortProperty) {

            private DateConverter dateConverter;

            @Override
            @SuppressWarnings("unchecked")
            protected <C> IConverter<C> getConverter(Class<C> type) {
                if (dateConverter == null) {
                    dateConverter = new DateConverter(dateFormat);
                }
                return (IConverter<C>) dateConverter;
            }

            @Override
            public String getCellCssClass(IModel<T> rowModel, int rowNum) {
                if (rowModel.getObject() instanceof CssProviderRowModel) {
                    return ((CssProviderRowModel) rowModel.getObject()).getCellCssClassForColumn(this.getId());
                } else {
                    return super.getCellCssClass(rowModel, rowNum);
                }
            }
        };
        return propertyColumn;
    }

    public CheckBoxColumn<T> newCheckBoxColumn(String property) {
        return new CheckBoxColumn<T>(property);
    }

    public CheckBoxColumn<T> newCheckBoxColumn(String title, String property) {
        CheckBoxColumn<T> checkBoxColumn = newCheckBoxColumn(property);
        checkBoxColumn.getHeaderModel().setObject(title);
        return checkBoxColumn;
    }


    public void setCssProviderHeaderModel(CssProviderHeaderModel cssProviderHeaderModel) {
        this.cssProviderHeaderModel = cssProviderHeaderModel;
    }

    static class DecimalConverter extends BigDecimalConverter {

        @Override
        public String convertToString(BigDecimal value, Locale locale) {
            return value.stripTrailingZeros().toPlainString();
        }
    }

    private class DateConverter implements IConverter<Date> {

        private DateFormat dateFormat;

        public DateConverter(DateFormat dateFormat) {
            this.dateFormat = dateFormat;
        }

        @Override
        public Date convertToObject(String s, Locale locale) {
            try {
                return dateFormat.parse(s);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String convertToString(Date date, Locale locale) {
            return dateFormat.format(date);
        }
    }

}
