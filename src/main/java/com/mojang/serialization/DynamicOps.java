// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Pair;
import org.apache.commons.lang3.mutable.MutableObject;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * An adapter for a hierarchical serialization format. Clients may use this interface to
 * interact with serialization formats such as JSON and NBT without knowing the specific
 * serialization format being used.
 *
 * <p>This interface, along with the class {@link Dynamic}, is a low-level serialization
 * abstraction used in the implementation of {@link Codec}. The functionality offered by
 * {@link Codec} is more easily composed than the fixed interface offered here.
 *
 * @param <T> The type this interface serializes to and deserializes from. For example,
 *            {@link com.google.gson.JsonElement} or NbtTag.
 * @see Dynamic
 * @see Codec
 */
public interface DynamicOps<T> {
    /**
     * Returns the empty value of the serialization type. This may be the primitive value corresponding to
     * empty, null, absent, etc, or it may literally be the representation of "no value".
     *
     * <p>The returned value must be a singleton. That is, clients are guaranteed to be able to compare
     * instances to {@code empty()} using reference equality ({@code ==}).
     */
    T empty();

    /**
     * Creates a new empty map of the serialization type. The returned map is suitable for passing to
     * {@link #mergeToMap(Object, Object, Object)}.
     *
     * @implSpec The default implementation is equivalent to {@code createMap(Collections.emptyMap())}.
     * @see #createMap(Map)
     */
    default T emptyMap() {
        return createMap(ImmutableMap.of());
    }

    /**
     * Creates a new empty list of the serialization type. The returned list is suitable for passing to
     * {@link #mergeToList(Object, Object)}.
     *
     * @implSpec The default implementation is equivalent to {@code createList(Stream.empty())}.
     * @see #createList(Stream)
     */
    default T emptyList() {
        return createList(Stream.empty());
    }

    /**
     * Converts a value of the serialization type to an equivalent value of another serialization type.
     *
     * @param outOps The {@link DynamicOps} object used to work with the output serialization type.
     * @param input  The value to convert to the output serialization type.
     * @param <U>    The output serialization type.
     * @return A value of the output serialization type equivalent to the input value.
     */
    <U> U convertTo(DynamicOps<U> outOps, T input);

    /**
     * Attempts to parse or coerce a {@link Number} value from the input.
     *
     * <p>This method may perform type coercions such as string-to-number and boolean-to-number conversion.
     *
     * @param input The serialized value.
     * @return A {@link DataResult} containing the parsed {@link Number}, or else an error message.
     * @see #getNumberValue(Object, Number)
     */
    DataResult<Number> getNumberValue(T input);

    /**
     * Attempts to parse or coerce a {@link Number} value from the input, falling back to a default value
     * if no number could be parsed.
     *
     * <p>This method performs the same type coercions as {@link #getNumberValue(Object)}.
     *
     * @param input        The serialized value.
     * @param defaultValue The default value to return if a number cannot be parsed.
     * @return The number parsed from the serialized value, or else the default value.
     * @see #getNumberValue(Object)
     */
    default Number getNumberValue(final T input, final Number defaultValue) {
        return getNumberValue(input).result().orElse(defaultValue);
    }

    /**
     * Serializes a {@link Number} value to the serialized type.
     *
     * <p>There are no restrictions on the form of the serialized value, save that calling
     * {@link #getNumberValue(Object)} on the returned value should result in a value equal to {@code i}.
     */
    T createNumeric(Number i);

    /**
     * Serializes a {@code byte} value to the serialized type.
     *
     * <p>There are no restrictions on the form of the serialized value, save that calling
     * {@link #getNumberValue(Object)} on the returned value should result in a value whose {@code byte} value
     * is equal to {@code i}.
     *
     * @implSpec The default implementation is equivalent to {@code createNumeric(Byte.valueOf(value))}.
     * @see #createNumeric(Number)
     */
    default T createByte(final byte value) {
        return createNumeric(value);
    }

