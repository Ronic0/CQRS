package framework.cqrs.dao;

import framework.cqrs.domaine.FakeAggregateRoot;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.framework.cqrs.bus.Bus;
import java.framework.cqrs.dao.RepositoryImpl;
import java.framework.cqrs.domaine.AggregateRootNotFoundException;
import java.framework.cqrs.domaine.Evenement;
import java.framework.cqrs.domaine.IdVersionnee;
import java.framework.cqrs.evenementStore.EvenementStore;
import java.framework.cqrs.evenementStore.enmemoire.EnMemoireEvenementStore;

/**
 * Created by Nicolas Caline on 07-04-15.
 */
public class RepositoryImplTest {
    private static final IdVersionnee TEST_ID = IdVersionnee.aleatoire();

    private Bus bus;
    private FakeAggregateRoot aggregateRoot;
    private EvenementStore<Evenement> evenementStore;
    private RepositoryImpl sujet;

    @Before
    public void setUp() {
        evenementStore = new EnMemoireEvenementStore<Evenement>();
        bus = EasyMock.createNiceMock(Bus.class);
        sujet = new RepositoryImpl(evenementStore, bus);

        aggregateRoot = new FakeAggregateRoot(TEST_ID);
        aggregateRoot.saluerUnePersonne("Erik");
        aggregateRoot.saluerUnePersonne("Sjors");
        sujet.ajouter(aggregateRoot);
    }

    @Test
    public void shouldFailToAddAggregateWithoutAnyUnsavedChanges() {
        FakeAggregateRoot a = new FakeAggregateRoot(IdVersionnee.aleatoire());
        try {
            sujet.ajouter(a);
            Assert.fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void shouldFailOnNonExistingAggregateRoot() {
        IdVersionnee id = IdVersionnee.aleatoire();
        try {
            sujet.getParIdVersionnee(FakeAggregateRoot.class, id);
            Assert.fail("AggregateRootNotFoundException expected");
        } catch (AggregateRootNotFoundException expected) {
            Assert.assertEquals(FakeAggregateRoot.class.getName(), expected.getAggregateRootType());
            Assert.assertEquals(id.getId(), expected.getAggregateRootId());
        }
    }

    @Test
    public void shouldLoadAggregateRootFromEventStore() {
        sujet.afterHandleMessage();

        FakeAggregateRoot result = sujet.getParIdVersionnee(FakeAggregateRoot.class, TEST_ID);

        Assert.assertNotNull(result);
        Assert.assertEquals(aggregateRoot.getDerniereSalutation(), result.getDerniereSalutation());
    }

    @Test
    public void shouldLoadAggregateOnlyOnce() {
        FakeAggregateRoot a = sujet.getParId(FakeAggregateRoot.class, TEST_ID.getId());

        Assert.assertSame(aggregateRoot, a);
    }

    @Test
    public void shouldRejectDifferentAggregatesWithSameId() {
        FakeAggregateRoot a = aggregateRoot;
        FakeAggregateRoot b = new FakeAggregateRoot(TEST_ID);
        b.saluerUnePersonne("Jan");

        sujet.ajouter(a);
        try {
            sujet.ajouter(b);
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void shouldCheckAggregateVersionOnLoadFromSession() {
        try {
            sujet.getParIdVersionnee(FakeAggregateRoot.class, TEST_ID.avecVersion(0));
            Assert.fail("OptimisticLockingFailureException expected");
        } catch (Exception e) {
        }
    }

    @Test
    public void shouldStoreAddedAggregate() {
        aggregateRoot.saluerUnePersonne("Erik");
        EasyMock.replay(bus);

        sujet.afterHandleMessage();

        EasyMock.verify(bus);
    }

    @Test
    public void shouldStoreLoadedAggregateWithNextVersion() {
        EasyMock.replay(bus);
        sujet.afterHandleMessage();

        FakeAggregateRoot result = sujet.getParIdVersionnee(FakeAggregateRoot.class, TEST_ID);
        result.saluerUnePersonne("Mark");
        sujet.afterHandleMessage();

        FakeAggregateRoot loaded = sujet.getParIdVersionnee(FakeAggregateRoot.class, TEST_ID.versionSuivante());

        Assert.assertEquals("Hi Mark", loaded.getDerniereSalutation());
        EasyMock.verify(bus);
    }

    @Test
    public void shouldPublishChangeEventsOnSave() {
        aggregateRoot.saluerUnePersonne("Erik");

        bus.publier(EasyMock.eq(aggregateRoot.getEvenementsNonSauves()));
        EasyMock.replay(bus);

        sujet.afterHandleMessage();

        EasyMock.verify(bus);
    }

    @Test
    public void shouldReplyWithNotificationsOnSave() {
        aggregateRoot.saluerUnePersonne("Erik");

        bus.repondre(EasyMock.eq(aggregateRoot.getNotifications()));
        EasyMock.replay(bus);

        sujet.afterHandleMessage();

        EasyMock.verify(bus);
    }
}
