package io.github.zap.arenaapi.stats;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.mockito.Mockito;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.logging.Logger;

public class StatsManagerTest {

    // I must follow Steank's convention 😤
    private final List<UUID> uuids = List.of(
            UUID.fromString("3b4086b3-9609-40e9-b342-0d2336a9b038"),
            UUID.fromString("e8b6484e-b5a2-495f-9440-e93e64ef8cea"),
            UUID.fromString("d57cbb91-1e47-46e9-a45b-1350846e30ca"),
            UUID.fromString("91821980-9d57-46cc-912d-bcd0d5261fa1"),
            UUID.fromString("5eb27c36-609f-47e2-9184-288c228c551d"),
            UUID.fromString("66380af5-eb03-4d65-8b2c-141ba9a5739c"),
            UUID.fromString("2f4c9f75-e084-4bff-a8c5-39b4b83b6b03"),
            UUID.fromString("f1d28d1b-ac1d-4bf2-b444-3cddabac827d"),
            UUID.fromString("4e8bf489-32b0-47ae-bf82-a5fbbc46b68c"),
            UUID.fromString("b7365522-61a2-413d-b52b-21ec4bf73c85"),
            UUID.fromString("477e81eb-dd9c-4e4f-b8e5-74a0fe7dd4a0"),
            UUID.fromString("b8d69654-54c9-4443-8b8b-94f7f2926d69"),
            UUID.fromString("9fd690d9-438e-4068-ad86-e450eef6bdef"),
            UUID.fromString("9b639cb4-6aec-4481-901b-0c8ddcaed9ae"),
            UUID.fromString("8ca82422-31d6-4dd8-97a6-a08991d2f340"),
            UUID.fromString("2feb60d2-dbaf-4fc7-b1e3-4fb5ba9d9301"),
            UUID.fromString("0f44dc69-e0d4-43ff-8f29-d974e175d06b"),
            UUID.fromString("f94434dd-9cad-409b-b9dc-48613711dfdb"),
            UUID.fromString("c3b38dd8-29ac-417c-8973-3483a4535fe0"),
            UUID.fromString("1a0fb37e-4b95-4b69-b656-3b12cbe1d6ee"),
            UUID.fromString("440eb40a-ff7c-4d7c-9ba3-7def99b931e8"),
            UUID.fromString("9d7807f1-14bf-49ae-b8ad-ee1fa8a49e1a"),
            UUID.fromString("c62dc464-e680-4f67-a0a2-e479911fa7c3"),
            UUID.fromString("6194c59c-b0cc-412b-b135-45e9126c8fb5"),
            UUID.fromString("c13583a8-a497-43f6-a0d3-c303df49550d"),
            UUID.fromString("f3871908-2d22-4573-993a-e211eb023267"),
            UUID.fromString("9b730752-d0f1-4f0c-8031-a955fce22f0f"),
            UUID.fromString("58d12484-2ad9-4b67-a4ce-958fbefe783f"),
            UUID.fromString("951a4cc6-62d8-4a21-98be-0d67e5da1617"),
            UUID.fromString("d10c79a2-0474-4ca5-a338-524c23d7ddd8"),
            UUID.fromString("5568c35e-076b-464f-93b6-22637fd46417"),
            UUID.fromString("d1579dea-e636-4aa4-9110-ae69dc5dc36d"),
            UUID.fromString("7af4f66e-8948-44d8-92f4-388d9f42dd0a"),
            UUID.fromString("cd339567-e902-4a49-8a83-07409189b4cb"),
            UUID.fromString("21ab51e8-478d-4ffd-be30-700ca3afc82f"),
            UUID.fromString("1df2b13d-8322-4c7b-898d-3d33a28ea3cb"),
            UUID.fromString("747d3afa-94e9-4730-a1ca-0e33cfbf108b"),
            UUID.fromString("a6b54610-e5bf-4b55-9d4f-e81138e19b64"),
            UUID.fromString("50ac73bc-fd33-4faf-9557-0c72a25a47a3"),
            UUID.fromString("d2cb5695-1a35-4d0f-9ba2-c42966c2aff8"),
            UUID.fromString("5ac8999a-e9ac-4aaf-8baa-40a4ba65356d"),
            UUID.fromString("3c007b18-61df-4e7d-8622-80ddf551aa95"),
            UUID.fromString("a1bf3bb0-4473-4bc4-8b83-3fd143e8d1ef"),
            UUID.fromString("8aeb6dc0-29d4-43ef-8254-ae502bcc7bd5"),
            UUID.fromString("9dc29f7a-85de-49cd-a21d-e26f368f3401"),
            UUID.fromString("97f1f355-3f73-44e9-8305-d5bd6bca2ace"),
            UUID.fromString("fa2a9c2f-1592-404f-af1b-891aa2d046ac"),
            UUID.fromString("c1f9a781-a1e9-4925-8d75-857da8fc18f1"),
            UUID.fromString("be49f1ea-9d1b-4a04-8867-99cd35a3bebc"),
            UUID.fromString("4c67610f-5b83-4dd2-b7c9-b5192bf53e1c"),
            UUID.fromString("5b65ef1d-232e-4c6b-bbfc-14101f424961"),
            UUID.fromString("b641736e-2ee8-4ac7-b6cc-c5a9843afc98"),
            UUID.fromString("7b8a6203-1087-4fc9-a086-4ee2bc004c86"),
            UUID.fromString("d9ed9948-fed2-49f3-b264-b609e7a3bce4"),
            UUID.fromString("f1b8ccd7-09c6-417c-9f7a-3819f16a3a44"),
            UUID.fromString("86bddb70-633f-4009-8e27-8eb1cef66767"),
            UUID.fromString("341aa053-6d6a-4123-8d88-7ecc60e7f5b4"),
            UUID.fromString("6cf3451f-423c-4d93-9f43-4ad522f45832"),
            UUID.fromString("30a58568-1833-4ce2-ba0c-3cacc7f7c1bf"),
            UUID.fromString("d1313cdb-b1e4-454e-9188-32f2e770a0bd"),
            UUID.fromString("dd35e418-f967-4ba9-8c5e-4cefbf216d00"),
            UUID.fromString("5c6c3cf5-3fd8-4320-aa3e-222ff77899b3"),
            UUID.fromString("d00ecfbb-ad40-40bd-83b6-11cbdb208599"),
            UUID.fromString("4669e7b4-7e9e-4694-ba17-08682b8187b4"),
            UUID.fromString("5c947989-d638-4d82-be82-d983e02cc8e8"),
            UUID.fromString("279cbeb2-22e7-48b4-b63d-c8d890ce215c"),
            UUID.fromString("809ac555-99fd-4333-ba1e-d8cc1cd74a41"),
            UUID.fromString("a655e4ce-8465-4ced-9b17-90f59de7c837"),
            UUID.fromString("c66fbc84-15a6-4d2d-94eb-4a66d6299994"),
            UUID.fromString("f41c5b3b-0c23-46fa-815d-c8ec3c89f499"),
            UUID.fromString("6803b161-9f92-4fc8-ac9f-009c0af4fdc4"),
            UUID.fromString("b7ba8c33-d0ae-49a9-88e5-70743dac4a22"),
            UUID.fromString("58d507e9-ada7-4353-b5de-a1a64fe2a80c"),
            UUID.fromString("9f9bf16b-fc63-4317-a2fd-cd988c14eb20"),
            UUID.fromString("965483f4-9a14-4c4d-bb82-62743d8effd4"),
            UUID.fromString("16c30038-5da6-460f-a8b5-4f0187b6891f"),
            UUID.fromString("cbdc1f1c-bef5-4b96-a299-ab9659048b74"),
            UUID.fromString("4227bbde-a7a9-4ea7-95b2-6fae753003fb"),
            UUID.fromString("b7da92e0-d71d-4fd4-86c5-6da9f11192dc"),
            UUID.fromString("5349829c-b36c-4601-a50d-743a1f8fb081"),
            UUID.fromString("046b8855-5618-46f7-9da3-ee532fc0f0b7"),
            UUID.fromString("1457ccd0-cba2-48e3-b54a-affdbf8c7a6a"),
            UUID.fromString("6d44d580-6eef-4d88-9248-1e474d13fcb7"),
            UUID.fromString("74d2184d-621f-4ed6-b9a4-f124157dc497"),
            UUID.fromString("97082dae-02d8-4229-940f-42e2add055cb"),
            UUID.fromString("74fa0d47-965e-4db4-927e-c268630757c2"),
            UUID.fromString("65b22575-73cb-4d0b-8819-63ca1a6b9796"),
            UUID.fromString("07ed4a6c-fbd2-4c3c-9464-fe40c95d192c"),
            UUID.fromString("7314fd95-8ea8-4bef-97d8-0d06482bccd2"),
            UUID.fromString("c0fac225-d4ba-4e8a-8b09-8f1cba20dc5a"),
            UUID.fromString("6a2ab61b-e42b-4e6d-bcbf-96ec3af44aa8"),
            UUID.fromString("5957c100-28f9-4e5e-bebb-cb4a292c4990"),
            UUID.fromString("be971c84-6f05-482c-bced-495db5f88ae4"),
            UUID.fromString("bb5de7c9-d02d-4471-8c6f-4033dfb6c8e6"),
            UUID.fromString("751b6c61-b9d0-41b4-af21-8b019ee690f3"),
            UUID.fromString("6f4db158-626f-43be-b225-3e8077f05288"),
            UUID.fromString("e518f279-b32e-47ac-9776-792029830a2b"),
            UUID.fromString("1d79bbfa-5112-43cb-b827-3e363173101e"),
            UUID.fromString("bb79e2cc-fafc-4f11-8623-598755b33eb7"),
            UUID.fromString("6838955b-bff8-4219-b738-39fa8f833441")
    );

