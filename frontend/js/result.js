/**
 * Script to manage Lead AI Result rendering and retry operations.
 */
document.addEventListener('DOMContentLoaded', async () => {
    const loadingOverlay = document.getElementById('loading-overlay');
    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toast-message');

    // Success panels
    const successContainer = document.getElementById('success-container');
    const scoreRing = document.getElementById('score-ring-element');
    const resScore = document.getElementById('res-score');
    const resCategory = document.getElementById('res-category');
    const resLeadName = document.getElementById('res-leadName');
    const resCompanyName = document.getElementById('res-companyName');
    const resJobRole = document.getElementById('res-jobRole');
    const resReason = document.getElementById('res-reason');
    const resRecommendation = document.getElementById('res-recommendation');

    // Failed panels
    const failedContainer = document.getElementById('failed-container');
    const failLeadName = document.getElementById('fail-leadName');
    const failCompanyName = document.getElementById('fail-companyName');
    const retryBtn = document.getElementById('btn-retry');

    const BASE_URL = 'http://localhost:8080/api/leads';

    // Extract ID query param
    const urlParams = new URLSearchParams(window.location.search);
    const leadId = urlParams.get('id');

    if (!leadId) {
        console.error('Lead ID missing in query string.');
        showToast('Lead ID is missing. Redirecting to history...', 'error');
        setTimeout(() => {
            window.location.href = 'history.html';
        }, 2000);
        return;
    }

    // Initial Fetch
    await loadAnalysisResult();

    // Fetch lead details and load page layout
    async function loadAnalysisResult() {
        showLoader('Retrieving AI Qualification Details...');
        try {
            const response = await fetch(`${BASE_URL}/${leadId}`);
            if (!response.ok) {
                throw new Error('Failed to fetch lead details.');
            }
            const lead = await response.json();
            console.log('Lead Details Loaded:', lead);

            if (lead.analysisStatus === 'SUCCESS') {
                renderSuccessState(lead);
            } else {
                renderFailedState(lead);
            }
        } catch (error) {
            console.error('Error loading result:', error);
            showToast('Unable to reach qualification server.', 'error');
            failedContainer.style.display = 'flex';
        } finally {
            hideLoader();
        }
    }

    // Render successful AI results
    function renderSuccessState(lead) {
        successContainer.style.display = 'flex';
        failedContainer.style.display = 'none';

        resScore.textContent = lead.leadScore;
        resCategory.textContent = lead.category;
        resLeadName.textContent = lead.leadName;
        resCompanyName.textContent = lead.companyName;
        resJobRole.textContent = lead.jobRole;
        resReason.textContent = lead.reason;
        resRecommendation.textContent = lead.recommendation;

        // Reset ring/badge color classes
        scoreRing.className = 'score-ring';
        resCategory.className = 'category-pill';

        const category = (lead.category || '').toLowerCase();
        if (category.includes('hot')) {
            scoreRing.classList.add('hot');
            resCategory.classList.add('hot');
        } else if (category.includes('warm')) {
            scoreRing.classList.add('warm');
            resCategory.classList.add('warm');
        } else {
            scoreRing.classList.add('cold');
            resCategory.classList.add('cold');
        }
    }

    // Render failed AI status page
    function renderFailedState(lead) {
        successContainer.style.display = 'none';
        failedContainer.style.display = 'flex';

        failLeadName.textContent = lead.leadName;
        failCompanyName.textContent = lead.companyName;
    }

    // Click handler: Retry Analysis
    retryBtn.addEventListener('click', async () => {
        showLoader('Retrying Google Gemini AI Analysis...');
        const startTime = Date.now();

        try {
            const response = await fetch(`${BASE_URL}/${leadId}/retry`, {
                method: 'POST'
            });

            const updatedLead = await response.json();
            console.log('Retry response received:', updatedLead);

            // Enforce minimum spinner display of 1500ms for visual consistency
            const elapsedTime = Date.now() - startTime;
            const remainingTime = Math.max(1500 - elapsedTime, 0);

            setTimeout(() => {
                hideLoader();
                if (response.ok && updatedLead.analysisStatus === 'SUCCESS') {
                    showToast('AI Analysis completed successfully!', 'success');
                    renderSuccessState(updatedLead);
                } else {
                    showToast('AI Analysis failed again. Please check your API configuration.', 'error');
                    renderFailedState(updatedLead);
                }
            }, remainingTime);

        } catch (error) {
            console.error('Retry execution failed:', error);
            const elapsedTime = Date.now() - startTime;
            const remainingTime = Math.max(1500 - elapsedTime, 0);
            
            setTimeout(() => {
                hideLoader();
                showToast('Unable to reach qualification server to retry.', 'error');
            }, remainingTime);
        }
    });

    // Helpers
    function showLoader(message) {
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
});
