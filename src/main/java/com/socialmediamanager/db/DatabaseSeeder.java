package com.socialmediamanager.db;

import com.socialmediamanager.dao.ContentDao;
import com.socialmediamanager.dao.PlatformDao;
import com.socialmediamanager.model.Content;
import com.socialmediamanager.model.Platform;
import com.socialmediamanager.model.Post;
import com.socialmediamanager.model.PostStatus;
import com.socialmediamanager.model.PublishingResult;
import com.socialmediamanager.observer.EngagementRecorderListener;
import com.socialmediamanager.service.PostService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class DatabaseSeeder {

    private static final List<String> DEFAULT_PLATFORMS = List.of("Facebook", "Instagram", "X");

    private static final List<String[]> SAMPLE_CONTENT = List.of(
            new String[]{
                    "Pohela Boishakh Greetings",
                    "Shubho Noboborsho! Wishing everyone a joyful Bengali New Year from all of us. "
                            + "May the year ahead bring peace and prosperity to every home.",
                    "TEXT"
            },
            new String[]{
                    "Cox's Bazar Sunset",
                    "Golden hour on the longest natural sea beach in the world. "
                            + "Cox's Bazar never disappoints.",
                    "IMAGE"
            },
            new String[]{
                    "Sundarbans Mangrove Tour",
                    "A short clip from our boat trip through the Sundarbans — "
                            + "home of the Royal Bengal Tiger and the world's largest mangrove forest.",
                    "VIDEO"
            },
            new String[]{
                    "Eid Collection Discount",
                    "Eid Mubarak! Get 25% off on our entire panjabi and saree collection. "
                            + "Use code EID25 at checkout. Free delivery inside Dhaka.",
                    "PROMOTIONAL"
            },
            new String[]{
                    "Victory Day Tribute",
                    "On this 16th December we remember the sacrifice of the freedom fighters of 1971. "
                            + "Fifty-four years since Bangladesh earned its independence, we honour every "
                            + "life given for the language, the flag and the country we call home. "
                            + "Today we visit Jatiyo Smriti Shoudho in Savar to pay our respects, and we "
                            + "invite our community to share the stories their families carry from the "
                            + "Liberation War so that the next generation never forgets what was paid.",
                    "TEXT"
            },
            new String[]{
                    "Padma Bridge Anniversary",
                    "Three years since the Padma Bridge opened and connected 21 southern districts "
                            + "to the capital. Built with our own financing.",
                    "IMAGE"
            });

    private DatabaseSeeder() {
    }

    public static void seed() throws SQLException {
        Connection connection = DatabaseManager.getConnection();
        seedPlatforms(connection);
        seedContent(connection);
    }

    /**
     * Gives the Analytics screen something to show.
     *
     * <p>Publishes a spread of posts across the platforms if none have been published yet,
     * then records engagement for any published post that is missing it — which covers
     * posts that were published before engagement recording existed.
     */
    public static void seedAnalytics() throws Exception {
        PostService postService = new PostService();
        EngagementRecorderListener recorder = new EngagementRecorderListener();

        if (postService.listByStatus(PostStatus.PUBLISHED).isEmpty()) {
            publishSamplePosts(postService);
        }
        backfillEngagement(postService, recorder);
    }

    private static void publishSamplePosts(PostService postService) throws Exception {
        List<Content> contents = new ContentDao().findAll();
        List<Platform> platforms = new PlatformDao().findAll();

        // Leave the newest couple of items unpublished so there is still content to
        // take through the draft, validate and publish flow by hand.
        int publishable = Math.max(0, contents.size() - 2);
        for (Content content : contents.subList(0, publishable)) {
            for (Platform platform : platforms) {
                try {
                    Post post = postService.createDraft(content.getId(), platform.getId());
                    postService.markValidated(post.getId());
                    postService.startPublishing(post.getId());
                    postService.markPublished(post.getId());
                } catch (Exception e) {
                    // Content the platform's rules reject, or a post that already exists,
                    // simply does not become a published sample.
                }
            }
        }
    }

    private static void backfillEngagement(PostService postService,
                                           EngagementRecorderListener recorder) throws Exception {
        for (Post post : postService.listByStatus(PostStatus.PUBLISHED)) {
            recorder.onPublishingOutcome(post, PublishingResult.SUCCESS, null);
        }
    }

    private static void seedPlatforms(Connection connection) throws SQLException {
        if (!isEmpty(connection, "platform")) {
            return;
        }

        String insertPlatform = "INSERT INTO platform (name) VALUES (?)";
        try (PreparedStatement statement = connection.prepareStatement(insertPlatform)) {
            for (String platformName : DEFAULT_PLATFORMS) {
                statement.setString(1, platformName);
                statement.executeUpdate();
            }
        }
    }

    private static void seedContent(Connection connection) throws SQLException {
        if (!isEmpty(connection, "content")) {
            return;
        }

        String insertContent = "INSERT INTO content (title, body, content_type) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertContent)) {
            for (String[] row : SAMPLE_CONTENT) {
                statement.setString(1, row[0]);
                statement.setString(2, row[1]);
                statement.setString(3, row[2]);
                statement.executeUpdate();
            }
        }
    }

    private static boolean isEmpty(Connection connection, String table) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            var resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + table);
            resultSet.next();
            return resultSet.getInt(1) == 0;
        }
    }
}
