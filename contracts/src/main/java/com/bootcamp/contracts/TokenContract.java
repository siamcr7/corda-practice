package com.bootcamp.contracts;

import com.bootcamp.states.TokenState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

public class TokenContract implements Contract {
    public static String ID = "com.bootcamp.contracts.TokenContract";


    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        if (tx.getInputStates().size() != 0) {
            throw new IllegalArgumentException("Input state ase! SO problemooo");
        }

        if (tx.getOutputs().size() != 1) {
            throw new IllegalArgumentException("Output ektar beshi howa jabe na");
        }

        if (tx.getCommands().size() != 1) {
            throw new IllegalArgumentException();
        }

        if (!(tx.getOutput(0) instanceof TokenState)) {
            throw new IllegalArgumentException();
        }

        if (((TokenState) tx.getOutput(0)).getAmount() <= 0) {
            throw new IllegalArgumentException();
        }

        if (!(tx.getCommand(0).getValue() instanceof TokenContract.Commands.Issue)) {
            throw new IllegalArgumentException();
        }

        if (!(tx.getCommand(0).getSigners().contains(((TokenState) tx.getOutput(0)).getIssuer().getOwningKey()))) {
            throw new IllegalArgumentException();
        }
    }


    public interface Commands extends CommandData {
        class Issue implements Commands { }
    }
}