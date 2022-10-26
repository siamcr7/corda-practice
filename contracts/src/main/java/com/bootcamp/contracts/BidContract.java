package com.bootcamp.contracts;

import com.bootcamp.states.BidState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

public class BidContract implements Contract {
    public static String ID = "com.bootcamp.contracts.BidContract";

    public void verify(LedgerTransaction tx) throws IllegalArgumentException {

    }

    public interface Commands extends CommandData {
        class Issue implements Commands { }
    }
}
