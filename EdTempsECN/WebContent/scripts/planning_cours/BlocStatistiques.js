/**
 * Module d'affichage des statistiques dans la barre de gauche de l'écran d'accueil
 * @module BlocStatistiques
 */
define(["underscore", "text!../../templates/bloc_statistiques.tpl", "RestManager", "jquery"], function(_, templateBlocStatistiques, RestManager) {
	
	/**
	 * @constructor
	 * @alias BlocStatistiques 
	 */
	var BlocStatistiques = function(restManager, jqBloc) {
		this.restManager = restManager;
		this.jqBloc = jqBloc;
		this.groupes = null;
		
		/** Clé : type de cours, valeur : objet indexé par idGroupe, dont les valeurs sont "actuel" et "prevu".
		 * Ex : statistiques[typeCours][idGroupe].actuel, statistiques[typeCours][idGroupe].prevu */
		this.statistiques = {};
		this.template = _.template(templateBlocStatistiques);
	};
	
	/**
	 * Définit les groupes affichés dans les statistiques
	 * @param {Object} Mapping id => nom des groupes
	 */
	BlocStatistiques.prototype.setGroupes = function(groupes) {
		this.groupes = groupes;
	};
	
	BlocStatistiques.prototype.refreshStatistiques = function(matiere, dateDebut, dateFin) {
		var me = this;
		this.restManager.effectuerRequete("GET", "statistiques", { 
			matiere: matiere,
			debut: dateDebut.getTime(),
			fin: dateFin.getTime(),
			token: this.restManager.getToken()
		}, function(response) {
			if(response.resultCode == RestManager.resultCode_Success) {
				me.statistiques = response.data.stats;
				me.draw();
			}
			else if(response.resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur de mise à jour des statistiques ; vérifiez votre connexion");
			}
			else {
				window.showToast("Erreur de mise à jour des statistiques");
			}
		});
	};
	
	BlocStatistiques.prototype.draw = function() {
		var html = this.template({ statistiques: this.statistiques, groupes: this.groupes });
		this.jqBloc.find("#contenu_statistiques").html(html);
	};
	
	return BlocStatistiques;
});