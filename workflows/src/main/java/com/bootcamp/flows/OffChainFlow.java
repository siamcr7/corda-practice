package com.bootcamp.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.bootcamp.contracts.FileContract;
import com.bootcamp.states.FileState;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singletonList;

public class OffChainFlow {
    @InitiatingFlow
    @StartableByRPC
    public static class OffChainUpload extends FlowLogic<SignedTransaction> {

        public OffChainUpload() {
        }

        private final ProgressTracker progressTracker = new ProgressTracker();

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {

            long startTime = System.nanoTime();

            /** Explicit selection of notary by CordaX500Name - argument can by coded in flows or parsed from config (Preferred)*/
            final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"));
            // We get a reference to our own identity.
            Party issuer = getOurIdentity();

            ArrayList<Party> peers = new ArrayList<>();
            List<net.corda.core.node.NodeInfo> parties = getServiceHub().getNetworkMapCache().getAllNodes();
            for (net.corda.core.node.NodeInfo party : parties) {
                Party p = party.getLegalIdentities().stream().findFirst().get();

                if (p.getOwningKey().equals(issuer.getOwningKey()) || getServiceHub().getNetworkMapCache().isNotary(p)) {
                    continue;
                }

                peers.add(p);
            }

            System.out.println("point1");
            InitOffChainUpload asyncOffChainUpload = new InitOffChainUpload();
            String attachmentHash = await(asyncOffChainUpload);

            System.out.println("attachmentHash: " + attachmentHash);


            // Sending to all nodes
            for (int i = 0; i < peers.size(); i++) {
                Party peer = peers.get(i);

                FileState fileState =  new FileState(issuer, peer, attachmentHash);

                TransactionBuilder transactionBuilder = new TransactionBuilder(notary)
                        .addOutputState(fileState)
                        .addCommand(new FileContract.Commands.Issue(), Arrays.asList(issuer.getOwningKey(),
                                peer.getOwningKey()));

                transactionBuilder.verify(getServiceHub());

                FlowSession session = initiateFlow(peer);

                SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);
                SignedTransaction fullySignedTransaction = subFlow(new CollectSignaturesFlow(signedTransaction,
                        singletonList(session)));

                subFlow(new FinalityFlow(fullySignedTransaction, singletonList(session)));
            }

            long endTime   = System.nanoTime();
            long totalTime = endTime - startTime;
            System.out.println("totalTime: " + totalTime);

            return null;
        }
    }

    @InitiatedBy(OffChainUpload.class)
    public static class OffChainUploadResponder extends FlowLogic<Void> {
        //private variable
        private FlowSession counterpartySession;

        //Constructor
        public OffChainUploadResponder(FlowSession counterpartySession) {
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

class InitOffChainUpload implements FlowExternalOperation<String> {
    @NotNull
    @Override
    public String execute(@NotNull String deduplicationId) {
        System.out.println("point2");
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.HOURS)
                .writeTimeout(10, TimeUnit.HOURS)
                .readTimeout(30, TimeUnit.HOURS)
                .build();
        System.out.println("point3");
        String url = "http://localhost:8080";

        try {
            return client.newCall(
                            new Request.Builder()
                                    .url(url)
                                    .get()
                                    .build())
                    .execute()
                    .body()
                    .string();

        } catch (IOException e) {
            System.out.println("point 3.9");
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            System.out.println("point4: EEEEEE " + errors.toString());
            throw new HospitalizeFlowException("External Api Called Failed", e);
        }
    }
}
