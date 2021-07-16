/*
 * Copyright 2012 Google Inc.
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

import com.google.common.css.compiler.ast.CssTreeVisitor;
import com.google.common.css.compiler.passes.UniformVisitor.Adapters;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

/**
 * Tests {@link com.google.common.css.compiler.passes.UniformVisitor.Adapters}.
 */
class UniformVisitorAdaptersTest {

    @Test
    void asVisitor() throws Exception {
        UniformVisitor uniformVisitor = mock(UniformVisitor.class);
        CssTreeVisitor visitor = Adapters.asVisitor(uniformVisitor);

        visitor.enterSelector(null /* selector */);
        visitor.leaveSelector(null /* selector */);

        InOrder verifier = inOrder(uniformVisitor);
        verifier.verify(uniformVisitor).enter(null);
        verifier.verify(uniformVisitor).leave(null);
        verifier.verifyNoMoreInteractions();
    }

    @Test
    void asCombinedVisitor() throws Exception {
        CombinedVisitor combinedVisitor = mock(CombinedVisitor.class);
        CssTreeVisitor visitor = Adapters.asCombinedVisitor(combinedVisitor);

        visitor.enterSelector(null /* selector */);
        visitor.leaveSelector(null /* selector */);

        InOrder verifier = inOrder(combinedVisitor);
        verifier.verify(combinedVisitor).enter(null);
        verifier.verify(combinedVisitor).enterSelector(null);
        verifier.verify(combinedVisitor).leaveSelector(null);
        verifier.verify(combinedVisitor).leave(null);
    }

    @Test
    void testCombinedVisitorObjectMethods() throws Exception {
        CombinedVisitor combinedVisitor = mock(CombinedVisitor.class);
        CssTreeVisitor visitor = Adapters.asCombinedVisitor(combinedVisitor);

        assertThat(visitor.toString()).isEqualTo(combinedVisitor.toString());
    }

    @Test
    void testUniformVisitorObjectMethods() throws Exception {
        UniformVisitor uniformVisitor = mock(UniformVisitor.class);
        CssTreeVisitor visitor = Adapters.asVisitor(uniformVisitor);

        assertThat(visitor.toString()).isEqualTo(uniformVisitor.toString());
    }

    private interface CombinedVisitor extends CssTreeVisitor, UniformVisitor {
    }

    ;
}
