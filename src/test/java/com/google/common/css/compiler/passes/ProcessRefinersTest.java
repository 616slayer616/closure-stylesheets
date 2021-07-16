/*
 * Copyright 2011 Google Inc.
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

import com.google.common.css.compiler.ast.testing.NewFunctionalTestBase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author fbenz@google.com (Florian Benz)
 */
class ProcessRefinersTest extends NewFunctionalTestBase {
    private boolean simplifyCss;
    private String compactPrintedResult;

    @Override
    protected void runPass() {
        new ProcessRefiners(tree.getMutatingVisitController(), errorManager,
                simplifyCss).runPass();
        CompactPrinter compactPrinterPass = new CompactPrinter(tree);
        compactPrinterPass.runPass();
        compactPrintedResult = compactPrinterPass.getCompactPrintedString();
    }

    protected void runParseA(int expected, String argument) {
        ProcessRefiners processRefiners = new ProcessRefiners(null, errorManager,
                simplifyCss);
        int indexOfN = argument.indexOf('n');
        int actual = processRefiners.parseA(argument, indexOfN);
        assertThat(actual).isEqualTo(expected);
    }

    protected void runParseB(int expected, String argument) {
        ProcessRefiners processRefiners = new ProcessRefiners(null, errorManager,
                simplifyCss);
        int indexOfN = argument.indexOf('n');
        int actual = processRefiners.parseB(argument, indexOfN);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testNthCompact1() throws Exception {
        simplifyCss = true;
        parseAndRun("div :nth-child(+n) {}");
        assertThat(compactPrintedResult).isEqualTo("div :nth-child(1n){}");
    }

    @Test
    void testNthCompact2() throws Exception {
        simplifyCss = true;
        parseAndRun("div :nth-child(+1n) {}");
        assertThat(compactPrintedResult).isEqualTo("div :nth-child(1n){}");
    }

    @Test
    void testNthCompact3() throws Exception {
        simplifyCss = true;
        parseAndRun("div :nth-child(1n) {}");
        assertThat(compactPrintedResult).isEqualTo("div :nth-child(1n){}");
    }

    @Test
    void testNthCompact4() throws Exception {
        simplifyCss = true;
        parseAndRun("div :nth-child(+n+0) {}");
        assertThat(compactPrintedResult).isEqualTo("div :nth-child(1n){}");
    }

    @Test
    void testNthCompact5() throws Exception {
        simplifyCss = true;
        parseAndRun("div :nth-child( +2n+5 ) {}");
        assertThat(compactPrintedResult).isEqualTo("div :nth-child(2n+5){}");
    }

    @Test
    void testNthCompact6() throws Exception {
        simplifyCss = true;
        parseAndRun("div :nth-child( n-7 ) {}");
        assertThat(compactPrintedResult).isEqualTo("div :nth-child(n-7){}");
    }

    @Test
    void testNthCompact7() throws Exception {
        simplifyCss = true;
        parseAndRun("#id :nth-child( 2 ) {}");
        assertThat(compactPrintedResult).isEqualTo("#id :nth-child(2){}");
    }

    @Test
    void testNthCompact8() throws Exception {
        simplifyCss = true;
        parseAndRun(".class :nth-child( -3 ) {}");
        assertThat(compactPrintedResult).isEqualTo(".class :nth-child(-3){}");
    }

    @Test
    void testNthCompact9() throws Exception {
        simplifyCss = true;
        parseAndRun("* :nth-child( odd ) {}");
        assertThat(compactPrintedResult).isEqualTo("* :nth-child(odd){}");
    }

    @Test
    void testNthCompact10() throws Exception {
        simplifyCss = true;
        parseAndRun("div :nth-child( even ) {}");
        assertThat(compactPrintedResult).isEqualTo("div :nth-child(2n){}");
    }

    @Test
    void testNthCompact11() throws Exception {
        simplifyCss = true;
        parseAndRun("div :nth-child( +3 ) {}");
        assertThat(compactPrintedResult).isEqualTo("div :nth-child(3){}");
    }

    @Test
    void testNthCompact12() throws Exception {
        simplifyCss = true;
        parseAndRun("div :nth-child( 0 ) {}");
        assertThat(compactPrintedResult).isEqualTo("div :nth-child(0){}");
    }

    @Test
    void testNthCompact13() throws Exception {
        simplifyCss = true;
        parseAndRun("div :nth-child( 0n ) {}");
        assertThat(compactPrintedResult).isEqualTo("div :nth-child(0){}");
    }

    @Test
    void testNthCompact14() throws Exception {
        simplifyCss = true;
        parseAndRun("div :nth-child( 0n+0 ) {}");
        assertThat(compactPrintedResult).isEqualTo("div :nth-child(0){}");
    }

    @Test
    void testNthCompact15() throws Exception {
        simplifyCss = true;
        parseAndRun("div :nth-child( 0 n - 1 ) {}");
        assertThat(compactPrintedResult).isEqualTo("div :nth-child(-1){}");
    }

    @Test
    void testNthCompact16() throws Exception {
        simplifyCss = true;
        parseAndRun("div :nth-child( 1 n - 0 ) {}");
        assertThat(compactPrintedResult).isEqualTo("div :nth-child(1n){}");
    }

    @Test
    void testNthCompact17() throws Exception {
        simplifyCss = true;
        parseAndRun("div :nth-child( -1 n - 0 ) {}");
        assertThat(compactPrintedResult).isEqualTo("div :nth-child(-1n){}");
    }

    @Test
    void testNthCompact18() throws Exception {
        simplifyCss = true;
        parseAndRun("div :nth-child( -n ) {}");
        assertThat(compactPrintedResult).isEqualTo("div :nth-child(-1n){}");
    }

    @Test
    void testNthCompact19() throws Exception {
        simplifyCss = true;
        parseAndRun("div :nth-child( -n+5 ) {}");
        assertThat(compactPrintedResult).isEqualTo("div :nth-child(-1n+5){}");
    }

    @Test
    void testNthBad1() throws Exception {
        simplifyCss = false;
        parseAndRun("div :nth-child(1.1) {}",
                ProcessRefiners.INVALID_NTH_ERROR_MESSAGE);
    }

    @Test
    void testNthBad2() throws Exception {
        simplifyCss = false;
        parseAndRun("div :nth-child(n+2.3) {}",
                ProcessRefiners.INVALID_NTH_ERROR_MESSAGE);
    }

    @Test
    void testNthBad3() throws Exception {
        simplifyCss = false;
        parseAndRun("div :nth-child(m+7) {}",
                ProcessRefiners.INVALID_NTH_ERROR_MESSAGE);
    }

    @Test
    void testNthBad4() throws Exception {
        simplifyCss = false;
        parseAndRun("div :nth-child(oddy) {}",
                ProcessRefiners.INVALID_NTH_ERROR_MESSAGE);
    }

    @Test
    void testNthBad5() throws Exception {
        simplifyCss = false;
        parseAndRun("div :nth-child(_even) {}",
                ProcessRefiners.INVALID_NTH_ERROR_MESSAGE);
    }

    @Test
    void testNotBad1() throws Exception {
        simplifyCss = false;
        parseAndRun("div :not(:not(*)) {}",
                ProcessRefiners.INVALID_NOT_SELECTOR_ERROR_MESSAGE);
    }

    @Test
    void testNotBad2() throws Exception {
        simplifyCss = false;
        parseAndRun("div :not(::first-line) {}",
                ProcessRefiners.INVALID_NOT_SELECTOR_ERROR_MESSAGE);
    }

    @Test
    void testNotBad3() throws Exception {
        simplifyCss = false;
        parseAndRun("div :not(.A.B) {}",
                ProcessRefiners.INVALID_NOT_SELECTOR_ERROR_MESSAGE);
    }

    @Test
    void testNotWithComment() throws Exception {
        simplifyCss = true;
        parseAndRun("div:not(.A /* C */) {}");
        assertThat(compactPrintedResult).isEqualTo("div:not(.A){}");
    }


    @Test
    void testParseA() throws Exception {
        runParseA(0, "2");
        runParseA(0, "-3");
        runParseA(1, "n");
        runParseA(1, "+n");
        runParseA(-1, "-n");
        runParseA(2, "2n");
        runParseA(3, "+3n");
        runParseA(-2, "-2n");
        runParseA(42, "42n+3");
        runParseA(-23, "-23n-67");
        runParseA(-23, "-23n-0");
        runParseA(-23, "-23n+0");
    }

    @Test
    void testParseB() throws Exception {
        runParseB(2, "2");
        runParseB(-3, "-3");
        runParseB(0, "n");
        runParseB(0, "+n");
        runParseB(0, "-n");
        runParseB(0, "2n");
        runParseB(0, "+3n");
        runParseB(0, "-2n");
        runParseB(3, "42n+3");
        runParseB(-67, "-23n-67");
        runParseB(0, "-23n+0");
        runParseB(0, "-21n-0");
    }

    @Test
    void testLang() throws Exception {
        simplifyCss = true;
        parseAndRun("div :lang(en) {}");
        assertThat(compactPrintedResult).isEqualTo("div :lang(en){}");
    }
}
