document.addEventListener('DOMContentLoaded', () => {
    const toggleChartBtn = document.getElementById('toggleChartBtn');
    const chartColumn = document.getElementById('combinedChartColumn');
    const tableColumn = document.getElementById('tableColumn');
    const chartTimeFrameSelector = document.getElementById('chartSelect');
    if (!toggleChartBtn || !chartColumn || !tableColumn || !chartTimeFrameSelector) {
        console.warn('Toggle chart functionality disabled: One or more required elements not found.');
        return;
    }
    toggleChartBtn.addEventListener('click', () => {
        const isChartAreaHidden = chartColumn.classList.contains('d-none');

        if (isChartAreaHidden) {
            chartColumn.classList.remove('d-none');
            chartColumn.classList.add('col-md-6');
            tableColumn.classList.remove('col-md-12');
            tableColumn.classList.add('col-md-6');
            toggleChartBtn.textContent = 'Hide Chart';
            chartTimeFrameSelector.classList.remove('d-none');
        } else {
            chartColumn.classList.add('d-none');
            tableColumn.classList.remove('col-md-6');
            tableColumn.classList.add('col-md-12');
            toggleChartBtn.textContent = 'Show Chart';
            chartTimeFrameSelector.classList.add('d-none');
        }
    });
});