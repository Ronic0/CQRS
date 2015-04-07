package framework.cqrs.evenementstore;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.*;
import static org.junit.Assert.*;

import java.framework.cqrs.evenementStore.EvenementCollecteur;
import java.framework.cqrs.evenementStore.EvenementSource;
import java.framework.cqrs.evenementStore.EvenementStore;
import java.util.List;
import java.util.UUID;

/**
 * Created by Nicolas Caline on 07-04-15.
 */
public abstract class AbstractEvenementStoreTest {

    private static final UUID ID_1 = UUID.randomUUID();
    private static final UUID ID_2 = UUID.randomUUID();

    private static final long T1 = 1000;
    private static final long T2 = 2000;

    private EvenementStore<String> sujet;

    protected abstract EvenementStore<String> creerSujet();

    @Before
    public void setUp() {
        sujet = creerSujet();
    }

    @Test
    public void should_create_event_stream_with_initial_version_and_events() {
        List<String> evenements = asList("foo", "bar");
        FakeEvenementSource source = new FakeEvenementSource("type", 0, T1, evenements);
        FakeEvenementCollecteur collecteur = new FakeEvenementCollecteur("type", 0, T1, evenements);

        sujet.creerFluxEvenements(ID_1, source);
        sujet.chagerEvenementsDepuisLaDerniereVersionDuFlux(ID_1, collecteur);

        collecteur.verifier();
    }

    @Test
    public void should_fail_to_create_stream_with_duplicate_id() {
        sujet.creerFluxEvenements(ID_1, new FakeEvenementSource("type", 0, T1, asList("foo", "bar")));
        try {
            sujet.creerFluxEvenements(ID_1, new FakeEvenementSource("type", 0, T2, asList("baz")));
            Assert.fail("DataIntegrityViolationException expected");
        } catch (Exception expected) {
        }
    }

    @Test
    public void should_store_events_into_stream() {
        sujet.creerFluxEvenements(ID_1, new FakeEvenementSource("type", 0, T1, asList("foo", "bar")));
        sujet.sotckerEvenementsDansFlux(ID_1, 0, new FakeEvenementSource("type", 1, T2, asList("baz")));

        FakeEvenementCollecteur collecteur = new FakeEvenementCollecteur("type", 1, T2, asList("foo", "bar", "baz"));
        sujet.chagerEvenementsDepuisLaDerniereVersionDuFlux(ID_1, collecteur);

        collecteur.verifier();
    }

    @Test
    public void should_load_events_from_specific_stream_version() {
        sujet.creerFluxEvenements(ID_1, new FakeEvenementSource("type", 0, T1, asList("foo", "bar")));
        sujet.sotckerEvenementsDansFlux(ID_1, 0, new FakeEvenementSource("type", 1, T2, asList("baz")));

        FakeEvenementCollecteur collecteur = new FakeEvenementCollecteur("type", 1, T2, asList("foo", "bar", "baz"));
        sujet.chargerEvenementsDepuisLaVersionAttendueDuFlux(ID_1, 1, collecteur);

        collecteur.verifier();
    }

    @Test
    public void should_fail_to_load_events_from_specific_stream_version_when_expected_version_does_not_match_actual_version() {
        sujet.creerFluxEvenements(ID_1, new FakeEvenementSource("type", 0, T1, asList("foo", "bar")));
        sujet.sotckerEvenementsDansFlux(ID_1, 0, new FakeEvenementSource("type", 1, T2, asList("baz")));

        try {
            sujet.chargerEvenementsDepuisLaVersionAttendueDuFlux(ID_1, 0, new FakeEvenementCollecteur("type", 1, T2, asList("foo", "bar", "baz")));
            fail("OptimisticLockingFailureException expected");
        } catch (Exception expected) {
        }
    }

