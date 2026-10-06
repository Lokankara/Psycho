package com.unconscious.collective.agile.model.json;

import java.util.List;

public record JsonTrelloBoard(
        String name,
        String desc,
        Boolean closed,
        List<JsonTrelloList> lists,
        List<JsonTrelloCard> cards) {
}
