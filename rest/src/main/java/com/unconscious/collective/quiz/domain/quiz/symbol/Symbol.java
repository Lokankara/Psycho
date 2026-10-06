package com.unconscious.collective.quiz.domain.quiz.symbol;

import java.io.Serializable;

public record Symbol(
        String name,
        String meaning,
        SymbolCategoryType categoryType
) implements Serializable {
}