    @Test
    public void should_store_separate_event_logs_for_different_event_streams() {
        sujet.creerFluxEvenements(ID_1, new FakeEvenementSource("type", 0, T1, asList("foo", "bar")));
        sujet.creerFluxEvenements(ID_2, new FakeEvenementSource("type", 0, T2, asList("baz")));

        FakeEvenementCollecteur collecteur1 = new FakeEvenementCollecteur("type", 0, T1, asList("foo", "bar"));
        sujet.chargerEvenementsDepuisLaVersionAttendueDuFlux(ID_1, 0, collecteur1);
        FakeEvenementCollecteur collecteur2 = new FakeEvenementCollecteur("type", 0, T2, asList("baz"));
        sujet.chargerEvenementsDepuisLaVersionAttendueDuFlux(ID_2, 0, collecteur2);

        collecteur1.verifier();
        collecteur2.verifier();
    }

    @Test
    public void should_fail_to_store_events_into_stream_when_versions_do_not_match() {
        sujet.creerFluxEvenements(ID_1, new FakeEvenementSource("type", 1, T1, asList("foo", "bar")));
        try {
            sujet.sotckerEvenementsDansFlux(ID_1, 0, new FakeEvenementSource("type", 1, T2, asList("baz")));
            fail("OptimisticLockingFailureException expected");
        } catch (Exception expected) {
        }
    }

    @Test
    public void should_check_optimistic_locking_error_before_decreasing_version_or_timestamp() {
        sujet.creerFluxEvenements(ID_1, new FakeEvenementSource("type", 5, T1, asList("foo", "bar")));
        try {
            sujet.sotckerEvenementsDansFlux(ID_1, 4, new FakeEvenementSource("type", 3, T2, asList("baz")));
            fail("OptimisticLockingFailureException expected");
        } catch (Exception expected) {
        }
    }

