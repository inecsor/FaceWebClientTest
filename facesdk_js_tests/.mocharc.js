module.exports = (function() {
    // Default configuration for using spec reporter
    let config = {
        extension: ['js', 'cjs', 'mjs'],
        package: './package.json',
        reporter: 'spec', // Default reporter
        'reporter-option': [],
    };

    // Check if USE_REPORTPORTAL environment variable is set to true
    if (process.env.UseReportPortal === 'true') {
        config.reporter = '@reportportal/agent-js-mocha';
        config['reporter-option'] = [
            `endpoint=http://reportportal.regula.local/api/v1`,
            'apiKey=regula_vBWLQ42VQM-6P3Ta0ZXpLDLonBzJok-jw2HhJ5tzgBeKFvgBgk9d8c0WWvZE3zeq',
            `launch=js-client`,
            'project=web-client-faces',
            'attributes=Mocha',
            'reportHooks=true',
        ];
    }

    return config;
})();
