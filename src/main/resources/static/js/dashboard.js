const timeGroupingSelect = document.getElementById("timeGrouping");
const refreshDashboardButton = document.getElementById("refreshDashboard");
const dashboardStatus = document.getElementById("dashboardStatus");

const rankingBody = document.getElementById("rankingTableBody");
const alertsBody = document.getElementById("alertsTableBody");
const trendList = document.getElementById("trendList");

const rankingEmpty = document.getElementById("rankingEmpty");
const alertsEmpty = document.getElementById("alertsEmpty");
const trendsEmpty = document.getElementById("trendsEmpty");

const kpiLines = document.getElementById("kpiLines");
const kpiAlerts = document.getElementById("kpiAlerts");
const kpiTrends = document.getElementById("kpiTrends");

function setStatus(message) {
    dashboardStatus.textContent = message;
}

function setEmptyVisibility(element, isEmpty) {
    element.classList.toggle("hidden", !isEmpty);
}

function renderRankings(rankings) {
    rankingBody.innerHTML = "";
    setEmptyVisibility(rankingEmpty, rankings.length === 0);

    rankings.forEach((row) => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td>${row.rank ?? "-"}</td>
            <td>${row.lineName ?? "-"}</td>
            <td>${row.totalDefects ?? 0}</td>
        `;
        rankingBody.appendChild(tr);
    });
}

function renderAlerts(alerts) {
    alertsBody.innerHTML = "";
    setEmptyVisibility(alertsEmpty, alerts.length === 0);

    alerts.forEach((row) => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td>${row.lotIdentifier ?? "-"}</td>
            <td>${row.defectName ?? "-"}</td>
            <td>${row.defectSeverity ?? "-"}</td>
            <td>${row.shipDate ?? "-"}</td>
            <td>${row.customerName ?? "-"}</td>
        `;
        alertsBody.appendChild(tr);
    });
}

function trendChipClass(direction) {
    if (direction === "INCREASING") {
        return "increasing";
    }
    if (direction === "DECREASING") {
        return "decreasing";
    }
    return "stable";
}

function renderTrends(trends) {
    trendList.innerHTML = "";
    setEmptyVisibility(trendsEmpty, trends.length === 0);

    trends.forEach((trend) => {
        const item = document.createElement("li");
        item.className = "trend-item";

        const direction = trend.trendDirection ?? "STABLE";
        item.innerHTML = `
            <span>${trend.defectName ?? "-"}</span>
            <span>${trend.currentPeriodCount ?? 0} vs ${trend.previousPeriodCount ?? 0}</span>
            <span class="trend-chip ${trendChipClass(direction)}">${direction}</span>
        `;
        trendList.appendChild(item);
    });
}

async function loadDashboard() {
    const grouping = timeGroupingSelect.value;
    setStatus("Loading...");

    try {
        const response = await fetch(`/api/dashboard/summary?timeGrouping=${grouping}`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const data = await response.json();
        const rankings = data.productionLineRankings ?? [];
        const alerts = data.shippingRiskAlerts ?? [];
        const trends = data.defectTrends ?? [];

        renderRankings(rankings);
        renderAlerts(alerts);
        renderTrends(trends);

        kpiLines.textContent = String(rankings.length);
        kpiAlerts.textContent = String(alerts.length);
        kpiTrends.textContent = String(trends.length);
        setStatus(`Loaded ${grouping.toLowerCase()} view`);
    } catch (error) {
        rankingBody.innerHTML = "";
        alertsBody.innerHTML = "";
        trendList.innerHTML = "";
        setEmptyVisibility(rankingEmpty, true);
        setEmptyVisibility(alertsEmpty, true);
        setEmptyVisibility(trendsEmpty, true);
        kpiLines.textContent = "--";
        kpiAlerts.textContent = "--";
        kpiTrends.textContent = "--";
        setStatus(`Error: ${error.message}`);
    }
}

refreshDashboardButton.addEventListener("click", loadDashboard);
timeGroupingSelect.addEventListener("change", loadDashboard);

loadDashboard();
