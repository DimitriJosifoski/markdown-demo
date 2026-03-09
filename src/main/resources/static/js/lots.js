const lotSearchForm = document.getElementById("lotSearchForm");
const lotIdInput = document.getElementById("lotId");
const startDateInput = document.getElementById("startDate");
const endDateInput = document.getElementById("endDate");

const searchStatus = document.getElementById("searchStatus");
const searchTableBody = document.getElementById("searchTableBody");
const searchEmpty = document.getElementById("searchEmpty");

const orphanedTableBody = document.getElementById("orphanedTableBody");
const conflictsTableBody = document.getElementById("conflictsTableBody");
const orphanedEmpty = document.getElementById("orphanedEmpty");
const conflictsEmpty = document.getElementById("conflictsEmpty");

const consolidatedHint = document.getElementById("consolidatedHint");
const consolidatedContent = document.getElementById("consolidatedContent");
const viewLot = document.getElementById("viewLot");
const viewPart = document.getElementById("viewPart");
const viewCreated = document.getElementById("viewCreated");
const viewLines = document.getElementById("viewLines");
const viewUnits = document.getElementById("viewUnits");
const viewDowntime = document.getElementById("viewDowntime");
const viewDefects = document.getElementById("viewDefects");
const viewShipping = document.getElementById("viewShipping");
const viewSources = document.getElementById("viewSources");

const refreshOrphanedButton = document.getElementById("refreshOrphaned");
const refreshConflictsButton = document.getElementById("refreshConflicts");

function setSearchStatus(message) {
    searchStatus.textContent = message;
}

function setEmptyVisibility(element, isEmpty) {
    element.classList.toggle("hidden", !isEmpty);
}

function asYesNo(value) {
    return value ? "Yes" : "No";
}

function shippingChip(status) {
    if (status === "SHIPPED") {
        return `<span class="chip ok">${status}</span>`;
    }
    return `<span class="chip warn">${status ?? "UNKNOWN"}</span>`;
}

function conflictChip(hasConflict) {
    if (hasConflict) {
        return `<span class="chip bad">Conflict</span>`;
    }
    return `<span class="chip ok">Clean</span>`;
}

function buildSearchParams() {
    const params = new URLSearchParams();

    if (lotIdInput.value.trim().length > 0) {
        params.append("lotId", lotIdInput.value.trim());
    }
    if (startDateInput.value) {
        params.append("startDate", startDateInput.value);
    }
    if (endDateInput.value) {
        params.append("endDate", endDateInput.value);
    }

    return params;
}

async function searchLots(event) {
    event.preventDefault();
    setSearchStatus("Searching...");
    searchTableBody.innerHTML = "";
    setEmptyVisibility(searchEmpty, false);

    const params = buildSearchParams();
    const searchPath = params.toString().length > 0 ? `/api/lots/search?${params}` : "/api/lots/search";

    try {
        const response = await fetch(searchPath);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        const results = await response.json();
        renderSearchResults(results);
        setSearchStatus(`Found ${results.length} lot(s)`);
    } catch (error) {
        setSearchStatus(`Search failed: ${error.message}`);
        setEmptyVisibility(searchEmpty, true);
    }
}

function renderSearchResults(results) {
    searchTableBody.innerHTML = "";
    setEmptyVisibility(searchEmpty, results.length === 0);

    results.forEach((result) => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td>${result.lotIdentifier ?? "-"}</td>
            <td>${result.partNumber ?? "-"}</td>
            <td>${result.productionLineName ?? "-"}</td>
            <td>${shippingChip(result.shippingStatus)}</td>
            <td>${result.defectName ?? "-"}</td>
            <td>${conflictChip(result.hasDataConflict)}</td>
            <td><button type="button" data-lot-id="${result.lotId}">Open</button></td>
        `;

        const button = tr.querySelector("button");
        button.addEventListener("click", () => loadConsolidatedView(result.lotId));
        searchTableBody.appendChild(tr);
    });
}

async function loadConsolidatedView(lotId) {
    consolidatedHint.classList.add("hidden");
    consolidatedContent.classList.add("hidden");
    consolidatedHint.textContent = "Loading consolidated details...";
    consolidatedHint.classList.remove("hidden");

    try {
        const response = await fetch(`/api/lots/${lotId}/consolidated`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const data = await response.json();
        viewLot.textContent = data.lotIdentifier ?? "-";
        viewPart.textContent = data.partNumber ?? "-";
        viewCreated.textContent = data.createdDate ?? "-";
        viewLines.textContent = (data.associatedProductionLines ?? []).join(", ") || "-";
        viewUnits.textContent = `${data.totalUnitsPlanned ?? 0} / ${data.totalUnitsActual ?? 0}`;
        viewDowntime.textContent = `${data.totalDowntimeMinutes ?? 0} min`;
        viewDefects.textContent = (data.defectsFound ?? []).join(", ") || "None";
        viewShipping.textContent = data.shippingStatus ?? "Unknown";
        viewSources.textContent = [
            data.productionSourceFile,
            data.qualitySourceFile,
            data.shippingSourceFile
        ].filter(Boolean).join(" | ");

        consolidatedHint.classList.add("hidden");
        consolidatedContent.classList.remove("hidden");
    } catch (error) {
        consolidatedHint.textContent = `Could not load details: ${error.message}`;
    }
}

async function loadOrphaned() {
    orphanedTableBody.innerHTML = "";
    setEmptyVisibility(orphanedEmpty, false);

    try {
        const response = await fetch("/api/lots/orphaned");
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        const rows = await response.json();
        setEmptyVisibility(orphanedEmpty, rows.length === 0);

        rows.forEach((row) => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${row.lotIdentifier ?? "-"}</td>
                <td>${asYesNo(row.inProduction)}</td>
                <td>${asYesNo(row.inShipping)}</td>
                <td>${asYesNo(row.inQuality)}</td>
                <td>${row.reason ?? "-"}</td>
            `;
            orphanedTableBody.appendChild(tr);
        });
    } catch (error) {
        orphanedEmpty.classList.remove("hidden");
        orphanedEmpty.textContent = `Could not load orphaned records: ${error.message}`;
    }
}

async function loadConflicts() {
    conflictsTableBody.innerHTML = "";
    setEmptyVisibility(conflictsEmpty, false);

    try {
        const response = await fetch("/api/lots/conflicts");
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        const rows = await response.json();
        setEmptyVisibility(conflictsEmpty, rows.length === 0);

        rows.forEach((row) => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${row.lotIdentifier ?? "-"}</td>
                <td>${(row.conflictingProductionLines ?? []).join(", ") || "-"}</td>
                <td>${row.description ?? "-"}</td>
            `;
            conflictsTableBody.appendChild(tr);
        });
    } catch (error) {
        conflictsEmpty.classList.remove("hidden");
        conflictsEmpty.textContent = `Could not load conflict data: ${error.message}`;
    }
}

lotSearchForm.addEventListener("submit", searchLots);
refreshOrphanedButton.addEventListener("click", loadOrphaned);
refreshConflictsButton.addEventListener("click", loadConflicts);

loadOrphaned();
loadConflicts();
