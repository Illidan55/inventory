package org.scottishfolds.requestDTO;

import lombok.Data;

@Data
public class CreateProduct {
    private String name;
    private String type;
    private int backStock;
    private int inStoreStock;
    private int onlineStock;
    private float inStorePrice;
    private float onlinePrice;
}
