package util.paging;

import java.util.List;

/**
 * Result of a paged query: the elements found on the requested page
 * together with the total number of elements available across all pages.
 *
 * @param <E> the type of the elements on the page
 */
public class Page<E> {
    private final List<E> elementsOnPage;
    private final int totalNumberOfElements;

    /**
     * Constructs a page.
     *
     * @param elementsOnPage        the elements found on the requested page (empty if the page lies after the last one)
     * @param totalNumberOfElements the total number of elements across all pages
     */
    public Page(List<E> elementsOnPage, int totalNumberOfElements) {
        this.elementsOnPage = elementsOnPage;
        this.totalNumberOfElements = totalNumberOfElements;
    }

    /**
     * Retrieves the total number of elements across all pages.
     *
     * @return the total element count
     */
    public int getTotalNumberOfElements() {
        return totalNumberOfElements;
    }

    /**
     * Retrieves the elements found on this page.
     *
     * @return the list of elements on the page
     */
    public List<E> getElementsOnPage() {
        return elementsOnPage;
    }
}