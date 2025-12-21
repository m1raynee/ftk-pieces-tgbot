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
@EqualsAndHashCode(exclude = { "requestedActions", "performedActions" })
@ToString(exclude = { "requestedActions", "performedActions" })
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "requester", fetch = FetchType.LAZY)
    private List<PieceAction> requestedActions;

    @OneToMany(mappedBy = "performer", fetch = FetchType.LAZY)
    private List<PieceAction> performedActions;

    public Student(String name) {
        this.name = name;
    }

    public String getTagId() {
        return "(STU-%d)".formatted(id);
    }

}
