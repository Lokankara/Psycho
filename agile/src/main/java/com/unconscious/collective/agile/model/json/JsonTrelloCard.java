package com.unconscious.collective.agile.model.json;

import java.util.List;

public record JsonTrelloCard(
        String id,
        String idList,
        String name,
        String desc,
        Boolean closed,
        Integer pos,
        List<JsonTrelloLabel> labels) {
}
