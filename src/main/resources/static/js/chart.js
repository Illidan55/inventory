// chart.js
document.addEventListener('DOMContentLoaded', async function () {
    const salesChartCanvas = document.getElementById('salesChart');
    if (!salesChartCanvas) {
        console.error('Sales chart canvas element not found!');
        return;
    }
    const ctx = salesChartCanvas.getContext('2d');
    let salesChartInstance;

    async function fetchChartData(timeframe = 'PAST_WEEK') {
        try {
            const response = await fetch('/sales/saleData?timeframe=' + timeframe);
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            const chartDataFromServer = await response.json();
            console.log('Fetched chart data:', chartDataFromServer);
            return chartDataFromServer;
        } catch (error) {
            console.error('Error fetching chart data:', error);
            return { chartLabels: ['Error'], chartSalesData: [0] };
        }
    }

    function createOrUpdateChart(serverData) {
        const labels = serverData.chartLabels || ['N/A'];
        const dataValues = serverData.chartSalesData || [0];

        const dataConfig = {
            labels: labels,
            datasets: [{
                label: 'Number of Sales',
                data: dataValues,
                fill: false,
                borderColor: 'rgb(54, 162, 235)',
                tension: 0.1
            }]
        };

        const config = {
            type: 'line',
            data: dataConfig,
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: 'Sales'
                        }
                    },
                    x: {
                        title: {
                            display: true,
                            text: 'Time'
                        }
                    }
                },
                plugins: {
                    legend: {
                        position: 'top',
                    },
                    title: {
                        display: true,
                        text: serverData.chartTitle || 'Sales Chart'
                    }
                },
                interaction: {
                    intersect: false,
                }
            }
        };

        if (salesChartInstance) {
            salesChartInstance.destroy();
        }

        salesChartInstance = new Chart(ctx, config);
    }

    // Get references to the dropdown and the chart container
    const timeframeSelector = document.getElementById('chartTimeFrameSelector');
    const chartContainer = document.getElementById('chartColumn'); // The div containing the canvas

    // Function to load/reload the chart based on the selected timeframe
    // This function will be called when the dropdown changes or when the chart becomes visible.
    async function loadChart() {
        // Only load if the timeframe selector exists and the chart container is visible
        if (!timeframeSelector || !chartContainer || chartContainer.classList.contains('d-none')) {
            // If the chart is not visible and an instance exists, destroy it to free resources
            if (salesChartInstance && chartContainer && chartContainer.classList.contains('d-none')) {
                salesChartInstance.destroy();
                salesChartInstance = null;
                console.log('Chart hidden, instance destroyed.');
            }
            return;
        }

        const selectedTimeframe = timeframeSelector.value;
        console.log(`Loading chart for timeframe: ${selectedTimeframe}`);
        const chartData = await fetchChartData(selectedTimeframe);
        if (chartData) {
            createOrUpdateChart(chartData);
        }
    }

    // Add event listener to the timeframe dropdown
    if (timeframeSelector) {
        timeframeSelector.addEventListener('change', loadChart);
    }

    // Observe the chart container for visibility changes (due to toggleChartBtn)
    // This ensures the chart loads when it's made visible.
    if (chartContainer) {
        const chartVisibilityObserver = new MutationObserver((mutationsList) => {
            for (let mutation of mutationsList) {
                if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
                    const isHidden = chartContainer.classList.contains('d-none');
                    if (!isHidden) { // Chart became visible
                        console.log('Chart container became visible via class change.');
                        loadChart(); // Load or reload the chart
                    } else { // Chart became hidden
                        if (salesChartInstance) {
                            salesChartInstance.destroy();
                            salesChartInstance = null;
                            console.log('Chart container hidden via class change, chart instance destroyed.');
                        }
                    }
                    // No need to iterate further for this set of mutations for this specific purpose
                    return;
                }
            }
        });

        chartVisibilityObserver.observe(chartContainer, { attributes: true });

        // Optional: Initial load if the chart is already visible when the page loads
        // (and not hidden by default by 'd-none' or if 'd-none' is removed by another script synchronously)
        if (!chartContainer.classList.contains('d-none') && timeframeSelector) {
            console.log('Chart container initially visible, attempting to load chart.');
            loadChart();
        }
    }
});