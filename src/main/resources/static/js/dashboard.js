/**
 * Script for Lead Qualification Dashboard page interactions, charts, filtering, search, and reports.
 */
document.addEventListener('DOMContentLoaded', async () => {
    // API Endpoints
    const DASHBOARD_URL = `${CONFIG.API_BASE_URL}/api/dashboard`;
    const LEADS_URL = `${CONFIG.API_BASE_URL}/api/leads`;

    // State Variables
    let allLeads = [];
    let filteredLeads = [];
    let deleteTargetId = null;

    // Chart instances
    let categoryChart = null;
    let volumeChart = null;
    let scoreTrendChart = null;

    // DOM Elements
    const kpiTotal = document.getElementById('kpi-total-leads');
    const kpiHot = document.getElementById('kpi-hot-leads');
    const kpiWarm = document.getElementById('kpi-warm-leads');
    const kpiCold = document.getElementById('kpi-cold-leads');
    const kpiPending = document.getElementById('kpi-pending-leads');
    const kpiFailed = document.getElementById('kpi-failed-leads');
    const kpiAvgScore = document.getElementById('kpi-avg-score');
    const kpiSuccessRate = document.getElementById('kpi-success-rate');

    const searchInput = document.getElementById('search-input');
    const filterDate = document.getElementById('filter-date');
    const filterCategory = document.getElementById('filter-category');
    const filterIndustry = document.getElementById('filter-industry');

    const tableBody = document.getElementById('recent-leads-body');
    const loadingOverlay = document.getElementById('loading-overlay');
    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toast-message');

    // Modals
    const viewModal = document.getElementById('view-modal');
    const viewModalClose = document.getElementById('view-modal-close');
    const deleteModal = document.getElementById('delete-confirm-modal');
    const deleteCancel = document.getElementById('delete-cancel');
    const deleteConfirm = document.getElementById('delete-confirm');

    // Export Buttons
    const btnExportCsv = document.getElementById('btn-export-csv');
    const btnExportPdf = document.getElementById('btn-export-pdf');

    // Initial load
    initDashboard();

    // Event listeners
    searchInput.addEventListener('input', applyFilters);
    filterDate.addEventListener('change', applyFilters);
    filterCategory.addEventListener('change', applyFilters);
    filterIndustry.addEventListener('change', applyFilters);

    // Export button actions
    btnExportCsv.addEventListener('click', exportToCSV);
    btnExportPdf.addEventListener('click', () => {
        window.print();
    });

    // Close view modal
    viewModalClose.addEventListener('click', () => {
        viewModal.classList.remove('active');
    });

    // Cancel delete modal
    deleteCancel.addEventListener('click', () => {
        deleteModal.classList.remove('active');
        deleteTargetId = null;
    });

    // Confirm delete
    deleteConfirm.addEventListener('click', executeDelete);

    // Click outside modal content to close it
    window.addEventListener('click', (e) => {
        if (e.target === viewModal) viewModal.classList.remove('active');
        if (e.target === deleteModal) {
            deleteModal.classList.remove('active');
            deleteTargetId = null;
        }
    });

    /**
     * Orchestrates dashboard setup by fetching and rendering everything.
     */
    async function initDashboard() {
        showLoader('Loading Dashboard Analytics...');
        try {
            // 1. Fetch KPI metrics summary
            await fetchSummary();

            // 2. Fetch charts data & build them
            await buildCharts();

            // 3. Fetch all leads for table, filters, and dynamic fields
            await fetchLeadsList();

            console.log('Dashboard initialization completed successfully.');
        } catch (error) {
            console.error('Error during dashboard load:', error);
            showToast('Unable to fetch some dashboard resources. Make sure the backend is running.', 'error');
        } finally {
            hideLoader();
        }
    }

    /**
     * Fetch KPI metrics card data.
     */
    async function fetchSummary() {
        try {
            const res = await fetch(`${DASHBOARD_URL}/summary`);
            if (!res.ok) throw new Error('KPI fetch failed');
            const summary = await res.json();

            kpiTotal.textContent = summary.totalLeads;
            kpiHot.textContent = summary.hotLeads;
            kpiWarm.textContent = summary.warmLeads;
            kpiCold.textContent = summary.coldLeads;
            kpiPending.textContent = summary.pendingAnalysis;
            kpiFailed.textContent = summary.failedAnalysis;
            kpiAvgScore.textContent = summary.averageLeadScore;
            kpiSuccessRate.textContent = summary.successRate;
        } catch (err) {
            console.error('Summary fetch error:', err);
            showToast('Could not load general KPIs.', 'error');
        }
    }

    /**
     * Fetch statistical arrays and render the 3 Chart.js graphs.
     */
    async function buildCharts() {
        // Colors corresponding to CSS theme variables
        const colors = {
            hot: '#10b981',      // Emerald Green
            warm: '#fbbf24',     // Amber
            cold: '#ef4444',     // Red
            purple: '#a855f7',
            indigo: '#6366f1',
            teal: '#14b8a6',
            gridLines: 'rgba(255, 255, 255, 0.05)',
            text: '#9ca3af'
        };

        try {
            // Category distribution (Doughnut)
            const distRes = await fetch(`${DASHBOARD_URL}/category-distribution`);
            const distData = distRes.ok ? await distRes.json() : { "Hot": 0, "Warm": 0, "Cold": 0 };
            
            const catCtx = document.getElementById('categoryChart').getContext('2d');
            if (categoryChart) categoryChart.destroy();
            categoryChart = new Chart(catCtx, {
                type: 'doughnut',
                data: {
                    labels: Object.keys(distData),
                    datasets: [{
                        data: Object.values(distData),
                        backgroundColor: [colors.hot, colors.warm, colors.cold],
                        borderColor: '#0a0e17',
                        borderWidth: 2,
                        hoverOffset: 4
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'bottom',
                            labels: {
                                color: colors.text,
                                font: { family: 'Outfit', size: 12 }
                            }
                        }
                    }
                }
            });

            // Monthly volume (Bar)
            const volRes = await fetch(`${DASHBOARD_URL}/monthly-analysis`);
            const volData = volRes.ok ? await volRes.json() : {};
            
            const volCtx = document.getElementById('volumeChart').getContext('2d');
            if (volumeChart) volumeChart.destroy();
            volumeChart = new Chart(volCtx, {
                type: 'bar',
                data: {
                    labels: Object.keys(volData),
                    datasets: [{
                        label: 'Leads Created',
                        data: Object.values(volData),
                        backgroundColor: colors.indigo,
                        borderRadius: 6
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { display: false }
                    },
                    scales: {
                        x: {
                            grid: { display: false },
                            ticks: { color: colors.text, font: { family: 'Outfit' } }
                        },
                        y: {
                            grid: { color: colors.gridLines },
                            ticks: { color: colors.text, font: { family: 'Outfit' } }
                        }
                    }
                }
            });

            // Average score trend (Line)
            const trendRes = await fetch(`${DASHBOARD_URL}/monthly-score-trend`);
            const trendData = trendRes.ok ? await trendRes.json() : {};

            const trendCtx = document.getElementById('scoreTrendChart').getContext('2d');
            if (scoreTrendChart) scoreTrendChart.destroy();
            scoreTrendChart = new Chart(trendCtx, {
                type: 'line',
                data: {
                    labels: Object.keys(trendData),
                    datasets: [{
                        label: 'Average Score',
                        data: Object.values(trendData),
                        borderColor: colors.teal,
                        backgroundColor: 'rgba(20, 184, 166, 0.1)',
                        fill: true,
                        tension: 0.35,
                        pointBackgroundColor: colors.teal,
                        pointBorderColor: '#0a0e17',
                        pointBorderWidth: 2,
                        pointRadius: 5
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { display: false }
                    },
                    scales: {
                        x: {
                            grid: { display: false },
                            ticks: { color: colors.text, font: { family: 'Outfit' } }
                        },
                        y: {
                            grid: { color: colors.gridLines },
                            ticks: { color: colors.text, font: { family: 'Outfit' } },
                            min: 0,
                            max: 100
                        }
                    }
                }
            });
        } catch (err) {
            console.error('Error generating charts:', err);
            showToast('Unable to construct charts.', 'error');
        }
    }

    /**
     * Fetch full lead collection to perform local filter/search on the dashboard view,
     * and dynamically extract unique industry options.
     */
    async function fetchLeadsList() {
        try {
            const res = await fetch(`${LEADS_URL}?sort=newest`);
            if (!res.ok) throw new Error('Leads list fetch failed');
            allLeads = await res.json();
            
            // Extract unique industries for filter dropdown
            populateIndustryDropdown(allLeads);

            // Apply filters to initial table state
            applyFilters();
        } catch (err) {
            console.error('Error fetching leads list:', err);
            showToast('Could not load latest leads table.', 'error');
        }
    }

    /**
     * Extract unique industries from the leads list and load them into select element.
     */
    function populateIndustryDropdown(leads) {
        const uniqueIndustries = new Set();
        leads.forEach(l => {
            if (l.industry) uniqueIndustries.add(l.industry);
        });

        // Clear existing, leaving first "All" option
        filterIndustry.innerHTML = '<option value="all">All Industries</option>';

        uniqueIndustries.forEach(ind => {
            const opt = document.createElement('option');
            opt.value = ind;
            opt.textContent = ind;
            filterIndustry.appendChild(opt);
        });
    }

    /**
     * Applies filter selectors & text search queries on the cached leads array.
     */
    function applyFilters() {
        const searchVal = searchInput.value.toLowerCase().trim();
        const dateVal = filterDate.value;
        const categoryVal = filterCategory.value;
        const industryVal = filterIndustry.value;

        const now = new Date();

        filteredLeads = allLeads.filter(lead => {
            // 1. Text Search Filter (Lead Name & Company Name)
            const matchesSearch = !searchVal || 
                (lead.leadName && lead.leadName.toLowerCase().includes(searchVal)) ||
                (lead.companyName && lead.companyName.toLowerCase().includes(searchVal));

            // 2. Category Filter
            let matchesCategory = true;
            if (categoryVal !== 'all') {
                if (categoryVal === 'PENDING' || categoryVal === 'FAILED') {
                    matchesCategory = lead.analysisStatus === categoryVal;
                } else {
                    matchesCategory = lead.category === categoryVal;
                }
            }

            // 3. Industry Filter
            const matchesIndustry = industryVal === 'all' || lead.industry === industryVal;

            // 4. Date Created Filter
            let matchesDate = true;
            if (lead.createdAt && dateVal !== 'all') {
                const created = new Date(lead.createdAt);
                const diffTime = Math.abs(now - created);
                const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

                if (dateVal === 'today') {
                    // Check if calendar day is same
                    matchesDate = created.toDateString() === now.toDateString();
                } else if (dateVal === 'week') {
                    matchesDate = diffDays <= 7;
                } else if (dateVal === 'month') {
                    matchesDate = diffDays <= 30;
                }
            }

            return matchesSearch && matchesCategory && matchesIndustry && matchesDate;
        });

        renderLeadsTable(filteredLeads);
    }

    /**
     * Render the filtered leads list, limited to latest 10 rows.
     */
    function renderLeadsTable(leads) {
        tableBody.innerHTML = '';

        // Display latest 10 leads according to instructions
        const latest10 = leads.slice(0, 10);

        if (latest10.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="8" style="text-align: center; padding: 2rem; color: var(--text-secondary);">
                        No matching leads found.
                    </td>
                </tr>
            `;
            return;
        }

        latest10.forEach(lead => {
            const row = document.createElement('tr');
            
            // Format Created Date
            const createdDate = new Date(lead.createdAt);
            const formattedDate = createdDate.toLocaleDateString('en-US', {
                month: 'short', day: 'numeric', year: 'numeric'
            });

            // Set badge class depending on industry
            let industryBadgeClass = 'badge-default';
            if (lead.industry === 'Technology') industryBadgeClass = 'badge-tech';
            else if (lead.industry === 'Finance') industryBadgeClass = 'badge-fin';
            else if (lead.industry === 'Healthcare') industryBadgeClass = 'badge-health';
            else if (lead.industry === 'Education') industryBadgeClass = 'badge-edu';

            // Category badge class
            let categoryBadgeClass = 'badge-default';
            const cat = (lead.category || '').toLowerCase();
            if (cat.includes('hot')) categoryBadgeClass = 'badge-fin';
            else if (cat.includes('warm')) categoryBadgeClass = 'badge-health';

            // Status dot class
            let statusClass = 'pending';
            const status = lead.analysisStatus || 'PENDING';
            if (status === 'SUCCESS') statusClass = 'success';
            else if (status === 'FAILED') statusClass = 'failed';

            row.innerHTML = `
                <td style="font-weight: 600; color: var(--text-primary);">${escapeHtml(lead.leadName)}</td>
                <td>${escapeHtml(lead.companyName)}</td>
                <td><span class="badge ${industryBadgeClass}">${escapeHtml(lead.industry)}</span></td>
                <td style="font-weight: 700; color: var(--text-primary);">${lead.leadScore !== null ? lead.leadScore : '-'}</td>
                <td><span class="badge ${categoryBadgeClass}">${lead.category ? escapeHtml(lead.category) : '-'}</span></td>
                <td>
                    <span class="status-dot ${statusClass}"></span>
                    <span style="font-size: 0.85rem; font-weight: 500; text-transform: uppercase;">${escapeHtml(status)}</span>
                </td>
                <td>${formattedDate}</td>
                <td style="text-align: center;">
                    <div class="action-buttons">
                        <button class="btn-icon view-btn" title="View details" data-id="${lead.id}">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                        </button>
                        <button class="btn-icon ai-btn" title="View AI Report" data-id="${lead.id}">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="color: var(--accent-purple);"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>
                        </button>
                        <button class="btn-icon edit-btn" title="Edit lead" data-id="${lead.id}">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path><path d="M18.5 2.5a2.121 2.121 0 1 1 3 3L12 15l-4 1 1-4Z"></path></svg>
                        </button>
                        <button class="btn-icon delete-btn" title="Delete lead" data-id="${lead.id}">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path><line x1="10" y1="11" x2="10" y2="17"></line><line x1="14" y1="11" x2="14" y2="17"></line></svg>
                        </button>
                    </div>
                </td>
            `;

            // Bind click handlers
            row.querySelector('.view-btn').addEventListener('click', () => openViewModal(lead));
            row.querySelector('.ai-btn').addEventListener('click', () => {
                window.location.href = `result.html?id=${lead.id}`;
            });
            row.querySelector('.edit-btn').addEventListener('click', () => {
                window.location.href = `lead-form.html?id=${lead.id}`;
            });
            row.querySelector('.delete-btn').addEventListener('click', () => {
                deleteTargetId = lead.id;
                deleteModal.classList.add('active');
            });

            tableBody.appendChild(row);
        });
    }

    /**
     * Display detailed modal card content.
     */
    function openViewModal(lead) {
        const formattedRevenue = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 }).format(lead.annualRevenue);
        const formattedBudget = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 }).format(lead.budget);
        const createdDate = new Date(lead.createdAt);

        document.getElementById('view-leadName').textContent = lead.leadName || '-';
        document.getElementById('view-companyName').textContent = lead.companyName || '-';
        document.getElementById('view-industry').textContent = lead.industry || '-';
        document.getElementById('view-jobRole').textContent = lead.jobRole || '-';
        document.getElementById('view-companySize').textContent = lead.companySize ? `${lead.companySize} employees` : '-';
        document.getElementById('view-annualRevenue').textContent = formattedRevenue;
        document.getElementById('view-budget').textContent = formattedBudget;
        document.getElementById('view-timeline').textContent = lead.timeline || '-';
        document.getElementById('view-email').textContent = lead.email || '-';
        document.getElementById('view-phoneNumber').textContent = lead.phoneNumber || '-';
        document.getElementById('view-requirement').textContent = lead.requirement || '-';
        document.getElementById('view-createdAt').textContent = createdDate.toLocaleString();

        // Populate AI fields in modal
        document.getElementById('view-leadScore').textContent = lead.leadScore !== null && lead.leadScore !== undefined ? lead.leadScore : '-';
        document.getElementById('view-category').textContent = lead.category || '-';
        document.getElementById('view-reason').textContent = lead.reason || '-';
        document.getElementById('view-recommendation').textContent = lead.recommendation || '-';

        viewModal.classList.add('active');
    }

    /**
     * Delete a single lead from MongoDB.
     */
    async function executeDelete() {
        if (!deleteTargetId) return;

        deleteModal.classList.remove('active');
        showLoader('Deleting lead from database...');

        try {
            const res = await fetch(`${LEADS_URL}/${deleteTargetId}`, {
                method: 'DELETE'
            });

            if (res.ok) {
                showToast('Lead deleted successfully.', 'success');
                // Refresh both KPIs and table contents
                await initDashboard();
            } else {
                showToast('Failed to delete the lead.', 'error');
            }
        } catch (err) {
            console.error('Delete request error:', err);
            showToast('Unable to execute delete request.', 'error');
        } finally {
            hideLoader();
            deleteTargetId = null;
        }
    }

    /**
     * Assemble CSV lines of all FILTERED leads and trigger a download.
     */
    function exportToCSV() {
        if (filteredLeads.length === 0) {
            showToast('No leads available to export.', 'error');
            return;
        }

        const headers = [
            'Lead Name', 'Company Name', 'Industry', 'Job Role', 'Company Size', 
            'Annual Revenue', 'Budget', 'Timeline', 'Email', 'Phone', 'AI Score', 
            'Category', 'Status', 'Date Created'
        ];

        const csvRows = [headers.join(',')];

        filteredLeads.forEach(lead => {
            const row = [
                escapeCSV(lead.leadName),
                escapeCSV(lead.companyName),
                escapeCSV(lead.industry),
                escapeCSV(lead.jobRole),
                lead.companySize || '',
                lead.annualRevenue || '',
                lead.budget || '',
                escapeCSV(lead.timeline),
                escapeCSV(lead.email),
                escapeCSV(lead.phoneNumber),
                lead.leadScore !== null ? lead.leadScore : '',
                escapeCSV(lead.category),
                escapeCSV(lead.analysisStatus),
                lead.createdAt ? new Date(lead.createdAt).toISOString() : ''
            ];
            csvRows.push(row.join(','));
        });

        const csvContent = "data:text/csv;charset=utf-8," + csvRows.join("\n");
        const encodedUri = encodeURI(csvContent);
        const link = document.createElement("a");
        link.setAttribute("href", encodedUri);
        link.setAttribute("download", `LeadQualificationReport_${new Date().toISOString().slice(0,10)}.csv`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        showToast('CSV export successful.', 'success');
    }

    /**
     * Helper to wrap strings in quotes and double-quote inner quotes for CSV safety.
     */
    function escapeCSV(str) {
        if (!str) return '';
        let escaped = str.toString().replace(/"/g, '""');
        if (escaped.search(/("|,|\n)/g) >= 0) {
            escaped = `"${escaped}"`;
        }
        return escaped;
    }

    // Modal / Toast Helpers
    function showLoader(message = 'Processing...') {
        loadingOverlay.querySelector('.loading-text').textContent = message;
        loadingOverlay.classList.add('active');
    }

    function hideLoader() {
        loadingOverlay.classList.remove('active');
    }

    function showToast(message, type) {
        toast.className = 'toast show';
        if (type === 'success') {
            toast.classList.add('success');
            toastMessage.innerHTML = `<strong>Success:</strong> ${message}`;
        } else {
            toast.classList.add('error');
            toastMessage.innerHTML = `<strong>Error:</strong> ${message}`;
        }

        setTimeout(() => {
            toast.classList.remove('show');
        }, 4000);
    }

    function escapeHtml(str) {
        if (!str) return '';
        return str
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }
});
