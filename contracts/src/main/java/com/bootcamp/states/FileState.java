package com.bootcamp.states;

import com.bootcamp.contracts.FileContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@BelongsToContract(FileContract.class)
public class FileState implements ContractState {
    private final Party issuer;
    private final Party owner;
    private final String fileHashId;

    public FileState(Party issuer, Party owner, String fileHashId) {
        this.issuer = issuer;
        this.owner = owner;
        this.fileHashId = fileHashId;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(issuer, owner);
    }
}
