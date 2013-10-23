define(["RestManager"], function(RestManager) {

	var GroupeGestion = function(restManager) {
		this.restManager = restManager;
	};
	
	GroupeGestion.prototype.queryAbonnementsEtNonAbonnements = function(callback) {
		this.restManager.effectuerRequete("GET", "abonnementsetnonabonnements", {
			token: this.restManager.getToken()
		}, function(data) {
			if(data.resultCode == RestManager.resultCode_Success) {
				callback(data.resultCode, data.data);
			}
			else {
				callback(data.resultCode);
			}
		});
	};
	
	return GroupeGestion;
});