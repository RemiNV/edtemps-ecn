/**
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
	 * @property {number} id - ID du calendrier
	 * @property {string} nom - Nom du calendrier
	 * @property {string} type - Type du calendrier (TD, TP...)
	 * @property {string} matiere - Matière du calendrier
	 * @property {number[]} proprietaires - IDs des propriétaires du calendrier 
	 * @property {boolean} estCours - VRAI si le calendrier est lié à au moins un groupe qui est un cours 
	 */
	
	/**
	 * @callback listerCalendriersCallback
	 * @param {ResultCode} resultCode Code de retour de la requête
	 * @param {Calendrier[]} calendriers Calendriers renvoyés par la requête, en cas de succès
	 */
	
	/**
	 * Listing des calendriers appartenant à l'utilisateur
	 * @param {listerCalendriersCallback} callback Fonction rappelée une fois les calendriers listés 
	 */
	CalendrierGestion.prototype.listerMesCalendriers = function(callback) {
		var me = this;
		this.restManager.effectuerRequete("GET", "mescalendriers", { token: me.restManager.getToken() }, function(data) {
			callback(data.resultCode, data.data);
		});
	};

	/**
	 * Création d'un calendrier ayant les informations fournies en paramètre
	 * 
	 * @param nom : String
	 * @param matiere : String
	 * @param type : String
	 * @param idProprietaires : liste d'ID 
	 * @param idGroupesParents : liste d'ID
	 * @param callback : Fonction appelée une fois la requete de création effectuée
	 *
	 */
	CalendrierGestion.prototype.creerCalendrier = function(nom_arg, matiere_arg, type_arg, idProprietaires_arg, idGroupesParents_arg, callback) {
		var me = this;
		this.restManager.effectuerRequete(
			"POST",
			"calendrier/creation", 
			{ 
			  token: me.restManager.getToken(),
			  matiere: matiere_arg,
			  nom: nom_arg,
			  type: type_arg, 
			  idProprietaires: idProprietaires_arg,
			  idGroupesParents: idGroupesParents_arg
			},
			function(data){callback(data.resultCode);}
		);
	};

	/**
	 * Modification d'un calendrier avec les informations fournies en paramètre
	 * 
	 * @param id : int
	 * @param nom : String
	 * @param matiere : String
	 * @param type : String
	 * @param idProprietaires : liste d'ID
	 * @param idGroupesParents : liste d'ID
	 * @param callback : Fonction appelée une fois la requete de création effectuée
	 *
	 */
	CalendrierGestion.prototype.modifierCalendrier = function(id_arg, nom_arg, matiere_arg, type_arg, idProprietaires_arg, idGroupesParents_arg, callback) {
		var me = this;
		this.restManager.effectuerRequete(
			"POST",
			"calendrier/modification", 
			{ 
			  token: me.restManager.getToken(),
			  id: id_arg,
			  matiere: matiere_arg,
			  nom: nom_arg,
			  type: type_arg, 
			  idProprietaires: idProprietaires_arg,
			  idGroupesParents: idGroupesParents_arg
			},
			function(data){callback(data.resultCode);}
		);
	};
	
	
	/**
	 * Récupérer les calendriers dont l'utilisateur est propriétaire
	 * 
	 * @param callback : Fonction appelée une fois la requete de création effectuée
	 *
	 */
	CalendrierGestion.prototype.queryCalendrierUtilisateurProprietaire = function(callback) {
		var me = this;
		this.restManager.effectuerRequete(
			"GET",
			"calendrier", 
			{ 
			  token: me.restManager.getToken(),
			},
			function(data){callback(data.resultCode, data.data);}
		);
	};
	
	
	/**
	 * Supprimer un calendrier, via son ID en paramètre
	 * 
	 * @param id : ID du calendrier à supprimer
	 * @param callback : Fonction appelée une fois la requete de création effectuée
	 *
	 */
	CalendrierGestion.prototype.supprimerCalendrier = function(id, callback) {
		var me = this;
		this.restManager.effectuerRequete(
			"POST",
			"calendrier/suppression", 
			{ 
			  token: me.restManager.getToken(),
			  id: id
			},
			function(data){callback(data.resultCode);}
		);
	};
	

	/**
	 * Ne plus être propriétaire du calendrier dont l'id est en paramètre
	 * @param groupeId
	 * @param callback
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