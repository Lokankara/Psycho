export type BddStatus = 'NOT_RUN' | 'PASSED' | 'FAILED';

export interface ProgramIncrementOption {
  id: number;
  code: string;
  name: string;
  goal: string | null;
}

export interface SprintOption {
  id: number;
  code: string;
  name: string;
  goal: string | null;
  startDate: string | null;
  endDate: string | null;
}

export interface StoryCard {
  id: number;
  code: string;
  title: string;
  storyPoints: number | null;
  priority: string | null;
  sprintId: number | null;
  epicId: number | null;
  acceptanceCriteria: string | null;
  bddStory: string | null;
  executionStatus: BddStatus;
  lastRunAt: string | null;
}

export interface TaskCard {
  id: number;
  code: string;
  title: string;
  storyId: number | null;
  bddStory: string | null;
  executionStatus: BddStatus;
  lastRunAt: string | null;
}

export interface KanbanColumn {
  code: string;
  name: string;
  position: number;
  stories: StoryCard[];
  tasks: TaskCard[];
}

export interface BoardResponse {
  programIncrements: ProgramIncrementOption[];
  sprints: SprintOption[];
  activeProgramIncrementId: number | null;
  activeSprintId: number | null;
  columns: KanbanColumn[];
}

export type CardKind = 'story' | 'task';

export interface DragPayload {
  kind: CardKind;
  id: number;
}
