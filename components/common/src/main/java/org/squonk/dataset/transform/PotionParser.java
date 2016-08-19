package org.squonk.dataset.transform;

import org.squonk.types.QualifiedValue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by timbo on 09/08/2016.
 */
public class PotionParser {

    private String potion;
    private final Map<String, Class> fieldDefs = new LinkedHashMap();
    private List<Message> messages = new ArrayList<>();
    int errorCount = 0;
    int warningCount = 0;
    int infoCount = 0;
    private String[] lines;
    private int currentLineNumber;
    private String currentLineTotal;
    private String currentLineRemaining;
    private List<AbstractTransform> transforms;

    PotionParser(String potion, Map<String, Class> fieldDefs) {
        this.potion = potion;
        if (fieldDefs != null) {
            this.fieldDefs.putAll(fieldDefs);
        }
    }

    public List<Message> getMessages() {
        return messages;
    }

    public List<AbstractTransform> getTransforms() {
        return transforms;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public int getInfoCount() {
        return infoCount;
    }

    boolean parse() {
        messages = new ArrayList<>();
        transforms = new ArrayList<>();
        splitLines(potion);
        currentLineNumber = 0;
        for (String line : lines) {
            // TODO - handle tab as line continuation
            currentLineNumber++;
            parseLine(line);
        }

        return errorCount == 0;
    }

    private void splitLines(String potion) {
        lines = potion.split("\\n");
    }

    private boolean parseLine(String line) {
        currentLineTotal = line.trim();
        currentLineRemaining = currentLineTotal;
        if (currentLineTotal.isEmpty()) { // empty line - ignore
            return true;
        } else if (currentLineTotal.startsWith("#")) { // comment line - ignore
            return true;
        }

        if (!readFieldOrRowAction()) {
            return false;
        }
        return true;
    }

    private static Pattern ROW_ACTIONS = Pattern.compile("\\s*(delete)\\s*(.*)");
    private static Pattern FIELD_ACTIONS = Pattern.compile("\\s*(=|delete|integer|float|double|string|text|qvalue|molecule|rename|replace)\\s*(.*)");
    private static Pattern FIELD_NAME = Pattern.compile("\\s*((\\w+)|'([^']+)'|\"([^\"]+)\")\\s*(.*)");
    private static Pattern IF = Pattern.compile("\\s*IF\\s+(.*)");
    private static Pattern IF_ONERROR = Pattern.compile("\\s*(.*)\\s+ONERROR\\s+(fail|continue)\\s*");
    private static Pattern ONERROR = Pattern.compile("\\s*ONERROR\\s+(fail|continue)\\s*(.*)");
    private static Pattern ASSIGNMENT_IF = Pattern.compile("\\s*(.*)\\s+IF\\s+(.*)");
    private static Pattern TOKEN = Pattern.compile("\\s*(\\S+)\\s*(.*)");

    private String readOnError() {
        Matcher m = ONERROR.matcher(currentLineRemaining);
        if (m.matches()) {
            currentLineRemaining = m.group(2);
            return m.group(1);
        }
        return null;
    }

    private boolean readFieldOrRowAction() {

        Matcher m = ROW_ACTIONS.matcher(currentLineRemaining);
        if (m.matches()) {
            String action = m.group(1);
            String remainder = m.group(2);
            currentLineRemaining = remainder;
            if ("delete".equals(action)) {
                String condition = null;
                if (remainder != null && remainder.length() > 0) {
                    m = IF.matcher(remainder);
                    if (m.matches()) {
                        condition = m.group(1);
                    } else {
                        addError("expected an IF clause but found:" + ": '" + remainder);
                        return false;
                    }
                }
                transforms.add(new DeleteRowTransform(condition));
                currentLineRemaining = "";
                return true;
            }
        }

        String field = readFieldName();
        if (field != null) {
            if (readFieldAction(field)) {
                return true;
            } else {
                return false;
            }
        }

        addError("failed to read line: " + currentLineTotal);

        return false;
    }

    private String readFieldName() {
        Matcher m = FIELD_NAME.matcher(currentLineRemaining);
        if (m.matches()) {
            currentLineRemaining = m.group(5);
            if (m.group(2) != null) { // plain field name
                return m.group(2);
            } else if (m.group(3) != null) { // single quotes
                return m.group(3);
            } else if (m.group(4) != null) { // double quotes
                return m.group(4);
            }
        }
        return null;
    }

    private String readToken() {
        Matcher m = TOKEN.matcher(currentLineRemaining);
        if (m.matches()) {
            currentLineRemaining = m.group(2);
            return m.group(1);
        }
        return null;
    }

    private boolean readFieldAction(String fieldName) {
        Matcher m = FIELD_ACTIONS.matcher(currentLineRemaining);
        if (m.matches()) {
            String action = m.group(1);
            String remainder = m.group(2);
            currentLineRemaining = remainder;

            if ("=".equals(action)) {
                if (!hasMoreContent()) {
                    addError("assign action must have further arguments");
                    return false;
                }
                String preError = null;
                String onError = null;
                m = IF_ONERROR.matcher(remainder);
                if (m.matches()) {
                    preError = m.group(1);
                    onError = m.group(2);
                } else {
                    preError = remainder;
                }

                m = ASSIGNMENT_IF.matcher(preError);
                if (m.matches()) {
                    transforms.add(new AssignValueTransform(fieldName, m.group(1), m.group(2), onError));
                } else {
                    transforms.add(new AssignValueTransform(fieldName, preError, null, onError));
                }
                fieldDefs.put(fieldName, null); // we don't know what type it is
                return true;
            }

            if (!fieldDefs.containsKey(fieldName)) {
                addError("field " + fieldName + " does not exist");
                return false;
            }
            if ("delete".equals(action)) {
                if (hasMoreContent()) {
                    m = IF.matcher(remainder);
                    if (m.matches()) {
                        String condition = m.group(1);
                        transforms.add(new DeleteFieldTransform(fieldName, condition));
                    } else {
                        addError("expected an IF clause but found:" + ": '" + remainder);
                        return false;
                    }
                } else {
                    transforms.add(new DeleteFieldTransform(fieldName, null));
                    fieldDefs.remove(fieldName);
                }
            } else if ("integer".equals(action)) {
                String onError = readOnError();
                if (hasMoreContent()) {
                    addError("unexpected additional content for integer action");
                    return false;
                }
                transforms.add(new ConvertFieldTransform(fieldName, Integer.class, null, onError));
                fieldDefs.put(fieldName, Integer.class);
            } else if ("float".equals(action)) {
                String onError = readOnError();
                if (hasMoreContent()) {
                    addError("unexpected additional content for float action");
                    return false;
                }
                transforms.add(new ConvertFieldTransform(fieldName, Float.class, null, onError));
                fieldDefs.put(fieldName, Float.class);
            } else if ("double".equals(action)) {
                String onError = readOnError();
                if (hasMoreContent()) {
                    addError("unexpected additional content for double action");
                    return false;
                }
                transforms.add(new ConvertFieldTransform(fieldName, Double.class, null, onError));
                fieldDefs.put(fieldName, Double.class);
            } else if ("string".equals(action) || "text".equals(action)) {
                String onError = readOnError();
                if (hasMoreContent()) {
                    addError("unexpected additional content for string/text action");
                    return false;
                }
                transforms.add(new ConvertFieldTransform(fieldName, String.class, null, onError));
                fieldDefs.put(fieldName, String.class);
            } else if ("qvalue".equals(action)) {
                String onError = readOnError();
                if (onError != null) {
                    transforms.add(new ConvertFieldTransform(fieldName, QualifiedValue.class, Float.class, onError));
                } else {
                    String format = readToken();
                    if (format == null || format.isEmpty()) {
                        transforms.add(new ConvertFieldTransform(fieldName, QualifiedValue.class, Float.class, null));
                    } else {
                        onError = readOnError();
                        if ("float".equals(format)) {
                            transforms.add(new ConvertFieldTransform(fieldName, QualifiedValue.class, Float.class, onError));
                        } else if ("double".equals(format)) {
                            transforms.add(new ConvertFieldTransform(fieldName, QualifiedValue.class, Double.class, onError));
                        } else if ("integer".equals(format)) {
                            transforms.add(new ConvertFieldTransform(fieldName, QualifiedValue.class, Integer.class, onError));
                        } else {
                            addError("unexpected number type for qvalue: " + format);
                            return false;
                        }
                    }
                }
                fieldDefs.put(fieldName, QualifiedValue.class);
            } else if ("molecule".equals(action)) {
                if (!hasMoreContent()) {
                    addError("molecule action must specify the structure format");
                    return false;
                }
                String format = readToken();
                if (format != null && format.length() > 0) {
                    if (!hasMoreContent()) {
                        transforms.add(new ConvertToMoleculeTransform(fieldName, format));
                        fieldDefs.remove(fieldName);
                    } else {
                        addError("unexpected extra content for molecule action");
                        return false;
                    }
                } else {
                    addError("invalid structure format");
                    return false;
                }
                return true;
            } else if ("rename".equals(action)) {
                if (!hasMoreContent()) {
                    addError("rename action must specify new field name");
                    return false;
                }
                String newName = readFieldName();
                if (newName != null && newName.length() > 0) {
                    if (currentLineRemaining == null || currentLineRemaining.length() == 0) {
                        transforms.add(new RenameFieldTransform(fieldName, newName));
                        Class type = fieldDefs.remove(fieldName);
                        fieldDefs.put(newName, type);
                    } else {
                        addError("unexpected extra content for rename field");
                        return false;
                    }
                } else {
                    addError("invalid new field name");
                    return false;
                }
            } else if ("replace".equals(action)) {
                Class type = fieldDefs.get(fieldName);
                if (type == null) {
                    addWarning("cannot determine data type of field " + fieldName + " - assuming String");
                    type = String.class;
                }
                // TODO - allow the tokens to be quoted (single or double quotes)
                String from = readToken();
                if (from == null) {
                    addError("replace action must specify value to match and a replacement value");
                    return false;
                }
                String to = readToken();
                if (to == null) {
                    addError("replace action must specify a replacement value");
                    return false;
                }
                Object oFrom = null;
                Object oTo = null;
                try {
                    oFrom = convert(from, type);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    addError("unable to convert " + from + " to " + type.getSimpleName());
                    return false;
                }
                try {
                    oTo = convert(to, type);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    addError("unable to convert " + to + " to " + type.getSimpleName());
                    return false;
                }

                transforms.add(new ReplaceValueTransform(fieldName, oFrom, oTo));
            }

            return true;
        }
        return false;
    }

    boolean hasMoreContent() {
        return currentLineRemaining != null && currentLineRemaining.length() > 0;
    }


    private void addError(String message) {
        messages.add(new Message("ERROR on line " + currentLineNumber + ": " + message, Message.Severity.Error));
        errorCount++;
    }

    private void addWarning(String message) {
        messages.add(new Message("WARNING on line " + currentLineNumber + ": " + message, Message.Severity.Warning));
        warningCount++;
    }

    static private <C> C convert(String value, Class<C> type) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor c = type.getConstructor(String.class);
        return (C)c.newInstance(value);
    }
}
