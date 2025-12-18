package com.m1raynee.db.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Piece {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String article;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer amount;

    @Column(name = "alt_name")
    private String altName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_id")
    private Box box;

    @Column(name = "cell_hint")
    private String cellHint;

    @OneToMany(mappedBy = "piece", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PieceAction> actions = new HashSet<>();

    public Piece(String article, String name, Integer amount, String altName, Box box) {
        this.article = article;
        this.name = name;
        this.amount = amount;
        this.altName = altName;
        this.box = box;
    }

}
