package org.cosmetology.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "skin")
public class Skin {

    @Id
    @Column(name = "skin_type", nullable = false)
    private String skinType;

    @Column(name = "recommend", nullable = false)
    private String recommend;
}