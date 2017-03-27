package org.lambda3.indra.mongo;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.lambda3.indra.client.MutableAnalyzedTerm;
import org.lambda3.indra.core.translation.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/*-
 * ==========================License-Start=============================
 * Indra Mongo Module
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
public class MongoTranslator implements Translator {

    private static final String LEX_COLLECTION = "lex";
    private static final String TERM_FIELD = "term";
    private static final String TRANSLATION_FIELD = "tr";
    private static final String NULL_VALUE = "NULL";

    private Logger logger = LoggerFactory.getLogger(getClass());

    private MongoClient mongoClient;
    private String dbName;


    public MongoTranslator(MongoClient mongoClient, String dbName) {
        if (mongoClient == null) {
            throw new IllegalArgumentException("mongoClient can't be null");
        }

        this.mongoClient = mongoClient;
        this.dbName = dbName;
    }

    private MongoCollection<Document> getLexCollection() {
        return mongoClient.getDatabase(dbName).getCollection(LEX_COLLECTION);
    }

    @Override
    public void translate(List<MutableAnalyzedTerm> terms) {

        Set<String> allTokens = new HashSet<>();
        terms.forEach(t -> allTokens.addAll(t.getOriginalTokens()));
        logger.debug("Translating {} MutableAnalyzedTerms, resulting in {} tokens", terms.size(), allTokens.size());

        Map<String, Set<String>> translations = doTranslate(allTokens);
        for (MutableAnalyzedTerm term : terms) {
            Map<String, Set<String>> termTranslations = new HashMap<>();

            for (String token : term.getOriginalTokens()) {
                Set<String> tokenTranslations = translations.get(token);
                if (tokenTranslations != null) {
                    termTranslations.put(token, tokenTranslations);
                }
            }

            term.setTranslatedTokens(termTranslations);
        }
    }

    private Map<String, Set<String>> doTranslate(Set<String> tokens) {
        MongoCollection<Document> lexColl = getLexCollection();
        FindIterable<Document> lexs = lexColl.find(Filters.in(TERM_FIELD, tokens));

        Map<String, Set<String>> res = new HashMap<>();
        for (Document doc : lexs) {
            Document tr = (Document) doc.get(TRANSLATION_FIELD);

            if (tr != null) {
                res.put(doc.getString(TERM_FIELD), getRelavantTranslations((Map) tr));
            }
        }

        return res;
    }

    public static Set<String> getRelavantTranslations(Map<String, Double> tr) {
        tr.remove(NULL_VALUE);

        Set<String> res = new HashSet<>();

        if (tr.size() <= 2) {
            tr.keySet().forEach(k -> res.add(k));
        } else {

            LinkedHashMap<String, Double> tempWords = new LinkedHashMap<>();
            tr.keySet().forEach(k -> tempWords.put(k, tr.get(k)));

            LinkedHashMap<String, Double> sortedWords = sortByValue(tempWords);

            double maxDiff = Double.MIN_VALUE * -1;
            Double lastScore = sortedWords.entrySet().iterator().next().getValue();

            for (String word : sortedWords.keySet()) {
                Double score = sortedWords.get(word);
                double diff = lastScore - score;
                if (diff > maxDiff) {
                    maxDiff = diff;
                }

                lastScore = score;
            }

            lastScore = sortedWords.entrySet().iterator().next().getValue();
            for (String word : sortedWords.keySet()) {
                Double score = sortedWords.get(word);
                double diff = lastScore - score;
                if (diff >= maxDiff) {
                    break;
                }

                res.add(word);
                lastScore = score;
            }
        }

        return res;
    }

    public static LinkedHashMap sortByValue(Map<String, Double> map) {
        LinkedHashMap<String, Double> sortedMap = map.entrySet().stream().sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        return sortedMap;
    }

    public static void main(String[] args) {
        String mongoURI = "132.231.141.167:27017";
        MongoTranslator mt = new MongoTranslator(new MongoClient(mongoURI), "pt_en-Europarl_DGT_OpenSubtitile");

        Set<String> set = Arrays.stream(new String[]{"amor"}).collect(Collectors.toSet());
        Map<String, Set<String>> results = mt.doTranslate(set);
        for (String r : results.keySet()) {
            System.out.print(r + ": [");
            System.out.print(String.join(", ", results.get(r)));
            System.out.println("]");

        }

    }
}
