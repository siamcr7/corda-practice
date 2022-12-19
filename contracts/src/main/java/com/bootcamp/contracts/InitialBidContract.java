//package com.bootcamp.contracts;
//
//import net.corda.core.contracts.CommandData;
//import net.corda.core.contracts.Contract;
//import net.corda.core.transactions.LedgerTransaction;
//
//public class InitialBidContract implements Contract {
//    public static String ID = "com.bootcamp.contracts.InitialBidContract";
//
//    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
//        /*
//        Verifications are done at the beginning of the flow
//         */
//    }
//
//    public interface Commands extends CommandData {
//        class Issue implements Commands { }
//    }
//}
