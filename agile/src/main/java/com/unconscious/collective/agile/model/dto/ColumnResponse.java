package com.unconscious.collective.agile.model.dto;

import java.util.List;

public record ColumnResponse(
        String code, String name, int position,
        List<StoryCard> stories, List<TaskCard> tasks) {
}
