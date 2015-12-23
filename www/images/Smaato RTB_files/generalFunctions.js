function initializeTimeZoneSelection(countryHTMLSelection, timeZoneHTMLSelection, retrieveTimeZonesFromCountryIdURL, preSelectedTimeZoneId) {    
	initializeTimeZoneSelectionAjaxCall(countryHTMLSelection, timeZoneHTMLSelection, retrieveTimeZonesFromCountryIdURL, preSelectedTimeZoneId);
	$(countryHTMLSelection).change(function() {
		initializeTimeZoneSelectionAjaxCall(countryHTMLSelection, timeZoneHTMLSelection, retrieveTimeZonesFromCountryIdURL);
    });
};

function initializeTimeZoneSelectionAjaxCall(countryHTMLSelection, timeZoneHTMLSelection, retrieveTimeZonesFromCountryIdURL, preSelectedTimeZoneId) {
	
	$.ajax({
        type: "GET",
        url: retrieveTimeZonesFromCountryIdURL,
        data: "countryId=" + $(countryHTMLSelection).find("option:selected").val(),
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function(countryList) {
            $(timeZoneHTMLSelection).children().remove();
        	$.each(countryList.country, function(timezone) {
        		var preSelected = "";
        		if(this.localeId == preSelectedTimeZoneId) {
        			preSelected = "selected";
        		}
        		$(timeZoneHTMLSelection).append('<option ' + preSelected + ' value="' + this.localeId + '">(GMT' + this.gmt + ') ' + this.displayValue + '</option>');
                });
        },
        error: function(XMLHttpRequest, textStatus) {
        	$(timeZoneHTMLSelection).children().remove();
        }
    });
	
}

function pingHost(ajaxUrl) {
	$.ajax({
		type: "GET",
		url: ajaxUrl,
		contentType: "application/json; charset=utf-8",
		dataType: "json",
		success: function(jsonObj) {
			$("#pingResult").empty();
			$.each(jsonObj.pingLines, function (index, pingLine) {
				$("#pingResult").append(pingLine + '<br />\n');
			});
			$("#pingHostBtn").removeAttr('disabled');
		},
		error: function(XMLHttpRequest, textStatus) {
			$("#pingResult").empty();
			$("#pingResult").append('Could not ping your host.');
		}
	});
}