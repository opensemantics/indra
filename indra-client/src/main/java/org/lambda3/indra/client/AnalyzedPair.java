package org.lambda3.indra.client;

/*-
 * ==========================License-Start=============================
 * Indra Client Module
 * --------------------------------------------------------------------
 * Copyright (C) 2016 - 2017 Lambda^3
 * --------------------------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ==========================License-End===============================
 */

public final class AnalyzedPair {
    private final TextPair pair;
    private final AnalyzedTerm t1;
    private final AnalyzedTerm t2;

    public AnalyzedPair(TextPair pair, AnalyzedTerm t1, AnalyzedTerm t2) {
        if (pair == null || t1 == null || t2 == null) {
            throw new IllegalArgumentException("pair, t1 and t2 can't be null");
        }
        this.pair = pair;
        this.t1 = t1;
        this.t2 = t2;
    }

    public TextPair getTextPair() {
        return this.pair;
    }

    public AnalyzedTerm getAnalyzedT1() {
        return t1;
    }

    public AnalyzedTerm getAnalyzedT2() {
        return t2;
    }
}
