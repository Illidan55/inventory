package org.scottishfolds.requestDTO;

import lombok.Data;

/**
 * Dto for Item Modal
 */
@Data
public class CreateItem {
    private String name;
    private float costPerUnit;
    private String type;
}
