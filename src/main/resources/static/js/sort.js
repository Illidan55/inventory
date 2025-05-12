document.addEventListener('DOMContentLoaded', () => {
    const sortDataElement = document.getElementById('sortData');
    if (!sortDataElement) {
        console.error('CRITICAL: sortData element not found in the DOM.');
        return;
    }
    const currentSortField = sortDataElement.dataset.sortField;
    const currentSortDirection = sortDataElement.dataset.sortDirection;

    const allSortButtons = document.querySelectorAll('button.sort-link');
    allSortButtons.forEach(button => {
        const column = button.dataset.column;
        const iconElement = button.querySelector('i');

        if (!iconElement) {
            return;
        }
        iconElement.className = '';
        iconElement.classList.add('fas');

        if (column === currentSortField) {
            if (currentSortDirection === 'asc') {
                iconElement.classList.add('fa-sort-up');
            } else if (currentSortDirection === 'desc') {
                iconElement.classList.add('fa-sort-down');
            } else {
                iconElement.classList.add('fa-sort');
            }
        } else {
            iconElement.classList.add('fa-sort');
        }
    });
    // --- Click Handler for Sorting ---
    allSortButtons.forEach(button => {
        button.addEventListener('click', (event) => {

            const columnToSort = button.dataset.column;
            if (!columnToSort) {
                console.error('data-column attribute not found on the clicked button.');
                return;
            }

            const basePath = sortDataElement.dataset.basePath;
            const pageNumber = sortDataElement.dataset.pageNumber;
            const pageSize = sortDataElement.dataset.pageSize;
            const keyword = sortDataElement.dataset.keyword || '';

            const activeSortField = sortDataElement.dataset.sortField;
            const activeSortDirection = sortDataElement.dataset.sortDirection;

            if (!basePath || pageNumber === undefined || pageSize === undefined || activeSortField === undefined || activeSortDirection === undefined) {
                console.error('One or more required data-* attributes for navigation are missing from #sortData.');
                return;
            }

            let nextSortDirection;
            if (columnToSort === activeSortField) {
                nextSortDirection = activeSortDirection === 'asc' ? 'desc' : 'asc';
            } else {
                nextSortDirection = 'asc';
            }

            const url = `${basePath}?pageNumber=${pageNumber}&pageSize=${pageSize}&sortField=${columnToSort}&sortDirection=${nextSortDirection}&keyword=${keyword}`;
            window.location.href = url;
        });
    });
});