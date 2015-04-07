package framework.cqrs.domaine;

import org.junit.Assert;
import org.junit.Test;

import java.framework.cqrs.domaine.IdVersionnee;
import java.util.UUID;

/**
 * Created by Nicolas Caline on 07-04-15.
 */
public class IdVersionneeTest {
    private final static UUID FOO = UUID.randomUUID();
    private final static UUID BAR = UUID.randomUUID();

    private IdVersionnee a = IdVersionnee.pourVersionSpecifique(FOO, 3);

    @Test
    public void shouldStoreIdAndVersion() {
        Assert.assertEquals(FOO, a.getId());
        Assert.assertEquals(3, a.getVersion());
    }

    @Test
    public void testEqualsIgnoreVersion() {
        Assert.assertTrue(a.equalsIgnoreVersion(IdVersionnee.pourVersionSpecifique(FOO, 1)));
        Assert.assertFalse(a.equalsIgnoreVersion(null));
        Assert.assertFalse(a.equalsIgnoreVersion(IdVersionnee.pourVersionSpecifique(BAR, 3)));
    }

    @Test
    public void testCompatibility() {
        Assert.assertTrue(IdVersionnee.pourDerniereVersion(FOO).isCompatible(a));
        Assert.assertFalse(IdVersionnee.pourDerniereVersion(BAR).isCompatible(a));
        Assert.assertTrue(a.isCompatible(IdVersionnee.pourVersionSpecifique(FOO, 3)));
        Assert.assertFalse(a.isCompatible(IdVersionnee.pourVersionSpecifique(FOO, 2)));
        Assert.assertFalse(a.isCompatible(IdVersionnee.pourVersionSpecifique(BAR, 3)));
    }
}
