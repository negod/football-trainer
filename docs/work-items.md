# Work model: Epic, Feature, Task and Bug

GitHub Issue Forms under `.github/ISSUE_TEMPLATE` are the shared structure for
planning and delivery. The same structure applies even when work is requested
directly in a conversation without first creating a GitHub issue.

| Type | Use when | Expected result |
|---|---|---|
| Epic | A larger product outcome needs several deliveries | Scope, measurable outcome, breakdown into features/tasks |
| Feature | A coherent unit of user value can be built and accepted | Working behavior, tests and documentation |
| Task | A bounded technical, documentation or operational change is needed | The defined change and its verification |
| Bug | Existing behavior deviates from what's expected | Reproduction, regression test, root-cause fix and verification |

## Short requests to the AI assistant

- `Plan an epic: <goal>`: formulate the epic and break it down. Do not
  implement every feature inside it unless explicitly asked.
- `Build a feature: <user value>`: derive missing, reasonable details from the
  feature template, implement the full vertical slice, test it and update the
  docs.
- `Implement feature F-123` / `Implement epic E-45`: an existing issue is the
  source of the requirements. Analyze, break it into its own Task/Bug issues,
  plan and execute them one at a time — see "Implementing an epic or feature
  from an issue" below.
- `Do a task: <goal>`: perform the bounded work and verify its completion
  criteria.
- `Fix the bug: <symptom>`: reproduce first, add a regression test when
  practical, fix the root cause and verify nearby flows.

The assistant should read the repository's current state and instructions,
surface important assumptions, and continue independently once those
assumptions are safe and reversible. Ask a question only when a missing
decision would materially change product behavior, a security boundary, the
data model, or delivery scope.

A direct request does not automatically create a GitHub issue. Say, for
example, `create a feature issue for ...` when the work should first be
registered on GitHub, or `implement feature F-123` (below) when an existing
issue should be the source of both requirements and execution.

## Referring to issues

The GitHub issue number is the only identity — there is no separate counter
per type. `F-123` is shorthand for "issue #123 with the `type: feature`
label", matching `E-` for epic, `T-` for task and `B-` for bug. The letter
only indicates which template was used; always use `#123` in actual GitHub
references (commits, PR descriptions, checklists).

## Implementing an epic or feature from an issue

`Implement feature F-123` or `Implement epic E-45` starts this flow. The
command owns the whole delivery until every underlying issue is done or
blocked.

1. **Analyze.** Read the full issue (every field), its parent, the code it
   touches and the relevant architecture rules. Check whether the breakdown
   already exists — search for an existing checklist or linked issues on the
   parent before creating new ones, so two parallel AI sessions don't break
   down the same feature twice.
2. **Break down.** Create Task and Bug issues (`.github/ISSUE_TEMPLATE`) for
   whatever is missing. Every new issue must:
   - link back to the parent in **two** places, not just one: the template's
     `parent` text field ("Related epic or feature"), and GitHub's actual
     Relationships link (sub-issue). The text field is documentation only — it
     creates no real relation. Set the Relationships link in three steps,
     using the new (child) issue's `<child-number>` and the parent's
     `<parent-number>`:
     1. Look up the child issue's numeric database `id` (different from its
        `<child-number>`): `gh api repos/{owner}/{repo}/issues/<child-number> -q .id`.
     2. Create the relationship on the parent issue — note the path segment
        here is the parent's issue **number**, while `sub_issue_id` in the
        body is the child's database **id** from step 1:
        `gh api repos/{owner}/{repo}/issues/<parent-number>/sub_issues -F sub_issue_id=<child-database-id>`.
        (`gh issue create`/`edit` does not support this directly.)
     3. Verify on the child issue:
        `gh api repos/{owner}/{repo}/issues/<child-number> -q .parent_issue_url`;
   - be small and independent enough that a single AI session can own it
     without touching the same files as another issue in progress (see
     "Multiple AI assistants at once" below);
   - state explicit dependencies ("Depends on #124") when order matters,
     rather than pretending the work can happen in parallel.
   Update the parent's breakdown list (the epic template's "Features and
   tasks" field, or a comment on a feature) so every created issue is visible
   in one place: `- [ ] #124 ...`.
3. **Plan.** Comment on the parent issue with the execution order: which
   issues are independent and can be taken in any order, which must wait on
   others, and the overall technical approach.
4. **Run the review gate.** Before taking the next Task/Bug, check and, if
   needed, handle pending cross-agent review per "Review gate before a new
   task" below.
