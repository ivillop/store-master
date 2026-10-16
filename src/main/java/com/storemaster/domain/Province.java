package com.storemaster.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@Entity
@Table(name = "province")
@SQLRestriction("deleted_at IS NULL")
public class Province extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;
}
