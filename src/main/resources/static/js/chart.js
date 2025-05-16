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

        const chartJsDatasets = (serverData.datasets || []).map(ds => ({
            label: ds.label || 'Unknown Series',
            data: ds.data || [0],
            fill: ds.fill !== undefined ? ds.fill : false,
            borderColor: ds.borderColor || getRandomColor(),
            tension: ds.tension !== undefined ? ds.tension : 0.1,
            borderWidth: ds.borderWidth !== undefined ? ds.borderWidth : 2
        }));

        function getRandomColor() {
            const r = Math.floor(Math.random() * 200);
            const g = Math.floor(Math.random() * 200);
            const b = Math.floor(Math.random() * 200);
            return `rgb(${r},${g},${b})`;
        }

        const dataConfig = {
            labels: labels,
            datasets: chartJsDatasets
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
                        text: serverData.chartTitle || 'Sales'
                    }
                }
            }
        };

        if (salesChartInstance) {
            salesChartInstance.destroy();
        }
        salesChartInstance = new Chart(ctx, config);
    }

    const timeframeSelector = document.getElementById('chartTimeFrameSelector');
    const chartContainer = document.getElementById('chartColumn');

    async function loadChart() {
        if (!timeframeSelector || !chartContainer || chartContainer.classList.contains('d-none')) {
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

    if (timeframeSelector) {
        timeframeSelector.addEventListener('change', loadChart);
    }

    if (chartContainer) {
        const chartVisibilityObserver = new MutationObserver((mutationsList) => {
            for (let mutation of mutationsList) {
                if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
                    const isHidden = chartContainer.classList.contains('d-none');
                    if (!isHidden) {
                        console.log('Chart container became visible via class change.');
                        loadChart();
                    } else {
                        if (salesChartInstance) {
                            salesChartInstance.destroy();
                            salesChartInstance = null;
                            console.log('Chart container hidden via class change, chart instance destroyed.');
                        }
                    }
                    return;
                }
            }
        });
        chartVisibilityObserver.observe(chartContainer, { attributes: true });

        if (!chartContainer.classList.contains('d-none') && timeframeSelector) {
            console.log('Chart container initially visible, attempting to load chart.');
            loadChart();
        }
    }
});