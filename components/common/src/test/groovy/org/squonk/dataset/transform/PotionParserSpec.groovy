/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.dataset.transform

import org.squonk.types.QualifiedValue
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

    void "convert to integer action with onError"() {

        def potion = "foo integer ONERROR continue"
        PotionParser p = new PotionParser(potion, [foo:String.class])

        when:
        p.parse()
        def transforms = p.transforms

        then:
        transforms.size() == 1
        transforms[0].class == ConvertFieldTransform
        transforms[0].fieldName == 'foo'
        transforms[0].newType == Integer.class
        transforms[0].onError == 'continue'
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

    void "convert to qvalue action"() {

        PotionParser p = new PotionParser(potion, [foo:String.class])
        p.parse()
        def transforms = p.transforms

        expect:
        transforms.size() == 1
        transforms[0].class == ConvertFieldTransform
        transforms[0].newType == QualifiedValue.class
        transforms[0].genericType == cls
        transforms[0].onError == onError


        where:
        potion                            | cls           | onError
        "foo qvalue"                      | Float.class   | null
        "foo qvalue float"                | Float.class   | null
        "foo qvalue double"               | Double.class  | null
        "foo qvalue integer"              | Integer.class | null
        "foo qvalue ONERROR fail"         | Float.class   | "fail"
        "foo qvalue   ONERROR   fail  "   | Float.class   | "fail"
        "foo qvalue float ONERROR fail"   | Float.class   | "fail"
        "foo qvalue double ONERROR fail"  | Double.class  | "fail"
        "foo qvalue integer ONERROR fail" | Integer.class | "fail"
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

    void "convert to molecule action"() {

        def potion = "foo molecule smiles"
        PotionParser p = new PotionParser(potion, [foo:String.class])

        when:
        p.parse()
        def transforms = p.transforms

        then:
        transforms.size() == 1
        transforms[0].class == ConvertToMoleculeTransform.class
        transforms[0].structureFieldName == 'foo'
        transforms[0].structureFormat == 'smiles'
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

    void "assign action"() {

        PotionParser p = new PotionParser(potion, [bar:String.class])
        p.parse()
        def transforms = p.transforms

        expect:
        transforms.size() == 1
        transforms[0].class == AssignValueTransform
        transforms[0].expression == expr
        transforms[0].condition == condition
        transforms[0].onError == onError

        where:
        expr | condition | onError | potion
        'bar'| null      | null    | 'foo = bar'
        'bar'| 'xx'      | null    | 'foo = bar IF xx'
        'bar'| 'xx yy'   | 'fail'  | 'foo = bar IF xx yy ONERROR fail'
        'bar'| null      | 'fail'  | 'foo = bar ONERROR fail'
    }

    void "replace value action"() {

        PotionParser p = new PotionParser(potion, [foo:cls])
        p.parse()
        def transforms = p.transforms


        expect:
        transforms.size() == 1
        transforms[0].class == ReplaceValueTransform
        transforms[0].fieldName == 'foo'
        transforms[0].match == match
        transforms[0].result == result

        where:
        potion                      | cls           | match    | result
        "foo replace hello goodbye" | String.class  | "hello"  | "goodbye"
        "foo replace 1 2"           | Integer.class | 1i       | 2i
        "foo replace 1.1 2.1"       | Float.class   | 1.1f     | 2.1f
        "foo replace 1.1 2.1"       | Double.class  | 1.1d     | 2.1d
    }

}
