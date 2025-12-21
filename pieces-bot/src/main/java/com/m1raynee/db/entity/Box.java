package com.m1raynee.db.entity;

import java.util.List;

import org.hibernate.annotations.NaturalId;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = { "pieces" })
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

    @OneToMany(mappedBy = "box", fetch = FetchType.LAZY)
    private List<Piece> pieces;

    public Box(Integer index, String name, String placeCode) {
        this.index = index;
        this.name = name;
        this.placeCode = placeCode;
    }

    @Override
    public String toString() {
        return getTagId() + " " + name;
    }

    public String getTagId() {
        var result = "(BOX-" + index;
        if (placeCode != null)
            result += ", " + placeCode;
        return result + ")";
    }

}
