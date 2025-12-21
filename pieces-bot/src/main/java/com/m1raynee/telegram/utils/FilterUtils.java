package com.m1raynee.telegram.utils;

import java.util.function.Predicate;

import io.github.natanimn.telebof.filters.Filter;
import io.github.natanimn.telebof.filters.FilterExecutor;

public class FilterUtils {
    public static FilterExecutor filterInlineState(Predicate<String> checker) {
        return filter -> {
            var r = new ReflectedUtil<Filter>(filter);
            return checker.test(
                    r.getStorage().getName(r.getUpdate().inline_query.from.id));
        };
    }

    public static FilterExecutor filterInlineResultState(Predicate<String> checker) {
        return filter -> {
            var r = new ReflectedUtil<Filter>(filter);
            return checker.test(
                    r.getStorage().getName(r.getUpdate().chosen_inline_result.from.id));
        };
    }
}
