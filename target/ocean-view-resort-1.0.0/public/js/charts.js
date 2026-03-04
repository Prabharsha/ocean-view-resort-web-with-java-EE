/**
 * Ocean View Resort — Chart.js Initialization
 * Uses Chart.js loaded from CDN
 */

/**
 * Initialize the room occupancy bar chart on the dashboard
 * @param {Array} data - Array of {type, total, occupied, available, occupancyPct}
 */
function initOccupancyChart(data) {
    var canvas = document.getElementById('occupancyChart');
    if (!canvas || !data || data.length === 0) return;

    var labels = data.map(function(d) { return d.type; });
    var occupied = data.map(function(d) { return d.occupied; });
    var available = data.map(function(d) { return d.available; });

    new Chart(canvas.getContext('2d'), {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Occupied',
                    data: occupied,
                    backgroundColor: 'rgba(26, 60, 94, 0.85)',
                    borderColor: '#1a3c5e',
                    borderWidth: 1,
                    borderRadius: 4
                },
                {
                    label: 'Available',
                    data: available,
                    backgroundColor: 'rgba(45, 155, 94, 0.65)',
                    borderColor: '#2d9b5e',
                    borderWidth: 1,
                    borderRadius: 4
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'top',
                    labels: {
                        font: { size: 12, family: "'Segoe UI', system-ui, sans-serif" },
                        usePointStyle: true,
                        padding: 16
                    }
                },
                title: { display: false }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 1,
                        font: { size: 12 }
                    },
                    grid: { color: 'rgba(0,0,0,0.05)' }
                },
                x: {
                    ticks: { font: { size: 12 } },
                    grid: { display: false }
                }
            }
        }
    });
}

/**
 * Initialize the weekly revenue line chart
 * @param {Array} data - Array of {day, reservations, revenue, roomType, roomsOccupied}
 */
function initWeeklyChart(data) {
    var canvas = document.getElementById('weeklyChart');
    if (!canvas || !data || data.length === 0) return;

    // Aggregate revenue per day (may have multiple room types per day)
    var dayMap = {};
    data.forEach(function(d) {
        var day = d.day;
        if (!dayMap[day]) {
            dayMap[day] = { revenue: 0, reservations: 0 };
        }
        dayMap[day].revenue += parseFloat(d.revenue) || 0;
        dayMap[day].reservations += parseInt(d.reservations) || 0;
    });

    var labels = Object.keys(dayMap).sort();
    var revenues = labels.map(function(d) { return dayMap[d].revenue; });
    var reservations = labels.map(function(d) { return dayMap[d].reservations; });

    new Chart(canvas.getContext('2d'), {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Revenue (LKR)',
                    data: revenues,
                    borderColor: '#1a3c5e',
                    backgroundColor: 'rgba(26, 60, 94, 0.1)',
                    fill: true,
                    tension: 0.3,
                    pointBackgroundColor: '#1a3c5e',
                    pointRadius: 5,
                    pointHoverRadius: 7,
                    yAxisID: 'y'
                },
                {
                    label: 'Reservations',
                    data: reservations,
                    borderColor: '#f6ae2d',
                    backgroundColor: 'rgba(246, 174, 45, 0.1)',
                    fill: false,
                    tension: 0.3,
                    pointBackgroundColor: '#f6ae2d',
                    pointRadius: 5,
                    pointHoverRadius: 7,
                    yAxisID: 'y1'
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            interaction: {
                mode: 'index',
                intersect: false
            },
            plugins: {
                legend: {
                    position: 'top',
                    labels: {
                        font: { size: 12, family: "'Segoe UI', system-ui, sans-serif" },
                        usePointStyle: true,
                        padding: 16
                    }
                }
            },
            scales: {
                y: {
                    type: 'linear',
                    display: true,
                    position: 'left',
                    beginAtZero: true,
                    title: { display: true, text: 'Revenue (LKR)', font: { size: 12 } },
                    grid: { color: 'rgba(0,0,0,0.05)' },
                    ticks: { font: { size: 11 } }
                },
                y1: {
                    type: 'linear',
                    display: true,
                    position: 'right',
                    beginAtZero: true,
                    title: { display: true, text: 'Reservations', font: { size: 12 } },
                    grid: { drawOnChartArea: false },
                    ticks: { stepSize: 1, font: { size: 11 } }
                },
                x: {
                    ticks: { font: { size: 11 } },
                    grid: { display: false }
                }
            }
        }
    });
}