    @Test
    public void should_fail_to_store_events_into_stream_when_new_version_is_before_previous_version() {
        sujet.creerFluxEvenements(ID_1, new FakeEvenementSource("type", 5, T1, asList("foo", "bar")));
        try {
            sujet.sotckerEvenementsDansFlux(ID_1, 5, new FakeEvenementSource("type", 4, T2, asList("baz")));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void should_fail_to_store_events_into_stream_when_new_timestamp_is_before_previous_timestamp() {
        sujet.creerFluxEvenements(ID_1, new FakeEvenementSource("type", 0, T2, asList("foo", "bar")));
        try {
            sujet.sotckerEvenementsDansFlux(ID_1, 0, new FakeEvenementSource("type", 1, T1, asList("baz")));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void should_fail_to_load_events_when_event_stream_version_does_not_match() {
        sujet.creerFluxEvenements(ID_1, new FakeEvenementSource("type", 0, T1, asList("foo", "bar")));
        sujet.sotckerEvenementsDansFlux(ID_1, 0, new FakeEvenementSource("type", 1, T2, asList("baz")));

        try {
            sujet.chargerEvenementsDepuisLaVersionAttendueDuFlux(ID_1, 0, new FakeEvenementCollecteur("type", 1, T2, asList("foo", "bar", "baz")));
            fail("OptimisticLockingFailureException expected");
        } catch (Exception expected) {
        }

    }

    @Test
    public void should_fail_to_store_events_into_non_existing_event_stream() {
        try {
            sujet.sotckerEvenementsDansFlux(ID_1, 0, new FakeEvenementSource("type", 0, T1, asList("foo")));
            fail("EmptyResultDataAccessException expected");
        } catch (Exception expected) {
        }
    }

    @Test
    public void should_fail_to_load_events_from_non_existing_event_stream() {
        try {
            sujet.chargerEvenementsDepuisLaVersionAttendueDuFlux(ID_1, 0, new FakeEvenementCollecteur("type", 0, T1, asList("foo")));
            fail("EmptyResultDataAccessException expected");
        } catch (Exception expected) {
        }
    }

    @Test
    public void should_load_events_from_stream_at_specific_version() {
        sujet.creerFluxEvenements(ID_1, new FakeEvenementSource("type", 0, T1, asList("foo", "bar")));
        sujet.sotckerEvenementsDansFlux(ID_1, 0, new FakeEvenementSource("type", 1, T2, asList("baz")));

        FakeEvenementCollecteur collecteur = new FakeEvenementCollecteur("type", 0, T1, asList("foo", "bar"));
        sujet.chargerEvenementsJusqueLaVersionDuFlux(ID_1, 0, collecteur);

        collecteur.verifier();
    }

    @Test
    public void should_load_all_events_from_stream_when_specified_version_is_higher_than_actual_version() {
        sujet.creerFluxEvenements(ID_1, new FakeEvenementSource("type", 0, T1, asList("foo", "bar")));
        sujet.sotckerEvenementsDansFlux(ID_1, 0, new FakeEvenementSource("type", 1, T2, asList("baz")));

        FakeEvenementCollecteur collecteur = new FakeEvenementCollecteur("type", 1, T2, asList("foo", "bar", "baz"));
        sujet.chargerEvenementsJusqueLaVersionDuFlux(ID_1, 3, collecteur);

        collecteur.verifier();
    }

    @Test
    public void should_fail_to_load_events_from_stream_when_requested_version_is_before_first_event_version() {
        sujet.creerFluxEvenements(ID_1, new FakeEvenementSource("type", 2, T1, asList("foo", "bar")));
        try {
            sujet.chargerEvenementsJusqueLaVersionDuFlux(ID_1, 1, new FakeEvenementCollecteur("type", 0, T2, asList("foo", "bar")));
            fail("EmptyResultDataAccessException expected");
        } catch (Exception expected) {
        }
    }

    @Test
    public void should_load_events_from_stream_at_specific_timestamp() {
        long t = T1 + 250;

        sujet.creerFluxEvenements(ID_1, new FakeEvenementSource("type", 0, T1, asList("foo", "bar")));
        sujet.sotckerEvenementsDansFlux(ID_1, 0, new FakeEvenementSource("type", 1, T2, asList("baz")));

        FakeEvenementCollecteur collecteur = new FakeEvenementCollecteur("type", 0, T1, asList("foo", "bar"));
        sujet.chargerEvenementsAvecLeTimesTampDuFlux(ID_1, t, collecteur);

        collecteur.verifier();
    }

    @Test
    public void should_fail_to_load_events_from_stream_when_request_timestamp_is_before_first_event_timestamp() {
        sujet.creerFluxEvenements(ID_1, new FakeEvenementSource("type", 0, T2, asList("foo", "bar")));
        try {
            sujet.chargerEvenementsAvecLeTimesTampDuFlux(ID_1, T1, new FakeEvenementCollecteur("type", 0, T1, asList("foo", "bar")));
            fail("EmptyResultDataAccessException expected");
        } catch (Exception expected) {
        }
    }

    public static class FakeEvenementSource implements EvenementSource<String> {

        private final String type;
        private final long version;
        private final long timestamp;
        private final List<String> events;

        public FakeEvenementSource(String type, long version, long timestamp, List<String> events) {
            this.type = type;
            this.version = version;
            this.timestamp = timestamp;
            this.events = events;
        }

        public String getType() {
            return type;
        }

        public long getVersion() {
            return version;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public List<? extends String> getEvenements() {
            return events;
        }

    }

    public static final class FakeEvenementCollecteur implements EvenementCollecteur<String> {
        private final String expectedType;
        private final long expectedVersion;
        private final long expectedTimestamp;
        private final List<String> expectedEvents;
        private String actualType;
        private long actualVersion = -1;
        private long actualTimestamp = -1;
        private Iterable<? extends String> actualEvents;

        public FakeEvenementCollecteur(String expectedType, long expectedVersion, long expectedTimestamp, List<String> expectedEvents) {
            this.expectedType = expectedType;
            this.expectedVersion = expectedVersion;
            this.expectedTimestamp = expectedTimestamp;
            this.expectedEvents = expectedEvents;
        }

        public void setType(String actualType) {
            this.actualType = actualType;
        }

        public void setVersion(long actualVersion) {
            assertNotNull("type must be set before version", actualType);
            this.actualVersion = actualVersion;
        }

        public void setTimestamp(long actualTimestamp) {
            assertNotNull("type must be set before version", actualType);
            this.actualTimestamp = actualTimestamp;
        }

        public void setEvenements(Iterable<? extends String> actualEvents) {
            assertNotNull("type must be set before events", actualType);
            this.actualEvents = actualEvents;
        }

        public void verifier() {
            assertEquals(expectedType, actualType);
            assertEquals(expectedVersion, actualVersion);
            assertEquals(expectedTimestamp, actualTimestamp);
            assertEquals(expectedEvents, actualEvents);
        }

    }
}
