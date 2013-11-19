/**
 * @module UtilisateurGestion
 */
define(["RestManager"], function(RestManager) {
	
	/**
	 * @constructor
	 * @alias module:UtilisateurGestion
	 */
	var UtilisateurGestion = function(restManager) {
		this.restManager = restManager;
	};
	

	/**
	 * Récupère une liste des utilisateurs potentiellement propriétaires
	 * 
	 * @param callback Fonction appelée une fois la requête effectuée. Prend les arguments resultCode et proprietaires
	 */
	UtilisateurGestion.prototype.recupererProprietairesPotentiels = function(callback) {
		// Récupération de la liste des propriétaires potentiels
		this.restManager.effectuerRequete("POST", "proprietairespotentiels", {
			token: this.restManager.getToken()
		}, function(data) {
			if (data.resultCode == RestManager.resultCode_Success) {
				
				callback(data.resultCode, data.data.listeUtilisateurs);
			} else {
				callback(data.resultCode);
			}
		});
	};
	
	/**
	 * Récupère une liste des utilisateurs potentiellement propriétaires, au format autocomplete (objets avec attributs label, value)
	 * 
	 * @param callback Fonction appelée une fois la requête effectuée. Prend les arguments resultCode et proprietaires
	 */
	UtilisateurGestion.prototype.recupererProprietairesPotentielsAutocomplete = function(callback) {
		
		this.recupererProprietairesPotentiels(function(resultCode, utilisateurs) {
			
			if(resultCode == RestManager.resultCode_Success) {
				var res = new Array();
				for (var i=0, maxI=utilisateurs.length; i<maxI; i++) {
					var label_value = {
							label: utilisateurs[i].prenom + " " + utilisateurs[i].nom + " (" + (utilisateurs[i].email != null ? utilisateurs[i].email : "email inconnu") + ")",
							value: utilisateurs[i].id
					};
					
					res.push(label_value);
				}
				
				callback(resultCode, res);
			}
			else {
				callback(resultCode);
			}
		});	
	};
	
	return UtilisateurGestion;
});