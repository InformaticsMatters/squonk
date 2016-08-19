package org.squonk.types;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a numeric value which can have a qualifier (modifier). Examples
 * would be 17, &lt;22, &gt;44, 5. A critical aspect of this is how to sort and
 * filter. If the above examples are sorted in ascending order are the results
 * 5, 17, &lt;22, &gt;45, or &lt;22, 5, 17, &gt;45. The answer is a little
 * subjective (and so may need to be configurable), but generally speaking the
 * first answer is probably the right one.
 *
 */
public class QualifiedValue<T extends Number> implements Comparable<T>, Serializable {

    private static final Pattern patt = Pattern.compile("\\s*(=|<|>|~|<=|>=)?\\s*(\\-?[0-9,\\.]+)\\s*");

    public enum Qualifier {

        EQUALS("="),
        APPROX_EQUALS("~"),
        LESS_THAN("<"),
        GREATER_THAN(">"),
        LESS_THAN_OR_EQUALS("<="),
        GREATER_THAN_OR_EQUALS(">=");

        private final String symbol;

        Qualifier(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }

        public static Qualifier create(String symbol) {
            for (Qualifier q : Qualifier.values()) {
                if (q.symbol.equals(symbol)) {
                    return q;
                }
            }
            throw new IllegalArgumentException("Qualifier symbol " + symbol + " not supported");
        }
    }

    private final T value;
    private final Qualifier qualifier;

    QualifiedValue(
            @JsonProperty("value") String value,
            @JsonProperty("qualifier") Qualifier qualifier,
            @JsonProperty("type") Class<T> type) {
        this.value = convertFromText(value, type);
        this.qualifier = qualifier;
    }

    QualifiedValue(T v) {
        this.value = v;
        this.qualifier = Qualifier.EQUALS;
    }

    public QualifiedValue(T v, Qualifier q) {
        this.value = v;
        this.qualifier = q;
    }

    public QualifiedValue(T v, String q) {
        this.value = v;
        this.qualifier = Qualifier.create(q);
    }

    public QualifiedValue(QualifiedValue<T> qv) {
        this.value = qv.value;
        this.qualifier = qv.qualifier;
    }

    /** Generate a QualifiedValue of the specified type. If the value is of the specified type then
     * the QualifiedValue is created directly from this value (using EQUALS as qualifier), else the
     * QualifiedValue is created from the toString() representation of the value (usually this would
     * be a String) using the static parse() method.
     *
     * @param value
     * @param type
     */
    public QualifiedValue(Object value, Class<T> type) {
        if (value.getClass() == type) {
            this.value = (T)value;
            this.qualifier = Qualifier.EQUALS;
        } else {
            QualifiedValue<T> q = parse(value.toString(), type);
            this.value = q.getValue();
            this.qualifier = q.getQualifier();
        }
    }

    public T getValue() {
        return value;
    }

    public Qualifier getQualifier() {
        return qualifier;
    }

    public Class getType() {
        return value.getClass();
    }

    /**
     * Parse from a text representations. Values like this are supported: 123.4,
     * <123.4, < 123.4, <-123.4, >=15.2, 1234, -0.00001234. Integer, Float and
     * Double types are supported. If your format does not match this format
     * then parse it yourself and use one of the constructors.
     *
     * @param s The text to parse
     * @param cls the type to parse the number part to
     * @return The value
     */
    public static <Q extends Number> QualifiedValue<Q> parse(String s, Class<Q> cls) {
        Matcher matcher = patt.matcher(s);
        if (matcher.matches()) {
            String q = matcher.group(1);
            String v = matcher.group(2);
            if (v != null) {
                Q num = convertFromText(v, cls);
                Qualifier qual = null;
                if (q != null) {
                    qual = Qualifier.create(q);
                }
                return new QualifiedValue(num, qual == null ? Qualifier.EQUALS : qual);
            }
        }
        throw new IllegalArgumentException("Format " + s + " not supported");
    }

    private static <Q> Q convertFromText(String val, Class<Q> cls) {
        Number num = null;
        if (cls == Float.class) {
            num = new Float(val);
        } else if (cls == Integer.class) {
            num = new Integer(val);
        } else if (cls == Double.class) {
            num = new Double(val);
        } else {
            throw new IllegalArgumentException("Type " + cls.getName() + " not supported");
        }
        return (Q) num;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof QualifiedValue)) {
            return false;
        }
        QualifiedValue q = (QualifiedValue) o;
        return value.equals(q.getValue()) && qualifier.equals(q.getQualifier());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.value);
        hash = 41 * hash + Objects.hashCode(this.qualifier);
        return hash;
    }

    @Override
    public int compareTo(T o) {
        return ((Comparable) value).compareTo(o);
    }

    @Override
    public String toString() {
        if (qualifier == Qualifier.EQUALS) {
            return "" + value;
        } else {
            return "" + qualifier.symbol + value;
        }
    }
}