    private Plugin plugin;

    @BeforeEach
    public void setup() {
        this.plugin = Mockito.mock(Plugin.class);
        Logger logger = Logger.getLogger("ArenaApi");
        Mockito.when(this.plugin.getLogger()).thenReturn(logger);
    }

    @RepeatedTest(100) // For paranoia's sake, I am going to have this be repeated for a while
    public void testCorrectCacheModifications() {
        Map<UUID, ExampleStats> exampleStats = new ConcurrentHashMap<>();

        StatsManager statsManager = new StatsManager(this.plugin, Executors.newFixedThreadPool(8),
                10L) {
            @SuppressWarnings({"unchecked", "SuspiciousMethodCalls"})
            @Override
            protected <I, S extends Stats<I>> @NotNull S loadStats(@NotNull String cacheName, @NotNull I identifier,
                                                                   @NotNull Class<S> clazz,
                                                                   @NotNull Function<I, S> callback) {
                if (exampleStats.containsKey(identifier)) {
                    return (S) exampleStats.get(identifier);
                }
                else {
                    try {
                        return (S) clazz.getConstructors()[0].newInstance(identifier);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }

                Assertions.fail("Test class constructor search failed.");
                return null;
            }

            @Override
            protected <I, S extends Stats<I>> void writeStats(@NotNull String cacheName, @NotNull S stats) {
                if (stats instanceof ExampleStats flushExampleStats) {
                    exampleStats.put(flushExampleStats.getIdentifier(), flushExampleStats);
                }
            }
        };
        StatsCache<UUID, ExampleStats> statsCache = new StatsCache<>(this.plugin, "test",
                ExampleStats.class, 10);
        statsManager.registerCache(statsCache);
        for (int i = 0; i < 10000; i++) {
            statsManager.queueCacheRequest("test", uuids.get(i % 100),
                    ExampleStats::new, stats -> stats.setNumber(stats.getNumber() + 1));
        }
        Assertions.assertTrue(statsManager.destroy());
        for (ExampleStats stats : exampleStats.values()) {
            Assertions.assertEquals(100, stats.getNumber());
        }
    }

    private static class ExampleStats extends Stats<UUID> {

        private int number;

        public ExampleStats(UUID id) {
            super(id);
        }

        public int getNumber() {
            return number;
        }

        private void setNumber(int number) {
            this.number = number;
        }

    }

}
