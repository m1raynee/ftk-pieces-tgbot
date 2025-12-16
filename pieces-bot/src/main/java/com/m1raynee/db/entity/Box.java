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

    @OneToMany(mappedBy = "box", cascade = CascadeType.PERSIST)
    private Set<Piece> pieces = new HashSet<>();

}
