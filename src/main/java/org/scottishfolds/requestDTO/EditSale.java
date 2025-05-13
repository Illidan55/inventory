package org.scottishfolds.requestDTO;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.Instant;

@Data
public class EditSale {
    @Id
    private String id;
    private String saleDate;
    private String name;
    private String type;
    private int count;
    private String location;
    private float cost;
    private float salePrice;
}
