package org.scottishfolds.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@Document("sale")
public class Sale {
    @Id
    private String id;
    private Instant saleDate;
    private String name;
    private String type;
    private int count;
    private String location;
    private float cost;
    private float salePrice;
}
