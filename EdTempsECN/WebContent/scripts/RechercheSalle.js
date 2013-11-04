define([ "RestManager", "jquerymaskedinput", "jqueryui", "jquerymultiselect", "jqueryquicksearch" ], function(RestManager) {

	/**
	 * Constructeur
	 */
	function RechercheSalle(restManager, jqRechercheSalle) {
		this.restManager = restManager;
		this.jqRechercheSalleForm = jqRechercheSalle.find("#form_chercher_salle");
		this.jqRechercheSalleResultat = jqRechercheSalle.find("#resultat_chercher_salle");
		
		this.initAppele = false;
		this.initDialogResultatAppele = false;
		this.idSallesSelectionnees = null;
		this.sallesDisponibles = null;
		this.callbackSelectionSalles = null;
		
		// Variable qui permettent d'accéder facilement aux différents champs du formulaire
		this.jqDate = this.jqRechercheSalleForm.find("#form_recherche_salle_date");
		this.jqHeureDebut = this.jqRechercheSalleForm.find("#form_recherche_salle_debut");
		this.jqHeureFin = this.jqRechercheSalleForm.find("#form_recherche_salle_fin");
		this.jqCapacite = this.jqRechercheSalleForm.find("#form_recherche_salle_capacite");		
	};
	
	/**
	 * Affiche la boîte de dialogue de recherche d'une salle libre
	 */
	RechercheSalle.prototype.show = function() {
		if(!this.initAppele) {
			this.init();
			this.initAppele = true;
		}
		
		this.jqRechercheSalleForm.dialog("open");
	};

	/**
	 * Initialise la boîte de dialogue de recherche d'une salle libre
	 * Doit être appelé uniquement une fois.
	 * Est automatiquement appelé par show() si nécessaire.
	 */
	RechercheSalle.prototype.init = function() {
		var me = this;

		// Ajout des masques aux différents champs
		this.jqHeureDebut.mask("99:99");
		this.jqHeureFin.mask("99:99");
		this.jqCapacite.mask("9?999");
		this.jqDate.mask("99/99/9999");

		// Affectation d'une méthode au clic sur le bouton "Rechercher"
		this.jqRechercheSalleForm.find("#form_chercher_salle_valid").click(function() {
			// Si le formulaire est valide, la requête est effectuée
			if (me.validationFormulaire()) {

				// Traitement des dates et heures au format "yyyy-MM-ddTHH:mm:ss" (ISO8601)
				var strJour = $.datepicker.formatDate("yy-mm-dd", $.datepicker.parseDate("dd/mm/yy", me.jqDate.val()));
				
				var param_dateDebut = strJour + "T" + me.jqHeureDebut.val() + ":00";
				var param_dateFin = strJour + "T" + me.jqHeureFin.val() + ":00";
				
				var dateDebut = new Date(param_dateDebut);
				var dateFin = new Date(param_dateFin);

				// Création de la liste des matériels nécessaires
				var listeMateriel = me.getContenuListeMateriel(me.jqRechercheSalleForm.find("#form_chercher_salle_liste_materiel table"));
				
				// Message d'attente
				me.jqRechercheSalleForm.find("#form_chercher_salle_valid").attr("disabled", "disabled");
				me.jqRechercheSalleForm.find("#form_chercher_salle_chargement").css("display", "block");
				me.jqRechercheSalleForm.find("#form_chercher_salle_message_chargement").html("Recherche...");

				// Appel de la méthode de recherche de salle
				me.getSalle(dateDebut, dateFin, me.jqCapacite.val(), listeMateriel, function() {
					// Supression message d'attente une fois la recherche effectuée (mais l'utilisateur n'a rien sélectionné)
					me.jqRechercheSalleForm.find("#form_chercher_salle_valid").removeAttr("disabled");
					me.jqRechercheSalleForm.find("#form_chercher_salle_chargement").css("display", "none");
				}, 
				function(data) { alert(data.length + " salles sélectionnées - Action à déterminer"); });

			}
		});

		// Affectation d'une méthode au clic sur le bouton "Fermer"
		this.jqRechercheSalleForm.find("#form_chercher_salle_fermer").click(function() {
			me.jqRechercheSalleForm.dialog("close");
		});

		// Affectation d'une méthode au clic sur la zone Date
		this.jqDate.click(function() {
			me.jqDate.datepicker("show");
		});
		
		// Affectation d'une méthode au clique sur les différents champs
		this.jqRechercheSalleForm.find("#form_recherche_salle_date, #form_recherche_salle_debut, #form_recherche_salle_fin, #form_recherche_salle_capacite").click(function() {
			me.bordureSurChamp($(this), null);
		});

        // Ajout du datepicker sur le champ date
        this.jqDate.datepicker({
                showAnim : 'slideDown',
                showOn: 'button', // "both" pourrait être utilisé mais le datepicker serait ouvert à l'ouverture de la dialog (focus sur le champ)
                buttonText: "Calendrier",
                dateFormat: "dd/mm/yy",
                buttonImage: "img/datepicker.png", // image pour le bouton d'affichage du calendrier
                buttonImageOnly: true, // affiche l'image sans bouton
                monthNamesShort: [ "Jan", "Fév", "Mar", "Avr", "Mai", "Jui", "Jui", "Aou", "Sep", "Oct", "Nov", "Dec" ],
                monthNames: [ "Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre" ],
                dayNamesMin: [ "Di", "Lu", "Ma", "Me", "Je", "Ve", "Sa" ],
                dayNames: [ "Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi" ],
                gotoCurrent: true,
                prevText: "Précédent",
                nextText: "Suivant",
                constrainInput: true,
                firstDay: 1
        });
        
        // Ecrit la liste des matériels disponibles
		
		// Blocage du bouton de validation avant le chargement des matériels
		this.jqRechercheSalleForm.find("#form_chercher_salle_valid").attr("disabled", "disabled");
		this.jqRechercheSalleForm.find("#form_chercher_salle_chargement").css("display", "block");
		this.jqRechercheSalleForm.find("#form_chercher_salle_message_chargement").html("Chargement des options de matériel...");
		
		var me = this;
		this.ecritListeMateriel(this.jqRechercheSalleForm.find("#form_chercher_salle_liste_materiel table"), function(success, nbMateriels) {
			if(success) {
				// Reactivation du bouton de recherche
				me.jqRechercheSalleForm.find("#form_chercher_salle_valid").removeAttr("disabled");
				me.jqRechercheSalleForm.find("#form_chercher_salle_chargement").css("display", "none");
				
				// S'il n'y a pas de produits en base de données, on cache le tableau dans le formulaire
				if(nbMateriels === 0) {
					me.jqRechercheSalleForm.find("#form_chercher_salle_liste_materiel").hide();
				}
			}
		});
        
		// Affiche la boîte dialogue de recherche d'une salle libre
		this.jqRechercheSalleForm.dialog({
			autoOpen: false,
			width: 440,
			modal: true,
			show: {
				effect: "fade",
				duration: 200
			},
			hide: {
				effect: "explode",
				duration: 200
			}
		});

		this.initAppele = true;
	};

	/**
	 * Méthode qui vérifie que le formulaire est correct
	 * 
	 * @return VRAI si le formulaire est valide et FAUX sinon
	 */
	RechercheSalle.prototype.validationFormulaire = function() {
		var valid = true;

		// Validation de la date
		var dateValid = true;
		try {
			$.datepicker.parseDate("dd/mm/yy", this.jqDate.val());
		}
		catch(parsingException) {
			dateValid = false;
		}
		
		if (!dateValid || this.jqDate.val() === "") {
			this.bordureSurChamp(this.jqDate, "#FF0000");
			valid = false;
		} else {
			this.bordureSurChamp(this.jqDate, "#60C003");
		}

		// Validation de l'heure de début
		var decoupageHeureMinute = this.jqHeureDebut.val().split(":");
		var calculMinutesDebut = 60*decoupageHeureMinute[0] + decoupageHeureMinute[1];
		if (this.jqHeureDebut.val().length==0 || decoupageHeureMinute[0]>23 || isNaN(decoupageHeureMinute[0]) || decoupageHeureMinute[1]>59 || isNaN(decoupageHeureMinute[1])) {
			this.bordureSurChamp(this.jqHeureDebut, "#FF0000");
			valid = false;
		} else {
			this.bordureSurChamp(this.jqHeureDebut, "#60C003");
		}
		
		// Validation de l'heure de fin
		decoupageHeureMinute = this.jqHeureFin.val().split(":");
		var calculMinutesFin = 60*decoupageHeureMinute[0] + decoupageHeureMinute[1];
		if (this.jqHeureFin.val().length==0 || decoupageHeureMinute[0]>23 || isNaN(decoupageHeureMinute[0]) || decoupageHeureMinute[1]>59 || isNaN(decoupageHeureMinute[1]) || calculMinutesFin-calculMinutesDebut<=0) {
			this.bordureSurChamp(this.jqHeureFin, "#FF0000");
			valid = false;
		} else {
			this.bordureSurChamp(this.jqHeureFin, "#60C003");
		}

		// Validation de la capacité
		if (isNaN(this.jqCapacite.val()) || this.jqCapacite.val()>9999 || this.jqCapacite.val()<0) {
			this.bordureSurChamp(this.jqCapacite, "#FF0000");
			valid = false;
		} else {
			this.bordureSurChamp(this.jqCapacite, "#60C003");
		}
		
		// Validation des quantités de matériel
		var me = this;
		this.jqRechercheSalleForm.find(".quantite input[type=number]").each(function() {
			if (isNaN($(this).val()) || $(this).val()<0 || $(this).val()>9999 || $(this).val()=="" ) {
				me.bordureSurChamp($(this), "#FF0000");
				valid = false;
			} else {
				me.bordureSurChamp($(this), "#60C003");
			}
		});

		// Message d'erreur pour un problème d'ordre des heures
		if (calculMinutesFin-calculMinutesDebut<=0) {
			window.showToast("L'heure de fin doit être supérieure à l'heure de début");
		}
		
		// Message d'erreur général pour les champs incorrects
		if (!valid) {
			window.showToast("Veuillez vérifier et corriger les champs entourés en rouge");
		}
		
		return valid;

	};


	/**
	 * Ecrit la liste des matériels disponibles sélectionnables dans un tableau fourni.
	 * Si aucun matériel n'est sélectionnable, le tableau sera masqué.
	 * En cas d'échec, l'affichage d'un messae d'erreur est déjà géré dans la fonction : inutile de le faire.
	 * 
	 * @param jqTableMateriel Tableau à populer avec les matériels
	 * @param callback Fonction à rappeler une fois les matériels chargés et remplis.
	 * 	Callback prend un booléen en argument indiquant le succès de la procédure, et un entier indiquant le nombre de matériels chargés
	 */
	RechercheSalle.prototype.ecritListeMateriel = function(jqTableMateriel, callback) {
		var me = this;
		
		// Récupération de la liste des matériels en base de données
		this.restManager.effectuerRequete("GET", "listemateriels", {
			token: this.restManager.getToken()
		}, function(data) {
			if (data.resultCode == RestManager.resultCode_Success) {
				
				var maxI = data.data.listeMateriels.length;
				
				if (maxI!=0) {

					// Préparation du code html
					var str = "<tr><th class='libelle'>Libellé de l'équipement</th><th class='quantite'>Quantité</th></tr>";
					for (var i = 0 ; i < maxI ; i++) {
						str += "<tr>";
						str += "<td class='libelle'>" + data.data.listeMateriels[i].nom + "</td>";
						str += "<td class='quantite'><input type='number' class='input_small' materiel-id='" + data.data.listeMateriels[i].id + "' value='0' /></td>";
						str += "</tr>";
					}
					
					// Ajout du code html dans la liste de matériels
					jqTableMateriel.append(str);
					jqTableMateriel.addClass("liste_materiel");
			
					// Ajout des masques sur les quantités de matériel
					jqTableMateriel.find(".quantite input[type=number]").each(function() {
						$(this).mask("?9999", { placeholder: "" });
						$(this).click(function() {
							me.bordureSurChamp($(this), "#FFFFFF");
						});
					});

				}
				
				callback(true, maxI);

			} else if (data.resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur de récupération des matériels disponibles ; vérifiez votre connexion.");
				callback(false);
			} else {
				window.showToast(data.resultCode + " Erreur de récupération des matériels disponibles ; votre session a peut-être expiré ?");
				callback(false);
			}
		});

	};
	
	/**
	 * Récupération des matériels rentrés dans un tableau de matériel écrit par ecritListeMateriel()
	 * @param jqTableMateriel Tableau à examiner
	 * @returns {Array} Tableau des matériels trouvés, avec chacun pour attributs "id" et "quantite"
	 */
	RechercheSalle.prototype.getContenuListeMateriel = function(jqTableMateriel) {
		var listeMateriel = new Array();
		jqTableMateriel.find(".quantite input[type=number]").each(function() {
			var materiel = new Object();
			materiel.id=$(this).attr("materiel-id");
			materiel.quantite=$(this).val();
			listeMateriel.push(materiel);
		});
		
		return listeMateriel;
	};
	
	/**
	 * Méthode qui effectue la requête
	 * 
	 * @param dateDebut
	 * 		date de début de l'événement (objet Date javascript)
	 * 
	 * @param dateFin
	 * 		date de fin de l'événement (objet Date javascript)
	 * 
	 * @param effectif
	 * 		effectif requis pour l'événement
	 * 
	 * @param materiels
	 * 		liste du matériel nécessaire : une liste d'objets qui possèdent deux attributs : id et quantité
	 * 
	 * @param callbackChargement
	 * 		méthode appelée une fois la recherche effectuée, mais que l'utilisateur n'a pas encore sélectionné de salle.
	 * 		Prend un booléen en paramète indiquant le succès de la requête. Si elle a échoué, aucune salle ne pourra être fournie
	 * 		par le paramètre suivant "callback". Un message d'erreur est déjà affiché dans cette méthode en cas d'erreur.
	 * 
	 * @param callback
	 * 		méthode appellée en retour et qui recevra les salles sélectionnées en paramètre
	 */
	RechercheSalle.prototype.getSalle = function(dateDebut, dateFin, effectif, materiels, callbackChargement, callback) {
		var me = this;

		// Création d'une chaine de caractère pour traiter la liste de matériel
		// La syntaxe choisie est :
		//    - pour chaque matériel, il y a son identifiant suivi de la quantité, séparés par ":"
		//    - les matériels sont séparés les uns des autres par ","
		var listeMaterielQuantite = "";
		for (var i=0, maxI=materiels.length; i<maxI; i++) {
			if (listeMaterielQuantite!="") {
				listeMaterielQuantite += ",";
			}
			listeMaterielQuantite += materiels[i].id + ":" + materiels[i].quantite;
		}
		
		// Récupération de la liste des matériels en base de données
		this.restManager.effectuerRequete("GET", "recherchesallelibre", {
			debut: dateDebut.getTime(), fin: dateFin.getTime(), effectif: effectif, materiel: listeMaterielQuantite, token: this.restManager.getToken()
		}, function(response) {
			if (response.resultCode == RestManager.resultCode_Success) {
				callbackChargement(true);
				me.afficherResultat(response.data, callback);
			} else if (response.resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur lors de la recheche d'une salle libre ; vérifiez votre connexion.");
				callbackChargement(false);
			} else {
				window.showToast(response.resultCode + " Erreur lors de la recheche d'une salle libre ; votre session a peut-être expiré ?");
				callbackChargement(false);
			}
		});
		
	};
	
	RechercheSalle.prototype.initDialogResultat = function() {
		
		var me = this;
		
		// Affectation d'une méthode au clic sur le bouton "Fermer"
		this.jqRechercheSalleResultat.find("#resultat_chercher_salle_fermer").click(function() {
			me.jqRechercheSalleResultat.dialog("close");
		});

		// Affectation d'une méthode au clic sur le bouton "Créer un événement"
		this.jqRechercheSalleResultat.find("#resultat_chercher_salle_creer").click(function() {
			// Récupération des salles sélectionnées
			var sallesSelectionnees = new Array();
			for (var i = 0, maxI = me.sallesDisponibles.length; i < maxI; i++) {
				// Salle en cours de traitement
				var salle = me.sallesDisponibles[i];
				if (me.idSallesSelectionnees[salle.id]) {
					sallesSelectionnees.push(salle);
				}
			}
			// Appelle la méthode de callback avec les salles sélectionnées en paramètres
			me.callbackSelectionSalles(sallesSelectionnees);
			me.jqRechercheSalleResultat.dialog("close");
		});
		
		// Paramètres de l'objet multiSelect
		this.jqRechercheSalleResultat.find("#resultat_chercher_salle_select").multiSelect({
			selectableHeader: "Salles disponibles :<input type='text' class='resultat_chercher_salle_select-filtre' autocomplete='off' placeholder='Filtrer...' />",
			selectionHeader: "Salles sélectionnées :<input type='text' class='resultat_chercher_salle_select-filtre' autocomplete='off' placeholder='Filtrer...' />",
			afterInit: function(ms){
				var me = this,
				$selectableSearch = me.$selectableUl.prev(),
				$selectionSearch = me.$selectionUl.prev(),
				selectableSearchString = '#'+me.$container.attr('id')+' .ms-elem-selectable:not(.ms-selected)',
				selectionSearchString = '#'+me.$container.attr('id')+' .ms-elem-selection.ms-selected';

				me.qs1 = $selectableSearch.quicksearch(selectableSearchString);
				me.qs2 = $selectionSearch.quicksearch(selectionSearchString);
		    },
			afterSelect: function(idSalle) {
				me.idSallesSelectionnees[idSalle]=true;
			},
			afterDeselect: function(idSalle) {
				me.idSallesSelectionnees[idSalle]=false;
			}
		});

		// Affichage de la boîte de dialogue résultat
		this.jqRechercheSalleResultat.dialog({
			autoOpen: false,
			width: 400,
			modal: true,
			show: { effect: "fade", duration: 200 },
			hide: { effect: "explode", duration: 200 }
		});
		
		this.initDialogResultatAppele = true;
	};
	
	
	/**
	 * Méthode qui affiche le résultat
	 * 
	 * @param data
	 * 			liste des salles retournées par le serveur
	 * 
	 * @param callback
	 * 		méthode appellée en retour et qui recevra les salles sélectionnées en paramètre
	 */
	RechercheSalle.prototype.afficherResultat = function(data, callback) {
		
		if(!this.initDialogResultatAppele) {
			this.initDialogResultat();
		}
		
		// Stockage pour utilisation lors du clic sur le bouton "ajouter un évènement" (callback initialisé dans initDialogResultat)
		this.sallesDisponibles = data.sallesDisponibles;
		this.callbackSelectionSalles = callback;
		
		// Calcule le nombre de salles
		var maxI = data.sallesDisponibles.length;
		
		// S'il y a des salles, on les affiche dans une boîte de dialogue
		if (maxI>0) {
			
			// Variable recevant progressivement le code HTML à ajouter à l'élément MultiSelect
			var html = "";
			// Parcourt les salles 
			for (var i = 0; i < maxI; i++) {
				// Salle en cours de traitement
				var salle = data.sallesDisponibles[i];
				// Préparation de l'infobulle
				var infobulle = "Capacité: "+salle.capacite;
				for (var j=0, maxJ=salle.materiels.length ; j<maxJ ; j++) {
					infobulle += "&#13;";
					infobulle += salle.materiels[j].nom + ": " +salle.materiels[j].quantite; 
				}
				html += "<option value='"+salle.id+"' title='"+infobulle+"'>"+salle.nom+"</option>";
			}
			// Affichage
			this.jqRechercheSalleResultat.find("#resultat_chercher_salle_select").html(html)
				.multiSelect("refresh");

			// Remise à zéro d'une liste d'identifiants de salles sélectionnées
			// C'est un objet référencé par l'identifiant de la salle et qui porte true si la salle est sélectionnée
			this.idSallesSelectionnees = new Object();

			// Affichage de la dialog
			this.jqRechercheSalleResultat.dialog("open");
			
		} else {
			window.showToast("Aucune salle disponible avec ces critères");
		}

	};

	
	/**
	 * Met une bordure de couleur autour de l'élément
	 * 
	 * @param champ
	 * 			champ à entourer
	 * @param color
	 * 			couleur de la bordure, si elle vaut NULL la bordure est enlevée
	 */
	RechercheSalle.prototype.bordureSurChamp = function(champ, color) {
		if (color == null) {
			$(champ).css("box-shadow", "none");
			$(champ).css("border", "1px solid black");
		} else {
			$(champ).css("box-shadow", color+" 0 0 10px");
			$(champ).css("border", "1px solid "+color);
		}
	};

	return RechercheSalle;

});
