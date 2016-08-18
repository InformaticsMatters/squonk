package org.squonk.dataset.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by timbo on 09/08/2016.
 */
class PotionParser {

    private String potion;
    private Map<String, Class> fieldDefs;
    private List<Message> messages = new ArrayList<>();
    int errorCount = 0;
    int warningCount = 0;
    int infoCount = 0;
    private String[] lines;
    private int currentLineNumber;
    private String currentLineTotal;
    private String currentLineRemaining;
    private AbstractTransform currentTransform;
    private List<AbstractTransform> transforms;

    PotionParser(String potion, Map<String, Class> fieldDefs) {
        this.potion = potion;
        this.fieldDefs = fieldDefs;
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
    private static Pattern FIELD_ACTIONS = Pattern.compile("\\s*(=|delete|integer|float|double|string|text|molecule|rename|replace)\\s*(.*)");
    private static Pattern FIELD_NAME = Pattern.compile("\\s*((\\w+)|'([^']+)'|\"([^\"]+)\")\\s*(.*)");
    private static Pattern IF = Pattern.compile("\\s*IF\\s*(.*)");
    private static Pattern ASSIGNMENT_IF = Pattern.compile("\\s*(.*)\\s+IF\\s+(.*)");
    private static Pattern TOKEN = Pattern.compile("\\s*(\\w+)\\s*(.*)");

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
            if (fieldDefs.keySet().contains(field)) {
                if (readFieldAction(field)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                addError("unexpected term: " + field + " - neither an action or a field name");
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
                if (remainder == null || remainder.length() > 0) {
                    addError("assign action must have further arguments");
                    return false;
                }
                m = ASSIGNMENT_IF.matcher(remainder);
                if (m.matches()) {
                    transforms.add(new AssignValueTransform(fieldName, m.group(1), m.group(2)));
                } else {
                    transforms.add(new AssignValueTransform(fieldName, remainder, null));
                }
            } else if ("delete".equals(action)) {
                if (remainder != null && remainder.length() > 0) {
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
                }
            } else if ("integer".equals(action)) {
                if (remainder != null && remainder.length() > 0) {
                    addError("convert to integer action cannot have further arguments");
                    return false;
                }
                transforms.add(new ConvertFieldTransform(fieldName, Integer.class));
            } else if ("float".equals(action)) {
                if (remainder != null && remainder.length() > 0) {
                    // TODO - handle precision
                    addError("convert to float action cannot have further arguments");
                    return false;
                }
                transforms.add(new ConvertFieldTransform(fieldName, Float.class));
            } else if ("double".equals(action)) {
                if (remainder != null && remainder.length() > 0) {
                    // TODO - handle precision
                    addError("convert to double action cannot have further arguments");
                    return false;
                }
                transforms.add(new ConvertFieldTransform(fieldName, Double.class));
            } else if ("string".equals(action) || "text".equals(action)) {
                if (remainder != null && remainder.length() > 0) {
                    addError("convert to string action cannot have further arguments");
                    return false;
                }
                transforms.add(new ConvertFieldTransform(fieldName, String.class));
            } else if ("molecule".equals(action)) {
                if (remainder == null || remainder.length() == 0) {
                    addError("molecule action must specify the structure format");
                    return false;
                }
                String format = readToken();
                if (format != null && format.length() > 0) {
                    if (currentLineRemaining == null || currentLineRemaining.length() ==0) {
                        transforms.add(new ConvertToMoleculeTransform(fieldName, format));
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
                if (remainder == null || remainder.length() == 0) {
                    addError("rename action must specify new field name");
                    return false;
                }
                String newName = readFieldName();
                if (newName != null && newName.length() > 0) {
                    if (currentLineRemaining == null || currentLineRemaining.length() ==0) {
                        transforms.add(new RenameFieldTransform(fieldName, newName));
                    } else {
                        addError("unexpected extra content for rename field");
                        return false;
                    }
                } else {
                    addError("invalid new field name");
                    return false;
                }
            } else if ("replace".equals(action)) {

            }

            return true;
        }
        return false;
    }


    private void addError(String message) {
        messages.add(new Message("ERROR on line " + currentLineNumber + ": " + message, Message.Severity.Error));
        errorCount++;
    }

    private void addWarning(String message) {
        messages.add(new Message("WARNING on line " + currentLineNumber + ": " + message, Message.Severity.Warning));
        warningCount++;
    }
}
