define(["CalendrierGestion", "RestManager", "jquery", "jqueryui", "jquerymaskedinput"], function(CalendrierGestion, RestManager) {
	
	function AjoutEvenement(restManager, jqDialog, rechercheSalle) {
		
		this.jqDialog = jqDialog;
		this.restManager = restManager;
		this.strOptionsCalendriers = null; // HTML à ajouter au select pour sélectionner les calendriers
		this.rechercheSalle = rechercheSalle;
		this.sallesSelectionnees = new Array();
		
		// Initialisation de la dialog
		jqDialog.dialog({
			autoOpen: false,
			modal: true,
			width: 600,
			show: { effect: "fade", duration: 200 },
			hide: { effect: "explode", duration: 200 }
		});
		
		// Initialisation des champs
		jqDialog.find("#heure_debut").mask("99:99");
		jqDialog.find("#heure_fin").mask("99:99");
		
		// Listeners
		var me = this;
		jqDialog.find("#btn_ajout_evenement").click(function() {
			me.lancerRechercheSalle();
		});
	};
	
	AjoutEvenement.prototype.lancerRechercheSalle = function() {
		
	};
	
	/**
	 * Vérifie qu'un contrôle est non vide, et le marque comme valide
	 * si c'est le cas, sinon le marque comme non valide
	 * @return valeur du champ du contrôle
	 */
	function validateNotEmpty(jqControl) {
		var val = jqControl.val(); 
		if(val == "") {
			jqControl.addClass("invalide").removeClass("valide");
		}
		else {
			jqControl.addClass("valide").removeClass("invalide");
		}
		
		return val;
	} 
	
	/**
	 * Vérifie les données du formulaire et les renvoie
	 * Format de l'objet renvoyé : contient les attributs : 
	 * - valide : booléen, true si le formulaire est complet
	 * - valideRechercheSalle: booléen, true si les informations nécessaires à la recherche
	 * 		d'une salle sont présentes
	 * - nom
	 * - proprietaires : non implémenté pour l'instant (null)
	 * - calendriers : tableau d'ID de calendriers
	 * - dateDebut : date JavaScript
	 * - dateFin : date JavaScript
	 * - salles : tableau d'ID de salles
	 */
	AjoutEvenement.prototype.getDonneesFormulaire = function() {
		var res = {
			valide: true,
			valideRechercheSalle: true
		};
		
		var jqNom = this.jqDialog.find("#txt_nom_evenement");
		var jqDate = this.jqDialog.find("#date_evenement");
		var jqHeureDebut = this.jqDialog.find("#heure_debut");
		var jqHeureFin = this.jqDialog.find("#heure_fin");
		
		
		res.nom = validateNotEmpty(jqNom);
		
		// Récupération de la liste des propriétaires à implémenter
		res.proprietaires = null;
		
		// Récupération de la liste des calendriers
		res.calendriers = new Array();
		
		this.jqDialog.find("#calendriers_evenement .select_calendriers").each(function() {
			res.calendriers.push(parseInt($(this).val()));
		});
		
		var strDate = validateNotEmpty(jqDate);
		
		var heureDebut = validateNotEmpty(jqHeureDebut);
		var heureFin = validateNotEmpty(jqHeureFin);
		
		
		if(strDate !== "" && heureDebut !== "") {
			res.dateDebut = new Date(strDate + " " + heureDebut);
		}
		else {
			res.dateDebut = null;
		}
		
		if(strDate !== "" && heureFin !== "") {
			res.dateFin = new Date(strDate + " " + heureFin);
		}
		else {
			res.dateFin = null;
		}
		
		res.salles = this.sallesSelectionnees;
		
		// TODO : ajout du matériel
		if(res.dateDebut != null && res.dateFin != null) {
			
		}
	};
	
	/**
	 * Remplissage de la sélection des calendriers dans la boîte de dialogue d'ajout d'évènement
	 * @param callback Fonction rappelée une fois les calendriers remplis, avec un booléen en argument indiquant le succès de l'opération et le nombre de calendriers trouvés
	 */
	AjoutEvenement.prototype.remplirCalendriers = function(callback) {
		var calendrierGestion = new CalendrierGestion(this.restManager);
		
		var me = this;
		
		calendrierGestion.listerMesCalendriers(function(resultCode, data) {
			if(resultCode === RestManager.resultCode_Success) {
				
				if(data.length > 0) {
					var strRemplissageSelect = "";
						
					for(var i=0, max=data.length; i<max; i++) {
						strRemplissageSelect += "<option value='" + data[i].id + "'>" + data[i].nom + "</option>\n";
					}
					
					me.strOptionsCalendriers = strRemplissageSelect;
					me.jqDialog.find("#calendriers_evenement .select_calendriers").html(strRemplissageSelect);
				}
				
				callback(true, data.length);
			}
			else if(resultCode === RestManager.resultCode_NetworkError) {
				window.showToast("Erreur de récupération de vos calendriers ; vérifiez votre connexion");
				callback(false);
			}
			else {
				window.showToast("Erreur de récupération de vos calendriers");
				callback(false);
			}
		});
	};
	
	
	/**
	 * Affichage de la boîte de dialogue d'ajout d'évènement
	 */
	AjoutEvenement.prototype.show = function() {
		
		this.jqDialog.dialog("open");
		
		// Récupération des calendriers auxquels l'utilisateur peut ajouter des évènements
		this.jqDialog.find("#btn_valider_ajout_evenement").attr("disabled", "disabled");
		this.jqDialog.find("#dialog_ajout_evenement_chargement").css("display", "block");
		this.jqDialog.find("#dialog_ajout_evenement_message_chargement").html("Chargement des options de matériel...");
		
		// Les 2 remplissages s'effectueront dans un ordre "aléatoire" (asynchrone)
		var succesChargementGlobal = true;
		
		// Remplissage du tableau de sélection des matières
		this.rechercheSalle.ecritListeMateriel(this.jqDialog.find("#tbl_materiel"), function(success, nbMateriels) {
			succesChargementGlobal = succesChargementGlobal && success;
			enable();
		});
		
		// Remplissage des calendriers disponibles
		this.remplirCalendriers(function(success, nbCalendriers) {
			succesChargementGlobal = succesChargementGlobal && success && nbCalendriers > 0;
			if(nbCalendriers == 0) {
				window.showToast("Vous n'êtes propriétaire d'aucun calendrier dans lequel vous pouvez ajouter des évènements");
			}
			
			enable();
		});
		
		// Réactivation des contrôles une fois tout chargé
		var aCharger = 2;
		var me = this;
		function enable() {
			aCharger--;
			
			if(aCharger != 0) {
				return;
			}
			
			if(succesChargementGlobal) {
				me.jqDialog.find("#btn_valider_ajout_evenement").removeAttr("disabled");
			}
			
			me.jqDialog.find("#dialog_ajout_evenement_chargement").css("display", "none");
		}
	};
	
	return AjoutEvenement;
});