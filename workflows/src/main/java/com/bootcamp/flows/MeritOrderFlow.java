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

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;

public class MeritOrderFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class MeritOrderIssueFlowInitiator extends FlowLogic<SignedTransaction> {
//        private final Party peer1;
        private final List<Party> peers;
//        private final Party peer2;
//        private final Party peer3;
//        private final Party peer4;
        private final int resourceId;
        private final int energyVolume;
        private final int energyPrice;

        public MeritOrderIssueFlowInitiator(Party p1, Party p2, int rId, int ev, int ep) {
//            this.peer1 = p1;
//            this.peer2 = p2;
//            this.peer3 = peer3;
//            this.peer4 = peer4;
            this.resourceId = rId;
            this.energyVolume = ev;
            this.energyPrice = ep;

            peers = Arrays.asList(p1, p2);
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
//
//            /* ============================================================================
//             *         TODO 1 - Create our ResourceState to represent on-ledger resources!
//             * ===========================================================================*/
//            // We create our new ResourceState.
//            ResourceState resourceState =  new ResourceState(issuer, peer1, resourceId, energyVolume, energyPrice);
//
//            /* ============================================================================
//             *      TODO 3 - Build our resource issuance transaction to update the ledger!
//             * ===========================================================================*/
//            // We build our transaction.
//            TransactionBuilder transactionBuilder = new TransactionBuilder(notary)
//                    .addOutputState(resourceState)
//                    .addCommand(new ResourceContract.Commands.Issue(), Arrays.asList(issuer.getOwningKey(), peer1.getOwningKey()));
//
//            /* ============================================================================
//             *          TODO 2 - Write our ResourceContract to control resource issuance!
//             * ===========================================================================*/
//            // We check our transaction is valid based on its contracts.
//            transactionBuilder.verify(getServiceHub());
//
//            System.out.println("Trx verifiy");
//
//            FlowSession sessionForPeer1 = initiateFlow(peer1);
//
//            System.out.println("initiateFlow");
////            FlowSession sessionForPeer2 = initiateFlow(peer2);
////            FlowSession sessionForPeer3 = initiateFlow(peer3);
////            FlowSession sessionForPeer4 = initiateFlow(peer4);
//
//            // We sign the transaction with our private key, making it immutable.
//            SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);
//
//            System.out.println("signedTransaction");
//
//            // The counterparty signs the transaction
//            SignedTransaction fullySignedTransactionPeer1 = subFlow(new CollectSignaturesFlow(signedTransaction, singletonList(sessionForPeer1)));
//
//            System.out.println("fullySignedTransactionPeer1");
//
////            SignedTransaction fullySignedTransactionPeer2 = subFlow(new CollectSignaturesFlow(signedTransaction, singletonList(sessionForPeer2)));
//
////            SignedTransaction fullySignedTransactionPeer3 = subFlow(new CollectSignaturesFlow(signedTransaction, singletonList(sessionForPeer3)));
//
////            SignedTransaction fullySignedTransactionPeer4 = subFlow(new CollectSignaturesFlow(signedTransaction, singletonList(sessionForPeer4)));
//
//
//            // We get the transaction notarised and recorded automatically by the platform.
////            subFlow(new FinalityFlow(fullySignedTransactionPeer1, singletonList(sessionForPeer1)));
////            subFlow(new FinalityFlow(fullySignedTransactionPeer2, singletonList(sessionForPeer2)));
////            subFlow(new FinalityFlow(fullySignedTransactionPeer3, singletonList(sessionForPeer3)));
////            subFlow(new FinalityFlow(fullySignedTransactionPeer4, singletonList(sessionForPeer4)));
//
//            return subFlow(new FinalityFlow(fullySignedTransactionPeer1, singletonList(sessionForPeer1)));
        }
    }

    @InitiatedBy(MeritOrderIssueFlowInitiator.class)
    public static class MeritOrderIssueFlowResponder extends FlowLogic<Void>{
        //private variable
        private FlowSession counterpartySession;

        //Constructor
        public MeritOrderIssueFlowResponder(FlowSession counterpartySession) {
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
