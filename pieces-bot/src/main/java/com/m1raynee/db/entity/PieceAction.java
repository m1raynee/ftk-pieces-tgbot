package com.m1raynee.db.entity;

import java.time.LocalDateTime;

import com.m1raynee.db.converters.ActionStateConverter;
import com.m1raynee.db.enums.ActionState;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor

public class PieceAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "piece_id", nullable = false)
    private Piece piece;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_student_id", nullable = false)
    private Student requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performer_student_id", nullable = false)
    private Student performer;

    @Column(nullable = false)
    private Integer amount;

    @Column(name = "action_state", nullable = false, length = 1)
    @Convert(converter = ActionStateConverter.class)
    private ActionState actionState = ActionState.TAKEN;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "edited_at", nullable = false)
    private LocalDateTime editedAt;
    @Column(name = "taken_until")
    private LocalDateTime takenUntil;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        editedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        editedAt = LocalDateTime.now();
    }

    public PieceAction(Piece piece, Student requester, Student performer, Integer amount, ActionState actionState) {
        this.piece = piece;
        this.requester = requester;
        this.performer = performer;
        this.amount = amount;
        this.actionState = actionState;
    }

    public String getTagId() {
        return "(ACT-%d)".formatted(id);
    }

}
