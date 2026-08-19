# GitHub Actions

CI and automatic merge for pull requests. Workflows live under
`.github/workflows`.

## CI pipeline

On pull requests and pushes to the main branch:

| Job | Responsibility |
|---|---|
| `Backend` | Java 21, Maven cache, test and package with `mvn verify` |
| `Frontend` | Node 22, clean npm installs, lint, test and build |

The ruleset for `main` should require both jobs to pass and every review
thread to be resolved. It should not require manual approvals.

Both jobs are gated on `github.event.repository.is_template == false`, so CI
does not run against this template repository itself (it is marked as a
GitHub template repository, and its placeholders and dependency versions are
only meant to be exercised after a real project is copied from it). A repo
created from the template is not itself flagged as a template, so CI runs
normally there.

## Copilot review and automatic merge

GitHub's ruleset (`Automatic Copilot code review`, see "GitHub setup" in the
root `README.md`) requests a Copilot review when a PR becomes `Ready for
review` and after every new push. Drafts are not reviewed or merged.

`auto-merge.yml` enables GitHub's built-in squash auto-merge once a non-draft
PR carries the label `agent: claude` or `agent: codex`. GitHub only performs
the actual merge once `Backend`, `Frontend` and the resolved-threads
requirement are satisfied. No human approval or manual merge is needed.

If Copilot leaves a relevant comment, the authoring agent must address it or
reply with a rationale and resolve the thread. An unresolved thread blocks
auto-merge. Failing CI also blocks merge and must be handled in the same PR
worktree.

The workflow only uses the repository's short-lived `GITHUB_TOKEN`; no
personal token or AI API key is needed.

## Future release flow

Once a production target is chosen, merging to `main` should produce
version-tagged artifacts, run a staging migration and smoke test, and use a
documented rollback on failure.
