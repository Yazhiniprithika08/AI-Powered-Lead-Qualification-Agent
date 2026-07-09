const CONFIG = {
    // If running frontend on local port 8000, target localhost:8080.
    // Otherwise (served by Spring Boot or in production), use relative URL.
    API_BASE_URL: window.location.port === '8000'
        ? 'http://localhost:8080'
        : ''
};
