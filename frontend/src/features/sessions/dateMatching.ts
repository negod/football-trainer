import type { Weekday } from './api/sessionsApi';

const WEEKDAY_BY_JS_DAY: Weekday[] = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];

export const WEEKDAYS_IN_ORDER: Weekday[] = [
  'MONDAY',
  'TUESDAY',
  'WEDNESDAY',
  'THURSDAY',
  'FRIDAY',
  'SATURDAY',
  'SUNDAY',
];

export const WEEKDAY_LABELS: Record<Weekday, string> = {
  MONDAY: 'Mon',
  TUESDAY: 'Tue',
  WEDNESDAY: 'Wed',
  THURSDAY: 'Thu',
  FRIDAY: 'Fri',
  SATURDAY: 'Sat',
  SUNDAY: 'Sun',
};

/**
 * Client-side preview count only — mirrors the pure calendar arithmetic of
 * the backend's SessionGenerationService.generateDates (weekday matching
 * within a date range), not a business rule worth avoiding duplication of
 * the way MatchFormat.suggestedFor's age table was. The actual generation
 * (and its idempotency) always happens server-side via generateSessions.
 */
export function countMatchingDates(startDate: string, endDate: string, weekdays: Weekday[]): number {
  if (weekdays.length === 0) {
    return 0;
  }
  const selected = new Set(weekdays);
  const cursor = new Date(`${startDate}T00:00:00`);
  const end = new Date(`${endDate}T00:00:00`);
  let count = 0;
  while (cursor <= end) {
    if (selected.has(WEEKDAY_BY_JS_DAY[cursor.getDay()])) {
      count += 1;
    }
    cursor.setDate(cursor.getDate() + 1);
  }
  return count;
}
