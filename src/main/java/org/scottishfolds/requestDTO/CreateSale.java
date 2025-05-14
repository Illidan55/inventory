package org.scottishfolds.requestDTO;

import lombok.Data;

/**
 * Dto for Sale Modal
 */
@Data
public class CreateSale {
    private String saleDate;
    private String name;
    private String type;
    private int count;
    private String location;
    private float cost;
    private float salePrice;
}
