package com.m1raynee.db.entity;

import java.util.List;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = { "actions" })
@ToString(exclude = { "actions" })
public class Piece {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String article;

    @Column(nullable = false)
    private String name;

    @Column(name = "alt_name")
    private String altName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_id")
    private Box box;

    @Column(name = "cell_hint")
    private String cellHint;

    @OneToMany(mappedBy = "piece", fetch = FetchType.LAZY)
    private List<PieceAction> actions;

    @Transient
    private List<PieceAction> trActions;

    @Transient
    private Long positiveAmount;
    @Transient
    private Long negativeAmount;

    public Piece(String article, String name, String altName, Box box) {
        this.article = article;
        this.name = name;
        this.altName = altName;
        this.box = box;
    }

    public String getTagId() {
        return "(PCE-%d)".formatted(id);
    }

    public Long getCalculatedAmount() {
        return positiveAmount - negativeAmount;
    }

}
