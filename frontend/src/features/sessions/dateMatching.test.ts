import { describe, expect, it } from 'vitest';

import { countMatchingDates } from './dateMatching';

describe('countMatchingDates', () => {
  it('counts only dates on the selected weekdays across a leap year month boundary', () => {
    // Feb 1 2024 is a Thursday; Feb 29 2024 is the leap day (a Thursday, not selected).
    // Mondays: 5, 12, 19, 26. Wednesdays: 7, 14, 21, 28. Mar 1 is a Friday.
    expect(countMatchingDates('2024-02-01', '2024-03-01', ['MONDAY', 'WEDNESDAY'])).toBe(8);
  });

  it('includes both endpoints when they fall on a selected weekday', () => {
    expect(countMatchingDates('2026-01-06', '2026-01-13', ['TUESDAY'])).toBe(2);
  });

  it('returns 0 when no weekday is selected', () => {
    expect(countMatchingDates('2026-01-01', '2026-06-01', [])).toBe(0);
  });

  it('returns 0 when no weekday in the range matches', () => {
    expect(countMatchingDates('2026-01-06', '2026-01-06', ['SUNDAY'])).toBe(0);
  });
});
