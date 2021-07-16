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

import com.google.common.css.compiler.ast.CssNumericNode;
import com.google.common.css.compiler.ast.MutatingVisitController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link EliminateUnitsFromZeroNumericValues}.
 *
 * @author oana@google.com (Oana Florescu)
 */
@ExtendWith(MockitoExtension.class)
class EliminateUnitsFromZeroNumericValuesTest {

    @Mock
    MutatingVisitController mockVisitController;

    @Test
    void testRunPass() {
        EliminateUnitsFromZeroNumericValues pass =
                new EliminateUnitsFromZeroNumericValues(mockVisitController);
        mockVisitController.startVisit(pass);

        pass.runPass();
    }

    @Test
    void testEnterValueNode1() {
        EliminateUnitsFromZeroNumericValues pass =
                new EliminateUnitsFromZeroNumericValues(mockVisitController);

        CssNumericNode node = new CssNumericNode("3", "px");
        pass.enterValueNode(node);
        assertThat(node.getNumericPart()).isEqualTo("3");
        assertThat(node.getUnit()).isEqualTo("px");
    }

    @Test
    void testEnterValueNode2() {
        EliminateUnitsFromZeroNumericValues pass =
                new EliminateUnitsFromZeroNumericValues(mockVisitController);

        CssNumericNode node = new CssNumericNode("0", "px");
        pass.enterValueNode(node);
        assertThat(node.getNumericPart()).isEqualTo("0");
        assertThat(node.getUnit()).isEmpty();
    }

    @Test
    void testEnterValueNode3() {
        EliminateUnitsFromZeroNumericValues pass =
                new EliminateUnitsFromZeroNumericValues(mockVisitController);

        CssNumericNode node = new CssNumericNode("0.000", "px");
        pass.enterValueNode(node);
        assertThat(node.getNumericPart()).isEqualTo("0");
        assertThat(node.getUnit()).isEmpty();
    }

    @Test
    void testEnterValueNode4() {
        EliminateUnitsFromZeroNumericValues pass =
                new EliminateUnitsFromZeroNumericValues(mockVisitController);

        CssNumericNode node = new CssNumericNode("3.0", "px");
        pass.enterValueNode(node);
        assertThat(node.getNumericPart()).isEqualTo("3");
        assertThat(node.getUnit()).isEqualTo("px");
    }

    @Test
    void testEnterValueNode5() {
        EliminateUnitsFromZeroNumericValues pass =
                new EliminateUnitsFromZeroNumericValues(mockVisitController);

        CssNumericNode node = new CssNumericNode("003.0", "px");
        pass.enterValueNode(node);
        assertThat(node.getNumericPart()).isEqualTo("3");
        assertThat(node.getUnit()).isEqualTo("px");
    }

    @Test
    void testEnterValueNode6() {
        EliminateUnitsFromZeroNumericValues pass =
                new EliminateUnitsFromZeroNumericValues(mockVisitController);

        CssNumericNode node = new CssNumericNode("0.3", "px");
        pass.enterValueNode(node);
        assertThat(node.getNumericPart()).isEqualTo(".3");
        assertThat(node.getUnit()).isEqualTo("px");
    }

    @Test
    void testEnterValueNode7() {
        EliminateUnitsFromZeroNumericValues pass =
                new EliminateUnitsFromZeroNumericValues(mockVisitController);

        CssNumericNode node = new CssNumericNode("0.3000", "px");
        pass.enterValueNode(node);
        assertThat(node.getNumericPart()).isEqualTo(".3");
        assertThat(node.getUnit()).isEqualTo("px");
    }

    @Test
    void testEnterValueNode8() {
        EliminateUnitsFromZeroNumericValues pass =
                new EliminateUnitsFromZeroNumericValues(mockVisitController);

        CssNumericNode node = new CssNumericNode("002.3000", "px");
        pass.enterValueNode(node);
        assertThat(node.getNumericPart()).isEqualTo("2.3");
        assertThat(node.getUnit()).isEqualTo("px");
    }

    @Test
    void testEnterValueNode9() {
        EliminateUnitsFromZeroNumericValues pass =
                new EliminateUnitsFromZeroNumericValues(mockVisitController);

        CssNumericNode node = new CssNumericNode("woo34", "px");
        pass.enterValueNode(node);
        assertThat(node.getNumericPart()).isEqualTo("woo34");
        assertThat(node.getUnit()).isEqualTo("px");
    }
}
