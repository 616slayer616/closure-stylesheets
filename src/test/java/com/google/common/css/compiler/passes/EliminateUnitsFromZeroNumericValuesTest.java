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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

    @ParameterizedTest
    @CsvSource({
            "3, 3",
            "0, 0",
            "0.000, 0",
            "3.0, 3",
            "003.0, 3",
            "0.3, .3",
            "0.3000, .3",
            "002.3000, 2.3",
            "woo34, woo34"})
    void testEnterValueNode1(String value, String expected) {
        EliminateUnitsFromZeroNumericValues pass =
                new EliminateUnitsFromZeroNumericValues(mockVisitController);

        CssNumericNode node = new CssNumericNode(value, "px");
        pass.enterValueNode(node);
        assertThat(node.getNumericPart()).isEqualTo(expected);

        if (expected.equals("0")) {
            assertThat(node.getUnit()).isEmpty();
        } else {
            assertThat(node.getUnit()).isEqualTo("px");
        }
    }
}
