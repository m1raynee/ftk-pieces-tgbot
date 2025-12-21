package com.m1raynee.telegram.utils;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Pageable {
    public int page;
    public int size;

    public int startI() {
        return page * size;
    }

    public static Pageable fromCode(String code) {
        var c = code.split("-");
        return new Pageable(Integer.valueOf(c[0]), Integer.valueOf(c[1]));
    }
}
