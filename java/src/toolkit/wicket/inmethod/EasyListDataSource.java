package toolkit.wicket.inmethod;

import com.inmethod.grid.IGridSortState;
import org.apache.wicket.ajax.AjaxRequestTarget;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author simetrias
 */
public abstract class EasyListDataSource<T extends Serializable> implements EasyDataSource.Delegate<T> {

    private List<T> data;
    private Class<T> type;

    public EasyListDataSource(Class<T> type) {
        this.type = type;
    }

    @Override
    public int getTotalCount() {
        if (data == null) {
            data = loadData();
        }
        return data.size();
    }

    @Override
    public Iterator<? extends T> getIterator(int fromIndex, int toIndex) {
        return data.subList(fromIndex, toIndex).iterator();
    }

    @Override
    public void onSortStateChanged(AjaxRequestTarget target, IGridSortState.ISortStateColumn sortStateColumn) {
        String propertyName = sortStateColumn.getPropertyName().toString();
        IGridSortState.Direction direction = sortStateColumn.getDirection();
        Collections.sort(data, new PropertyComparator<T>(propertyName, direction));
    }

    @Override
    public void resetData() {
        data = null;
    }

    public abstract List<T> loadData();

    class PropertyComparator<T> implements Comparator<T> {

        private final IGridSortState.Direction direction;
        private final Class<?> returnType;
        private final String[] propertyPath;

        PropertyComparator(String propertyName, IGridSortState.Direction direction) {
            this.direction = direction;
            try {
                this.propertyPath = buildPropertyPath(propertyName);
                this.returnType = resolveReturnType();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private String[] buildPropertyPath(String propertyName) {
            String[] split = propertyName.split("\\.");
            String[] path = new String[split.length];
            for (int i = 0; i < split.length; i++) {
                String segment = split[i];
                path[i] = "get" + segment.substring(0, 1).toUpperCase() + segment.substring(1);
            }
            return path;
        }

        private Class resolveReturnType() throws Exception {
            Class<?> holderClass = type;
            Method method = null;
            for (String name : propertyPath) {
                method = holderClass.getMethod(name);
                holderClass = method.getReturnType();
            }
            return method.getReturnType();
        }

        @Override
        public int compare(T t1, T t2) {
            try {
                int result = 0;

                Object object1Value = resolveValue(t1);
                Object object2Value = resolveValue(t2);

                if (returnType.equals(String.class)) {
                    String value1 = (String) object1Value;
                    String value2 = (String) object2Value;
                    if (value1 != null && value2 != null) {
                        result = direction == IGridSortState.Direction.ASC ? value1.compareTo(value2) : value2.compareTo(value1);
                    }
                } else if (returnType.equals(Date.class)) {
                    Date value1 = (Date) object1Value;
                    Date value2 = (Date) object2Value;
                    if (value1 != null && value2 != null) {
                        result = direction == IGridSortState.Direction.ASC ? value1.compareTo(value2) : value2.compareTo(value1);
                    }
                } else if (returnType.equals(BigDecimal.class)) {
                    BigDecimal value1 = (BigDecimal) object1Value;
                    BigDecimal value2 = (BigDecimal) object2Value;
                    if (value1 != null && value2 != null) {
                        result = direction == IGridSortState.Direction.ASC ? value1.compareTo(value2) : value2.compareTo(value1);
                    }
                } else if (returnType.equals(Integer.class)) {
                    Integer value1 = (Integer) object1Value;
                    Integer value2 = (Integer) object2Value;
                    if (value1 != null && value2 != null) {
                        result = direction == IGridSortState.Direction.ASC ? value1.compareTo(value2) : value2.compareTo(value1);
                    }
                } else if (returnType.equals(Enum.class)) {
                    String value1 = object1Value.toString();
                    String value2 = object2Value.toString();
                    if (value1 != null && value2 != null) {
                        result = direction == IGridSortState.Direction.ASC ? value1.compareTo(value2) : value2.compareTo(value1);
                    }
                }

                return result;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        Object resolveValue(Object object) throws Exception {
            Object value = object;
            Class<?> holderClass = object.getClass();
            for (String name : propertyPath) {
                Method method = holderClass.getMethod(name);
                value = method.invoke(value);
                if (value == null) {
                    break;
                } else {
                    holderClass = value.getClass();
                }
            }
            return value;
        }
    }
}
