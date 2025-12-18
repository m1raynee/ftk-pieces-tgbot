package com.m1raynee.db.entity;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.NaturalId;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Box {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NaturalId
    private Integer index;

    @Column(nullable = false)
    private String name;

    @Column(name = "place_code", length = 6)
    private String placeCode;

    @OneToMany(mappedBy = "box", cascade = CascadeType.PERSIST)
    private Set<Piece> pieces = new HashSet<>();

    public Box(Integer index, String name, String placeCode) {
        this.index = index;
        this.name = name;
        this.placeCode = placeCode;
    }

}
