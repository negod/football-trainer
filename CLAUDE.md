# Claude Instructions

Read and follow [`docs/ai-instructions.md`](docs/ai-instructions.md) before changing code in this repository.

## Operational Notes

- Start with `git status --short`.
- Prefer `rg` and `rg --files` for discovery.
- Do not revert unrelated user changes.
- Use `apply_patch` for manual edits.
- Keep backend dependencies pointing inward: `infrastructure -> application -> domain`.
- Update docs whenever behavior, setup, API contracts, schema, or deployment changes.
