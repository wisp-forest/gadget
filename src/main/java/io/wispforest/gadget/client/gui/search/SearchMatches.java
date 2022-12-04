// Taken from https://github.com/wisp-forest/owo-lib/blob/1.19/src/main/java/io/wispforest/owo/config/ui/ConfigScreen.java.
// Includes modifications.

package io.wispforest.gadget.client.gui.search;

import java.util.List;

public record SearchMatches(String query, List<SearchAnchorComponent> matches) {

}