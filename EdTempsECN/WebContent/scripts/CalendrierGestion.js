define([], function() {

	function CalendrierGestion(restManager) {
		this.restManager = restManager;
	};
	
	
	CalendrierGestion.prototype.listerMesCalendriers = function(callback) {
		this.restManager.effectuerRequete("GET", "mescalendriers", { token: this.restManager.getToken() }, function(data) {
			callback(data.resultCode, data.data);
		});
	};

	
	return CalendrierGestion;
});