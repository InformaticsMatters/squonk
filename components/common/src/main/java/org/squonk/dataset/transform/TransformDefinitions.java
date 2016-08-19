package org.squonk.dataset.transform;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author timbo
 */
public class TransformDefinitions {

    private final List<AbstractTransform> transforms = new ArrayList<>();
    private final List<Message> messages = new ArrayList();

    public TransformDefinitions(@JsonProperty("transforms") List<AbstractTransform> transforms) {
        this.transforms.addAll(transforms);
    }

    public TransformDefinitions() {

    }


    public List<Message> getMessages() {
        return messages;
    }

    /**
     * Parse a potion in String form.  Support language as follows:
     * <p>
     * <pre>
     * # a comment
     *
     * # row operations
     * delete
     * delete IF <predicate>
     *
     * # field operations
     * <field> delete
     * <field> delete IF <predicate>
     *
     * # rename field
     * <field> rename <new name>
     *
     * # value replacement
     * <field> replace <from value> <to value>
     *
     * # data type conversions
     * <field> integer
     * <field> integer ONERROR (fail|continue)
     * <field> float
     * <field> float ONERROR (fail|continue)
     * <field> double
     * <field> double ONERROR (fail|continue)
     * <field> (string|text)
     * <field> (string|text) ONERROR (fail|continue)
     * <field> qvalue (float|double|integer)
     * <field> qvalue (float|double|integer) ONERROR (fail|continue)
     *
     * # convert BasicObject to MoleculeObject
     * <field> molecule <format>
     *
     * # Assignment
     * <field> = <expression>
     * <field> = <expression> IF <predicate>
     * <field> = <expression> IF <predicate> ONERROR (fail|continue)
     *
     * </pre>
     *
     * Before executing you must check for errors.
     *
     * @param potion    The potion to parse
     * @param fieldDefs Field definitions that allow the potion to better validated.
     * @return The PotionParser from which the Transforms and/or info, warnings and errors can be retrieved.
     */
    public static PotionParser parse(String potion, Map<String, Class> fieldDefs) {

        PotionParser p = new PotionParser(potion, fieldDefs);
        p.parse();

        return p;
    }

    public TransformDefinitions deleteField(String fieldName, String condition) {
        transforms.add(new DeleteFieldTransform(fieldName, condition));
        return this;
    }

    public TransformDefinitions deleteField(String fieldName) {
        return deleteField(fieldName, null);
    }

    public TransformDefinitions renameField(String fieldName, String newName) {
        transforms.add(new RenameFieldTransform(fieldName, newName));
        return this;
    }

    public TransformDefinitions convertField(String fieldName, Class type) {
        transforms.add(new ConvertFieldTransform(fieldName, type));
        return this;
    }

    public TransformDefinitions convertField(String fieldName, Class type, Class genericType, String onError) {
        transforms.add(new ConvertFieldTransform(fieldName, type, genericType, onError));
        return this;
    }

    public TransformDefinitions replaceValue(String fieldName, String match, String result) {
        transforms.add(new ReplaceValueTransform(fieldName, match, result));
        return this;
    }

    /**
     * @deprecated use replaceValue()
     */
    @Deprecated
    public TransformDefinitions transformValue(String fieldName, String match, String result) {
        return replaceValue(fieldName, match, result);
    }

    public TransformDefinitions assignValue(String fieldName, String expression, String condition, String onError) {
        transforms.add(new AssignValueTransform(fieldName, expression, condition, onError));
        return this;
    }

    public List<AbstractTransform> getTransforms() {
        return transforms;
    }

    public static void main(String[] args) throws Exception {
        TransformDefinitions o = new TransformDefinitions();
        o.deleteField("foo", null).renameField("bar", "baz");
        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writeValueAsString(o);
        System.out.println("JSON: " + json);

        TransformDefinitions b = mapper.readValue(json, TransformDefinitions.class);
        System.out.println("OBJ:  " + b);
        System.out.println("SIZE: " + b.getTransforms().size());

        for (AbstractTransform t : b.getTransforms()) {
            System.out.println("T: " + t);
        }
    }

}
