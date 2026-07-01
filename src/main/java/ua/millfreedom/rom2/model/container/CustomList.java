package ua.millfreedom.rom2.model.container;

import lombok.Getter;
import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static ua.millfreedom.rom2.Utils.join;

// CustomList is the default container for serialized lists in this project.
// Native MFC CArray<int/long/uint/ulong> helper behavior from 004A0A10-004A0F40 and generic pointer-slot CArray<>
// helpers @004758A0, @00475920, @00475B00, @00475B60, @00475D80, @00475DC0, @00475DF0, and @00476090 are represented
// by this collection plus CArchive.serialize(list, skip, countSize, cls). Java owns storage through ArrayList, so
// native allocation, construction, copy, and destruction helpers collapse into normal List operations.
// It delegates element IO to CArchive.serialize(list, skip, countSize, cls), which already supports:
// - String elements
// - Number/primitive-wrapper elements
// - MfcSerializable elements (serialized directly via their serialize(CArchive) method)
// `skip` allows MFC-style 1-based (skip-based in general) arrays where elements up to elements[skip] are not serialized.
public class CustomList<T> implements MfcSerializable, List<T> {
    final List<T> list = new ArrayList<>();
    @Getter
    final int skip;
    @Getter
    final int countSize;
    @Getter
    final Class<T> cls;


    // not ported.
    public CustomList(Class<T> cls) {
        this(cls, 0);
    }

    // not ported.
    public CustomList(Class<T> cls, int skip) {
        this(cls, skip, 4);
    }

    // not ported.
    public CustomList(Class<T> cls, int skip, int countSize) {
        this.skip = skip;
        this.countSize = countSize;
        this.cls = cls;
    }


    // not ported.
    public static  CustomList<String> OfString() {
        return new CustomList<>(String.class, 0, 0);
    }
    // not ported.
    public static <T> CustomList<T> std(Class<T> cls) {
        return new CustomList<>(cls, 0, 0);
    }

    /**
     * Native support for CArray<>::Serialize @004A0D60 and CArray<VisualElem>::Serialize @0046DB50 through CArchive
     * list serialization.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        // Delegates direct list element serialization (including MfcSerializable elements) to CArchive.
        ar.serialize(list, skip, countSize, cls);
    }

    @Override
    // not ported.
    public String toString() {
        return  join(",",list);
    }

    @Override
    // not ported.
    public int size() {
        return list.size();
    }

    @Override
    // not ported.
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    // not ported.
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    // not ported.
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @Override
    // not ported.
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    // not ported.
    public <T1> T1[] toArray(T1[] a) {
        return list.toArray(a);
    }

    @Override
    // not ported.
    public boolean add(T t) {
        return list.add(t);
    }

    @Override
    // not ported.
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    // not ported.
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    // not ported.
    public boolean addAll(Collection<? extends T> c) {
        return list.addAll(c);
    }

    @Override
    // not ported.
    public boolean addAll(int index, Collection<? extends T> c) {
        return list.addAll(index, c);
    }

    @Override
    // not ported.
    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    @Override
    // not ported.
    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    @Override
    // not ported.
    public void replaceAll(UnaryOperator<T> operator) {
        list.replaceAll(operator);
    }

    @Override
    // not ported.
    public void sort(Comparator<? super T> c) {
        list.sort(c);
    }

    @Override
    // not ported.
    public void clear() {
        list.clear();
    }

    @Override
    // not ported.
    public boolean equals(Object o) {
        return list.equals(o);
    }

    @Override
    // not ported.
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    // not ported.
    public T get(int index) {
        return list.get(index);
    }

    @Override
    // not ported.
    public T set(int index, T element) {
        return list.set(index, element);
    }

    @Override
    // not ported.
    public void add(int index, T element) {
        list.add(index, element);
    }

    @Override
    // not ported.
    public T remove(int index) {
        return list.remove(index);
    }

    @Override
    // not ported.
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    // not ported.
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    // not ported.
    public ListIterator<T> listIterator() {
        return list.listIterator();
    }

    @Override
    // not ported.
    public ListIterator<T> listIterator(int index) {
        return list.listIterator(index);
    }

    @Override
    // not ported.
    public List<T> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    @Override
    // not ported.
    public Spliterator<T> spliterator() {
        return list.spliterator();
    }

    @Override
    // not ported.
    public void addFirst(T t) {
        list.addFirst(t);
    }

    @Override
    // not ported.
    public void addLast(T t) {
        list.addLast(t);
    }

    @Override
    // not ported.
    public T getFirst() {
        return list.getFirst();
    }

    @Override
    // not ported.
    public T getLast() {
        return list.getLast();
    }

    @Override
    // not ported.
    public T removeFirst() {
        return list.removeFirst();
    }

    @Override
    // not ported.
    public T removeLast() {
        return list.removeLast();
    }

    @Override
    // not ported.
    public List<T> reversed() {
        return list.reversed();
    }

    // not ported.
    public static <E> List<E> of() {
        return List.of();
    }

    // not ported.
    public static <E> List<E> of(E e1) {
        return List.of(e1);
    }

    // not ported.
    public static <E> List<E> of(E e1, E e2) {
        return List.of(e1, e2);
    }

    // not ported.
    public static <E> List<E> of(E e1, E e2, E e3) {
        return List.of(e1, e2, e3);
    }

    // not ported.
    public static <E> List<E> of(E e1, E e2, E e3, E e4) {
        return List.of(e1, e2, e3, e4);
    }

    // not ported.
    public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5) {
        return List.of(e1, e2, e3, e4, e5);
    }

    // not ported.
    public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6) {
        return List.of(e1, e2, e3, e4, e5, e6);
    }

    // not ported.
    public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7) {
        return List.of(e1, e2, e3, e4, e5, e6, e7);
    }

    // not ported.
    public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8) {
        return List.of(e1, e2, e3, e4, e5, e6, e7, e8);
    }

    // not ported.
    public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9) {
        return List.of(e1, e2, e3, e4, e5, e6, e7, e8, e9);
    }

    // not ported.
    public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9, E e10) {
        return List.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);
    }

    @SafeVarargs
    // not ported.
    public static <E> List<E> of(E... elements) {
        return List.of(elements);
    }

    // not ported.
    public static <E> List<E> copyOf(Collection<? extends E> coll) {
        return List.copyOf(coll);
    }

    @Override
    // not ported.
    public <T1> T1[] toArray(IntFunction<T1[]> generator) {
        return list.toArray(generator);
    }

    @Override
    // not ported.
    public boolean removeIf(Predicate<? super T> filter) {
        return list.removeIf(filter);
    }

    @Override
    // not ported.
    public Stream<T> stream() {
        return list.stream();
    }

    @Override
    // not ported.
    public Stream<T> parallelStream() {
        return list.parallelStream();
    }

    @Override
    // not ported.
    public void forEach(Consumer<? super T> action) {
        list.forEach(action);
    }
}
