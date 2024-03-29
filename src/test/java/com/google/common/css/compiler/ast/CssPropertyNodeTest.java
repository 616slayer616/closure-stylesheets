/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.css.compiler.ast;

import com.google.common.css.SourceCode;
import com.google.common.css.SourceCodeLocation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CssPropertyNode}.
 *
 * @author oana@google.com (Oana Florescu)
 */
class CssPropertyNodeTest {

    @Test
    void testPropertyNodeCreation1() {
        CssPropertyNode property = new CssPropertyNode("color", null);

        assertThat(property.getParent()).isNull();
        assertThat(property.getSourceCodeLocation()).isNull();
        assertThat(property.isCustom()).isFalse();
        assertThat(property).hasToString("color");
        assertThat(property.getProperty().hasPositionalParameters()).isFalse();
    }

    @Test
    void testPropertyNodeCreation2() {
        CssPropertyNode property = new CssPropertyNode("cOloR", null);

        assertThat(property.getParent()).isNull();
        assertThat(property.getSourceCodeLocation()).isNull();
        assertThat(property.isCustom()).isFalse();
        assertThat(property).hasToString("color");
        assertThat(property.getProperty().hasPositionalParameters()).isFalse();
    }

    @Test
    void testPropertyNodeCreation3() {
        SourceCodeLocation codeLoc = new SourceCodeLocation(
                new SourceCode("file.css", null), 1, 1, 1, 1, 1, 1);
        CssPropertyNode property = new CssPropertyNode("color", codeLoc);

        assertThat(property.getParent()).isNull();
        assertThat(property.getSourceCodeLocation()).isEqualTo(codeLoc);
        assertThat(property.isCustom()).isFalse();
        assertThat(property).hasToString("color");
        assertThat(property.getProperty().hasPositionalParameters()).isFalse();
    }

    @Test
    void testCustomPropertyNodeCreation() {
        CssPropertyNode property = new CssPropertyNode("--cOloR", null);
        assertThat(property.getParent()).isNull();
        assertThat(property.getSourceCodeLocation()).isNull();
        assertThat(property.isCustom()).isTrue();
        assertThat(property).hasToString("--cOloR");
        assertThat(property.getProperty().hasPositionalParameters()).isFalse();
    }

    @Test
    void testPropertyNodePositionDependentValues() {
        CssPropertyNode borderColor = new CssPropertyNode("border-color", null);
        CssPropertyNode borderStyle = new CssPropertyNode("border-style", null);
        CssPropertyNode borderWidth = new CssPropertyNode("border-width", null);
        CssPropertyNode margin = new CssPropertyNode("margin", null);
        CssPropertyNode padding = new CssPropertyNode("padding", null);

        assertThat(borderColor).hasToString("border-color");
        assertThat(borderColor.getProperty().hasPositionalParameters()).isTrue();

        assertThat(borderStyle).hasToString("border-style");
        assertThat(borderStyle.getProperty().hasPositionalParameters()).isTrue();

        assertThat(borderWidth).hasToString("border-width");
        assertThat(borderWidth.getProperty().hasPositionalParameters()).isTrue();

        assertThat(margin).hasToString("margin");
        assertThat(margin.getProperty().hasPositionalParameters()).isTrue();

        assertThat(padding).hasToString("padding");
        assertThat(padding.getProperty().hasPositionalParameters()).isTrue();
    }

    @Test
    void testPropertyNodeShorthands() {
        assertThat(new CssPropertyNode("foo").getProperty().getShorthands()).isEmpty();

        assertThat(new CssPropertyNode("color").getProperty().getShorthands()).isEmpty();

        assertThat(new CssPropertyNode("caption-side").getProperty().getShorthands()).isEmpty();

        assertThat(new CssPropertyNode("border-collapse").getProperty().getShorthands()).isEmpty();

        assertThat(new CssPropertyNode("background-color").getProperty().getShorthands())
                .containsExactly("background");

        assertThat(new CssPropertyNode("border-color").getProperty().getShorthands())
                .containsExactly("border");

        assertThat(new CssPropertyNode("list-style-type").getProperty().getShorthands())
                .containsExactly("list-style");

        assertThat(new CssPropertyNode("border-left-style").getProperty().getShorthands())
                .containsExactly("border", "border-left", "border-style");
    }

