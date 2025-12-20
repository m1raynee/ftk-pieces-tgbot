package com.m1raynee.db.dto;

import lombok.Getter;

@Getter
public class PieceAmountDTO {
    private Long pieceId;
    private Long currentAmount;

    public PieceAmountDTO(Long pieceId, Long currentAmount) {
        this.pieceId = pieceId;
        this.currentAmount = currentAmount;
    }

}