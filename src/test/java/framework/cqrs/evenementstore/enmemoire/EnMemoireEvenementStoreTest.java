package framework.cqrs.evenementstore.enmemoire;

import framework.cqrs.evenementstore.AbstractEvenementStoreTest;

import java.framework.cqrs.evenementStore.enmemoire.EnMemoireEvenementStore;

/**
 * Created by Nicolas Caline on 07-04-15.
 */
public class EnMemoireEvenementStoreTest extends AbstractEvenementStoreTest {
    @Override
    protected EnMemoireEvenementStore<String> creerSujet() {
        return new EnMemoireEvenementStore<String>();
    }
}
