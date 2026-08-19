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

## Automatic response to Copilot review

`copilot-review-response.yml` triggers on `pull_request_review` (type
`submitted`) and, if the reviewer is `copilot-pull-request-reviewer[bot]`,
the PR is not draft, and it carries the label `agent: claude` or
`agent: codex`, runs `anthropics/claude-code-action` headless on the PR's own
branch. It reads unresolved review threads, addresses them or replies with a
rationale and resolves the thread, runs backend/frontend verification for
whatever it touches, and pushes a new commit to the same branch — without a
human needing to start an interactive session to do it. This does not
replace the review gate in `docs/work-items.md` ("Review gate before a new
task"); that still applies as a fallback and as a check that a Copilot
review actually arrived.

A new push triggers GitHub's ruleset for a fresh Copilot review, which can
trigger the workflow again. That's intentional — the same iteration a human
would do — and it stops itself as soon as no new unresolved finding remains,
since the step then neither commits nor pushes. As a guard against a loop
that doesn't stop itself, the workflow counts `claude[bot]` commits on the
PR's branch and fails after five, so a human has to look at the rest
manually.

The workflow requires the secret `ANTHROPIC_API_KEY` in the repository's
Actions secrets. No agent should set or manage the key's actual value — the
repository owner adds it manually under Settings → Secrets and variables →
Actions.

## Future release flow

Once a production target is chosen, merging to `main` should produce
version-tagged artifacts, run a staging migration and smoke test, and use a
documented rollback on failure.
