package org.scottishfolds.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document("product")
public class Product {
    @Id
    private String id;
    private String name;
    private String type;
    private int backStock;
    private int inStoreStock;
    private int onlineStock;
    private float inStorePrice;
    private float onlinePrice;
}
