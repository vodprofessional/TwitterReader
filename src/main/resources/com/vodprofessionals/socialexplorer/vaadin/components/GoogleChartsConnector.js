com_vodprofessionals_socialexplorer_vaadin_components_GoogleCharts = function () {
    var googleReady = false;
    var stateChangeInProgress = false;
    var e = this.getElement();
    var chartClass, dataTable, options;

    google.load('visualization', '1.0', {
        callback: function() {
            googleReady = true;
        },
        packages: ['corechart']
    });


    /**
     *
     * @param obj
     * @param path
     * @returns {*}
     */
    function deepFind(obj, path) {
        var paths = path.split('.')
            , current = obj
            , i;

        for (i = 0; i < paths.length; ++i) {
            if (current[paths[i]] == undefined) {
                return undefined;
            } else {
                current = current[paths[i]];
            }
        }
        return current;
    }

    /**
     * Draw the actual chart with Google Charts
     */
    function drawChart() {
        console.log(dataTable);
        // Create the data table.
        var data = google.visualization.arrayToDataTable(dataTable);

        // Instantiate and draw our chart, passing in some options.
        var c = deepFind(window, chartClass);
        var chart = new c(e);

        chart.draw(data, options);
    }

    /**
     *
     */
    function applyStateChange() {
        if (googleReady != true) {
            setTimeout(applyStateChange, 10);
        }
        else {
            drawChart();
            stateChangeInProgress = false;
        }
    };

    /**
     * State change callback from Vaadin
     */
    this.onStateChange = function() {
        var state = this.getState();

        chartClass = state.jsChartClass;
        dataTable = state.dataTable;
        options = state.options;

        if (!stateChangeInProgress) {
            stateChangeInProgress = true;
            applyStateChange();
        }
    };
};