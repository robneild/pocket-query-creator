package org.pquery.dao;

/**
 * Section Header for PQListItems
 */
public class PQListItemSection implements PQListItem {
    private String name;

    public PQListItemSection(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
