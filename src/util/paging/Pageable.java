package util.paging;

public class Pageable {

    private final int pageNumber;
    private final int pageSize;


    /**
     * Constructs a page request.
     *
     * @param pageNumber zero-based index of the requested page, must be >= 0
     * @param pageSize   maximum number of elements on a page, must be > 0
     * @throws IllegalArgumentException if pageNumber is negative or pageSize is not positive
     */
    public Pageable(int pageNumber, int pageSize) {
        if (pageNumber < 0 || pageSize <= 0) {
            throw new IllegalArgumentException("Page number must be >= 0 and page size must be > 0");
        }
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    /**
     * Retrieves the maximum number of elements on a page.
     *
     * @return the page size
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Retrieves the zero-based index of the requested page.
     *
     * @return the page number
     */
    public int getPageNumber() {
        return pageNumber;
    }
}
