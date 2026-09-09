# UML Class Diagram

Shows the service layer, the three pattern implementations it uses, and how they connect to the
DAO/persistence layer. UI controllers (`ContentController`, `PostController`, etc.) call the
services shown here and are omitted for clarity — each simply calls `ContentService` or
`PostService`.

```mermaid
classDiagram
    class PostService {
        -PostDao postDao
        -ContentDao contentDao
        -PlatformDao platformDao
        -PublishingHistoryDao publishingHistoryDao
        -PostLifecycle postLifecycle
        -PlatformRulesRegistry platformRulesRegistry
        -List~PostEventListener~ listeners
        +createDraft(contentId, platformId) Post
        +markValidated(postId)
        +schedule(postId, scheduledAt)
        +cancel(postId)
        +startPublishing(postId)
        +markPublished(postId)
        +markFailed(postId, reason)
        +addListener(listener)
        +searchPublishingHistory(result, platformId) List~PublishingHistoryEntry~
    }

    class PostLifecycle {
        -Map~PostStatus, Set~PostStatus~~ ALLOWED_TRANSITIONS
        +canTransition(from, to) boolean
        +assertTransition(from, to)
    }

    class PlatformRules {
        <<interface>>
        +getPlatformName() String
        +validate(content) ValidationResult
    }
    class FacebookRules
    class InstagramRules
    class XRules
    PlatformRules <|.. FacebookRules
    PlatformRules <|.. InstagramRules
    PlatformRules <|.. XRules

    class PlatformRulesRegistry {
        -Map~String, Supplier~PlatformRules~~ rulesByPlatform
        +rulesFor(platformName) PlatformRules
    }
    PlatformRulesRegistry ..> PlatformRules : creates

    class PostEventListener {
        <<interface>>
        +onPublishingOutcome(post, result, message)
    }
    class ActivityLogListener {
        -List~String~ entries
        +onPublishingOutcome(post, result, message)
        +getEntries() List~String~
    }
    PostEventListener <|.. ActivityLogListener

    class PostDao
    class ContentDao
    class PlatformDao
    class PublishingHistoryDao

    PostService --> PostLifecycle : enforces transitions with
    PostService --> PlatformRulesRegistry : looks up rules from
    PostService --> PostEventListener : notifies on publish outcome
    PostService --> PostDao
    PostService --> ContentDao
    PostService --> PlatformDao
    PostService --> PublishingHistoryDao
```

## Where each pattern lives

| Pattern | Classes | Role |
|---|---|---|
| State | `PostLifecycle` | Owns the table of valid `PostStatus` transitions; `PostService` asks it before changing a post's status instead of hard-coding `if` chains per screen. |
| Strategy | `PlatformRules` (interface), `FacebookRules`, `InstagramRules`, `XRules`, `PlatformRulesRegistry` | Each platform's validation logic is its own class behind a common interface; `PostService` picks the right one by platform name at runtime. |
| Observer | `PostEventListener` (interface), `ActivityLogListener` | `PostService` doesn't know who's listening — it just notifies registered listeners when a post finishes publishing. The persisted `publishing_history` row is written the same way, independent of any UI listener. |
