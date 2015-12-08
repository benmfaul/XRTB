
function PrivateRTB(params) {
	this.div = params.div;
    this.exchangeid = params.exchangeid;
    this.campaignid = params.campaign;
    this.url = params.url;
    this.nobid = params.nobid;
    this.video = params.video;
    this.ua = navigator.userAgent;
    this.lat = 0;
    this.lon = 0;
    this.platform = navigator.platform;
}

PrivateRTB.prototype.perform = function() {
	var div = this.div;
	var cmd = {};
	cmd.ua = this.ua;
	cmd.location = this.location;
	cmd.accountNumber  = this.exchangeid;
	cmd.campaign = this.campaignid;
	cmd.platform = this.platform;;
	if (typeof navigator.connection != 'undefined') {
		cmd.connectionType = navigator.connection.type;
	 	cmd.maxDownLink = navigator.connection.downlinkMax;;
	}
	else {
		cmd.connectionType = 'unk';
		cmd.maxDownLink = -1.0;
	}
	var url = this.url;
    var video = this.video;
	var self = this;
    if (typeof navigator.geolocation != 'undefined') {
        navigator.geolocation.getCurrentPosition(function(position) {
        	cmd.lat = position.coords.latitude;
        	cmd.lon = position.coords.longitude;
        	self.doAjax(url,cmd, div, video);
        });
    } else	
		self.doAjax(url,cmd,div, video);
}

PrivateRTB.prototype.doAjax = function(url,cmd,div, video) {
    $.ajax({
         type: 'POST',
         url: url,
         data: JSON.stringify(cmd),
         success: function(data, textStatus, request){
           if (request.status == 204) {
            if (typeof this.nobid != 'undefined')
           		this.nobid();
           	return;
           } else {
           	text = request.responseText;
           	console.log("TEXT: " + text);
           	if (typeof video === 'undefined')
          		div.innerHTML = text;
          	else {
          		data = unescape(data);
          		video(data);
          	}
          }
         },
         error: function (request, textStatus, errorThrown) {
           alert("Error: " + request.responseText);
      }});
}



