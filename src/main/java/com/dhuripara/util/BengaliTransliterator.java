package com.dhuripara.util;

/**
 * Utility class for approximate transliteration from Latin script (English) to Bengali script.
 * This is a simple character-mapping based transliteration and is NOT a perfect conversion.
 * For accurate results, manual verification or crowdsourced corrections are recommended.
 */
public class BengaliTransliterator {

    private BengaliTransliterator() {}

    // Character mapping from English (Latin) to Bengali approximation
    private static final String[][] TRANSLITERATION_MAP = {
        // Vowels
        {"a", "া"},
        {"aa", "আ"},
        {"A", "আ"},
        {"e", "ে"},
        {"i", "ি"},
        {"ii", "ী"},
        {"o", "ো"},
        {"u", "ু"},
        {"uu", "ূ"},
        
        // Common consonants
        {"k", "ক"},
        {"kh", "খ"},
        {"g", "গ"},
        {"gh", "ঘ"},
        {"ng", "ঙ"},
        {"c", "চ"},
        {"ch", "ছ"},
        {"j", "জ"},
        {"jh", "ঝ"},
        {"ny", "ঞ"},
        {"t", "ট"},
        {"th", "ঠ"},
        {"d", "ড"},
        {"dh", "ঢ"},
        {"n", "ন"},
        {"p", "প"},
        {"ph", "ফ"},
        {"b", "ব"},
        {"bh", "ভ"},
        {"m", "ম"},
        {"y", "য"},
        {"r", "র"},
        {"l", "ল"},
        {"sh", "শ"},
        {"s", "স"},
        {"h", "হ"},
    };

    /**
     * Approximate transliteration of English name to Bengali script.
     * This is a simple character replacement and may not be accurate.
     * 
     * @param englishText English/Latin text to transliterate
     * @return Approximate Bengali transliteration
     */
    public static String transliterate(String englishText) {
        if (englishText == null || englishText.isBlank()) {
            return null;
        }

        String result = englishText.toLowerCase();

        // Apply longer sequences first to avoid conflicts (e.g., "kh" before "k")
        for (String[] pair : TRANSLITERATION_MAP) {
            if (pair[0].length() > 1) {
                result = result.replace(pair[0], pair[1]);
            }
        }

        // Apply single characters
        for (String[] pair : TRANSLITERATION_MAP) {
            if (pair[0].length() == 1) {
                result = result.replace(pair[0], pair[1]);
            }
        }

        return result.isEmpty() ? null : result;
    }

    /**
     * Check if the given text appears to be already in Bengali script.
     * Simple heuristic: if text contains Bengali characters, assume it's Bengali.
     */
    public static boolean isBengaliScript(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        // Bengali Unicode range: U+0980 to U+09FF
        for (char c : text.toCharArray()) {
            if (c >= '\u0980' && c <= '\u09FF') {
                return true;
            }
        }
        return false;
    }
}
