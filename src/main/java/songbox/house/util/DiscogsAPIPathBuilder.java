package songbox.house.util;

import java.util.Map;

public final class DiscogsAPIPathBuilder {

    //        private String basePath;
    private String path;
    private Integer page;
    private Integer perPage;
    private String sort;
    private String sortOrder;
    private Map<String, String> params;

    private DiscogsAPIPathBuilder(String path) {
        this.path = path;
    }

    private DiscogsAPIPathBuilder withPage(int page, int perPage) {
        this.page = page;
        this.perPage = perPage;
        return this;
    }

    private DiscogsAPIPathBuilder withSort(String sort, boolean isAscending) {
        this.sort = sort;
        this.sortOrder = isAscending ? "asc" : "desc";
        return this;
    }

    private DiscogsAPIPathBuilder withParams(Map<String, String> params) {
        this.params = params;
        return this;
    }

    private String build() {


        return null;
//            return basePath + path
    }
}