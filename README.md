# Social Media Manager

## Project Overview

The **Social Media Manager** is a desktop application (JavaFX + Maven + SQLite) that simulates a
centralized hub for managing social media content. It does not call any real social media APIs —
publishing is simulated, including a random chance of failure, so the full lifecycle (including
error handling) can be exercised and demonstrated.

Users can:
1. **Manage content** — create, edit, and delete text/image/video/promotional content.
2. **Create posts** from that content targeted at a platform (Facebook, Instagram, or X), which
   are validated against that platform's rules before they can be scheduled.
3. **Schedule and cancel posts**, with automatic simulated publishing when the selected date and
  time arrive.
4. **Publish the same content on different platforms**, while preventing duplicate posts for the
  same content and platform.
5. **Review publishing history**, filterable by outcome and platform, and see live counts on a
   Dashboard.

## Why this Project?

This domain naturally requires multi-step business logic rather than simple CRUD forms.
Enforcing a post's lifecycle, applying different validation rules per platform, and reacting to
publishing outcomes gave a real environment to apply Design Patterns where each one solves an
actual problem, rather than being added just to check a box.

## Design Patterns

See [`docs/design-decisions.md`](docs/design-decisions.md) for the full problem/solution/
alternatives/future-benefit write-up for each pattern, and
[`docs/class-diagram.md`](docs/class-diagram.md) for the UML class diagram.

- **State** — `PostLifecycle` enforces valid post status transitions (Draft → Validated →
  Scheduled → Publishing → Published, plus cancel/retry branches).
- **Strategy** — `PlatformRules` implementations (`FacebookRules`, `InstagramRules`, `XRules`)
  each encode one platform's validation rules behind a common interface.
- **Observer** — `PostService` notifies registered `PostEventListener`s when a post finishes
  publishing, independent of the permanent `publishing_history` record it also writes.

## Database

SQLite, four tables: `platform`, `content`, `post`, `publishing_history`. See
[`docs/er-diagram.md`](docs/er-diagram.md) for the full ER diagram and relationship notes.

## Screens

1. Dashboard — live post counts (total/published/scheduled/failed), navigation to other screens.
2. Content Management — CRUD for content.
3. Create/Edit Post — create drafts, validate, choose a date and time, schedule, cancel, or
  simulate publishing manually.
4. Scheduled Posts — view and cancel currently scheduled posts.
5. Publishing History — view outcomes, filterable by result and platform.

## Running the app

```
mvn javafx:run
```

The SQLite database (`social_media_manager.db`) and schema are created automatically on first
run, along with seed data for the three platforms.

## Running tests

```
mvn test
```

Tests use an in-memory SQLite database (configured via the `db.url` system property in
`pom.xml`'s surefire config) so they never touch the real `social_media_manager.db` file.
