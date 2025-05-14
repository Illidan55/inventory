// chart.js
document.addEventListener('DOMContentLoaded', async function () {
    const salesChartCanvas = document.getElementById('salesChart');
    if (!salesChartCanvas) {
        console.error('Sales chart canvas element not found!');
        return;
    }
    const ctx = salesChartCanvas.getContext('2d');
    let salesChartInstance;

    async function fetchChartData() {
        try {
            const response = await fetch('/sales/saleData');
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
                label: 'Sales',
                data: dataValues,
                backgroundColor: 'rgba(75, 192, 192, 0.2)',
                borderColor: 'rgba(75, 192, 192, 1)',
                borderWidth: 1
            }]
        };

        const config = {
            type: 'bar',
            data: dataConfig,
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true
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
                }
            }
        };

        if (salesChartInstance) {
            salesChartInstance.destroy();
        }

        salesChartInstance = new Chart(ctx, config);
    }

    const dynamicChartData = await fetchChartData();
    if (dynamicChartData) {
        createOrUpdateChart(dynamicChartData);
    }
});