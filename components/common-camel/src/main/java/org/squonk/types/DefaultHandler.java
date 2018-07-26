package org.squonk.types;

import org.squonk.api.HttpHandler;
import org.squonk.api.VariableHandler;
import org.squonk.io.SquonkDataSource;
import org.squonk.util.Utils;

import java.lang.reflect.Constructor;
import java.util.Map;

public abstract class DefaultHandler<T> implements HttpHandler<T>, VariableHandler<T> {

    protected final Class<T> type;
    protected final Class genericType;

    public DefaultHandler(Class<T> type, Class genericType) {
        this.type = type;
        this.genericType = genericType;
    }

    public DefaultHandler(Class<T> type) {
        this.type = type;
        this.genericType = null;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    public Class getGenericType() {
        return genericType;
    }


    /** Create the variable handled by this handler
     *
     * @param input An SquonkDataSource from which the value must be composed
     * @return The assembled value
     */
    public T create(SquonkDataSource input) throws Exception {
        Constructor<T> constructor;
        T value;
        // first look for constructor that specifies generic type and SquonkDataSource
        if (genericType != null) {
            value = Utils.instantiate(getType(), new Class[] {Class.class, SquonkDataSource.class}, new Object[] {genericType, input});
            if (value != null) {
                return value;
            }
        }
        // OK, we'll try a constructor with just an SquonkDataSource
        value = Utils.instantiate(getType(), new Class[] {SquonkDataSource.class}, new Object[] {input});
        if (value != null) {
            return value;
        }
        throw new IllegalStateException("No suitable constructor defined for creating instance of " + getType().getName());
    }


    /** Create an instance from one or more SquonkDataSource. The inputs are passed in as a Map with the key identifying the
     * type of input. Where there is only a single input the name is ignored and the
     * {@link #create(SquonkDataSource)} method is called with that one input.
     * Where there are multiple inputs the {@link #createMultiple(Map<String,SquonkDataSource>)} method is
     * called that MUST be overrided by any subclass wanting to handle multiple inputs. The overriding method should use
     * the names that are present as the keys to the Map to distinguish the different inputs.
     *
     * @param inputs multiple InputStreams where the key is a name that identifies the type of input
     * @return
     * @throws Exception
     */
    public T create(Map<String,SquonkDataSource> inputs) throws Exception {

        if (inputs == null || inputs.size() == 0) {
            throw new IllegalArgumentException("At least one input mut be defined");
        }

        if (inputs.size() == 1) {
            SquonkDataSource input = inputs.values().iterator().next();
            return create(input);
        } else {
            return createMultiple(inputs);
        }
    }

    public T createMultiple(Map<String,SquonkDataSource> inputs) throws Exception {
        throw new UnsupportedOperationException("Do not know how to create value from multiple inputs. Subclass must override this method to implement");
    }
}
