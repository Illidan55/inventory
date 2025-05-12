//chart
document.addEventListener('DOMContentLoaded', function () {
    const labels = ['January', 'February', 'March', 'April', 'May'];
    const data = {
        labels: labels,
        datasets: [{
            label: 'Sales',
            data: [10, 20, 30, 40, 50], // Replace with dynamic data
            backgroundColor: 'rgba(75, 192, 192, 0.2)',
            borderColor: 'rgba(75, 192, 192, 1)',
            borderWidth: 1
        }]
    };

    const config = {
        type: 'bar',
        data: data,
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: 'top',
                },
                title: {
                    display: true,
                    text: 'Monthly Sales Data'
                }
            }
        }
    };

    const ctx = document.getElementById('salesChart').getContext('2d');
    new Chart(ctx, config);
});

