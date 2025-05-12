// Wait for the HTML document to be fully loaded
document.addEventListener('DOMContentLoaded', () => {
    // Find the container for sort data
    const sortDataElement = document.getElementById('sortData');
    if (!sortDataElement) {
        console.error('sortData element not found');
        return;
    }

    // Get all elements with the class 'sort-link'
    const sortLinks = document.querySelectorAll('.sort-link');

    // Add a click event listener to each sort link
    sortLinks.forEach(link => {
        link.addEventListener('click', (event) => {
            event.preventDefault(); // Prevent the default link behavior (#)

            // Get the column name from the link's data attribute
            const columnToSort = link.dataset.column;
            if (!columnToSort) {
                console.error('data-column attribute not found on the link');
                return;
            }
            // Get the base path for the URL - ADDED THIS LINE
            const basePath = sortDataElement.dataset.basePath;
            if (!basePath) {
                console.error('data-base-path attribute not found on sortData element');
                return; // Stop if the base path is missing
            }

            // Get current sorting and pagination info from the sortData div
            const pageNumber = sortDataElement.dataset.pageNumber;
            const pageSize = sortDataElement.dataset.pageSize;
            const keyword = sortDataElement.dataset.keyword || ''; // Handle potential undefined keyword
            // These are CRITICAL for toggling sort direction:
            const currentSortField = sortDataElement.dataset.sortField;
            const currentSortDirection = sortDataElement.dataset.sortDirection;

            let nextSortDirection;
            if (columnToSort === currentSortField) {
                nextSortDirection = currentSortDirection === 'asc' ? 'desc' : 'asc';
            } else {
                nextSortDirection = 'asc';
            }

            // Construct the URL using the dynamic base path
            const url = `${basePath}?pageNumber=${pageNumber}&pageSize=${pageSize}&sortField=${columnToSort}&sortDirection=${nextSortDirection}&keyword=${keyword}`;

            // Navigate to the new URL
            window.location.href = url;
        });
    });
});