5. **Execute one issue at a time**, in dependency order:
   1. Mark that you're taking it (comment or assignee) before starting, so
      another AI session sees it's taken. At the same time, move the
      Task/Bug, its parent Feature and its parent Epic to `In progress` per
      the status rules below, and verify the update.
   2. Create your own worktree and branch for the issue per "Isolated
      worktree per Task/Bug" below. Never switch branches in the main
      checkout, and never commit directly to `main`.
   3. Implement within the stated scope, nothing more.
   4. Test per the issue's verification field and this document's shared
      Definition of Done.
   5. Make every commit belong to exactly one Task/Bug issue and include its
      number in the commit subject, e.g. `Refs #124: add item domain model`.
      Push only the issue's own branch. The PR description uses `Closes #124`
      when the PR alone completes the task.
   6. Open a PR against `main` that links the issue and carries the correct
      agent label. Move the Task/Bug to `In review` once implementation and
      self-verification are done and the PR is actually waiting on review.
      Once the PR leaves draft, GitHub enables squash auto-merge
      automatically. `Backend`, `Frontend` (or your CI job names) and every
      review thread must be resolved; no manual approval or merge is
      expected — but a Copilot review is still expected to arrive (see
      "Review gate before a new task"). Shortly after the PR becomes
      reviewable, confirm the review actually arrives; a merge completed
      before then is not an exception to this requirement — see the same
      section for how late findings are handled.
   7. Update the issue's completion criteria and verify the project status
      before moving on to the next unblocked issue.
6. **Close the delivery.** Once every underlying issue for a feature is done:
   verify the feature's own acceptance criteria end-to-end and
   update/close the feature issue. For an epic, repeat the same one level up
   once all its features are done.

## Bugs discovered during work

Do not fix unplanned issues in passing unless they are trivial and directly
block the current issue's completion criteria. Instead create a new Bug issue
via the template, link it to the relevant feature/epic, mention it in the
current issue's comments or PR, and continue the original work.

## Review gate before a new task

**Every PR against `main` must have a Copilot review. This is not an optional
extra step** — GitHub's active ruleset should be configured to automatically
request a Copilot review when a PR becomes reviewable and after every new
push (see "GitHub setup" in the root `README.md`). Draft PRs are not reviewed
(unless the ruleset explicitly enables `review_draft_pull_requests`). The
author is therefore responsible for marking a PR `Ready for review` only once
implementation, self-review and relevant tests are done.

Copilot leaves comments and suggestions but never `APPROVE` or
`REQUEST_CHANGES`. If `required_approving_review_count` is `0` in your
ruleset, GitHub cannot technically block merge on a missing or unresolved
Copilot review — a formal branch-protection gate would require an `APPROVE`
review, which Copilot never gives. The requirement is therefore
**procedural, not technical**: the responsible agent must itself verify the
review actually arrived before considering the work done, and judge every
relevant comment — either address it or reply with a short rationale for why
no change is needed, then resolve the thread. Unresolved review threads and
failing CI still block automatic merge.

Known pitfall: if a repository's Copilot code review consistently doesn't
arrive despite CI passing and the ruleset being correctly configured, check
the account's Copilot plan — reviews require Copilot Pro or higher, not the
free tier — before suspecting the ruleset itself.

Before an agent takes a new Task/Bug it should:

1. list its own open, non-draft PRs and read the Copilot review, review
   threads and pending checks for the current head commit;
2. prioritize a PR that blocks the next task, then the oldest PR with
   unresolved relevant comments;
3. address findings in the PR's existing worktree, run the relevant
   verification, and push to the same issue branch;
4. reply to and resolve every handled thread, or document a technical
   blocker on the PR before starting new work;
5. confirm a Copilot review has actually run since the last push. If it's
   missing: request Copilot manually as a reviewer, and if that fails (e.g.
   "not a collaborator"), report it to the repository owner (account's
   Copilot plan or premium-request quota, not the ruleset — see above)
   instead of assuming the automation succeeded or that its absence is
   harmless.

Because auto-merge (step 5.6 above) can be enabled and complete before
Copilot has left its review, it isn't always possible to block the merge
itself on the review. If a Copilot review lands after merge and contains
relevant findings: handle them as newly discovered bugs/improvements per
"Bugs discovered during work" above, rather than ignoring them because the PR
is already closed.

An explicit user priority overrides the review queue. For security-critical,
hard-to-judge or architecture-wide changes, a human or another agent's review
may be requested in addition to Copilot.

## Multiple AI assistants at once

This workflow assumes the repository owner often runs two AI sessions
(Claude, ChatGPT/Codex, or others) in parallel against the same repo. The
rules above exist to make that safe:

- The breakdown in step 2 must always make issues small and file-wise
  independent of each other — that's what makes parallel work possible, not
  an afterthought.
