package framework.cqrs.bus;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.framework.cqrs.bus.AbstractHandler;
import java.framework.cqrs.bus.LocaleEnMemoireBus;
import java.framework.cqrs.bus.MessageHandlingException;
import java.framework.cqrs.bus.Reponse;

/**
 * Created by Nicolas Caline on 07-04-15.
 */
public class LocaleEnMemoireBusTest  {
    private LocaleEnMemoireBus sujet;
    private TestCommandeHandler commandeHandler;
    private TestEvenementHandler evenementHandler;

    private boolean testCommandeHandlerAppele;
    private boolean testEvenementHandlerAppele;

    @Before
    public void setUp() {
        commandeHandler = new TestCommandeHandler();
        evenementHandler = new TestEvenementHandler();
        sujet = new LocaleEnMemoireBus();
        sujet.setHandlers(commandeHandler, evenementHandler);
    }

    @Test
    public void shouldSendCommandToRegisteredHandler() {
        sujet.envoyer(new TestCommande("salut"));
        Assert.assertTrue(testCommandeHandlerAppele);
        Assert.assertEquals("salut", commandeHandler.dernierMessage);
    }

    @Test
    public void shouldSendEventToRegisteredHandler() {
        sujet.publier(new TestEvenement(""));
        Assert. assertTrue(testEvenementHandlerAppele);
    }

    @Test
    public void shouldFailToReplyWhenThereIsNoCurrentMessage() {
        try {
            sujet.repondre(new TestEvenement("foo"));
            Assert.fail("MessageHandlingException attendue");
        } catch (MessageHandlingException expected) {
            Assert. assertEquals("pas de message courant à répondre", expected.getMessage());
        }
    }

    @Test
    public void shouldInvokeMessageHandlerForReply() {
        sujet.setHandlers(evenementHandler, new AbstractHandler<TestCommande>(TestCommande.class) {
            public void handleMessage(TestCommande message) {
                sujet.repondre(new TestEvenement(""));
                Assert.assertFalse(testEvenementHandlerAppele);
            }
        });

        sujet.envoyerEtAttendreUneReponse(new TestCommande("test commande"));

        Assert.assertTrue(testEvenementHandlerAppele);
    }

    @Test
    public void shouldPostponeInvokingHandlersUntilCurrentMessageHasBeenProcessed() {
        sujet.setHandlers(evenementHandler, new AbstractHandler<TestCommande>(TestCommande.class) {
            public void handleMessage(TestCommande message) {
                sujet.publier(new TestEvenement(""));
                Assert.assertFalse(testEvenementHandlerAppele);
            }
        });

        sujet.envoyer(new TestCommande("test commande"));

        Assert.assertTrue(testEvenementHandlerAppele);
    }

    @Test
    public void shouldRespondWithRepliedMessages() {
        sujet.setHandlers(evenementHandler, new AbstractHandler<TestCommande>(TestCommande.class) {
            public void handleMessage(TestCommande message) {
                sujet.repondre(new TestEvenement("evenement"));
            }
        });

        Reponse response = sujet.envoyerEtAttendreUneReponse(new TestCommande("salut"));

        Assert.assertTrue(response.contientReponseDeType(TestEvenement.class));
        Assert.assertEquals("event", response.getReponseDeType(TestEvenement.class).getMessage());
    }

    @Test
    public void shouldSupportNestedSendingOfCommands() {
        sujet.setHandlers(evenementHandler, new AbstractHandler<TestCommande>(TestCommande.class) {
            public void handleMessage(TestCommande message) {
                if (!testCommandeHandlerAppele) {
                    sujet.repondre(new TestEvenement("top"));
                    testCommandeHandlerAppele = true;
                    Reponse reponse = sujet.envoyerEtAttendreUneReponse(new TestCommande("there"));
                    Assert.assertTrue(reponse.contientReponseDeType(TestEvenement.class));
                    Assert.assertEquals("nested", reponse.getReponseDeType(TestEvenement.class).getMessage());
                } else {
                    sujet.repondre(new TestEvenement("nested"));
                }
            }
        });

        Reponse reponse = sujet.envoyerEtAttendreUneReponse(new TestCommande("hello"));

        Assert.assertTrue(reponse.contientReponseDeType(TestEvenement.class));
        Assert.assertEquals("top", reponse.getReponseDeType(TestEvenement.class).getMessage());
    }

    private static class TestCommande {
        private final String message;

        public TestCommande(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    private static class TestEvenement {
        private final String message;

        public TestEvenement(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    private class TestCommandeHandler extends AbstractHandler<TestCommande> {
        private String dernierMessage;

        public TestCommandeHandler() {
            super(TestCommande.class);
        }

        public void handleMessage(TestCommande commande) {
            Assert.assertSame(commande, sujet.getMessageCourant());
            dernierMessage = commande.getMessage();
            testCommandeHandlerAppele = true;
        }
    }

    private class TestEvenementHandler extends AbstractHandler<TestEvenement> {
        public TestEvenementHandler() {
            super(TestEvenement.class);
        }

        public void handleMessage(TestEvenement evenement) {
            Assert.assertSame(evenement, sujet.getMessageCourant());
            testEvenementHandlerAppele = true;
        }
    }
}
