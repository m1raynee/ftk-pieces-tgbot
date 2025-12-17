package com.m1raynee.db.entity;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.NaturalId;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Box {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NaturalId
    @NonNull
    private Integer index;

    @Column(nullable = false)
    @NonNull
    private String name;

    @Column(name = "place_code", length = 6)
    @NonNull
    private String placeCode;

    @OneToMany(mappedBy = "box", cascade = CascadeType.PERSIST)
    private Set<Piece> pieces = new HashSet<>();

}
