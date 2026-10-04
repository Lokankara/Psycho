export const octantNames: Record<string, string> = {
  GUARDIAN_LEADER: 'Хранитель / Духовный лидер',
  PROPHET_IDEOLOGUE: 'Пророк / Идеолог',
  CAREGIVER_EVERYMAN: 'Опекун / Обыватель',
  LEADER_REFORMER: 'Вождь / Реформатор',
  SAGE_ANALYST: 'Мудрец / Аналитик',
  SEEKER_INNOVATOR: 'Искатель / Инноватор',
  MASTER_PRAGMATIST: 'Мастер / Прагматик',
  REBEL_PIONEER: 'Бунтарь / Первопроходец',
};

export function octantLabel(code: string): string {
  return octantNames[code] ?? code;
}
