# Design Decisions

This document explains the three design patterns used in the project: the problem each one
solves, why that pattern fits, what we considered instead, and how it helps the design handle
future change. See [`class-diagram.md`](class-diagram.md) for the classes involved and
[`er-diagram.md`](er-diagram.md) for the schema they sit on top of.

We did not force in a Factory, Singleton, Observer-everywhere, or any other pattern beyond the
three below. Content creation, for example, is a plain `new Content()` plus setters — there is no
complex construction logic that would justify a Factory, so adding one would only be decoration.

---

## 1. Managing the post lifecycle (State pattern)

**Problem.** A post moves through a specific sequence of stages — Draft, Validated, Scheduled,
Publishing, Published — with several conditional side branches (failed validation sends it back
to Draft, a scheduled post can be cancelled, publishing can fail). Every screen and every service
method that changes a post's status needs to agree on which transitions are legal. Without a
single place owning that rule set, the same `if (status == ...)` logic would get duplicated (and
drift out of sync) across `ContentController`, `PostController`, and `ScheduledPostsController`.

**Why this pattern.** `PostLifecycle` centralizes the transition rules as a lookup table
(`PostStatus -> allowed next PostStatus`). `PostService.moveTo(...)` calls
`postLifecycle.assertTransition(current, target)` before ever touching the database, so an
invalid move (e.g. scheduling a post straight from Draft) is rejected in one place, with one
error message, regardless of which screen triggered it.

**How it improves the design.** The status logic is testable in isolation
(`PostLifecycleTest`) without touching the database or the UI. `PostService` methods stay short
because they don't each re-implement "is this move allowed" — they just describe *what* the
action does (`schedule`, `cancel`, `markPublished`) and delegate the *is this legal* question.

**Alternatives considered.** A `switch`/`if` chain directly inside `PostService` would work for
the current 7 statuses, but it mixes the transition *rules* with the transition *side effects*
(recording history, notifying listeners), and every new status or branch would mean editing a
growing conditional block rather than one table entry. A full class-per-state object (a `DraftState`,
`ScheduledState`, etc., each implementing its own `next()`) was also considered, but for a status
set this size a transition table is equivalent in behavior and simpler to read — the extra
indirection of a state-object hierarchy wasn't buying us anything our project actually needed.

**Future benefit.** Adding a new status (e.g. an `ARCHIVED` state after Published) means adding
one entry to the `ALLOWED_TRANSITIONS` map — no existing `PostService` method needs to change.
The transition rules can also be unit-tested exhaustively without spinning up JavaFX or SQLite.

---

## 2. Validating content per platform (Strategy pattern)

**Problem.** Facebook, Instagram, and X each have different rules for whether a piece of content
is postable on them — X enforces a character limit, Instagram requires image/video/promotional
content, Facebook just needs non-empty text under a higher limit. This logic needs to run at the
Draft -> Validated step, and it must be easy to add a new platform later without touching the
rules of the existing ones.

**Why this pattern.** Each platform's rule is its own class (`FacebookRules`, `InstagramRules`,
`XRules`) implementing a common `PlatformRules` interface. `PlatformRulesRegistry` picks the
right implementation by platform name, and `PostService.markValidated(...)` calls
`rules.validate(content)` without knowing or caring which concrete class it got.

**How it improves the design.** `PostService` has no platform-specific `if` statements at all —
it treats "validate this content for this platform" as one call. Each platform's rule is
independently testable (`PlatformRulesTest`) and independently readable — you can look at
`XRules.java` alone to know exactly what X requires.

**Alternatives considered.** A single method with an `if (platform.equals("X")) ... else if
(platform.equals("Instagram")) ...` chain was the simplest alternative, but it means every new
platform edits a method that already handles every other platform, growing the risk of breaking
an existing platform's rule while adding a new one. Putting validation logic directly on a
`Platform` enum was also considered, but that couples the domain model (a simple name/id record)
to business rules that change independently of what a platform *is*.

**Future benefit.** Supporting a new platform (e.g. LinkedIn) means writing one new class that
implements `PlatformRules` and adding one line to `PlatformRulesRegistry` — no existing platform's
class is touched, and no existing test can be broken by the change.

---

## 3. Reacting to publishing outcomes (Observer pattern)

**Problem.** When a post finishes publishing (successfully or not), more than one thing needs to
happen: a permanent history row must be written to `publishing_history`, and other parts of the
app may want to know it happened (an in-app activity log, and potentially a future notification
mechanism) — without `PostService` needing to know about every interested party in advance.

**Why this pattern.** `PostService` holds a list of `PostEventListener`s and calls
`onPublishingOutcome(post, result, message)` on each one inside `markPublished`/`markFailed`,
after writing the `PublishingHistoryEntry` row. `ActivityLogListener` is the one listener
currently registered (from `PostController`), but `PostService` has no reference to it by name —
it only knows the `PostEventListener` interface.

**How it improves the design.** The persisted history (the part every screen and report depends
on) and the "notify interested parties" behavior are separate concerns. `PostService` stays
correct and testable even with zero listeners registered — the `PostServiceTest` history tests
don't need an `ActivityLogListener` to pass, and adding a listener never risks breaking the
database write it depends on.

**Alternatives considered.** Hard-coding a call to `ActivityLogListener` directly inside
`PostService` was the simplest option, but it would mean every future reactive behavior
(a desktop notification, a future email/webhook simulation) requires editing `PostService` again
and adding another direct dependency. That defeats the purpose of decoupling — the whole point is
that `PostService` shouldn't need to change just because something new wants to react to
publishing events.

**Future benefit.** Adding a new reaction to publish events (e.g. a `NotificationBadgeListener`
that updates a UI counter) is `postService.addListener(new NotificationBadgeListener())` — no
change to `PostService` itself, and no risk to the existing `ActivityLogListener` or the
`publishing_history` write.
