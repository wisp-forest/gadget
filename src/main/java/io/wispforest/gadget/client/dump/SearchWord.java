package io.wispforest.gadget.client.dump;

import java.util.ArrayList;
import java.util.List;

public record SearchWord(String match, boolean inverted) {
    public boolean matches(String text) {
        return text.contains(match) ^ inverted;
    }

    public static List<SearchWord> parseSearch(String searchText) {
        if (searchText.isEmpty()) return List.of();

        List<SearchWord> words = new ArrayList<>();

        for (String word : searchText.split(" ")) {
            if (word.startsWith("!"))
                words.add(new SearchWord(word.substring(1), true));
            else
                words.add(new SearchWord(word, false));
        }

        return words;
    }
}
