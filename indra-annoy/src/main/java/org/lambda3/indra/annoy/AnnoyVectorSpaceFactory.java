package org.lambda3.indra.annoy;

/*-
 * ==========================License-Start=============================
 * Indra Annoy Module
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

import org.lambda3.indra.client.AbstractBasicRequest;
import org.lambda3.indra.core.translation.IndraTranslator;
import org.lambda3.indra.core.vs.VectorSpace;
import org.lambda3.indra.core.vs.VectorSpaceFactory;
import org.lambda3.indra.exception.ModelNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;


public final class AnnoyVectorSpaceFactory extends VectorSpaceFactory {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private File baseDir;

    public AnnoyVectorSpaceFactory(File baseDir) {
        this.baseDir = baseDir;
        if (!baseDir.canRead()) {
            throw new RuntimeException("Can't read data from " + baseDir);
        }
        logger.debug("Setting baseDir to {}", this.baseDir);
    }

    @Override
    public Collection<String> getAvailableModels() {
        Collection<String> results = new LinkedList<>();

        if (baseDir.exists() && baseDir.isDirectory()) {
            File[] models = baseDir.listFiles(File::isDirectory);
            for (File model : models) {
                File[] langs = model.listFiles(File::isDirectory);
                for (File lang : langs) {
                    File[] corpora = lang.listFiles(File::isDirectory);
                    for (File corpus : corpora) {
                        results.add(String.format("%s-%s-%s", model.getName(), lang.getName(), corpus.getName()));
                    }
                }
            }
        }

        return results;
    }

    @Override
    protected VectorSpace doCreate(AbstractBasicRequest request) {
        File vsDirFile = createVSFile(request);

        if (vsDirFile.exists() && vsDirFile.isDirectory()) {
            return new AnnoyVectorSpace(vsDirFile.getAbsolutePath());
        } else {
            throw new ModelNotFoundException(String.format("%s-%s-%s", request.getModel(), request.getLanguage(), request.getCorpus()));
        }
    }

    @Override
    protected String createKey(AbstractBasicRequest request) {
        return createVSFile(request).getAbsolutePath() + request.isMt();
    }

    private File createVSFile(AbstractBasicRequest request) {
        String lang = request.isMt() ? IndraTranslator.DEFAULT_TRANSLATION_TARGET_LANGUAGE.toLowerCase() :
                request.getLanguage().toLowerCase();
        return Paths.get(baseDir.getAbsolutePath(), request.getModel().toLowerCase(), lang,
                request.getCorpus().toLowerCase()).toFile();
    }
}
