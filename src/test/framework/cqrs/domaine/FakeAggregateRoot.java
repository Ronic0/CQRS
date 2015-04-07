package framework.cqrs.domaine;

/**
 * Created by Nicolas Caline on 07-04-15.
 */
public class FakeAggregateRoot extends AggregateRoot {

    private String derniereSalutation;

    public FakeAggregateRoot() {
        this(AggregateRootTest.TEST_ID);
    }

    public FakeAggregateRoot(IdVersionnee id) {
        super(id);
    }

    public void saluerUnePersonne(String nom) {
        appliquer(new SalutationEvenement(getIdVersionnee(), "Bonjour " + nom));
        notifier(new SalutationNotification("Salué " + nom));
    }

    public String getDerniereSalutation() {
        return derniereSalutation;
    }

    @Override
    public void onEvenement(Evenement evenement) {
        derniereSalutation = ((SalutationEvenement) evenement).getMessage();
    }

}