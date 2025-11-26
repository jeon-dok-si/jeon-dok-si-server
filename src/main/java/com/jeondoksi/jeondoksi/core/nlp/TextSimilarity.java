package com.jeondoksi.jeondoksi.core.nlp;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TextSimilarity {

    // 코사인 유사도 계산
    public double calculateCosineSimilarity(List<String> doc1, List<String> doc2) {
        Set<String> allWords = new HashSet<>();
        allWords.addAll(doc1);
        allWords.addAll(doc2);

        Map<String, Integer> vector1 = getTermFrequency(doc1, allWords);
        Map<String, Integer> vector2 = getTermFrequency(doc2, allWords);

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String word : allWords) {
            int v1 = vector1.getOrDefault(word, 0);
            int v2 = vector2.getOrDefault(word, 0);

            dotProduct += v1 * v2;
            norm1 += Math.pow(v1, 2);
            norm2 += Math.pow(v2, 2);
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private Map<String, Integer> getTermFrequency(List<String> doc, Set<String> allWords) {
        Map<String, Integer> tf = new HashMap<>();
        for (String word : doc) {
            tf.put(word, tf.getOrDefault(word, 0) + 1);
        }
        return tf;
    }
}
