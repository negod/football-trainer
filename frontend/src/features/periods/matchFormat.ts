import type { MatchFormat } from './api/periodsApi';

export const MATCH_FORMAT_LABELS: Record<MatchFormat, string> = {
  THREE_V_THREE: '3v3',
  FIVE_V_FIVE: '5v5',
  SEVEN_V_SEVEN: '7v7',
  NINE_V_NINE: '9v9',
  ELEVEN_V_ELEVEN: '11v11',
};

export const MATCH_FORMATS = Object.keys(MATCH_FORMAT_LABELS) as MatchFormat[];
