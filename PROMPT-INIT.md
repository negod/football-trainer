# Initial Prompt

Paste the block below into a fresh Claude or ChatGPT (Codex) session to kick
off a new project from this template. Use it after you've copied the
template into a new (ideally empty) GitHub repository and completed the
steps in [`README.md`](README.md) under "GitHub setup for parallel agents"
(labels, Project board, ruleset, secrets).

Give the *same* prompt, with the *same* answers, to a second assistant in a
separate session/worktree once the first session has finished the setup
questions below — that's what lets Claude and ChatGPT build the repo in
parallel from day one, per [`docs/work-items.md`](docs/work-items.md).

---

```text
Read AGENTS.md (or CLAUDE.md) and docs/ai-instructions.md first, then
docs/work-items.md. This repository is an unconfigured copy of an AI
project template.

Before changing any file, running the bootstrap script, or touching GitHub
(issues, labels, project, commits), ask me the following questions and wait
for my answers:

1. What is this project's product goal, in one or two sentences? (This
   becomes the first Epic.)
2. Project display name, lowercase slug, Java package (e.g.
   se.company.project), and database name — the values that replace
   __PROJECT_NAME__ / __PROJECT_SLUG__ / __JAVA_PACKAGE__ /
   __JAVA_PACKAGE_PATH__ / __DB_NAME__ throughout the repo.
3. The local filesystem path where this project's main checkout should
   live, since docs/work-items.md requires every Task/Bug to be done in a
   sibling worktree next to it (e.g. .../my-app/ next to
   .../my-app-worktrees/). If the current working directory isn't already
   that path, tell me the mismatch and stop rather than assuming it's fine.
4. The GitHub owner/repo this was (or will be) pushed to — replaces
   __OWNER__/__REPO__ in README.md and .github/ISSUE_TEMPLATE/config.yml.
   Confirm whether the GitHub Project's Status field
   (Triage/Backlog/Ready/In progress/In review/Done) and the type/agent
   labels described in docs/work-items.md already exist on that repo. If
   they don't, say so instead of silently skipping the workflow or
   guessing they're there.
5. Do you want the first Epic broken down and planned only for now, or
   also implemented immediately (including opening its Task/Bug issues and
   starting the first one)?

Once I've answered all five: replace the placeholders
(scripts/bootstrap-template.sh handles most of them), run the backend and
frontend production builds to confirm the template compiles clean with the
new names, then act on question 5 — either just create and plan the Epic
issue, or also break it into Task/Bug issues and start the first one in its
own worktree, per docs/work-items.md.
```