    @Test
    void testPropertyNodePartition() {
        assertThat(new CssPropertyNode("foo").getPartition()).isEqualTo("foo");

        assertThat(new CssPropertyNode("color").getPartition()).isEqualTo("color");

        assertThat(new CssPropertyNode("caption-side").getPartition()).isEqualTo("caption-side");

        assertThat(new CssPropertyNode("border-collapse").getPartition()).isEqualTo("border-collapse");

        assertThat(new CssPropertyNode("background-color").getPartition()).isEqualTo("background");

        assertThat(new CssPropertyNode("border-color").getPartition()).isEqualTo("border");

        assertThat(new CssPropertyNode("list-style-type").getPartition()).isEqualTo("list-style");

        assertThat(new CssPropertyNode("border-left-style").getPartition()).isEqualTo("border");
    }

    @Test
    void testPropertyNodeCopy() {
        CssPropertyNode property = new CssPropertyNode("color", null);
        CssPropertyNode propertyCopy = new CssPropertyNode(property);
        CssPropertyNode property1 = new CssPropertyNode("border-color", null);
        CssPropertyNode property1Copy = new CssPropertyNode(property1);
        CssPropertyNode property2 = new CssPropertyNode("--theme-color", null);
        CssPropertyNode property2Copy = new CssPropertyNode(property2);

        assertThat(property.getParent()).isNull();
        assertThat(propertyCopy.getParent()).isNull();
        assertThat(property1.getParent()).isNull();
        assertThat(property1Copy.getParent()).isNull();
        assertThat(property2.getParent()).isNull();
        assertThat(property2Copy.getParent()).isNull();

        assertThat(property.getSourceCodeLocation()).isNull();
        assertThat(propertyCopy.getSourceCodeLocation()).isNull();
        assertThat(property1.getSourceCodeLocation()).isNull();
        assertThat(property1Copy.getSourceCodeLocation()).isNull();
        assertThat(property2.getSourceCodeLocation()).isNull();
        assertThat(property2Copy.getSourceCodeLocation()).isNull();

        assertThat(property).hasToString("color");
        assertThat(propertyCopy).hasToString("color");
        assertThat(property1).hasToString("border-color");
        assertThat(property1Copy).hasToString("border-color");
        assertThat(property2).hasToString("--theme-color");
        assertThat(property2Copy).hasToString("--theme-color");

        assertThat(property.getProperty().isRecognizedProperty()).isTrue();
        assertThat(propertyCopy.getProperty().isRecognizedProperty()).isTrue();
        assertThat(property1.getProperty().isRecognizedProperty()).isTrue();
        assertThat(property1Copy.getProperty().isRecognizedProperty()).isTrue();
        assertThat(property2.getProperty().isRecognizedProperty()).isFalse();
        assertThat(property2Copy.getProperty().isRecognizedProperty()).isFalse();

        assertThat(property.getProperty().hasPositionalParameters()).isFalse();
        assertThat(propertyCopy.getProperty().hasPositionalParameters()).isFalse();
        assertThat(property1.getProperty().hasPositionalParameters()).isTrue();
        assertThat(property1Copy.getProperty().hasPositionalParameters()).isTrue();
        assertThat(property2.getProperty().hasPositionalParameters()).isFalse();
        assertThat(property2Copy.getProperty().hasPositionalParameters()).isFalse();

        assertThat(property.isCustom()).isFalse();
        assertThat(propertyCopy.isCustom()).isFalse();
        assertThat(property1.isCustom()).isFalse();
        assertThat(property1Copy.isCustom()).isFalse();
        assertThat(property2.isCustom()).isTrue();
        assertThat(property2Copy.isCustom()).isTrue();
    }

}
