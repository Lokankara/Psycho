package com.unconscious.collective.quiz.domain.archetype;

import com.unconscious.collective.quiz.domain.quiz.symbol.Symbol;

import java.io.Serializable;
import java.util.List;

/**
 * The result of matching a {@link SemanticProfile} against the archetypal model.
 *
 * @param octant      resolved semantic octant
 * @param archetype   primary archetype profile
 * @param coreDrive   dominant fundamental motive
 * @param symbols     associated symbols of the collective unconscious
 * @param confidence  strength of the dominant axis, in {@code [0, 1]}
 */
public record ArchetypeMatch(
        Octant octant,
        Archetype archetype,
        CoreDriveType coreDrive,
        List<Symbol> symbols,
        double confidence) implements Serializable {
}
