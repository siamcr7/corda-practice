package com.bootcamp.contracts;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

public class ResourceContract implements Contract {

    public static String ID = "com.bootcamp.contracts.ResourceContract";

    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        if (tx.getInputStates().size() != 0) {
            throw new IllegalArgumentException("Input state ase! SO problemooo");
        }

        if (tx.getOutputs().size() != 1) {
            throw new IllegalArgumentException("Output ektar beshi howa jabe na");
        }
    }

    public interface Commands extends CommandData {
        class Issue implements Commands { }
    }
}