    /**
     * Serializes a {@code short} value to the serialized type.
     *
     * <p>There are no restrictions on the form of the serialized value, save that calling
     * {@link #getNumberValue(Object)} on the returned value should result in a value whose {@code short} value
     * is equal to {@code i}.
     *
     * @implSpec The default implementation is equivalent to {@code createNumeric(Short.valueOf(value))}.
     * @see #createNumeric(Number)
     */
    default T createShort(final short value) {
        return createNumeric(value);
    }

    /**
     * Serializes a {@code int} value to the serialized type.
     *
     * <p>There are no restrictions on the form of the serialized value, save that calling
     * {@link #getNumberValue(Object)} on the returned value should result in a value whose {@code int} value
     * is equal to {@code i}.
     *
     * @implSpec The default implementation is equivalent to {@code createNumeric(Integer.valueOf(value))}.
     * @see #createNumeric(Number)
     */
    default T createInt(final int value) {
        return createNumeric(value);
    }

    /**
     * Serializes a {@code long} value to the serialized type.
     *
     * <p>There are no restrictions on the form of the serialized value, save that calling
     * {@link #getNumberValue(Object)} on the returned value should result in a value whose {@code long} value
     * is equal to {@code i}.
     *
     * @implSpec The default implementation is equivalent to {@code createNumeric(Long.valueOf(value))}.
     * @see #createNumeric(Number)
     */
    default T createLong(final long value) {
        return createNumeric(value);
    }

    /**
     * Serializes a {@code float} value to the serialized type.
     *
     * <p>There are no restrictions on the form of the serialized value, save that calling
     * {@link #getNumberValue(Object)} on the returned value should result in a value whose {@code float} value
     * is equal to {@code i}.
     *
     * @implSpec The default implementation is equivalent to {@code createNumeric(Float.valueOf(value))}.
     * @see #createNumeric(Number)
     */
    default T createFloat(final float value) {
        return createNumeric(value);
    }

    /**
     * Serializes a {@code double} value to the serialized type.
     *
     * <p>There are no restrictions on the form of the serialized value, save that calling
     * {@link #getNumberValue(Object)} on the returned value should result in a value whose {@code double} value
     * is equal to {@code i}.
     *
     * @implSpec The default implementation is equivalent to {@code createNumeric(Double.valueOf(value))}.
     * @see #createNumeric(Number)
     */
    default T createDouble(final double value) {
        return createNumeric(value);
    }

    /**
     * Attempts to parse or coerce a {@code boolean} value from the input.
     *
     * <p>This method may perform type coercions such as number-to-boolean conversion.
     *
     * @param input The serialized value.
     * @return A {@link DataResult} containing the parsed {@code boolean}, or else an error message.
     * @implSpec The default implementation calls {@link #getNumberValue(Object)} and compares
     * the {@code byte} value of the result to {@code 0}.
     * @see #createBoolean(boolean)
     * @see #getNumberValue(Object)
     */
    default DataResult<Boolean> getBooleanValue(final T input) {
        return getNumberValue(input).map(number -> number.byteValue() != 0);
    }

    /**
     * Serializes a {@code boolean} value to the serialized type.
     *
     * <p>There are no restrictions on the form of the serialized value, save that calling
     * {@link #getBooleanValue(Object)} on the returned value should result in a value whose value
     * is equal to {@code value}.
     *
     * @implSpec The default implementation is equivalent to {@code createByte((byte) (value ? 1 : 0))}.
     * @see #getBooleanValue(Object)
     * @see #createByte(byte)
     */
    default T createBoolean(final boolean value) {
        return createByte((byte) (value ? 1 : 0));
    }

    /**
     * Attempts to parse or coerce a {@link String} value from the input.
     *
     * <p>This method may perform type coercions such as number-to-string and boolean-to-string conversion.
     *
     * @param input The serialized value.
     * @return A {@link DataResult} containing the parsed {@link String} value, or else an error message.
     * @see #createString(String)
     */
    DataResult<String> getStringValue(T input);

