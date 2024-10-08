package org.scottishfolds.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Item Entity for mongodb
 */
@Data
@NoArgsConstructor
@Document("items")
public class Item {
    @Id
    private String id;
    private String name;
    private String type;
    private int count;
    private float costPerUnit;

}
