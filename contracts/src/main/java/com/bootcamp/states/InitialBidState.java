package com.bootcamp.states;

import com.bootcamp.contracts.InitialBidContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@BelongsToContract(InitialBidContract.class)
public class InitialBidState implements ContractState {
    private final Party issuer;
    private final Party peer;
    private final int price;
    private final LocalDateTime lastBidTime;
    private final int bidId;

    private final int waitTimeInMinutes;

    public InitialBidState(Party issuer, Party peer, int bidId, int price,
                           LocalDateTime lastBidTime, int waitTimeInMinutes) {
        this.issuer = issuer;
        this.peer = peer;
        this.price = price;
        this.lastBidTime = lastBidTime;
        this.bidId = bidId;
        this.waitTimeInMinutes = waitTimeInMinutes;
    }

    public int getPrice() {
        return price;
    }

    public LocalDateTime getLastBidTime() {
        return lastBidTime;
    }

    public int getBidId() {
        return bidId;
    }

    public void showInConsole(Party lastBidder, int currentPrice,
                                     LocalDateTime lastBidTimeByBidder) {
        String output = "";

        output += ("Issuer: " + this.issuer.getName() + "\n");
        output += ("Bid Id: " + this.bidId + "\n");
        output += ("Current Bid: " + currentPrice + "\n");

        if (lastBidder != null) {
            output += ("Last Bidder: " + lastBidder.getName() + "\n");
            output += ("Last Bid Time: " + lastBidTimeByBidder.toString() + "\n");
            output += ("Next Bid Deadline: " + lastBidTimeByBidder.plusMinutes(waitTimeInMinutes).toString() + "\n");
        } else {
            output += ("Last Bidder: NONE" + "\n");
            output += ("Last Bid Time: NONE" + "\n");
            output += ("Next Bid Deadline: " + this.lastBidTime.plusMinutes(waitTimeInMinutes).toString() + "\n");
        }

        output += ("Wait Time between bids (in minutes): " + waitTimeInMinutes + "\n");


        output += "-------------------\n";
        output += ("\n");

        System.out.println(output);
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(issuer, peer);
    }
}
