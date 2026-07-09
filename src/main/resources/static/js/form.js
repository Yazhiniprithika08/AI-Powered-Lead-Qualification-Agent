/**
 * Script for Lead Qualification Form interactions and API integration.
 * Supports Lead Creation (POST) and Lead Editing (PUT).
 */
document.addEventListener('DOMContentLoaded', async () => {
    const form = document.getElementById('lead-qualification-form');
    const loadingOverlay = document.getElementById('loading-overlay');
    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toast-message');
    const formTitle = document.querySelector('.form-title');
    const formSubtitle = document.querySelector('.form-subtitle');
    const submitBtn = document.getElementById('btn-submit');
    const resetBtn = document.getElementById('btn-reset');
    
    const BASE_URL = `${CONFIG.API_BASE_URL}/api/leads`;

    // 1. Detect if we are in Edit Mode
    const urlParams = new URLSearchParams(window.location.search);
    const editId = urlParams.get('id');
    const isEditMode = !!editId;

    console.log(`Lead Form loaded. Mode: ${isEditMode ? 'Edit' : 'Create'}`);

    if (isEditMode) {
        // Adjust headers and buttons for edit mode
        if (formTitle) formTitle.textContent = 'Edit Lead Profile';
        if (formSubtitle) formSubtitle.textContent = 'Modify the lead attributes below to update database records.';
        if (submitBtn) {
            submitBtn.innerHTML = `
                Update Lead
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path><polyline points="17 21 17 13 7 13 7 21"></polyline><polyline points="7 3 7 8 15 8"></polyline></svg>
            `;
        }
        
        // Hide Reset Button during Edit Mode since we want to cancel or keep original data
        if (resetBtn) {
            resetBtn.textContent = 'Cancel';
            resetBtn.type = 'button'; // Prevent default reset
            resetBtn.addEventListener('click', () => {
                window.location.href = 'history.html';
            });
        }

        // Fetch existing lead data
        try {
            loadingOverlay.querySelector('.loading-text').textContent = 'Loading Lead Data...';
            loadingOverlay.classList.add('active');

            const response = await fetch(`${BASE_URL}/${editId}`);
            if (!response.ok) {
                throw new Error('Failed to retrieve lead information.');
            }
            const lead = await response.json();
            
            // Populate form fields
            document.getElementById('leadName').value = lead.leadName || '';
            document.getElementById('companyName').value = lead.companyName || '';
            document.getElementById('industry').value = lead.industry || '';
            document.getElementById('jobRole').value = lead.jobRole || '';
            document.getElementById('companySize').value = lead.companySize || '';
            document.getElementById('annualRevenue').value = lead.annualRevenue || '';
            document.getElementById('budget').value = lead.budget || '';
            document.getElementById('timeline').value = lead.timeline || '';
            document.getElementById('email').value = lead.email || '';
            document.getElementById('phoneNumber').value = lead.phoneNumber || '';
            document.getElementById('requirement').value = lead.requirement || '';

            console.log('Form populated with existing lead details.');
        } catch (error) {
            console.error('Error fetching lead details:', error);
            showToast('Error loading lead information. Redirecting to history...', 'error');
            setTimeout(() => {
                window.location.href = 'history.html';
            }, 2500);
        } finally {
            loadingOverlay.classList.remove('active');
            // Restore loading text
            loadingOverlay.querySelector('.loading-text').textContent = 'Analyzing Lead Profile...';
        }
    }

    // Remove red validation highlights when user starts typing on an invalid element
    form.querySelectorAll('.form-control').forEach(input => {
        input.addEventListener('input', () => {
            if (input.classList.contains('is-invalid')) {
                input.classList.remove('is-invalid');
                input.style.borderColor = '';
                input.style.boxShadow = '';
            }
        });
        input.addEventListener('change', () => {
            if (input.classList.contains('is-invalid')) {
                input.classList.remove('is-invalid');
                input.style.borderColor = '';
                input.style.boxShadow = '';
            }
        });
    });

    // Handle form submit
    form.addEventListener('submit', async (event) => {
        event.preventDefault();

        // 2. Perform HTML5 Validation
        let isFormValid = true;
        const formControls = form.querySelectorAll('.form-control');

        formControls.forEach(control => {
            if (!control.checkValidity()) {
                isFormValid = false;
                control.classList.add('is-invalid');
                control.style.borderColor = 'var(--danger)';
                control.style.boxShadow = '0 0 0 3px rgba(239, 68, 68, 0.15)';
            } else {
                control.classList.remove('is-invalid');
                control.style.borderColor = '';
                control.style.boxShadow = '';
            }
        });

        if (!isFormValid) {
            showToast('Please correct the highlighted fields in the form.', 'error');
            return;
        }

        // 3. Extract input values and form DTO payload
        const formData = new FormData(form);
        const payload = {
            leadName: formData.get('leadName'),
            companyName: formData.get('companyName'),
            industry: formData.get('industry'),
            jobRole: formData.get('jobRole'),
            companySize: parseInt(formData.get('companySize'), 10),
            annualRevenue: parseFloat(formData.get('annualRevenue')),
            budget: parseFloat(formData.get('budget')),
            timeline: formData.get('timeline'),
            email: formData.get('email'),
            phoneNumber: formData.get('phoneNumber'),
            requirement: formData.get('requirement')
        };

        // 4. Show Loading Animation
        loadingOverlay.querySelector('.loading-text').textContent = isEditMode ? 'Updating Lead Database...' : 'Saving Lead Profile...';
        loadingOverlay.classList.add('active');

        // Capture starting timestamp to enforce minimum duration for loading animation
        const startTime = Date.now();

        try {
            let requestUrl = `${BASE_URL}/analyze`;
            let requestMethod = 'POST';

            if (isEditMode) {
                requestUrl = `${BASE_URL}/${editId}`;
                requestMethod = 'PUT';
            }

            console.log(`Sending payload to ${requestMethod} API:`, payload);

            // 5. Send request to Spring Boot backend
            const response = await fetch(requestUrl, {
                method: requestMethod,
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            });

            const data = await response.json();
            console.log('API Response received:', data);

            // Enforce minimum loader display of 1200ms for premium fluid feel
            const elapsedTime = Date.now() - startTime;
            const remainingTime = Math.max(1200 - elapsedTime, 0);

            setTimeout(() => {
                loadingOverlay.classList.remove('active');

                if (response.ok) {
                    const targetId = isEditMode ? editId : data.leadId;
                    
                    if (isEditMode) {
                        showToast('Lead updated successfully. Redirecting to results...', 'success');
                    } else {
                        showToast('Lead saved and analyzed. Redirecting to results...', 'success');
                    }
                    
                    // Reset the form
                    form.reset();

                    // Redirect to result.html?id=targetId
                    setTimeout(() => {
                        window.location.href = `result.html?id=${targetId}`;
                    }, 1500);
                } else {
                    // Extract validation errors from backend if available
                    let errMsg = data.message || 'An error occurred while processing the lead.';
                    if (data.errors) {
                        const errorDetails = Object.values(data.errors).join(', ');
                        errMsg = `${data.message}: ${errorDetails}`;
                    }
                    showToast(errMsg, 'error');
                }
            }, remainingTime);

        } catch (error) {
            console.error('Fetch operation failed:', error);
            
            const elapsedTime = Date.now() - startTime;
            const remainingTime = Math.max(1200 - elapsedTime, 0);

            setTimeout(() => {
                loadingOverlay.classList.remove('active');
                showToast('Unable to reach qualification server. Make sure the Spring Boot backend is running.', 'error');
            }, remainingTime);
        }
    });

    // Custom Toast Notification handler
    function showToast(message, type) {
        toast.className = 'toast show';
        
        if (type === 'success') {
            toast.classList.add('success');
            toastMessage.innerHTML = `<strong>Success:</strong> ${message}`;
        } else {
            toast.classList.add('error');
            toastMessage.innerHTML = `<strong>Error:</strong> ${message}`;
        }

        // Hide toast after 4 seconds
        setTimeout(() => {
            toast.classList.remove('show');
        }, 4000);
    }
});