    /**
     * Serializes a {@link String} value to the serialized type.
     *
     * <p>There are no restrictions on the form of the serialized value, save that calling
     * {@link #getStringValue(Object)} on the returned value should result in a value whose value
     * is equal to {@code value}.
     *
     * @see #getStringValue(Object)
     */
    T createString(String value);

    /**
     * Creates a new serialized list from the given serialized list with the given value appended.
     * Only successful if first argument is a list/array or empty.
     *
     * @param list  The list that is appended to.
     * @param value The value to append to the list.
     * @return A {@link DataResult} containing the merged list, or an error message if the lefthand argument
     * was not a list.
     */
    DataResult<T> mergeToList(T list, T value);

    /**
     * Creates a new serialized list from the given serialized list with the values from the given
     * {@link List} appended. If {@code list} does not represent a list, or {@code values} could not
     * be appended, and error is returned.
     *
     * @param list   The list that is appended to.
     * @param values The {@link List} of values to append.
     * @return A {@link DataResult} containing the merged list, or else an error message.
     * @implSpec The default implementation calls {@link #mergeToList(Object, Object)} for each element
     * of {@code values}.
     */
    default DataResult<T> mergeToList(final T list, final List<T> values) {
        DataResult<T> result = DataResult.success(list);

        for (final T value : values) {
            result = result.flatMap(r -> mergeToList(r, value));
        }
        return result;
    }

    /**
     * Creates a new serialized map from the given serialized map with the given key-value mapping added.
     * Only successful if first argument is a map or empty and the key is convertible to {@link String}.
     *
     * @param map   The map to add to.
     * @param key   The key to add. The key must be convertible to {@link String}.
     * @param value The value to add.
     * @return A {@link DataResult} containing the merged map, or an error message if either the map or
     * the key are of an incorrect type.
     * @see #mergeToMap(Object, Map)
     */
    DataResult<T> mergeToMap(T map, T key, T value);

    /**
     * Creates a new serialized map from the given serialized map with the entries in the given {@link Map}
     * added. Only successful if the first argument is a map.
     *
     * @param map    The map to add to.
     * @param values A {@link Map} containing the values to add.
     * @return A {@link DataResult} containing the merged map, or an error message if the serialized map
     * is of an incorrect type.
     * @implSpec The default implementation converts the given map to a {@link MapLike}
     * and calls {@link #mergeToMap(Object, MapLike)}.
     * @see #mergeToMap(Object, MapLike)
     */
    default DataResult<T> mergeToMap(final T map, final Map<T, T> values) {
        return mergeToMap(map, MapLike.forMap(values, this));
    }

    /**
     * Creates a new serialized map from the given serialized map with the entries in the given {@link MapLike}
     * added. Only successful if the first argument is a map.
     *
     * @param map    The map to add to.
     * @param values A {@link MapLike} containing the values to add.
     * @return A {@link DataResult} containing the merged map, or an error message if the serialized map
     * is of an incorrect type.
     * @implSpec The default implementation iterates over each entry in {@code values}, and calls
     * {@link #mergeToMap(Object, Object, Object)} for each one.
     * @see #mergeToMap(Object, Object, Object)
     * @see MapLike
     */
    default DataResult<T> mergeToMap(final T map, final MapLike<T> values) {
        // TODO: AtomicReference.getPlain/setPlain in java9+
        final MutableObject<DataResult<T>> result = new MutableObject<>(DataResult.success(map));

        values.entries().forEach(entry ->
            result.setValue(result.getValue().flatMap(r -> mergeToMap(r, entry.getFirst(), entry.getSecond())))
        );
        return result.getValue();
    }

