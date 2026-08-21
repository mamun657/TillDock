package com.tilldock.auth.dto;

import com.tilldock.auth.entity.InventoryMovement;

import java.time.OffsetDateTime;
import java.util.UUID;

public class StockMovementResponse {

    private UUID id;
    private UUID productId;
    private String movementType;
    private int delta;
    private int previousQuantity;
    private int newQuantity;
    private String reason;
    private OffsetDateTime createdAt;

    public static StockMovementResponse from(InventoryMovement m) {
        StockMovementResponse r = new StockMovementResponse();
        r.id = m.getId();
        r.productId = m.getProductId();
        r.movementType = m.getMovementType().name();
        r.delta = m.getDelta();
        r.previousQuantity = m.getPreviousQuantity();
        r.newQuantity = m.getNewQuantity();
        r.reason = m.getReason();
        r.createdAt = m.getCreatedAt();
        return r;
    }

    public UUID getId() { return id; }
    public UUID getProductId() { return productId; }
    public String getMovementType() { return movementType; }
    public int getDelta() { return delta; }
    public int getPreviousQuantity() { return previousQuantity; }
    public int getNewQuantity() { return newQuantity; }
    public String getReason() { return reason; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}