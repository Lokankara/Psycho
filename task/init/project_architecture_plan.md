# Architecture Plan: Collective Unconscious Engine

## 1. Domain Vision & Core Concepts
The **Collective Unconscious Engine** is a high-performance Java 25 / Spring 7 service designed to model, analyze, and quantify psychological and semantic archetypes within a continuous **3D Vector Space** $(X, Y, Z)$.

### Core Axes & Poles
1. **Axis $X$ — Social Integration Vector:**
   - $X^-$ (Individualism / Autonomy): Autonomy, independence, breaking external constraints.
   - $X^+$ (Collectivism / Belonging): Teamwork, community, tradition, social cohesion.
2. **Axis $Y$ — Environmental Modification Vector:**
   - $Y^-$ (Stability / Control): Rules, order, risk mitigation, institutional permanence.
   - $Y^+$ (Transformation / Risk): Breakaway innovation, reform, boundary pushing, chaos navigating.
3. **Axis $Z$ — Epistemological Focus Vector:**
   - $Z^-$ (Materialism / Empirical): Facts, physical results, pragmatic outcomes, immediate utility.
   - $Z^+$ (Abstraction / Transcendence): Meaning, philosophy, fundamental laws, conceptualization.

### 8 Archetypal Octants
- **$(-X, +Y, +Z)$**: `SEEKER_INNOVATOR` (Искатель / Инноватор)
- **$(-X, +Y, -Z)$**: `REBEL_PIONEER` (Бунтарь / Первопроходец)
- **$(-X, -Y, +Z)$**: `SAGE_ANALYST` (Мудрец / Аналитик)
- **$(-X, -Y, -Z)$**: `MASTER_PRAGMATIST` (Мастер / Прагматик)
- **$(+X, +Y, +Z)$**: `PROPHET_IDEOLOGUE` (Пророк / Идеолог)
- **$(+X, +Y, -Z)$**: `LEADER_REFORMER` (Вождь / Реформатор)
- **$(+X, -Y, +Z)$**: `GUARDIAN_LEADER` (Хранитель / Духовный лидер)
- **$(+X, -Y, -Z)$**: `CAREGIVER_EVERYMAN` (Опекун / Обыватель)

---

## 2. Technical Stack & Java 25 Capabilities
- **Language:** Java 25 with enabled Preview Features.
- **Framework:** Spring Boot 3.4+ / Spring 7 (Virtual Threads enabled by default).
- **Key Java 25 Features Utilized:**
  - `ScopedValue`: Light-weight, immutable context propagation for request tracing across Virtual Threads without `ThreadLocal` overhead.
  - `Record Patterns & Switch Pattern Matching`: Expressive destructuring of coordinates and assessment payloads.
  - `Math.clamp()`: Enforcing strict $[-1.0, 1.0]$ bounds on coordinate spaces.
  - `Sealed Interfaces`: Modeling strict, type-safe Archetype hierarchies and quiz response types.