    /**
     * Creates a new serialized primitive from the given serialized primitive with the given value
     * added. In practical terms, this is only successful if the first argument is empty.
     *
     * @param prefix The existing primitive value to add to.
     * @param value  The value to add.
     * @return A {@link DataResult} containing the merged primitive, or an error message if the serialized primitive
     * is of an incorrect type.
     * @implSpec The default implementation checks {@code prefix} for equality with {@link #empty()}, and returns
     * {@code value} unchanged in this case, else an error.
     */
    default DataResult<T> mergeToPrimitive(final T prefix, final T value) {
        if (!Objects.equals(prefix, empty())) {
            return DataResult.error("Do not know how to append a primitive value " + value + " to " + prefix, value);
        }
        return DataResult.success(value);
    }

    /**
     * Extracts a {@link Stream} of map entries from the given serialized value.
     *
     * @param input The input value.
     * @return A {@link DataResult} containing the extracted map entries, or an error message if {@code input}
     * is not a map.
     */
    DataResult<Stream<Pair<T, T>>> getMapValues(T input);

    /**
     * Extracts a {@link Consumer} from the given value that iterates over the entries of the serialized map.
     * The returned value logically encapsulates a iteration over the entries in the given map,
     * performing some user-specified action on each entry.
     *
     * @param input The input value.
     * @return An iteration over the entries in the input, which may perform some user-specified action on each entry.
     * @see #getMapValues(Object)
     */
    default DataResult<Consumer<BiConsumer<T, T>>> getMapEntries(final T input) {
        return getMapValues(input).map(s -> c -> s.forEach(p -> c.accept(p.getFirst(), p.getSecond())));
    }

    /**
     * Serializes a {@link Stream} of entries to a map serialized value. The keys (first element in each entry)
     * must be convertible to {@link String}.
     *
     * @param map The stream of entries.
     * @return The serialized value.
     * @see #createMap(Map)
     */
    T createMap(Stream<Pair<T, T>> map);

    /**
     * Extracts the entries in the given value, returning them in a {@link MapLike} object.
     *
     * @param input The serialized value.
     * @return A {@link DataResult} containing the extracted entries, or an error message if the entries
     * could not be extracted.
     */
    default DataResult<MapLike<T>> getMap(final T input) {
        return getMapValues(input).flatMap(s -> {
            try {
                return DataResult.success(MapLike.forMap(s.collect(Pair.toMap()), this));
            } catch (final IllegalStateException e) {
                return DataResult.error("Error while building map: " + e.getMessage());
            }
        });
    }

    /**
     * Serializes the entries in the given {@link Map} value to the serialized type. The keys in the map must be
     * convertible to the type {@link String}.
     *
     * @param map The map containing the entries to serialize.
     * @return The serialized value.
     */
    default T createMap(final Map<T, T> map) {
        return createMap(map.entrySet().stream().map(e -> Pair.of(e.getKey(), e.getValue())));
    }

    /**
     * Extracts a {@link Stream} of list elements from the given serialized value.
     *
     * @param input The serialized value.
     * @return A {@link DataResult} containing the extracted elements, or an error message if the input
     * does not represent a list.
     * @see #getIntStream(Object)
     * @see #getLongStream(Object)
     */
    DataResult<Stream<T>> getStream(T input);

    /**
     * Extracts a {@link Consumer} from the given value that iterates over the elements of the serialized list.
     * The returned value logically encapsulates an iteration over the elements in the given list,
     * performing some user-specified action on each element.
     *
     * @param input The input value.
     * @return An iteration over the elements in the input, which may perform some user-specified action on each element.
     * @see #getStream(Object)
     */
    default DataResult<Consumer<Consumer<T>>> getList(final T input) {
        return getStream(input).map(s -> s::forEach);
    }

    /**
     * Serializes a list of values, in the form of a {@link Stream} to the serialized type.
     *
     * @param input The elements to serialize.
     * @return The serialized value.
     */
    T createList(Stream<T> input);

