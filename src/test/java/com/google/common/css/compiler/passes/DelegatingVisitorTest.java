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

import com.google.common.css.compiler.ast.CssSelectorNode;
import com.google.common.css.compiler.ast.CssTreeVisitor;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests {@link DelegatingVisitor}
 */
class DelegatingVisitorTest {

    @Test
    void testInvocationOrder() {
        List<String> orderRecord = new ArrayList<>();
        DefaultTreeVisitor visitor1 = new RecordingVisitor("visitor1", orderRecord);
        DefaultTreeVisitor visitor2 = new RecordingVisitor("visitor2", orderRecord);

        CssTreeVisitor delegatingVisitor = DelegatingVisitor.from(visitor1, visitor2);

        delegatingVisitor.enterSelector(null /* selector */);
        delegatingVisitor.leaveSelector(null /* selector */);

        assertThat(orderRecord).asList()
                .containsExactly("enter visitor1", "enter visitor2", "leave visitor2", "leave visitor1");
    }

    private static class RecordingVisitor extends DefaultTreeVisitor {

        private final String name;
        private final List<String> orderRecord;

        RecordingVisitor(String name, List<String> orderRecord) {
            this.name = name;
            this.orderRecord = orderRecord;
        }

        @Override
        public boolean enterSelector(CssSelectorNode selector) {
            orderRecord.add("enter " + name);
            return super.enterSelector(selector);
        }

        @Override
        public void leaveSelector(CssSelectorNode selector) {
            orderRecord.add("leave " + name);
            super.leaveSelector(selector);
        }
    }
}
