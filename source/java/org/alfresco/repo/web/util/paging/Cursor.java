package org.alfresco.repo.web.util.paging;


/**
 * Cursor - Allows for scrolling through a row set.
 * 
 * @author davidc
 */
public interface Cursor
{
    /**
     * Gets the page type
     * 
     * @return  page type
     */
    public String getPageType();
    
    /**
     * Gets the page size
     * 
     * @return  page size
     */
    int getPageSize();
    
    /**
     * Gets total number of pages
     * 
     * @return  total number of pages
     */
    int getTotalPages();
    
    /**
     * Gets total rows
     * 
     * @return  total rows
     */
    int getTotalRows();

    /**
     * Gets the current page number
     * 
     * @return  current page number
     */
    int getCurrentPage();

    /**
     * Gets the first page number
     * 
     * @return  first page number
     */
    int getFirstPage();

    /**
     * Gets the last page number
     * 
     * @return  last page number
     */
    int getLastPage();

    /**
     * Gets the next page number
     * 
     * @return  next page number (-1 if no more pages)
     */
    int getNextPage();

    /**
     * Gets the previous page number
     * 
     * @return  previous page number (-1 if no previous pages)
     */
    int getPrevPage();

    /**
     * Is the page within range of the result set
     * 
     * @return  true => page is within range
     */
    boolean isInRange();

    /**
     * Is there a known first page?
     * 
     * @return  true => getFirstPage() will succeed
     */
    boolean getHasFirstPage();
    
    /**
     * Is there a known last page?
     * 
     * @return  true => getLastPage() will succeed
     */
    boolean getHasLastPage();

    /**
     * Is there a known next page?
     * 
     * @return  true => getNextPage() will succeed
     */
    boolean getHasNextPage();
    
    /**
     * Is there a known prev page?
     * 
     * @return  true => getPrevPage() will succeed
     */
    boolean getHasPrevPage();
    
    /**
     * Gets the start row within result set for this page
     * 
     * @return  start row index
     */
    int getStartRow();
    
    /**
     * Gets the end row within result set for this page
     * 
     * @return  end row index
     */
    int getEndRow();

    /**
     * Gets the count of rows within result set for this page
     * 
     * @return  row count
     */
    int getRowCount();

}