    /**
     * Extracts a {@link ByteBuffer} from the given serialized value.
     *
     * @param input The serialized value.
     * @return A {@link DataResult} containing the extracted buffer, or an error message if a buffer could
     * not be extracted.
     * @implSpec The default implementation parses a list from {@code value}, then converts each element of
     * that list to a {@code byte} value.
     */
    default DataResult<ByteBuffer> getByteBuffer(final T input) {
        return getStream(input).flatMap(stream -> {
            final List<T> list = stream.collect(Collectors.toList());
            if (list.stream().allMatch(element -> getNumberValue(element).result().isPresent())) {
                final ByteBuffer buffer = ByteBuffer.wrap(new byte[list.size()]);
                for (int i = 0; i < list.size(); i++) {
                    buffer.put(i, getNumberValue(list.get(i)).result().get().byteValue());
                }
                return DataResult.success(buffer);
            }
            return DataResult.error("Some elements are not bytes: " + input);
        });
    }

    /**
     * Serializes a {@link ByteBuffer} to the serialized type.
     *
     * <p>There are no restrictions on the form of the serialized value, save that calling
     * {@link #getByteBuffer(Object)} on the returned value should result in a value whose value
     * is equal to {@code input}.
     *
     * @implSpec The default implementation serializes each {@code byte} in {@code input} using
     * {@link #createByte(byte)}, then serializes the list of bytes using {@link #createList(Stream)}.
     * @see #getByteBuffer(Object)
     */
    default T createByteList(final ByteBuffer input) {
        return createList(IntStream.range(0, input.capacity()).mapToObj(i -> createByte(input.get(i))));
    }

    /**
     * Extracts an {@link IntStream} from the serialized value. This method is a specialization of
     * {@link #getStream(Object)} for elements convertible to primitive {@code int} values.
     *
     * @param input The serialized value.
     * @return An {@link DataResult} containing the extracted stream, or else an error message.
     * @implSpec The default implementation extracts a list from {@code input}, then converts each
     * element to an {@code int} value.
     * @see #getStream(Object)
     */
    default DataResult<IntStream> getIntStream(final T input) {
        return getStream(input).flatMap(stream -> {
            final List<T> list = stream.collect(Collectors.toList());
            if (list.stream().allMatch(element -> getNumberValue(element).result().isPresent())) {
                return DataResult.success(list.stream().mapToInt(element -> getNumberValue(element).result().get().intValue()));
            }
            return DataResult.error("Some elements are not ints: " + input);
        });
    }

    /**
     * Serializes a {@link IntStream} to the serialized type.
     *
     * <p>There are no restrictions on the form of the serialized value, save that calling
     * {@link #getIntStream(Object)} on the returned value should result in a value whose value
     * is equal to {@code input}.
     *
     * @implSpec The default implementation serializes each {@code int} in {@code input} using
     * {@link #createInt(int)}, then serializes the list of bytes using {@link #createList(Stream)}.
     * @see #getIntStream(Object)
     */
    default T createIntList(final IntStream input) {
        return createList(input.mapToObj(this::createInt));
    }

    /**
     * Extracts an {@link LongStream} from the serialized value. This method is a specialization of
     * {@link #getStream(Object)} for elements convertible to primitive {@code long} values.
     *
     * @param input The serialized value.
     * @return An {@link DataResult} containing the extracted stream, or else an error message.
     * @implSpec The default implementation extracts a list from {@code input}, then converts each
     * element to an {@code long} value.
     * @see #getStream(Object)
     */
    default DataResult<LongStream> getLongStream(final T input) {
        return getStream(input).flatMap(stream -> {
            final List<T> list = stream.collect(Collectors.toList());
            if (list.stream().allMatch(element -> getNumberValue(element).result().isPresent())) {
                return DataResult.success(list.stream().mapToLong(element -> getNumberValue(element).result().get().longValue()));
            }
            return DataResult.error("Some elements are not longs: " + input);
        });
    }

    /**
     * Serializes a {@link LongStream} to the serialized type.
     *
     * <p>There are no restrictions on the form of the serialized value, save that calling
     * {@link #getLongStream(Object)} on the returned value should result in a value whose value
     * is equal to {@code input}.
     *
     * @implSpec The default implementation serializes each {@code long} in {@code input} using
     * {@link #createLong(long)}, then serializes the list of bytes using {@link #createList(Stream)}.
     * @see #getLongStream(Object)
     */
    default T createLongList(final LongStream input) {
        return createList(input.mapToObj(this::createLong));
    }

