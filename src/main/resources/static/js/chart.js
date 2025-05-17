// In chart.js
document.addEventListener('DOMContentLoaded', async function () {
    // --- Setup for First Chart (Sales Count) ---
    const salesChartCanvas = document.getElementById('salesChart');
    let salesCtx;
    if (salesChartCanvas) {
        salesCtx = salesChartCanvas.getContext('2d');
    } else {
        console.error('Sales chart canvas element not found!');
    }

    // --- Setup for Second Chart (Revenue) ---
    const revenueChartCanvas = document.getElementById('revenueChartCanvas');
    let revenueCtx;
    if (revenueChartCanvas) {
        revenueCtx = revenueChartCanvas.getContext('2d');
    } else {
        console.error('Revenue chart canvas element not found!');
    }

    let chartInstances = { // Store chart instances
        sales: null,
        revenue: null
    };

    // --- Generic Fetch Function (fetchDataForChart - remains the same) ---
    async function fetchDataForChart(endpoint, timeframe = 'PAST_WEEK') {
        try {
            const response = await fetch(`${endpoint}?timeframe=${timeframe}`);
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status} for ${endpoint}`);
            }
            const chartDataFromServer = await response.json();
            console.log(`Workspaceed data for ${endpoint} with timeframe ${timeframe}:`, chartDataFromServer);
            return chartDataFromServer;
        } catch (error) {
            console.error(`Error fetching data for ${endpoint}:`, error);
            return {chartLabels: ['Error'], datasets: [{label: 'Error', data: [0]}], chartTitle: 'Error Loading Data'};
        }
    }

    // --- Generic Create/Update Chart Function (createOrUpdateGenericChart - remains the same) ---
    function createOrUpdateGenericChart(ctx, chartInstance, serverData, defaultTitle, yAxisLabel, yTickFormatter, tooltipLabelFormatter) {
        if (!ctx) return null;
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
                            text: yAxisLabel
                        },
                        ticks: {
                            callback: yTickFormatter || function(value) {
                                return value;
                            }
                        }
                    },
                    x: {
                        title: {
                            display: true,
                            text: 'Time Period'
                        }
                    }
                },
                plugins: {
                    legend: {
                        position: 'top'
                    },
                    title: {
                        display: true,
                        text: serverData.chartTitle || defaultTitle
                    },
                    tooltip: {
                        callbacks: {
                            label: tooltipLabelFormatter || function(tooltipItem) {
                                let label = tooltipItem.dataset.label || '';
                                if (label) {
                                    label += ': ';
                                }
                                if (tooltipItem.parsed.y !== null) {
                                    label += tooltipItem.parsed.y;
                                }
                                return label;
                            }
                        }
                    }
                }
            }
        };
        if (chartInstance) {
            chartInstance.destroy();
        } // Destroy previous instance by direct reference
        return new Chart(ctx, config); // Return new instance
    }

    // --- Load Functions for Each Chart ---
    const timeframeSelector = document.getElementById('chartTimeFrameSelector'); // Ensure timeframeSelector is defined here

    async function loadSalesCountChart() {
        const combinedContainer = document.getElementById('combinedChartColumn');
        if (!salesCtx || !timeframeSelector || !combinedContainer || combinedContainer.classList.contains('d-none')) {
            if (chartInstances.sales) {
                chartInstances.sales.destroy();
                chartInstances.sales = null;
            }
            return;
        }
        const selectedTimeframe = timeframeSelector.value;
        const salesData = await fetchDataForChart('/sales/saleChart', selectedTimeframe);
        if (salesData) {
            chartInstances.sales = createOrUpdateGenericChart(salesCtx, chartInstances.sales, salesData, 'Sales Over Time', 'Number of Sales');
        }
    }

    async function loadRevenueChart() {
        const combinedContainer = document.getElementById('combinedChartColumn');
        if (!revenueCtx || !timeframeSelector || !combinedContainer || combinedContainer.classList.contains('d-none')) {
            if (chartInstances.revenue) {
                chartInstances.revenue.destroy();
                chartInstances.revenue = null;
            }
            return;
        }
        const selectedTimeframe = timeframeSelector.value;
        const revenueData = await fetchDataForChart('/sales/revenueChart', selectedTimeframe);
        if (revenueData) {
            const yTickCurrencyFormatter = function(value) {
                if (typeof value === 'number') {
                    return '$' + value.toFixed(2);
                }
                return value;
            };

            // Currency formatter for tooltip labels
            const tooltipCurrencyFormatter = function(tooltipItem) {
                let label = tooltipItem.dataset.label || '';
                if (label) {
                    label += ': ';
                }
                if (tooltipItem.parsed.y !== null && typeof tooltipItem.parsed.y === 'number') {
                    label += '$' + tooltipItem.parsed.y.toFixed(2);
                } else if (tooltipItem.parsed.y !== null) {
                    label += tooltipItem.parsed.y;
                }
                return label;
            };

            chartInstances.revenue = createOrUpdateGenericChart(
                revenueCtx,
                chartInstances.revenue,
                revenueData,
                'Sales Revenue Over Time', // More descriptive title
                'Total Revenue', // The tick formatter will add the '$' symbol
                yTickCurrencyFormatter,    // Pass the Y-axis tick formatter
                tooltipCurrencyFormatter   // Pass the tooltip label formatter
            );
        }
    }

    if (timeframeSelector) {
        timeframeSelector.addEventListener('change', () => {
            const combinedContainer = document.getElementById('combinedChartColumn');
            if (combinedContainer && !combinedContainer.classList.contains('d-none')) {
                loadSalesCountChart();
                loadRevenueChart();
            }
        });
    }

    // --- Mutation Observer for Visibility of the Combined Chart Column ---
    const combinedChartContainer = document.getElementById('combinedChartColumn');
    if (combinedChartContainer) {
        const observer = new MutationObserver((mutationsList) => {
            for (let mutation of mutationsList) {
                if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
                    const isHidden = combinedChartContainer.classList.contains('d-none');
                    if (!isHidden) {
                        console.log('Combined chart column became visible.');
                        if (timeframeSelector) { // Ensure selector exists before trying to load
                            loadSalesCountChart();
                            loadRevenueChart();
                        }
                    } else {
                        console.log('Combined chart column hidden.');
                        if (chartInstances.sales) {
                            chartInstances.sales.destroy();
                            chartInstances.sales = null;
                        }
                        if (chartInstances.revenue) {
                            chartInstances.revenue.destroy();
                            chartInstances.revenue = null;
                        }
                    }
                    return;
                }
            }
        });
        observer.observe(combinedChartContainer, {attributes: true});

        if (!combinedChartContainer.classList.contains('d-none') && timeframeSelector) {
            console.log('Combined chart column initially visible.');
            loadSalesCountChart();
            loadRevenueChart();
        }
    }
});