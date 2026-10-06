package com.unconscious.collective.agile.model.json;

import java.util.List;

public record JsonTrelloList(
        String id,
        String name,
        Boolean closed,
        Integer pos) {
}
