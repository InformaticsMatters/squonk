package org.squonk.dataset.transform;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author timbo
 */
public class TransformDefinitions {

    private final List<AbstractTransform> transforms;

    public TransformDefinitions(@JsonProperty("transforms") List<AbstractTransform> transforms) {
        this.transforms = transforms;
    }

    public TransformDefinitions() {
        transforms = new ArrayList<>();
    }

    public TransformDefinitions deleteField(String fieldName) {
        transforms.add(new DeleteFieldTransform(fieldName));
        return this;
    }
    
    public TransformDefinitions renameField(String fieldName, String newName) {
        transforms.add(new RenameFieldTransform(fieldName, newName));
        return this;
    }
    
    public TransformDefinitions convertField(String fieldName, Class type) {
        transforms.add(new ConvertFieldTransform(fieldName, type));
        return this;
    }

    public TransformDefinitions convertField(String fieldName, Class type, Class genericType) {
        transforms.add(new ConvertFieldTransform(fieldName, type, genericType));
        return this;
    }

    public TransformDefinitions transformValue(String fieldName, String match, String result) {
        transforms.add(new TransformValueTransform(fieldName, match, result));
        return this;
    }


    public List<AbstractTransform> getTransforms() {
        return transforms;
    }

    public static void main(String[] args) throws Exception {
        TransformDefinitions o = new TransformDefinitions();
        o.deleteField("foo").renameField("bar", "baz");
        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writeValueAsString(o);
        System.out.println("JSON: " + json);

        TransformDefinitions b = mapper.readValue(json, TransformDefinitions.class);
        System.out.println("OBJ:  " + b);
        System.out.println("SIZE: " + b.getTransforms().size());
        
        for (AbstractTransform t: b.getTransforms()) {
            System.out.println("T: " + t);
        }   
    }

}
