package org.scottishfolds.requestDTO;

import lombok.Data;

@Data
public class CreateItem {
    private String name;
    private float costPerUnit;
    private String type;
}
