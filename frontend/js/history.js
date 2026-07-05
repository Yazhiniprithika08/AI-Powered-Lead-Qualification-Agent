/**
 * Script for managing Lead History Page data rendering, sorting, searching, and deleting.
 */
document.addEventListener('DOMContentLoaded', () => {
    const tableBody = document.getElementById('leads-table-body');
    const loadingOverlay = document.getElementById('loading-overlay');
    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toast-message');
    const searchInput = document.getElementById('search-company');
    const sortSelect = document.getElementById('sort-select');

    // Modals
    const viewModal = document.getElementById('view-modal');
    const viewModalClose = document.getElementById('view-modal-close');
    const deleteModal = document.getElementById('delete-confirm-modal');
    const deleteCancel = document.getElementById('delete-cancel');
    const deleteConfirm = document.getElementById('delete-confirm');

    const BASE_URL = 'http://localhost:8080/api/leads';
    let currentLeads = [];
    let deleteTargetId = null;

    console.log('Lead History Script loaded.');

    // Initial load
    fetchLeads();

    // Event listener: Sorting
    sortSelect.addEventListener('change', () => {
        // If there's search text, we sort the search, else full list
        const query = searchInput.value.trim();
        if (query.length > 0) {
            searchLeads(query, sortSelect.value);
        } else {
            fetchLeads(sortSelect.value);
        }
    });

    // Event listener: Searching (with debounce for fluent experience)
    let debounceTimer;
    searchInput.addEventListener('input', () => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => {
            const query = searchInput.value.trim();
            if (query.length > 0) {
                searchLeads(query, sortSelect.value);
            } else {
                fetchLeads(sortSelect.value);
            }
        }, 300);
    });

    // Fetch all leads from REST API
    async function fetchLeads(sort = 'newest') {
        showLoader('Fetching Leads from Database...');
        try {
            const response = await fetch(`${BASE_URL}?sort=${sort}`);
            if (!response.ok) {
                throw new Error('Failed to retrieve database contents.');
            }
            currentLeads = await response.json();
            renderTable(currentLeads);
        } catch (error) {
            console.error('Error fetching leads:', error);
            showToast('Unable to connect to database server. Please ensure the backend is running.', 'error');
            renderTable([]);
        } finally {
            hideLoader();
        }
    }

    // Search leads by company name snippet
    async function searchLeads(companySnippet, sort = 'newest') {
        showLoader(`Searching company '${companySnippet}'...`);
        try {
            const response = await fetch(`${BASE_URL}/search?company=${encodeURIComponent(companySnippet)}`);
            if (!response.ok) {
                throw new Error('Failed to perform search query.');
            }
            let searchResults = await response.json();
            
            // Client side sorting fallback if search endpoint returns default unsorted list
            searchResults.sort((a, b) => {
                const dateA = new Date(a.createdAt);
                const dateB = new Date(b.createdAt);
                return sort === 'newest' ? dateB - dateA : dateA - dateB;
            });

            renderTable(searchResults);
        } catch (error) {
            console.error('Error searching leads:', error);
            showToast('Error executing search query.', 'error');
        } finally {
            hideLoader();
        }
    }

    // Render data rows in history table
    function renderTable(leads) {
        tableBody.innerHTML = '';

        if (leads.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="7" style="text-align: center; padding: 2rem; color: var(--text-secondary);">
                        No leads found in database.
                    </td>
                </tr>
            `;
            return;
        }

        leads.forEach(lead => {
            const row = document.createElement('tr');
            
            // Format Currency
            const formattedBudget = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 }).format(lead.budget);
            
            // Format Date
            const createdDate = new Date(lead.createdAt);
            const formattedDate = createdDate.toLocaleDateString('en-US', { 
                year: 'numeric', 
                month: 'short', 
                day: 'numeric' 
            });

            // Set badge class depending on industry to look premium
            let badgeClass = 'badge-default';
            if (lead.industry === 'Technology') badgeClass = 'badge-tech';
            else if (lead.industry === 'Finance') badgeClass = 'badge-fin';
            else if (lead.industry === 'Healthcare') badgeClass = 'badge-health';
            else if (lead.industry === 'Education') badgeClass = 'badge-edu';

            // Category badge class
            let categoryClass = 'badge-default';
            const cat = (lead.category || '').toLowerCase();
            if (cat.includes('hot')) categoryClass = 'badge-fin';
            else if (cat.includes('warm')) categoryClass = 'badge-health';
            else if (cat.includes('cold')) categoryClass = 'badge-default';

            // Status badge class
            let statusClass = 'badge-default';
            let statusText = lead.analysisStatus || 'PENDING';
            let statusStyle = '';
            if (statusText === 'SUCCESS') {
                statusClass = 'badge-fin';
            } else if (statusText === 'FAILED') {
                statusStyle = 'background: rgba(239, 68, 68, 0.15); color: #f87171; border: 1px solid rgba(248, 113, 113, 0.2);';
            } else {
                statusStyle = 'background: rgba(245, 158, 11, 0.15); color: #fbbf24; border: 1px solid rgba(251, 191, 36, 0.2);';
            }

            row.innerHTML = `
                <td style="font-weight: 600; color: var(--text-primary);">${escapeHtml(lead.leadName)}</td>
                <td>${escapeHtml(lead.companyName)}</td>
                <td><span class="badge ${badgeClass}">${escapeHtml(lead.industry)}</span></td>
                <td>${formattedBudget}</td>
                <td><span style="font-weight: 700; color: var(--text-primary);">${lead.leadScore !== null && lead.leadScore !== undefined ? lead.leadScore : '-'}</span></td>
                <td><span class="badge ${categoryClass}">${lead.category ? escapeHtml(lead.category) : '-'}</span></td>
                <td><span class="badge ${statusClass}" style="${statusStyle}">${escapeHtml(statusText)}</span></td>
                <td style="text-align: center;">
                    <div class="action-buttons">
                        <button class="btn-icon view-btn" title="View details" data-id="${lead.id}">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                        </button>
                        <button class="btn-icon ai-btn" title="View AI Report" data-id="${lead.id}">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="color: var(--accent-purple);"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>
                        </button>
                        <button class="btn-icon edit-btn" title="Edit lead" data-id="${lead.id}">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path><path d="M18.5 2.5a2.121 2.121 0 1 1 3 3L12 15l-4 1 1-4Z"></path></svg>
                        </button>
                        <button class="btn-icon delete-btn" title="Delete lead" data-id="${lead.id}">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path><line x1="10" y1="11" x2="10" y2="17"></line><line x1="14" y1="11" x2="14" y2="17"></line></svg>
                        </button>
                    </div>
                </td>
            `;

            // Bind button events inside the row
            row.querySelector('.view-btn').addEventListener('click', () => openViewModal(lead));
            row.querySelector('.ai-btn').addEventListener('click', () => {
                window.location.href = `result.html?id=${lead.id}`;
            });
            row.querySelector('.edit-btn').addEventListener('click', () => {
                window.location.href = `lead-form.html?id=${lead.id}`;
            });
            row.querySelector('.delete-btn').addEventListener('click', () => openDeleteModal(lead.id));

            tableBody.appendChild(row);
        });
    }

    // Modal Control: Details View
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
        document.getElementById('view-createdAt').textContent = createdDate.toLocaleString() || '-';

        // Populate AI fields in modal
        document.getElementById('view-leadScore').textContent = lead.leadScore !== null && lead.leadScore !== undefined ? lead.leadScore : '-';
        document.getElementById('view-category').textContent = lead.category || '-';
        document.getElementById('view-reason').textContent = lead.reason || '-';
        document.getElementById('view-recommendation').textContent = lead.recommendation || '-';

        viewModal.classList.add('active');
    }

    viewModalClose.addEventListener('click', () => {
        viewModal.classList.remove('active');
    });

    // Modal Control: Custom styled Deletion Confirm
    function openDeleteModal(id) {
        deleteTargetId = id;
        deleteModal.classList.add('active');
    }

    deleteCancel.addEventListener('click', () => {
        deleteModal.classList.remove('active');
        deleteTargetId = null;
    });

    deleteConfirm.addEventListener('click', async () => {
        if (!deleteTargetId) return;

        deleteModal.classList.remove('active');
        showLoader('Deleting record from database...');
        
        try {
            const response = await fetch(`${BASE_URL}/${deleteTargetId}`, {
                method: 'DELETE'
            });

            if (response.ok) {
                showToast('Lead deleted successfully.', 'success');
                // Refresh records
                const query = searchInput.value.trim();
                if (query.length > 0) {
                    searchLeads(query, sortSelect.value);
                } else {
                    fetchLeads(sortSelect.value);
                }
            } else {
                showToast('Failed to delete the lead.', 'error');
            }
        } catch (error) {
            console.error('Error deleting lead:', error);
            showToast('Unable to reach server to execute delete.', 'error');
        } finally {
            hideLoader();
            deleteTargetId = null;
        }
    });

    // Click outside modal content to close it
    window.addEventListener('click', (e) => {
        if (e.target === viewModal) {
            viewModal.classList.remove('active');
        }
        if (e.target === deleteModal) {
            deleteModal.classList.remove('active');
            deleteTargetId = null;
        }
    });

    // Helpers
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
