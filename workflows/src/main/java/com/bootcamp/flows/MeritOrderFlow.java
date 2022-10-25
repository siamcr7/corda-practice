package com.bootcamp.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.bootcamp.states.ResourceState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.TransactionState;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.ProgressTracker;

import java.util.*;

public class MeritOrderFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class MeritOrderIssueFlowInitiator extends FlowLogic<SignedTransaction> {

        public MeritOrderIssueFlowInitiator() {

        }

        private final ProgressTracker progressTracker = new ProgressTracker();

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {

            // We get a reference to our own identity.
            Party issuer = getOurIdentity();

//            val auctionStates = serviceHub.vaultService.queryBy(AuctionState::class.java)

            List<StateAndRef<ResourceState>> resourceStates = getServiceHub().getVaultService().queryBy(ResourceState.class).getStates();

            Map<Integer, ResourceState> map = new HashMap<Integer, ResourceState>();
            for (int i = 0; i < resourceStates.size(); i++) {
                ResourceState state = resourceStates.get(i).getState().getData();
                map.put(state.getResourceId(), state);
            }

            ArrayList<ResourceState> states = new ArrayList<ResourceState>();
            for (int id : map.keySet()) {
                states.add(map.get(id));
            }

            states.sort(Comparator.comparingInt(ResourceState::getEnergyPrice));

            System.out.println("Merit Order: ");
            for (ResourceState state : states) {
                System.out.println(state);
            }

//            states.sort(new ResourceMeritOrderComparator());



//            for (int i = 0; i < peers.size(); i++) {
//                Party peer = peers.get(i);
//
//                ResourceState resourceState =  new ResourceState(issuer, peer, resourceId, energyVolume, energyPrice);
//                TransactionBuilder transactionBuilder = new TransactionBuilder(notary)
//                        .addOutputState(resourceState)
//                        .addCommand(new ResourceContract.Commands.Issue(), Arrays.asList(issuer.getOwningKey(), peer.getOwningKey()));
//                transactionBuilder.verify(getServiceHub());
//                FlowSession session = initiateFlow(peer);
//                SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);
//                SignedTransaction fullySignedTransaction = subFlow(new CollectSignaturesFlow(signedTransaction, singletonList(session)));
//                subFlow(new FinalityFlow(fullySignedTransaction, singletonList(session)));
//            }

            return null;
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
