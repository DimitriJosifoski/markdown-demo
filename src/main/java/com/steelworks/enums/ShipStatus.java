package com.steelworks.enums;

/**
 * Shipping status for a lot.
 * AC3: "Shipped" if Lot ID appears in Shipping log with valid ship date;
 *      otherwise "In Inventory."
 */
public enum ShipStatus {
    SHIPPED,
    IN_INVENTORY
}
