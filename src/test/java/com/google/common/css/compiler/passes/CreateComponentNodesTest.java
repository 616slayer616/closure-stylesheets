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

package com.google.common.css.compiler.passes;

import com.google.common.css.compiler.ast.CssComponentNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CreateComponentNodes}.
 */
class CreateComponentNodesTest extends NewFunctionalTestBase {

    @Override
    protected void runPass() {
        new CreateComponentNodes(tree.getMutatingVisitController(), errorManager).runPass();
    }

    @Test
    void testCreateComponentNode1() throws Exception {
        parseAndRun("@component CSS_X { @def X Y; }");
        assertThat(getFirstActualNode()).isInstanceOf(CssComponentNode.class);
        CssComponentNode comp = (CssComponentNode) getFirstActualNode();
        assertThat(comp.getName().getValue()).isEqualTo("CSS_X");
        assertThat(comp.getParentName()).isNull();
        assertThat(comp.isAbstract()).isFalse();
        assertThat(comp.getBlock()).hasToString("[@def[X, Y]]");
    }

    @Test
    void testCreateComponentNode2() throws Exception {
        parseAndRun("@abstract_component CSS_X { @def X Y; }");
        assertThat(getFirstActualNode()).isInstanceOf(CssComponentNode.class);
        CssComponentNode comp = (CssComponentNode) getFirstActualNode();
        assertThat(comp.getName().getValue()).isEqualTo("CSS_X");
        assertThat(comp.getParentName()).isNull();
        assertThat(comp.isAbstract()).isTrue();
        assertThat(comp.getBlock()).hasToString("[@def[X, Y]]");
    }

    @Test
    void testCreateComponentNode3() throws Exception {
        parseAndRun("@abstract_component CSS_X { @def X Y; }\n" +
                "@component CSS_Y extends CSS_X { @def X Y; }");
        List<CssNode> children = tree.getRoot().getBody().getChildren();
        assertThat(children).asList().hasSize(2);
        assertThat(children.get(0)).isInstanceOf(CssComponentNode.class);
        assertThat(children.get(1)).isInstanceOf(CssComponentNode.class);
        CssComponentNode comp = (CssComponentNode) children.get(1);
        assertThat(comp.getName().getValue()).isEqualTo("CSS_Y");
        assertThat(comp.getParentName()).isNotNull();
        assertThat(comp.getParentName().getValue()).isEqualTo("CSS_X");
        assertThat(comp.isAbstract()).isFalse();
        assertThat(comp.getBlock()).hasToString("[@def[X, Y]]");
    }

    @Test
    void testImplicitlyNamedComponent() throws Exception {
        parseAndRun("@component { @def X Y; }");
        assertThat(getFirstActualNode()).isInstanceOf(CssComponentNode.class);
        CssComponentNode comp = (CssComponentNode) getFirstActualNode();
        assertThat(comp.getName().getValue()).isSameAs(CssComponentNode.IMPLICIT_NODE_NAME);
        assertThat(comp.getParentName()).isNull();
        assertThat(comp.isAbstract()).isFalse();
        assertThat(comp.getBlock()).hasToString("[@def[X, Y]]");
    }

    @Test
    void testBlockError() throws Exception {
        parseAndRun("@component CSS_X;", "@component without block");
        assertThat(isEmptyBody()).isTrue();
    }

    @Test
    void testNameError() throws Exception {
        parseAndRun("@component 1px {}", "@component without a valid literal as name");
        assertThat(isEmptyBody()).isTrue();
    }

    @Test
    void testExtendsError1() throws Exception {
        parseAndRun("@component CSS_X 1px CSS_Y {}",
                "@component with invalid second parameter (expects 'extends')");
        assertThat(isEmptyBody()).isTrue();
    }

    @Test
    void testExtendsError2() throws Exception {
        parseAndRun("@component CSS_X foo CSS_Y {}",
                "@component with invalid second parameter (expects 'extends')");
        assertThat(isEmptyBody()).isTrue();
    }

    @Test
    void testParentNameError() throws Exception {
        parseAndRun("@component CSS_X extends 1px {}",
                "@component with invalid literal as parent name");
        assertThat(isEmptyBody()).isTrue();
    }

    @Test
    void testInvalidParamNumError1() throws Exception {
        parseAndRun("@component CSS_X extends {}", "@component with invalid number of parameters");
        assertThat(isEmptyBody()).isTrue();
    }

    @Test
    void testInvalidParamNumError2() throws Exception {
        parseAndRun("@component CSS_X extends CSS_Y CSS_Z {}",
                "@component with invalid number of parameters");
        assertThat(isEmptyBody()).isTrue();
    }
}