    /**
     * Returns an equivalent value with the entry associated with the given key removed. The input is not modified.
     *
     * @param input The input value.
     * @param key   The key to remove.
     * @return A value equivalent to {@code input} minus the given key. If {@code input} does not contain that key,
     * or is not a map, then it is returned unchanged.
     */
    T remove(T input, String key);

    /**
     * Whether the caller should serialize maps using a compressed representation.
     *
     * @apiNote This setting is used in {@link MapEncoder} and {@link MapDecoder} to determine whether
     * to serialize maps as a list of values keyed by index.
     * @implSpec The default implementation returns {@code false}. Implementations should override the default
     * and return {@code true} if callers would gain space savings by compressing maps before serializing them.
     */
    default boolean compressMaps() {
        return false;
    }

    /**
     * Extracts the value associated with the given key from the input.
     *
     * @param input The serialized value.
     * @param key   The key to search for.
     * @return A {@link DataResult} containing the extracted value, or an error message if a value associated
     * with the given key could not be extracted.
     * @implSpec The default implementation calls {@link #getGeneric(Object, Object)} using the serialized form
     * of {@code key}.
     * @see #getGeneric(Object, Object)
     */
    default DataResult<T> get(final T input, final String key) {
        return getGeneric(input, createString(key));
    }

    /**
     * Extracts the value associated with the given key from the input.
     *
     * @param input The serialized value.
     * @param key   The key to search for.
     * @return A {@link DataResult} containing the extracted value, or an error message if a value associated
     * with the given key could not be extracted.
     * @implSpec The default implementation extracts all entries using {@link #getMap(Object)}, then searches
     * for the entry associated with the given key.
     * @see #get(Object, String)
     */
    default DataResult<T> getGeneric(final T input, final T key) {
        return getMap(input).flatMap(map -> Optional.ofNullable(map.get(key))
            .map(DataResult::success)
            .orElseGet(() -> DataResult.error("No element " + key + " in the map " + input))
        );
    }

    /**
     * Sets the entry associated with the given key in the input to the given value.
     *
     * @param input The serialized map to add the entry to.
     * @param key   The key, as a string.
     * @param value The serialized value.
     * @return A value equivalent to the input map with the given key set to the given value.
     * @apiNote Unlike many other methods in this interface, this method does not return an error {@link DataResult}
     * if the entry could not be set.
     * @implSpec The default implementation is equivalent to {@link #mergeToMap(Object, Object, Object)},
     * except that the input is returned unchanged if an error would otherwise be returned.
     * @see #mergeToMap(Object, Object, Object)
     */
    // TODO: eats error if input is not a map
    default T set(final T input, final String key, final T value) {
        return mergeToMap(input, createString(key), value).result().orElse(input);
    }

    /**
     * Sets the entry associated with the given key in the input to a value computed using the existing value
     * associated with that key. If there is no existing entry associated with the given key, the map is
     * returned unchanged.
     *
     * @param input    The seraialized map to set the entry to.
     * @param key      The key, as a string.
     * @param function A function which computes a new value using the existing value.
     * @return A value equivalent to the input map, with the new value associated with the given key.
     * @apiNote Unlike many other methods in this interface, this method does not return an error {@link DataResult}
     * if the entry could not be set.
     * @implSpec The default implementation extracts the existing value from the map using
     * {@link #get(Object, String)}, then sets a new value using {@link #set(Object, String, Object)}.
     */
    // TODO: eats error if input is not a map
    default T update(final T input, final String key, final Function<T, T> function) {
        return get(input, key).map(value -> set(input, key, function.apply(value))).result().orElse(input);
    }

