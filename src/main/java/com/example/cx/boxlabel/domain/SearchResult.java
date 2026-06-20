package com.example.cx.boxlabel.domain;

import java.util.List;

public class SearchResult<T> {

    private List<T> items;

    public SearchResult(List<T> items) {
        this.items = items;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}
