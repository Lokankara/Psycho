export type Axis = 'X' | 'Y' | 'Z';
export type Pole = 'POSITIVE' | 'NEGATIVE';

export interface Question {
  id: string;
  axis: Axis;
  title: string;
  negative: string;
  positive: string;
}

export interface QuizAnswer {
  questionId: string;
  chosenPole: Pole;
}

export interface AssessmentPayload {
  sessionId: string;
  answers: QuizAnswer[];
}

export interface Vector3D {
  x: number;
  y: number;
  z: number;
}

export interface SymbolView {
  name: string;
  meaning: string;
  categoryType: string;
}

export interface AnalysisResult {
  objectId: string;
  position: Vector3D;
  octant: string;
  dominant: string;
  trajectory: string | null;
  shadowManifestation: string;
  drive: string;
  confidence: number;
  symbols: SymbolView[];
}

export interface ResultSymbol {
  name: string;
  meaning: string;
  category: string;
}

export interface QuizResultResponse {
  objectId: string;
  x: number;
  y: number;
  z: number;
  coordinateLabel: string;
  octant: string;
  octantName: string;
  archetype: string;
  meaning: string;
  dominantArchetypes: string;
  narrativeTrajectory: string;
  shadow: string;
  drive: string;
  driveLabel: string;
  driveDescription: string;
  confidence: number;
  symbols: ResultSymbol[];
}

export interface HistoryEntry {
  id: number;
  createdAt: string;
  x: number;
  y: number;
  z: number;
  octantCode: string;
}
