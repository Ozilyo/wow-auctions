package com.radcortez.wow.auctions.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @author Ivan St. Ivanov
 */
@Entity
@Data
@NamedQueries({
    @NamedQuery(name = "AuctionItemStatistics.findByRealmAndItem",
                query = "SELECT ais FROM AuctionItemStatistics ais " +
                        "WHERE ais.realm.id = :realmId AND ais.itemId = :itemId " +
                        "ORDER BY ais.timestamp DESC, ais.auctionHouse ASC")
})
public class AuctionItemStatistics {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Integer itemId;
    private Long quantity;

    private Long bid;
    private Long minBid;
    private Long maxBid;

    private Long buyout;
    private Long minBuyout;
    private Long maxBuyout;

    private Double avgBid;
    private Double avgBuyout;
    private Long timestamp;

    private AuctionHouse auctionHouse;

    @ManyToOne
    private Realm realm;
}
