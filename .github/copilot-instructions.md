# Copilot review instructions

Review pull requests for actionable correctness, security, privacy,
data-loss, authorization, migration, API-contract, and regression risks.
Prioritize issues that could cross an ownership/tenant boundary or expose
private data.

Respect the architecture direction `infrastructure -> application -> domain`.
Domain code must remain independent of framework, ORM, servlet and database
code; authorization belongs in use cases, and persistence entities must not
cross the application or HTTP boundary.

For behavioral changes, check that tests cover the changed behavior and
relevant validation, not-found, authorization, and error paths. For database
migrations, flag edits to previously shared changesets, missing
master-changelog entries, unsafe migrations, and mismatches with a
validate-only schema mode.

Only leave comments that identify a concrete risk or defect and explain how
it can be verified or corrected. Avoid style-only feedback, broad
refactoring suggestions, and comments unrelated to the pull request's issue
scope.
