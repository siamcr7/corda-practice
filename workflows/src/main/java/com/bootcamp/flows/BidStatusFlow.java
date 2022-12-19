//package com.bootcamp.flows;
//
//import co.paralleluniverse.fibers.Suspendable;
//import com.bootcamp.states.BidState;
//import com.bootcamp.states.InitialBidState;
//import com.bootcamp.states.ResourceState;
//import net.corda.core.contracts.StateAndRef;
//import net.corda.core.contracts.TransactionState;
//import net.corda.core.flows.*;
//import net.corda.core.identity.Party;
//import net.corda.core.transactions.SignedTransaction;
//import net.corda.core.utilities.ProgressTracker;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class BidStatusFlow {
//    @InitiatingFlow
//    @StartableByRPC
//    public static class CheckStatus extends FlowLogic<SignedTransaction> {
//        public CheckStatus() {
//
//        }
//
//        private final ProgressTracker progressTracker = new ProgressTracker();
//
//        @Override
//        public ProgressTracker getProgressTracker() {
//            return progressTracker;
//        }
//
//        @Suspendable
//        @Override
//        public SignedTransaction call() throws FlowException {
////            Party issuer = getOurIdentity();
//
//            List<StateAndRef<InitialBidState>> initialBidStates =
//                    getServiceHub().getVaultService().queryBy(InitialBidState.class).getStates();
//
//            Map<Integer, InitialBidState> initialBidStateMappedToId = new HashMap<Integer, InitialBidState>();
//            for (int i = 0; i < initialBidStates.size(); i++) {
//                InitialBidState state = initialBidStates.get(i).getState().getData();
//                initialBidStateMappedToId.put(state.getBidId(), state);
//            }
//
//            List<StateAndRef<BidState>> bidStates =
//                    getServiceHub().getVaultService().queryBy(BidState.class).getStates();
//
//            Map<Integer, BidState> bidStateMappedToId = new HashMap<Integer, BidState>();
//            for (int i = 0; i < bidStates.size(); i++) {
//                BidState state = bidStates.get(i).getState().getData();
//
//                if (!bidStateMappedToId.containsKey(state.getBidId())) {
//                    bidStateMappedToId.put(state.getBidId(), state);
//                } else {
//                    BidState prevState = bidStateMappedToId.get(state.getBidId());
//                    if (state.getPrice() > prevState.getPrice()) {
//                        bidStateMappedToId.put(state.getBidId(), state);
//                    }
//                }
//            }
//
//            System.out.println("Current Bidding Status: ");
//            for (int id : initialBidStateMappedToId.keySet()) {
//                InitialBidState initialBidState = initialBidStateMappedToId.get(id);
//                BidState bidState = bidStateMappedToId.get(id);
//
//                if (bidState == null) {
//                    initialBidState.showInConsole( null, initialBidState.getPrice(), null);
//                } else {
//                    initialBidState.showInConsole(bidState.getBidder(), bidState.getPrice(), bidState.getLastBidTime());
//                }
//            }
//
//            return null;
//        }
//    }
//
//    @InitiatedBy(CheckStatus.class)
//    public static class CheckStatusFlowResponder extends FlowLogic<Void> {
//        //private variable
//        private FlowSession counterpartySession;
//
//        //Constructor
//        public CheckStatusFlowResponder(FlowSession counterpartySession) {
//            this.counterpartySession = counterpartySession;
//        }
//
//        @Suspendable
//        @Override
//        public Void call() throws FlowException {
//            SignedTransaction signedTransaction = subFlow(new SignTransactionFlow(counterpartySession) {
//                @Suspendable
//                @Override
//                protected void checkTransaction(SignedTransaction stx) throws FlowException {
//                    /*
//                     * SignTransactionFlow will automatically verify the transaction and its signatures before signing it.
//                     * However, just because a transaction is contractually valid doesn’t mean we necessarily want to sign.
//                     * What if we don’t want to deal with the counterparty in question, or the value is too high,
//                     * or we’re not happy with the transaction’s structure? checkTransaction
//                     * allows us to define these additional checks. If any of these conditions are not met,
//                     * we will not sign the transaction - even if the transaction and its signatures are contractually valid.
//                     * ----------
//                     * For this hello-world cordapp, we will not implement any aditional checks.
//                     * */
//                }
//            });
//            //Stored the transaction into data base.
//            subFlow(new ReceiveFinalityFlow(counterpartySession, signedTransaction.getId()));
//            return null;
//        }
//    }
//}