    /**
     * Sets the entry associated with the given key in the input to a value computed using the existing value
     * associated with that key. If there is no existing entry associated with the given key, the map is
     * returned unchanged.
     *
     * @param input    The seraialized map to set the entry to.
     * @param key      The serialized key.
     * @param function A function which computes a new value using the existing value.
     * @return A value equivalent to the input map, with the new value associated with the given key.
     * @apiNote Unlike many other methods in this interface, this method does not return an error {@link DataResult}
     * if the entry could not be set.
     * @implSpec The default implementation extracts the existing value from the map using
     * {@link #getGeneric(Object, Object)}, then sets a new value using {@link #mergeToMap(Object, Object, Object)}.
     */
    default T updateGeneric(final T input, final T key, final Function<T, T> function) {
        return getGeneric(input, key).flatMap(value -> mergeToMap(input, key, function.apply(value))).result().orElse(input);
    }

    /**
     * Returns a new {@link ListBuilder} for creating lists of the serialized type.
     *
     * @implSpec The default implementation returns a new instance of {@link ListBuilder.Builder}.
     * @see #list(Iterable, Object, Encoder)
     */
    default ListBuilder<T> listBuilder() {
        return new ListBuilder.Builder<>(this);
    }

    /**
     * Serializes a list of objects to the serialized type using the given element {@link Encoder}.
     *
     * @param list    The list of objects to serialize.
     * @param prefix  The existing serialized list to merge with.
     * @param encoder The element encoder.
     * @param <E>     The element type.
     * @return The serialized list of objects.
     * @implSpec The default implementation adds each element of the list to the builder returned
     * by {@link #listBuilder()}.
     * @see #createList(Stream)
     */
    default <E> DataResult<T> list(final Iterable<E> list, final T prefix, final Encoder<E> encoder) {
        final ListBuilder<T> builder = listBuilder();
        builder.addAll(list, encoder);
        return builder.build(prefix);
    }

    /**
     * Serializes a list of objects to the serialized type using the given element serialization function.
     *
     * @param list              The list of objects to serialize.
     * @param prefix            The existing serialized list to merge with.
     * @param elementSerializer A function that serializes values of the element type.
     * @param <E>               The element type.
     * @return The serialized list of objects.
     * @implSpec The default implementation adds each element of the list to the builder returned
     * by {@link #listBuilder()}.
     * @see #list(Iterable, Object, Encoder)
     */
    default <E> DataResult<T> list(final Iterable<E> list, final T prefix, final Function<? super E, ? extends DataResult<T>> elementSerializer) {
        final ListBuilder<T> builder = listBuilder();
        list.forEach(element -> builder.add(elementSerializer.apply(element)));
        return builder.build(prefix);
    }

    /**
     * Returns a new {@link RecordBuilder} for creating maps of the serialized type.
     *
     * @implSpec The default implementation returns a new instance of {@link RecordBuilder.MapBuilder}.
     */
    default RecordBuilder<T> mapBuilder() {
        return new RecordBuilder.MapBuilder<>(this);
    }

    /**
     * Serializes a map of objects to the serialized type using the given key and value serialization functions.
     *
     * @param map               The map of objects to serialize.
     * @param prefix            The existing serialized list to merge with.
     * @param keySerializer     A function that serializes values of the key type.
     * @param elementSerializer A function that serializes values of the element type.
     * @param <K>               The key type.
     * @param <V>               The element type.
     * @return The serialized map of objects.
     * @implSpec The default implementation adds each entry in the map to the builder returned
     * by {@link #mapBuilder()}.
     */
    default <K, V> DataResult<T> map(final Map<K, V> map, final T prefix, final Function<? super K, ? extends DataResult<T>> keySerializer, final Function<? super V, ? extends DataResult<T>> elementSerializer) {
        final RecordBuilder<T> builder = mapBuilder();
        map.forEach((key, value) -> builder.add(keySerializer.apply(key), elementSerializer.apply(value)));
        return builder.build(prefix);
    }

