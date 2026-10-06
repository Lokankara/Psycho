package com.unconscious.collective.quiz.domain.quiz.symbol;

import com.unconscious.collective.quiz.domain.archetype.Octant;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Read-only catalogue of the universal symbols associated with each octant.
 */
public final class SymbolCatalog {

    private static final Map<Octant, List<Symbol>> SYMBOLS = Map.of(
            Octant.GUARDIAN_LEADER, List.of(
                    new Symbol("Мировое древо", "Ось мира и опора традиции, связь поколений.", SymbolCategoryType.WORLD_TREE),
                    new Symbol("Храм", "Форма, удерживающая сакральный порядок.", SymbolCategoryType.SELF),
                    new Symbol("Корона", "Власть структуры и непреложный закон.", SymbolCategoryType.DEVOURING_FATHER),
                    new Symbol("Скипетр", "Орудие поддержания иерархии.", SymbolCategoryType.AXIS_MUNDI),
                    new Symbol("Стена", "Защита внутреннего пространства от хаоса.", SymbolCategoryType.TOWER),
                    new Symbol("Щит", "Сохранение фундаментальных ценностей.", SymbolCategoryType.THRESHOLD_GUARDIAN)
            ),
            Octant.PROPHET_IDEOLOGUE, List.of(
                    new Symbol("Священный огонь", "Преобразующая сила общей веры.", SymbolCategoryType.ANIMA_ANIMUS),
                    new Symbol("Пророчество", "Смысл, ведущий сообщество сквозь кризис.", SymbolCategoryType.WISE_OLD_MAN),
                    new Symbol("Свиток", "Истина, открытая через духовное откровение.", SymbolCategoryType.TREASURE),
                    new Symbol("Маяк", "Ориентир во тьме хаоса.", SymbolCategoryType.REBIRTH),
                    new Symbol("Видение", "Прорыв за пределы текущей реальности.", SymbolCategoryType.TRANSFORMATION),
                    new Symbol("Алтарь", "Точка соединения человека и идеала.", SymbolCategoryType.SACRIFICE)
            ),
            Octant.CAREGIVER_EVERYMAN, List.of(
                    new Symbol("Очаг", "Тепло дома и устойчивость быта.", SymbolCategoryType.GREAT_MOTHER),
                    new Symbol("Хлеб", "Простая польза и забота о близких.", SymbolCategoryType.GREAT_MOTHER),
                    new Symbol("Колыбель", "Защита потенциала и исток жизни.", SymbolCategoryType.CHILD),
                    new Symbol("Чаша", "Вместилище жизни и принятие физической доли.", SymbolCategoryType.DIVINE_COUPLE),
                    new Symbol("Родник", "Неиссякаемый источник жизненной силы.", SymbolCategoryType.REBIRTH),
                    new Symbol("Плуг", "Связь с землей и ежедневный труд.", SymbolCategoryType.PERSONA)
            ),
            Octant.LEADER_REFORMER, List.of(
                    new Symbol("Знамя", "Объединяющий символ общего движения вперёд.", SymbolCategoryType.HERO),
                    new Symbol("Пламя", "Экспрессия и воля к изменению.", SymbolCategoryType.TRANSFORMATION),
                    new Symbol("Меч", "Инструмент рассечения старого порядка.", SymbolCategoryType.SACRIFICE),
                    new Symbol("Горн", "Призыв к пробуждению и действию.", SymbolCategoryType.THRESHOLD_GUARDIAN),
                    new Symbol("Колесница", "Динамический прорыв сквозь сопротивление.", SymbolCategoryType.HERO),
                    new Symbol("Трон", "Принятие ответственности за порядок.", SymbolCategoryType.DEVOURING_FATHER)
            ),
            Octant.SAGE_ANALYST, List.of(
                    new Symbol("Книга", "Накопленное знание и проверенный опыт.", SymbolCategoryType.PERSONA),
                    new Symbol("Кристалл", "Ясная структура и точность фактов.", SymbolCategoryType.SELF),
                    new Symbol("Песочные часы", "Измерение неизбежного движения материи.", SymbolCategoryType.TOWER),
                    new Symbol("Зеркало", "Беспристрастное отражение реальности.", SymbolCategoryType.WISE_OLD_MAN),
                    new Symbol("Сова", "Способность видеть скрытое во тьме.", SymbolCategoryType.WISE_OLD_MAN),
                    new Symbol("Лабиринт", "Структурированный поиск истины.", SymbolCategoryType.LABYRINTH)
            ),
            Octant.SEEKER_INNOVATOR, List.of(
                    new Symbol("Компас", "Ориентация в неизвестном и поиск пути.", SymbolCategoryType.PERSONA),
                    new Symbol("Горизонт", "Граница известного, зовущая к исследованию.", SymbolCategoryType.ABYSS_OCEAN),
                    new Symbol("Корабль", "Средство преодоления стихии бессознательного.", SymbolCategoryType.NIGHT_SEA_JOURNEY),
                    new Symbol("Путеводная звезда", "Далёкий идеал, направляющий поиск.", SymbolCategoryType.TREASURE),
                    new Symbol("Перекрёсток", "Момент выбора неизведанного направления.", SymbolCategoryType.THRESHOLD_GUARDIAN),
                    new Symbol("Крылья", "Преодоление инерции и ограничений.", SymbolCategoryType.TRANSFORMATION)
            ),
            Octant.MASTER_PRAGMATIST, List.of(
                    new Symbol("Молот", "Сила, формирующая материю по замыслу.", SymbolCategoryType.HERO),
                    new Symbol("Механизм", "Сконструированная система, работающая без лишних слов.", SymbolCategoryType.DEVOURING_FATHER),
                    new Symbol("Наковальня", "Место трансформации сырого материала.", SymbolCategoryType.LABYRINTH),
                    new Symbol("Чертёж", "Ментальный план преодоления хаоса.", SymbolCategoryType.AXIS_MUNDI),
                    new Symbol("Мост", "Инженерное связывание двух разделенных берегов.", SymbolCategoryType.THRESHOLD_GUARDIAN),
                    new Symbol("Якорь", "Фиксация результатов в материальном мире.", SymbolCategoryType.SELF)
            ),
            Octant.REBEL_PIONEER, List.of(
                    new Symbol("Оковы", "Структура, которую необходимо сломать.", SymbolCategoryType.SHADOW),
                    new Symbol("Восстание", "Освобождение через преодоление предела.", SymbolCategoryType.TRICKSTER),
                    new Symbol("Маска", "Орудие разрушения чужих иллюзий.", SymbolCategoryType.TRICKSTER),
                    new Symbol("Молния", "Внезапный разрыв привычной реальности.", SymbolCategoryType.TRANSFORMATION),
                    new Symbol("Феникс", "Сгорание дотла ради последующего перерождения.", SymbolCategoryType.REBIRTH),
                    new Symbol("Шторм", "Очищающий хаос, смывающий старые формы.", SymbolCategoryType.ABYSS_OCEAN)
            )
    );

    private SymbolCatalog() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Returns the unmodifiable symbol list for an octant in catalog order.
     *
     * @throws NullPointerException if the octant is null
     */
    public static List<Symbol> of(Octant octant) {
        Objects.requireNonNull(octant, "Octant cannot be null");
        return SYMBOLS.getOrDefault(octant, List.of());
    }

    /**
     * Returns an unmodifiable list of the octant's symbols in the requested category,
     * preserving catalog order. A null category yields an empty list.
     *
     * @throws NullPointerException if the octant is null
     */
    public static List<Symbol> byCategory(Octant octant, SymbolCategoryType categoryType) {
        return of(octant).stream()
                .filter(s -> s.categoryType() == categoryType)
                .toList();
    }
}