- Always check whether an issue is already taken (assignee or comment)
  before starting it; pick a different unblocked issue if it's taken.
- A dedicated worktree and branch per Task/Bug issue is mandatory. A branch
  isolates history but not the files in a checkout; a different branch alone
  is not enough.
- The main checkout is a stable integration/demo environment. No AI may run
  `git switch` or `git checkout` there for implementation work, and no agent
  may use another agent's worktree.
- If an issue turns out to depend on unmerged work in another issue in
  progress: write that in a comment and pause the issue instead of guessing
  or duplicating the work.

### What work can run in parallel?

- Normally start with features from **different epics**. That reduces the
  risk of both touching the same domain package, view or migration chain.
- Different epics are not a guarantee of independence. Always check
  dependencies, affected files, API contracts, migration prefixes and runtime
  resources before starting.
- Two features within the same epic may run in parallel only when they have
  no dependency on each other and their tasks have clearly separate file
  areas.
- Two agents' tasks must never simultaneously own the same files, schema
  objects or contracts. Move or pause a task if overlap is discovered.
- An agent works on one Task/Bug at a time. Feature and epic issues collect
  the delivery but are never used as a shared implementation branch.

A good example of parallelization is a frontend task in one bounded feature
package and a backend task in a different epic with no shared API. Two tasks
that both edit the same migration changelog, the same domain model or the
same route are not independent even if their parents live in different
epics.

## Isolated worktree per Task/Bug

Each AI session creates its worktree as a **sibling directory** to the repo,
never inside the main checkout or another worktree. Recommended structure:

```text
project/
├── my-app/                       # stable main checkout/demo
└── my-app-worktrees/
    ├── issue-124-item-model/     # Claude, task #124
    └── issue-130-list-frontend/  # Codex/ChatGPT, task #130
```

Create the worktree from an updated `origin/main` without switching branches
in the main checkout:

```bash
git fetch origin main
git worktree add -b claude/issue-124-item-model \
  ../my-app-worktrees/issue-124-item-model origin/main
```

Codex/ChatGPT uses the matching agent prefix, e.g.
`codex/issue-130-list-frontend`. To resume an already-created branch, omit
`-b`:

```bash
git worktree add ../my-app-worktrees/issue-124-item-model \
  claude/issue-124-item-model
```

After creating it, the agent should:

1. change the terminal's working directory to the new worktree;
2. check `git status --short` and `git branch --show-current`;
3. comment on the issue with the agent, branch and worktree used;
4. do all file changes, tests, commits and pushes from there;
5. verify before staging that only the task's files changed;
6. use explicit paths for `git add`, never a blanket `git add -A` in a mixed
   worktree.

Every commit subject includes the issue number. A branch must not collect
several independent tasks:

```text
Refs #124: add item domain model
Refs #124: verify item persistence
```

The PR links both the task and its parent feature, uses `Closes #124`, and
must not close the whole feature until all its sub-issues and end-to-end
criteria are done.

Remove a worktree only after merge, and only from a different checkout.
Verify the exact path and that the worktree is clean before removing it:

```bash
git -C ../my-app-worktrees/issue-124-item-model status --short
git worktree remove ../my-app-worktrees/issue-124-item-model
git worktree prune
```

Forced removal or deleting the directory directly on disk is not used as
routine cleanup.

### Ports, processes and database

- Only the main checkout owns the default stack's ports and database; it's
  used for stable demo/integration.
- Worktrees run tests and builds without their own long-lived runtime by
  default.
- If a worktree needs interactive execution, the agent reserves unique
  frontend/backend ports and its own database name, and writes the
  reservation on the issue before starting the processes.
- Don't run the root dev command from multiple worktrees in parallel — default
  ports, dev-process metadata and the default database will collide.
- Two ongoing schema changes must never use the same database, even when they
  use different migration files.
- Secrets and private data are not copied between worktrees.

## Statuses and swimlanes

The GitHub Project is the canonical, live status picture for all planned
work. Issue state (`open`/`closed`), comments, branches and PRs are
supplementary and never replace the project's `Status` field. An agent that
changes the work state is also responsible for updating the field
immediately and reading the value back to verify the change succeeded.

The project's status field has six values:

| Status | Meaning | Rule to move on |
|---|---|---|
| Triage | New item not yet assessed | Type, scope and relevance are established |
| Backlog | Accepted work, not yet ready or prioritized | Requirements are refined and the work is prioritized |
| Ready | Clear enough to start | The template's Definition of Ready is met |
| In progress | Active development is happening | An owner exists and work has actually started |
| In review | The solution is done and being verified | A reviewable PR is open, or the result awaits approval |
| Done | Definition of Done is met | The issue is closed and any code is merged |

