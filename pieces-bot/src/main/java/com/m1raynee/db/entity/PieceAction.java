package com.m1raynee.db.entity;

import java.time.LocalDateTime;
import com.m1raynee.db.enums.ActionState;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class PieceAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "piece_id", nullable = false)
    @NonNull
    private Piece piece;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_student_id", nullable = false)
    @NonNull
    private Student requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performer_student_id", nullable = false)
    @NonNull
    private Student performer;

    @Column(nullable = false)
    @NonNull
    private Integer amount;

    @Column(name = "action_state", nullable = false)
    @NonNull
    private ActionState actionState = ActionState.TAKEN;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "edited_at", nullable = false)
    private LocalDateTime editedAt;
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
}
