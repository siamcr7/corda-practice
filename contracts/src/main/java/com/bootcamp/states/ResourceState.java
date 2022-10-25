package com.bootcamp.states;

import com.bootcamp.contracts.ResourceContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@BelongsToContract(ResourceContract.class)
public class ResourceState implements ContractState {

    private final Party issuer;
    private final Party peer1;
//    private final Party peer2;
//    private final Party peer3;
//    private final Party peer4;
    private final int resourceId;
    private final int energyVolume;
    private final int energyPrice;

    public ResourceState(Party issuer, Party peer1, int resourceId, int energyVolume, int energyPrice) {
        this.issuer = issuer;
        this.peer1 = peer1;
//        this.peer2 = peer2; // COULD THIS BE THE PROBLEM?/*/*?*/*/
//        this.peer3 = peer3;
//        this.peer4 = peer4;
        this.resourceId = resourceId;
        this.energyVolume = energyVolume;
        this.energyPrice = energyPrice;
    }

    public int getResourceId() {
        return resourceId;
    }

    public int getEnergyVolume() {
        return energyVolume;
    }

    public int getEnergyPrice() {
        return energyPrice;
    }

    public Party getIssuer() {
        return issuer;
    }

    @Override
    public String toString() {
        String output = "";

        output += ("Resource ID: " + resourceId + "\n");
        output += ("Energy Volume: " + energyVolume + "\n");
        output += ("Energy Price: " + energyPrice + "\n");
        output += ("\n" + "\n");

        return output;
    }

//    public List<Party> getPeers() {
////        return Arrays.asList(peer1, peer2, peer3, peer4);
//        return Arrays.asList(peer1, peer2);
//    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
//        return Arrays.asList(issuer, peer1, peer2, peer3, peer4);
        return Arrays.asList(issuer, peer1);
    }
}
