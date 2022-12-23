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

import net.corda.core.crypto.SecureHash;
import net.corda.core.node.ServiceHub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static java.util.Collections.singletonList;

public class OnChainFlow {
    @InitiatingFlow
    @StartableByRPC
    public static class OnChainUpload extends FlowLogic<SignedTransaction> {

        private final int trxNum;
        private final int fileSzInMB;

        public OnChainUpload(int sz, int trx) {
            trxNum = trx;
            fileSzInMB = sz;
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
            ArrayList<Long> times = new ArrayList<>();

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

            for (int idx = 1; idx <= trxNum; idx++) {
                String fileName = fileSzInMB + "mb_" + idx;
                String zipPath = "../../../../" + fileName + ".zip";

                SecureHash attachmentHash = null;
                try {
                    attachmentHash = SecureHash.parse(uploadAttachment(
                            zipPath,
                            getServiceHub(),
                            getOurIdentity(),
                            fileName + "zip")
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Sending to all nodes
                for (int i = 0; i < peers.size(); i++) {
                    Party peer = peers.get(i);

                    FileState fileState =  new FileState(issuer, peer, attachmentHash.toString());

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
                times.add(totalTime);
            }

            long totalDiff = 0;
            long count = 0;
            for (int i = 0; i < times.size(); i++) {
                if (i > 0) {
                    totalDiff += (times.get(i) - times.get(i - 1));
                    count++;
                }
            }

            double avgTime = (double) totalDiff / (double) count;
            double elapsedTimeInSecond = (double) avgTime / 1_000_000_000;
            System.out.println("Time taken on avg = " + elapsedTimeInSecond);

            double totalTimeTaken = (double) times.get(times.size() - 1) / 1_000_000_000;
            System.out.println("Total time taken for " + times.size() + " trx = " + totalTimeTaken);

            return null;
        }

        private String uploadAttachment(String path, ServiceHub service, Party whoami,
                                        String filename) throws IOException {
            SecureHash attachmentHash = service.getAttachments().importAttachment(
                    new FileInputStream(new File(path)),
                    whoami.toString(),
                    filename
            );

            return attachmentHash.toString();
        }
    }

    @InitiatedBy(OnChainUpload.class)
    public static class OnChainUploadResponder extends FlowLogic<Void> {
        //private variable
        private FlowSession counterpartySession;

        //Constructor
        public OnChainUploadResponder(FlowSession counterpartySession) {
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
