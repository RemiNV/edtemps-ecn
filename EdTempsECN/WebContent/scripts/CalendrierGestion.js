/**
 * Module de gestion/récupération des calendriers
 * @module CalendrierGestion
 */
define([], function() {

	/**
	 * @constructor
	 * @alias module:CalendrierGestion
	 */
	var CalendrierGestion = function(restManager) {
		this.restManager = restManager;
	};
	
	/**
	 * @typedef {Object} Calendrier
	 * 
	 * @property {number} id ID du calendrier
	 * @property {string} nom Nom du calendrier
	 * @property {string} type Type du calendrier (TD, TP...)
	 * @property {string} matiere Matière du calendrier
	 * @property {number[]} proprietaires IDs des propriétaires du calendrier 
	 * @property {boolean} estCours VRAI si le calendrier est lié à au moins un groupe qui est un cours 
	 */
	
	
	/**
	 * Lister les calendriers appartenant à l'utilisateur
	 * 
	 * @param {function} callback Fonction appelée une fois les calendriers listés
	 */
	CalendrierGestion.prototype.listerMesCalendriers = function(callback) {
		var me = this;
		this.restManager.effectuerRequete("GET", "mescalendriers", {
			token: me.restManager.getToken()
		}, function(data) {
			callback(data.resultCode, data.data);
		});
	};

	/**
	 * Création d'un calendrier
	 * 
	 * @param {string} nom Nom du calendrier
	 * @param {string} matiere Matière rattachée
	 * @param {string} type Type des cours liés
	 * @param {number[]} idProprietaires Liste des identifiants des propriétaires
	 * @param {number[]} idGroupesParents Liste des groupes parents
	 * @param {function} callback Fonction appelée une fois la requête de création effectuée (paramètres resultCode)
	 */
	CalendrierGestion.prototype.creerCalendrier = function(nom_arg, matiere_arg, type_arg, idProprietaires_arg, idGroupesParents_arg, callback) {
		var me = this;
		this.restManager.effectuerRequete("POST", "calendrier/creation", {
			  token: me.restManager.getToken(),
			  matiere: matiere_arg,
			  nom: nom_arg,
			  type: type_arg, 
			  idProprietaires: idProprietaires_arg,
			  idGroupesParents: idGroupesParents_arg
		}, function(data) {
			callback(data.resultCode);
		});
	};

	/**
	 * Modifier un calendrier
	 * 
	 * @param {number} id Identifiant du calendrier
	 * @param {string} nom Nom du calendrier
	 * @param {string} matiere Matière rattachée
	 * @param {string} type Type des cours liés
	 * @param {number[]} idProprietaires Liste des identifiants des propriétaires
	 * @param {number[]} idGroupesParents Liste des groupes parents
	 * @param {function} callback Fonction appelée une fois la requête de modification effectuée (paramètres resultCode)
	 */
	CalendrierGestion.prototype.modifierCalendrier = function(id_arg, nom_arg, matiere_arg, type_arg, idProprietaires_arg, idGroupesParents_arg, callback) {
		var me = this;
		this.restManager.effectuerRequete("POST", "calendrier/modification", { 
			  token: me.restManager.getToken(),
			  id: id_arg,
			  matiere: matiere_arg,
			  nom: nom_arg,
			  type: type_arg, 
			  idProprietaires: idProprietaires_arg,
			  idGroupesParents: idGroupesParents_arg
		}, function (data) {
			callback(data.resultCode);
		});
	};
	
	
	/**
	 * Récupérer les calendriers dont l'utilisateur est propriétaire
	 * 
	 * @param {function} callback Fonction appelée une fois la requête effectuée (paramètres resultCode et data)
	 */
	CalendrierGestion.prototype.queryCalendrierUtilisateurProprietaire = function(callback) {
		var me = this;
		this.restManager.effectuerRequete("GET", "calendrier", { 
			  token: me.restManager.getToken()
		}, function(data) {
			callback(data.resultCode, data.data);
		});
	};
	
	
	/**
	 * Supprimer un calendrier
	 * 
	 * @param {number} idCalendrier Identifiant du calendrier à supprimer
	 * @param {function} callback Fonction appelée une fois la requête de suppression effectuée (paramètres resultCode)
	 */
	CalendrierGestion.prototype.supprimerCalendrier = function(idCalendrier, callback) {
		var me = this;
		this.restManager.effectuerRequete("POST", "calendrier/suppression",	{
			  token: me.restManager.getToken(), id: idCalendrier
		}, function (data){
			callback(data.resultCode);
		});
	};
	

	/**
	 * Ne plus être propriétaire du calendrier
	 * 
	 * @param {number} idCalendrier Identifiant du calendrier
	 * @param {function} callback Fonction appelée une fois la requête effectuée (paramètres resultCode)
	 */
	CalendrierGestion.prototype.queryNePlusEtreProprietaire = function(idCalendrier, callback) {
		this.restManager.effectuerRequete("POST", "calendrier/nePlusEtreProprietaire", {
			token: this.restManager.getToken(), idCalendrier: idCalendrier
		}, function(data) {
			callback(data.resultCode);
		});
	};
	
	
	return CalendrierGestion;
});