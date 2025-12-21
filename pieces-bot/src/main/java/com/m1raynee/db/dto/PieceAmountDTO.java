package com.m1raynee.db.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PieceAmountDTO {
    final Long pieceId;
    final Long positiveAmount;
    final Long negativeAmount;
}
