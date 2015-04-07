package framework.cqrs.domaine;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.framework.cqrs.domaine.IdVersionnee;

/**
 * Created by Nicolas Caline on 07-04-15.
 */
public class AggregateRootTest {

    public static final IdVersionnee TEST_ID = IdVersionnee.aleatoire();

    private FakeAggregateRoot sujet;

    @Before
    public void setUp() {
        sujet = new FakeAggregateRoot();
        sujet.saluerUnePersonne("Erik");
    }

    @Test
    public void shouldDispatchAppliedEvents() {
        Assert.assertEquals("Hi Erik", sujet.getDerniereSalutation());
    }

    @Test
    public void shouldTrackUnsavedEvents() {
        Assert.assertEquals(new SalutationEvenement(sujet.getIdVersionnee(), "Hi Erik"), sujet.getEvenementsNonSauves().iterator().next());
    }

    @Test
    public void shouldClearUnsavedChanges() {
        sujet.viderEvenementsNonSauves();

        Assert.assertEquals(0, sujet.getEvenementsNonSauves().size());
    }
}
