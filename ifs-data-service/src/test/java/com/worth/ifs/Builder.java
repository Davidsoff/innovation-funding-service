package com.worth.ifs;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by dwatson on 07/10/15.
 */
public interface Builder<T, S> {

    S with(Consumer<T> amendFunction);

    S with(BiConsumer<Integer, T> amendFunction);

    T build();

    List<T> build(int numberToBuild);

    T[] buildArray(int numberToBuild, Class<T> clazz);
}
