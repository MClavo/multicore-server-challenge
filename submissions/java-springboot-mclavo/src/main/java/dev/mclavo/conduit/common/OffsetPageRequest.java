package dev.mclavo.conduit.common;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** Spring Data pagination preserving arbitrary offsets, including partial pages. */
public record OffsetPageRequest(int pageSize, long offset, Sort sort) implements Pageable {
    public static OffsetPageRequest parse(String limit, String offset) {
        long size = number(limit, 20);
        long start = number(offset, 0);
        if (size < 1 || size > 100) throw ApiException.invalid("limit must be between 1 and 100");
        return new OffsetPageRequest((int) size, start, Sort.by(Sort.Direction.DESC, "id"));
    }

    private static long number(String value, long fallback) {
        if (value == null) return fallback;
        if (!value.matches("[0-9]+")) throw ApiException.invalid("Pagination must use unsigned decimal integers");
        try { return Long.parseLong(value); }
        catch (NumberFormatException exception) { throw ApiException.invalid("Pagination value is too large"); }
    }

    @Override public int getPageNumber() { return (int) Math.min(Integer.MAX_VALUE, offset / pageSize); }
    @Override public int getPageSize() { return pageSize; }
    @Override public long getOffset() { return offset; }
    @Override public Sort getSort() { return sort; }
    @Override public Pageable next() { return new OffsetPageRequest(pageSize, Math.addExact(offset, pageSize), sort); }
    @Override public Pageable previousOrFirst() { return new OffsetPageRequest(pageSize, Math.max(0, offset - pageSize), sort); }
    @Override public Pageable first() { return new OffsetPageRequest(pageSize, 0, sort); }
    @Override public Pageable withPage(int pageNumber) {
        if (pageNumber < 0) throw new IllegalArgumentException("Negative page");
        return new OffsetPageRequest(pageSize, Math.multiplyExact((long) pageNumber, pageSize), sort);
    }
    @Override public boolean hasPrevious() { return offset > 0; }
}
