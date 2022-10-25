package com.bootcamp.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.bootcamp.contracts.ResourceContract;
import com.bootcamp.states.ResourceState;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;

public class ResourceIssueFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class ResourceIssueFlowInitiator extends FlowLogic<SignedTransaction> {
        private final int resourceId;
        private final int energyVolume;
        private final int energyPrice;

        public ResourceIssueFlowInitiator(int rId, int ev, int ep) {
            this.resourceId = rId;
            this.energyVolume = ev;
            this.energyPrice = ep;
        }

        private final ProgressTracker progressTracker = new ProgressTracker();

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {

            /** Explicit selection of notary by CordaX500Name - argument can by coded in flows or parsed from config (Preferred)*/
            final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"));
            // We get a reference to our own identity.
            Party issuer = getOurIdentity();

            ArrayList<Party> peers = new ArrayList<>();
            List<net.corda.core.node.NodeInfo> parties = getServiceHub().getNetworkMapCache().getAllNodes();
            for (net.corda.core.node.NodeInfo party : parties) {
                Party p = party.getLegalIdentities().stream().findFirst().get();

                if (p.getOwningKey() == issuer.getOwningKey() || getServiceHub().getNetworkMapCache().isNotary(p)) {
                    continue;
                }

                peers.add(p);
            }

            for (int i = 0; i < peers.size(); i++) {
                Party peer = peers.get(i);

                ResourceState resourceState =  new ResourceState(issuer, peer, resourceId, energyVolume, energyPrice);

                TransactionBuilder transactionBuilder = new TransactionBuilder(notary)
                        .addOutputState(resourceState)
                        .addCommand(new ResourceContract.Commands.Issue(), Arrays.asList(issuer.getOwningKey(), peer.getOwningKey()));

                transactionBuilder.verify(getServiceHub());

                FlowSession session = initiateFlow(peer);

                SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);
                SignedTransaction fullySignedTransaction = subFlow(new CollectSignaturesFlow(signedTransaction, singletonList(session)));

                subFlow(new FinalityFlow(fullySignedTransaction, singletonList(session)));
            }

            return null;
        }
    }

    @InitiatedBy(ResourceIssueFlowInitiator.class)
    public static class ResourceIssueFlowResponder extends FlowLogic<Void> {
        //private variable
        private FlowSession counterpartySession;

        //Constructor
        public ResourceIssueFlowResponder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Suspendable
        @Override
        public Void call() throws FlowException {
            SignedTransaction signedTransaction = subFlow(new SignTransactionFlow(counterpartySession) {
                @Suspendable
                @Override
                protected void checkTransaction(SignedTransaction stx) throws FlowException {
                    /*
                     * SignTransactionFlow will automatically verify the transaction and its signatures before signing it.
                     * However, just because a transaction is contractually valid doesn’t mean we necessarily want to sign.
                     * What if we don’t want to deal with the counterparty in question, or the value is too high,
                     * or we’re not happy with the transaction’s structure? checkTransaction
                     * allows us to define these additional checks. If any of these conditions are not met,
                     * we will not sign the transaction - even if the transaction and its signatures are contractually valid.
                     * ----------
                     * For this hello-world cordapp, we will not implement any aditional checks.
                     * */
                }
            });
            //Stored the transaction into data base.
            subFlow(new ReceiveFinalityFlow(counterpartySession, signedTransaction.getId()));
            return null;
        }
    }
}
