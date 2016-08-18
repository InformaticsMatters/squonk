package org.squonk.dataset.transform

import spock.lang.Specification

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by timbo on 09/08/2016.
 */
class PotionParserSpec extends Specification {

    void "delete row action"() {

        def potion = "delete"
        PotionParser p = new PotionParser(potion, [:])

        when:
        p.parse()
        def transforms = p.transforms

        then:
        transforms.size() == 1
        transforms[0].class == DeleteRowTransform
        p.getMessages().size() == 0
    }

    void "ignore comment"() {

        def potion = "# a comment\ndelete\n# another comment"
        PotionParser p = new PotionParser(potion, [:])

        when:
        p.parse()
        def transforms = p.transforms

        then:
        transforms.size() == 1
        transforms[0].class == DeleteRowTransform
        p.getMessages().size() == 0
    }

    void "ignore empty line"() {

        def potion = "\ndelete\n"
        PotionParser p = new PotionParser(potion, [:])

        when:
        p.parse()
        def transforms = p.transforms

        then:
        transforms.size() == 1
        transforms[0].class == DeleteRowTransform
        p.getMessages().size() == 0
    }

    void "delete row action with condition"() {

        def potion = "delete IF foo == null"
        PotionParser p = new PotionParser(potion, [:])

        when:
        p.parse()
        def transforms = p.transforms

        then:
        transforms.size() == 1
        transforms[0].class == DeleteRowTransform
        transforms[0].condition == 'foo == null'
        p.getMessages().size() == 0
    }

    void "delete row action with invalid condition"() {

        def potion = "delete bla bla"
        PotionParser p = new PotionParser(potion, [:])

        when:
        p.parse()
        def transforms = p.transforms

        then:
        transforms.size() == 0
        p.messages.size() == 1
        p.messages[0].level == Message.Severity.Error
    }

    void "delete field action"() {

        def potion = "foo delete"
        PotionParser p = new PotionParser(potion, [foo:null])

        when:
        p.parse()
        def transforms = p.transforms

        then:
        transforms.size() == 1
        transforms[0].class == DeleteFieldTransform
        transforms[0].fieldName == 'foo'
        p.getMessages().size() == 0
    }

    void "field name single quotes"() {

        def potion = "'foo bar' delete"
        PotionParser p = new PotionParser(potion, ['foo bar':null])

        when:
        p.parse()
        def transforms = p.transforms

        then:
        transforms.size() == 1
        transforms[0].class == DeleteFieldTransform
        transforms[0].fieldName == 'foo bar'
        p.getMessages().size() == 0
    }

    void "field name double quotes"() {

        def potion = '"foo bar" delete'
        PotionParser p = new PotionParser(potion, ["foo bar":null])

        when:
        p.parse()
        def transforms = p.transforms

        then:
        transforms.size() == 1
        transforms[0].class == DeleteFieldTransform
        transforms[0].fieldName == 'foo bar'
        p.getMessages().size() == 0
    }

    void "field name mixed quotes"() {

        def potion = '"foo\' delete'
        PotionParser p = new PotionParser(potion, [foo:null])

        when:
        p.parse()

        then:
        p.getMessages().size() == 1
    }


    void "convert to integer action"() {

        def potion = "foo integer"
        PotionParser p = new PotionParser(potion, [foo:String.class])

        when:
        p.parse()
        def transforms = p.transforms

        then:
        transforms.size() == 1
        transforms[0].class == ConvertFieldTransform
        transforms[0].fieldName == 'foo'
        transforms[0].newType == Integer.class
        p.getMessages().size() == 0
    }

    void "convert to float action"() {

        def potion = "foo float"
        PotionParser p = new PotionParser(potion, [foo:String.class])

        when:
        p.parse()
        def transforms = p.transforms

        then:
        transforms.size() == 1
        transforms[0].class == ConvertFieldTransform
        transforms[0].fieldName == 'foo'
        transforms[0].newType == Float.class
        p.getMessages().size() == 0
    }

    void "convert to double action"() {

        def potion = "foo double"
        PotionParser p = new PotionParser(potion, [foo:String.class])

        when:
        p.parse()
        def transforms = p.transforms

        then:
        transforms.size() == 1
        transforms[0].class == ConvertFieldTransform
        transforms[0].fieldName == 'foo'
        transforms[0].newType == Double.class
        p.getMessages().size() == 0
    }

    void "convert to string action"() {

        def potion = "foo string"
        PotionParser p = new PotionParser(potion, [foo:String.class])

        when:
        p.parse()
        def transforms = p.transforms

        then:
        transforms.size() == 1
        transforms[0].class == ConvertFieldTransform
        transforms[0].fieldName == 'foo'
        transforms[0].newType == String.class
        p.getMessages().size() == 0
    }

    void "rename field action"() {

        def potion = "foo rename bar"
        PotionParser p = new PotionParser(potion, [foo:String.class])

        when:
        p.parse()
        def transforms = p.transforms

        then:
        transforms.size() == 1
        transforms[0].class == RenameFieldTransform
        transforms[0].fieldName == 'foo'
        transforms[0].newName == 'bar'
        p.getMessages().size() == 0
    }

    void "regexp"() {

        Pattern p = Pattern.compile("\\s*(.*)\\s+IF\\s+(.*)");

        when:
        Matcher m = p.matcher(' field2 * 2 IF field3 == null')

        then:
        m.matches()
        println m.group(1)
        println m.group(2)
        //println m.group(3)


    }

}
