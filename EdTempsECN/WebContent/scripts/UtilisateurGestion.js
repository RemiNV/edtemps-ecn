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
							label: utilisateurs[i].prenom + " " + utilisateurs[i].nom,
							value: utilisateurs[i].id,
							tooltip: utilisateurs[i].email
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
	
	
	/**
	 * Vérifie si un utilisateur a le droit d'effectuer une action
	 * Cette méthode ne fait pas de requête au serveur. Elle va chercher l'information dans le RestManager
	 * qui a récupéré la liste des actions que l'utilisateur peut faire, au moment de sa connexion
	 * 
	 * @param codeAction Action à vérifier
	 * @return VRAI ou FAUX en fonction des droits de l'utilisateur
	 */
	UtilisateurGestion.prototype.aDroit = function(codeAction) {
		if (jQuery.inArray(codeAction, this.restManager._listeActionsAutorisees)>0) {
			return true;
		} else {
			return false;
		}
	};
	
	
	return UtilisateurGestion;
});