//package com.bootcamp.states;
//
//import com.bootcamp.contracts.InitialBidContract;
//import net.corda.core.contracts.BelongsToContract;
//import net.corda.core.contracts.ContractState;
//import net.corda.core.identity.AbstractParty;
//import net.corda.core.identity.Party;
//import org.jetbrains.annotations.NotNull;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.Arrays;
//import java.util.List;
//
//@BelongsToContract(InitialBidContract.class)
//public class InitialBidState implements ContractState {
//    private final Party issuer;
//    private final Party peer;
//    private final int price;
//    private final LocalDateTime lastBidTime;
//    private final int bidId;
//
//    private final int waitTimeInMinutes;
//
//    public InitialBidState(Party issuer, Party peer, int bidId, int price,
//                           LocalDateTime lastBidTime, int waitTimeInMinutes) {
//        this.issuer = issuer;
//        this.peer = peer;
//        this.price = price;
//        this.lastBidTime = lastBidTime;
//        this.bidId = bidId;
//        this.waitTimeInMinutes = waitTimeInMinutes;
//    }
//
//    public int getPrice() {
//        return price;
//    }
//
//    public LocalDateTime getLastBidTime() {
//        return lastBidTime;
//    }
//
//    public int getBidId() {
//        return bidId;
//    }
//
//    public int getWaitTimeInMinutes() { return waitTimeInMinutes; }
//
//    public Party getIssuer() { return issuer; }
//
//    public void showInConsole(Party lastBidder, int currentPrice,
//                                     LocalDateTime lastBidTimeByBidder) {
//
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//
//        String output = "";
//
//        output += ("Issuer: " + this.issuer.getName() + "\n");
//        output += ("Bid Id: " + this.bidId + "\n");
//        output += ("Last Bid: " + currentPrice + "\n");
//
//        boolean foundWinner = false;
//        if (lastBidder != null) {
//            if (LocalDateTime.now().isAfter(lastBidTimeByBidder.plusMinutes(waitTimeInMinutes))) {
//                output += ("Last Bidder: " + lastBidder.getName() + "\n");
//                foundWinner = true;
//            } else {
//                output += ("Last Bidder: " + lastBidder.getName() + "\n");
//            }
//            output += ("Last Bid Time: " + lastBidTimeByBidder.format(formatter) + "\n");
//            output += ("Next Bid Deadline: " + lastBidTimeByBidder.plusMinutes(waitTimeInMinutes).format(formatter) + "\n");
//
//
//        } else {
//            output += ("Last Bidder: N/A" + "\n");
//            output += ("Initial Bid Start Time: " + this.lastBidTime.format(formatter) + "\n");
//
//            if (LocalDateTime.now().isAfter(this.lastBidTime.plusMinutes(waitTimeInMinutes))) {
//                output += "This Bidding is cancelled! \n";
//            } else {
//                output += ("Next Bid Deadline: " + this.lastBidTime.plusMinutes(waitTimeInMinutes).format(formatter) + "\n");
//            }
//        }
//
//        output += ("Wait Time between bids (in minutes): " + waitTimeInMinutes + "\n");
//
//        if (foundWinner) {
//            output += ("Bidding has ended. The Winner is last bidder: " + lastBidder.getName() + "\n");
//        }
//
//        output += "-------------------\n";
//        output += ("\n");
//
//        System.out.println(output);
//    }
//
//    @NotNull
//    @Override
//    public List<AbstractParty> getParticipants() {
//        return Arrays.asList(issuer, peer);
//    }
//}
