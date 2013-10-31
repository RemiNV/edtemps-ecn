define(["RestManager"], function(RestManager) {

	var GroupeGestion = function(restManager) {
		this.restManager = restManager;
	};
	
	/**
	 * Récupérer les groupes auxquels est abonné / n'est pas abonné l'utilisateur
	 * @param callback
	 */
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
	
	/**
	 * Désabonner l'utilisateur du groupe dont l'id est en paramètre
	 * @param idgroupe
	 * @param callback
	 */
	GroupeGestion.prototype.seDesabonner = function(idgroupe, callback) {
		this.restManager.effectuerRequete("POST", "sedesabonner", {
			token : this.restManager.getToken(),
			idGroupe : idgroupe
		}, function(data) {
			callback(data.resultCode);
		});
	};
	
	/**
	 * Abonner l'utilisateur au groupe dont l'id est en paramètre
	 * @param idgroupe
	 * @param callback
	 */
	GroupeGestion.prototype.sAbonner = function(idgroupe, callback) {
		this.restManager.effectuerRequete("POST", "sabonner", {
			token: this.restManager.getToken(),
			idGroupe: idgroupe
		}, function(data) {
			callback(data.resultCode);
		});
	};
	
	return GroupeGestion;
});