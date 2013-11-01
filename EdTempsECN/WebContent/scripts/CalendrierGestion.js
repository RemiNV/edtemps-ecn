define([], function() {

	function CalendrierGestion(restManager) {
		this.restManager = restManager;
	};
	
	
	CalendrierGestion.prototype.listerMesCalendriers = function(callback) {
		var me = this;
		this.restManager.effectuerRequete("GET", "mescalendriers", { token: me.restManager.getToken() }, function(data) {
			callback(data.resultCode, data.data);
		});
	};

	
	return CalendrierGestion;
});