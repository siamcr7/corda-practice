//package com.bootcamp.flows;
//
//import co.paralleluniverse.fibers.Suspendable;
//import com.bootcamp.contracts.InitialBidContract;
//import com.bootcamp.states.BidState;
//import com.bootcamp.states.InitialBidState;
//import net.corda.core.flows.*;
//import net.corda.core.identity.CordaX500Name;
//import net.corda.core.identity.Party;
//import net.corda.core.node.NodeInfo;
//import net.corda.core.transactions.SignedTransaction;
//import net.corda.core.transactions.TransactionBuilder;
//import net.corda.core.utilities.ProgressTracker;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import static java.util.Collections.singletonList;
//
//public class BiddingFlow {
//    @InitiatingFlow
//    @StartableByRPC
//    public static class DoBid extends FlowLogic<SignedTransaction> {
//        private final int id;
//        private final int price;
//
//        public DoBid(int id, int price) {
//            this.id = id;
//            this.price = price;
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
//
//            List<net.corda.core.contracts.StateAndRef<InitialBidState>> initialBidStates =
//                    getServiceHub().getVaultService().queryBy(InitialBidState.class).getStates();
//
//            InitialBidState initialBid = null;
//            for (net.corda.core.contracts.StateAndRef<InitialBidState> state : initialBidStates) {
//                if (state.getState().getData().getBidId() == id) {
//                    initialBid = state.getState().getData();
//                    break;
//                }
//            }
//
//            if (initialBid == null) {
//                System.out.println("The Bid Id does not exist");
//                throw new IllegalArgumentException("The Bid Id does not exist");
//            }
//
//            List<net.corda.core.contracts.StateAndRef<BidState>> bidStates =
//                    getServiceHub().getVaultService().queryBy(BidState.class).getStates();
//
//            BidState latestBid = null;
//            for (net.corda.core.contracts.StateAndRef<BidState> state : bidStates) {
//                if (latestBid == null) {
//                    latestBid = state.getState().getData();
//                }
//
//                if (state.getState().getData().getPrice() > latestBid.getPrice()) {
//                    latestBid = state.getState().getData();
//                }
//            }
//
//            LocalDateTime lastBidTimeDeadLine = initialBid.getLastBidTime().plusMinutes(initialBid.getWaitTimeInMinutes());
//            int lastPrice = initialBid.getPrice();
//            if (latestBid != null) {
//                lastBidTimeDeadLine = latestBid.getLastBidTime().plusMinutes(initialBid.getWaitTimeInMinutes());
//                lastPrice = latestBid.getPrice();
//            }
//
//            if (LocalDateTime.now().isAfter(lastBidTimeDeadLine)) {
//                System.out.println("The Bidding has already ended!");
//                throw new IllegalArgumentException("The Bidding has already ended!");
//            }
//
//            if (lastPrice >= price) {
//                System.out.println("Current bidding must be more than the last bid");
//                throw new IllegalArgumentException("Current bidding must be more than the last bid");
//            }
//
//            Party issuer = getOurIdentity();
//
//            if (issuer.getOwningKey().equals(initialBid.getIssuer().getOwningKey())) {
//                System.out.println("The issuers can not bid on their own items.");
//                throw new IllegalArgumentException("The issuers can not bid on their own items.");
//            }
//
//            final Party notary = getServiceHub().getNetworkMapCache()
//                    .getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"));
//
//            ArrayList<Party> peers = new ArrayList<>();
//            List<NodeInfo> parties = getServiceHub().getNetworkMapCache().getAllNodes();
//            for (net.corda.core.node.NodeInfo party : parties) {
//                Party p = party.getLegalIdentities().stream().findFirst().get();
//
//                if (p.getOwningKey().equals(issuer.getOwningKey()) || getServiceHub().getNetworkMapCache().isNotary(p)) {
//                    continue;
//                }
//
//                peers.add(p);
//            }
//
//            for (int i = 0; i < peers.size(); i++) {
//                Party peer = peers.get(i);
//
//                BidState bidState =  new BidState(issuer, peer, id, price, LocalDateTime.now());
//
//                TransactionBuilder transactionBuilder = new TransactionBuilder(notary)
//                        .addOutputState(bidState)
//                        .addCommand(new InitialBidContract.Commands.Issue(), Arrays.asList(issuer.getOwningKey(), peer.getOwningKey()));
//
//                transactionBuilder.verify(getServiceHub());
//
//                FlowSession session = initiateFlow(peer);
//
//                SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);
//
//                SignedTransaction fullySignedTransaction = subFlow(new CollectSignaturesFlow(signedTransaction, singletonList(session)));
//
//                subFlow(new FinalityFlow(fullySignedTransaction, singletonList(session)));
//            }
//
//            return null;
//        }
//    }
//
//    @InitiatedBy(DoBid.class)
//    public static class DoBidFlowResponder extends FlowLogic<Void> {
//        //private variable
//        private FlowSession counterpartySession;
//
//        //Constructor
//        public DoBidFlowResponder(FlowSession counterpartySession) {
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
