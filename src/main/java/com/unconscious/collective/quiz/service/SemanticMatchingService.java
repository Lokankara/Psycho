package com.unconscious.collective.quiz.service;

import com.unconscious.collective.quiz.domain.value.Coordinates;
import com.unconscious.collective.quiz.domain.archetype.Octant;
import com.unconscious.collective.quiz.domain.archetype.SemanticProfile;
import com.unconscious.collective.quiz.domain.archetype.Archetype;
import com.unconscious.collective.quiz.domain.archetype.ArchetypeMatch;
import com.unconscious.collective.quiz.domain.archetype.PersonaArchetype;
import com.unconscious.collective.quiz.domain.archetype.CoreDriveType;
import com.unconscious.collective.quiz.domain.symbol.Symbol;
import com.unconscious.collective.quiz.domain.symbol.SymbolCatalog;
import com.unconscious.collective.quiz.domain.value.Axis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Processes a scored {@link SemanticProfile} and matches it against the
 * archetypal model: the dominant octant, the primary archetype, the leading
 * {@link CoreDriveType} and the associated {@link Symbol symbols}.
 */
@Service
public class SemanticMatchingService {

    private static final Logger log = LoggerFactory.getLogger(SemanticMatchingService.class);

    public ArchetypeMatch match(SemanticProfile profile) {
        Coordinates coordinates = profile.coordinates();
        Octant octant = profile.octant();
        CoreDriveType coreDrive = dominantCoreDrive(coordinates);
        Archetype archetype = dominantArchetype(coreDrive);
        List<Symbol> symbols = SymbolCatalog.of(octant);

        ArchetypeMatch match = new ArchetypeMatch(
                octant,
                archetype,
                coreDrive,
                symbols,
                strength(coordinates));

        log.debug("Matched octant {} to archetype {} and core drive {} with confidence {}",
                octant, archetype.getTitle(), coreDrive, match.confidence());
        return match;
    }

    private static Archetype dominantArchetype(CoreDriveType drive) {
        return Arrays.stream(PersonaArchetype.values())
                .filter(archetype -> archetype.drive() == drive)
                .findFirst()
                .orElse(PersonaArchetype.EVERYMAN);
    }

    private static CoreDriveType dominantCoreDrive(Coordinates coordinates) {
        Axis dominant = Axis.X;
        for (Axis axis : Axis.values()) {
            if (Math.abs(coordinates.value(axis)) > Math.abs(coordinates.value(dominant))) {
                dominant = axis;
            }
        }
        return CoreDriveType.of(dominant, coordinates.pole(dominant));
    }

    private static double strength(Coordinates coordinates) {
        double max = 0.0;

        for (Axis axis : Axis.values()) {
            max = Math.max(max, Math.abs(coordinates.value(axis)));
        }
        return max;
    }
}
