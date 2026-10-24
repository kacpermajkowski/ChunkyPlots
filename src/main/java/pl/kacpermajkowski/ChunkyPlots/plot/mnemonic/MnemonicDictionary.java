package pl.kacpermajkowski.ChunkyPlots.plot.mnemonic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ordered dictionary of words for mnemonic chunk and plot identifiers.
 * The order of words strictly determines coordinate mapping.
 */
public class MnemonicDictionary {
    public static final int EXPECTED_WORD_COUNT = 2000;

    private final List<String> words;
    private final Map<String, Integer> wordToIndex;

    public MnemonicDictionary(List<String> words) {
        if (words == null || words.isEmpty()) {
            throw new IllegalArgumentException("Word list cannot be empty");
        }
        this.words = List.copyOf(words);
        Map<String, Integer> indexMap = new HashMap<>(this.words.size());
        for (int i = 0; i < this.words.size(); i++) {
            String word = this.words.get(i).toLowerCase();
            indexMap.putIfAbsent(word, i);
        }
        this.wordToIndex = Collections.unmodifiableMap(indexMap);
    }

    public static MnemonicDictionary loadFromStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        List<String> loaded = new ArrayList<>(EXPECTED_WORD_COUNT);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim().toLowerCase();
                if (!line.isEmpty()) {
                    loaded.add(line);
                }
            }
        }
        return new MnemonicDictionary(loaded);
    }

    public static MnemonicDictionary loadFromFile(File file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            return loadFromStream(in);
        }
    }

    public int size() {
        return words.size();
    }

    public String getWord(int index) {
        if (index < 0 || index >= words.size()) {
            throw medicalBoundsError(index);
        }
        return words.get(index);
    }

    private IndexOutOfBoundsException medicalBoundsError(int index) {
        return new IndexOutOfBoundsException("Word index " + index + " out of bounds [0, " + words.size() + ")");
    }

    public int getIndex(String word) {
        if (word == null) return -1;
        return wordToIndex.getOrDefault(word.toLowerCase(), -1);
    }

    public boolean containsWord(String word) {
        return getIndex(word) >= 0;
    }

    public List<String> getWords() {
        return words;
    }
}