    /**
     * Performs a reduction operation on the map entries extracted from the input. Specifically, this method
     * performs a left fold on the entries extracted from the input.
     *
     * @param input    The serialized value.
     * @param empty    An empty or identity element of the result type.
     * @param combiner Combines the current serialized key, value, and previous result to produce a new result.
     * @param <R>      The result type.
     * @return A {@link DataResult} containing the final result.
     * @implSpec The default implementation extracts the entries using {@link #getMapValues(Object)}, then
     * accumulates the final result value by iterating over the stream in encounter order.
     * @see Stream#reduce(Object, BiFunction, BinaryOperator)
     * @see <a href="https://en.wikipedia.org/wiki/Fold_(higher-order_function)">The fold higher order function</a>
     */
    default <R> DataResult<R> readMap(final T input, final DataResult<R> empty, final Function3<R, T, T, DataResult<R>> combiner) {
        return getMapValues(input).flatMap(stream -> {
            // TODO: AtomicReference.getPlain/setPlain in java9+
            final MutableObject<DataResult<R>> result = new MutableObject<>(empty);
            stream.forEach(p -> result.setValue(result.getValue().flatMap(r -> combiner.apply(r, p.getFirst(), p.getSecond()))));
            return result.getValue();
        });
    }

    /**
     * Returns a serialization function that uses the given {@link Encoder}, along with this implementation,
     * to encode data to the serialized type.
     *
     * @param encoder The encoder to use.
     * @param <E>     The type of values the encoder encodes.
     * @return A serialization function.
     * @see Encoder#encode(Object, DynamicOps, Object)
     */
    default <E> Function<E, DataResult<T>> withEncoder(final Encoder<E> encoder) {
        return e -> encoder.encodeStart(this, e);
    }

    /**
     * Returns a deserialization function that uses the given {@link Decoder}, along with this implementation,
     * to decode data from the serialized type.
     *
     * @param decoder The decoder to use.
     * @param <E>     The type of values the decoder decodes.
     * @return A deserialization function.
     * @see #withParser(Decoder)
     * @see Decoder#decode(DynamicOps, Object)
     */
    default <E> Function<T, DataResult<Pair<E, T>>> withDecoder(final Decoder<E> decoder) {
        return t -> decoder.decode(this, t);
    }

    /**
     * Returns a deserialization function that uses the given {@link Decoder}, along with this implementation,
     * to parse data from the serialized type.
     *
     * @param decoder The decoder to use.
     * @param <E>     The type of values the decoder parses.
     * @return A deserialization function.
     * @see Decoder#parse(DynamicOps, Object)
     */
    default <E> Function<T, DataResult<E>> withParser(final Decoder<E> decoder) {
        return t -> decoder.parse(this, t);
    }

    /**
     * Converts a serialized list of values of the serialized type to another serialized type.
     *
     * @param outOps The {@link DynamicOps} for the output type.
     * @param input  The serialized list.
     * @param <U>    The output type.
     * @return The serialized list converted to the output type.
     * @implSpec The default implementation extracts all elements using {@link #getStream(Object)}, then
     * converts each element of the input list using {@link #convertTo(DynamicOps, Object)}.
     * @see #convertTo(DynamicOps, Object)
     */
    default <U> U convertList(final DynamicOps<U> outOps, final T input) {
        return outOps.createList(getStream(input).result().orElse(Stream.empty()).map(e -> convertTo(outOps, e)));
    }

    /**
     * Converts a serialized map of entries of the serialized type to another serialized type.
     *
     * @param outOps The {@link DynamicOps} for the output type.
     * @param input  The serialized map.
     * @param <U>    The output type.
     * @return The serialized map converted to the output type.
     * @implSpec The default implementation extracts all entries using {@link #getMapValues(Object)}, then
     * converts each key and value using {@link #convertTo(DynamicOps, Object)}.
     * @see #convertTo(DynamicOps, Object)
     */
    default <U> U convertMap(final DynamicOps<U> outOps, final T input) {
        return outOps.createMap(getMapValues(input).result().orElse(Stream.empty()).map(e ->
            Pair.of(convertTo(outOps, e.getFirst()), convertTo(outOps, e.getSecond()))
        ));
    }
}