Normal flow is:

`Triage → Backlog → Ready → In progress → In review → Done`

### Required status transitions

| Event | Task/Bug | Parent Feature | Parent Epic |
|---|---|---|---|
| New issue created | `Triage` | Unchanged | Unchanged |
| Accepted but not ready to start | `Backlog` | Unchanged | Unchanged |
| Requirements, dependencies and verification are clear | `Ready` | At least `Ready` if the whole feature is ready to start | Unchanged |
| Agent takes the issue and creates a worktree | `In progress` | `In progress` | `In progress` |
| Active implementation paused but resumable without external action | `Ready` | Keep `In progress` if other tasks are active | Keep `In progress` |
| Implementation and self-verification are done; PR awaits review | `In review` | Keep `In progress` | Keep `In progress` |
| PR is merged and the issue's Definition of Done is met | `Done`, issue closed | Re-evaluate sub-issues | Re-evaluate features |
| All required sub-issues and feature criteria are done | — | `Done`, issue closed | Re-evaluate features |
| All required features and epic criteria are done | — | — | `Done`, issue closed |

`In progress` means someone is actually working on the item, not just that
it's been broken down or prioritized. `In review` means the implementation is
ready for external review. An early draft PR still used for active
implementation therefore leaves the issue's status at `In progress`; only
once the PR is reviewable does it move to `In review`.

### Status discipline for the agent

At every work session, the agent should:

1. **Before starting:** check the issue, parent, assignee/claim comment,
   dependencies and current project status. Don't take an item that's
   already active.
2. **On starting:** comment with agent, worktree and branch. Set Task/Bug,
   Feature and Epic to `In progress`; only update ancestors that haven't
   already progressed further.
3. **While working:** keep the status unchanged while work is genuinely in
   progress. Comments are for important decisions and dependencies, not a
   substitute for status.
4. **On waiting/blocking:** add the right label (`status: waiting` or
   `status: blocked`) and a comment stating the cause, what's needed, and who
   can resolve it. Move back to `Ready` only if no active implementation is
   underway and someone else could pick it up; otherwise keep the work
   status. Remove the label once resolved.
5. **On review:** confirm tests and self-review are done, link the PR, and
   move the Task/Bug to `In review`. Verify the field after writing it.
6. **On merge:** verify the Definition of Done is met, close the issue and
   set it to `Done`. Then check the parent Feature and Epic and close them
   only if every required child and their own acceptance criteria are done.
7. **On reopening or new review findings:** move back to `Triage` if scope
   must be re-evaluated, otherwise to `In progress` once the fix actually
   starts. Reopen/move back the parent if its delivery is no longer complete.

Status updates happen in the same work session as the event. The agent
should not wait until the end of a feature to retroactively update the
board, and should not report a status to the user without first reading it
back from the GitHub Project.

Blocking is not itself a project status. Add `status: blocked` when work
can't continue, or `status: waiting` when it's waiting on a decision,
information or an external party. Normally keep the item's work status; move
to `Ready` only per the pause rule above. Always comment what it's waiting on
and who can resolve it.

Per-item rules:

- An epic moves to `In progress` when its first underlying feature starts,
  and to `Done` when every required sub-issue is done.
- A feature must meet the Definition of Ready before `Ready` and the full
  Definition of Done before `Done`.
- A small, clear task may go directly from `Triage` to `Ready`.
- A critical bug may go directly from `Triage` to `In progress`, but still
  gets a reproduction and a regression test.
- Rejected work is closed with reason `Not planned`; no separate status is
  created.
- Reopened work moves back to `Triage` for reassessment.

When multiple agents work in parallel, status is also a locking mechanism:
`In progress` together with the claim comment shows who owns the work. If the
field, the comment and reality disagree, whichever agent notices the
mismatch corrects the status and documents why before starting new work.

The GitHub Project should automate: new issues added as `Triage`, closed
issues and merged PRs moved to `Done`, reopened issues moved back to
`Triage`, and completed items archived after some period (e.g. 30 days).
Status is set in the project or by its automations, not in the Issue Forms.
Automation is a safety net; the agent is still responsible for the
`Ready`, `In progress` and `In review` transitions and for correct parent
levels.

## Shared Definition of Done

An implemented work item isn't done until:

- the acceptance criteria are met;
- relevant automated tests pass;
- validation, not-found and authorization are tested where they apply;
- private data and ownership boundaries are handled per the architecture
  rules;
- the frontend covers relevant loading, empty, error and success states;
- accessibility and mobile layout have been verified when the UI changed;
- API, schema, setup, operations and user-facing behavior are documented
  wherever they changed;
- verification performed and any remaining risks are reported.
