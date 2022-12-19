package com.bootcamp.contracts;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

public class FileContract implements Contract {
    public static String ID = "com.bootcamp.contracts.FileContract";

    public void verify(LedgerTransaction tx) throws IllegalArgumentException {

    }

    public interface Commands extends CommandData {
        class Issue implements FileContract.Commands { }
    }
}
