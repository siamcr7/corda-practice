package com.bootcamp.states;

import com.bootcamp.contracts.OnChainFileContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@BelongsToContract(OnChainFileContract.class)
public class OnChainFileState implements ContractState {
    private final Party issuer;
    private final Party owner;
    private final byte[] data;

    public OnChainFileState(Party issuer, Party owner, byte[] data) {
        this.issuer = issuer;
        this.owner = owner;
        this.data = data;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(issuer, owner);
    }
}
