/**
 * Script for home page interactions.
 */
document.addEventListener('DOMContentLoaded', () => {
    console.log('Lead Qualification Agent - Home Page loaded.');

    const startBtn = document.getElementById('btn-start-analysis');
    if (startBtn) {
        startBtn.addEventListener('click', (e) => {
            console.log('Redirecting user to Lead Qualification Form...');
        });
    }
});
