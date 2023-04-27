package io.wispforest.gadget.dump.read;

import io.wispforest.gadget.dump.read.handler.SearchTextGatherer;
import io.wispforest.gadget.util.ContextData;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

public class SearchTextData {
    public static final ContextData.Key<DumpedPacket, SearchTextData> KEY = new ContextData.Key<>(SearchTextData::new);

    private final DumpedPacket packet;
    private SoftReference<String> searchText;
    private final List<Throwable> searchTextErrors = new ArrayList<>();

    public SearchTextData(DumpedPacket packet) {
        this.packet = packet;
    }

    public String searchText() {
        if (searchText == null || searchText.get() == null) {
            StringBuilder sb = new StringBuilder();

            searchTextErrors.clear();
            SearchTextGatherer.EVENT.invoker().gatherSearchText(packet, sb, searchTextErrors::add);

            searchText = new SoftReference<>(sb.toString());
        }

        return searchText.get();
    }

    public List<Throwable> searchTextErrors() {
        return searchTextErrors;
    }
}
