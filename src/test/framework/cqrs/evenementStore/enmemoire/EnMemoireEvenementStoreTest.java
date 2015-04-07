package framework.cqrs.evenementStore.enmemoire;

import framework.cqrs.evenementStore.AbstractEvenementStoreTest;

/**
 * Created by Nicolas Caline on 07-04-15.
 */
public class EnMemoireEvenementStoreTest extends AbstractEvenementStoreTest {
    @Override
    protected EnMemoireEvenementStore<String> creerSujet() {
        return new EnMemoireEvenementStore<String>();
    }
}
