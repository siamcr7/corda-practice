//package com.bootcamp.states;
//
//import com.bootcamp.contracts.BidContract;
//import com.bootcamp.contracts.InitialBidContract;
//import net.corda.core.contracts.BelongsToContract;
//import net.corda.core.contracts.ContractState;
//import net.corda.core.identity.AbstractParty;
//import net.corda.core.identity.Party;
//import org.jetbrains.annotations.NotNull;
//
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.List;
//
//@BelongsToContract(BidContract.class)
//public class BidState implements ContractState {
//    private final Party bidder;
//    private final Party peer;
//    private final int bidId;
//    private final int price;
//    private final LocalDateTime lastBidTime;
//
//    public BidState(Party bidder, Party peer, int bidId, int price, LocalDateTime lastBidTime) {
//        this.bidder = bidder;
//        this.peer = peer;
//        this.bidId = bidId;
//        this.price = price;
//        this.lastBidTime = lastBidTime;
//    }
//
//    public int getBidId() {
//        return bidId;
//    }
//
//    public LocalDateTime getLastBidTime() {
//        return lastBidTime;
//    }
//
//    public int getPrice() {
//        return price;
//    }
//
//    public Party getBidder() {
//        return bidder;
//    }
//
//    @NotNull
//    @Override
//    public List<AbstractParty> getParticipants() {
//        return Arrays.asList(bidder, peer);
//    }
//}
