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
	 * Transforme au format autocomplete une liste d'utilisateurs (fonction statique)
	 * @param {Utilisateur[]} utilisateurs Utilisateurs au format nom, prenom, email...
	 * @return {Object[]} Objet avec attributs label, value, title 
	 */
	UtilisateurGestion.makeUtilisateursAutocomplete = function(utilisateurs) {
		var res = new Array();
		for (var i=0, maxI=utilisateurs.length; i<maxI; i++) {
			res.push({
					label: utilisateurs[i].prenom + " " + utilisateurs[i].nom,
					value: utilisateurs[i].id,
					tooltip: utilisateurs[i].email,
					readOnly: utilisateurs[i].readOnly === true
			});
		}
		
		return res;
	};
	
	/**
	 * Récupère une liste des utilisateurs potentiellement propriétaires, au format autocomplete (objets avec attributs label, value)
	 * 
	 * @param callback Fonction appelée une fois la requête effectuée. Prend les arguments resultCode et proprietaires
	 */
	UtilisateurGestion.prototype.recupererProprietairesPotentielsAutocomplete = function(callback) {
		
		this.recupererProprietairesPotentiels(function(resultCode, utilisateurs) {
			
			if(resultCode == RestManager.resultCode_Success) {
				var res = UtilisateurGestion.makeUtilisateursAutocomplete(utilisateurs);
				
				callback(resultCode, res);
			}
			else {
				callback(resultCode);
			}
		});	
	};
	
	return UtilisateurGestion;
});