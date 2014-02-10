/**
 * Module de gestion de l'interface du calendrier fullcalendar
 * @module Calendrier
 */
define(["RestManager", "text!../templates/dialog_details_evenement.tpl", "underscore", "lib/fullcalendar.translated.min", "jqueryui"], function(RestManager, tplDialogDetailsEvenement, _) {

	/**
	 * @constructor
	 * @alias module:Calendrier
	 */
	var Calendrier = function(eventsSource, dialogAjoutEvenement, evenementGestion, jqDialogDetailsEvenement, jqDatepicker, dialogRepeter) {
		var me = this;
		this.dialogRepeter = dialogRepeter;
		
		// Dialog de détails des événements
		var templateDialogDetails = _.template(tplDialogDetailsEvenement);
		
		var closeDialogDetailsCallback = function(event) {
			if(!jqDialogDetailsEvenement.dialog("isOpen")) {
				return;
			}
			
			// On n'est pas à l'intérieur d'une dialog
			var jqTarget = $(event.target);
			if(!jqTarget.is(".ui_dialog, .fc-event") 
					&& jqTarget.closest(".ui-dialog").length == 0
					&& jqTarget.closest(".fc-event").length == 0) {
				jqDialogDetailsEvenement.dialog("close");
				return false;
			}
		};
		
		jqDialogDetailsEvenement.dialog({
			autoOpen: false,
			appendTo: "#dialog_hook",
			draggable: false,
			width: 500,
			open: function(){
				$(document).bind("click", closeDialogDetailsCallback);
			},
			close: function() {
				$(document).unbind("click", closeDialogDetailsCallback);
			}
		});
		
		jqDialogDetailsEvenement.dialog("widget").find(".ui-dialog-titlebar").addClass("dialog_details_evenement_header");
		
		// Mémorise l'événement pour lequel la dialog dialogDetailsEvenement est ouverte
		var evenementDialogDetailsOuverte = null;
		
		jqDialogDetailsEvenement.find("#btnModifierEvenement").click(function() {
			jqDialogDetailsEvenement.dialog("close");
			dialogAjoutEvenement.showEdit(evenementDialogDetailsOuverte);
		});
		
		jqDialogDetailsEvenement.find("#btnSupprimerEvenement").click(function() {
			
			confirm("Etes-vous sûr(e) de vouloir supprimer l'événement : " + evenementDialogDetailsOuverte.title + " ?", function() {
				evenementGestion.supprimerEvenement(evenementDialogDetailsOuverte, function(resultCode) {
					if(resultCode == RestManager.resultCode_Success) {
						jqDialogDetailsEvenement.dialog("close");
						me.refetchEvents();
					}
					else if(resultCode == RestManager.resultCode_NetworkError) {
						window.showToast("Echec de la suppression de cet événement : vérifiez votre connexion");
					}
					else {
						window.showToast("Echec de la suppression de cet événement");
					}
				});
			}, null);

		});	
		
		jqDialogDetailsEvenement.find("#btnRepeterEvenement").click(function(e) {
			dialogRepeter.show(evenementDialogDetailsOuverte);
		});
		
		// Mémorise les anciennes dates des évènements lors du drag&drop, resize
		var oldDatesDrag = Object();

		this.jqCalendar = $("#calendar");
		
		var posCalendrier = this.jqCalendar.offset();
		
		// Initialisation du calendrier
		this.jqCalendar.fullCalendar({
			weekNumbers: true,
			weekNumberTitle: "Sem.",
			firstDay: 1,
			editable: true,
			defaultView: "agendaWeek",
			timeFormat: "HH'h'(mm){ - HH'h'(mm)}",
			axisFormat: "HH'h'(mm)",
			titleFormat: {
				month: 'MMMM yyyy',                             // Septembre 2013
				week: "d [ MMM] [ yyyy] '&ndash;' {d MMM yyyy}", // 7 - 13 Sep 2013
				day: 'dddd d MMM yyyy'                  // Mardi 8 Sep 2013
			},
			columnFormat: {
				month: 'ddd',    // Lun
				week: 'ddd d/M', // Lun 7/9
				day: 'dddd d/M'  // Lundi 7/9
			},
			header: {
				right: '',
				center: 'title',
				left: 'prev,next today month,agendaWeek,agendaDay'
			},
			height: Math.max(window.innerHeight - posCalendrier.top - 10, 500),
			windowResize: function(view) {
				posCalendrier = me.jqCalendar.offset();
				me.jqCalendar.fullCalendar("option", "height", Math.max(window.innerHeight - posCalendrier.top - 10, 500));
			},
			events: eventsSource,
			dayClick: function(date, allDay, jsEvent, view) {
				
				// La dialog de détails se fermera juste après ce listener (clic en-dehors)
				if(jqDialogDetailsEvenement.dialog("isOpen")) {
					return;
				}
				
				// Durée d'1h par défaut
				var dateFin = new Date(date.getTime() + 1000 * 3600);
				
				dialogAjoutEvenement.show(date, dateFin, null);
			},
			eventRender: function(event, jqElement) {
				if(event.loading) {
					jqElement.append("<img src='img/spinner_chargement_outer_small.gif' class='spinner_evenement_loading' alt='Enregistrement...' />");
				}
				
				jqElement.click(function() {
					
					evenementDialogDetailsOuverte = event;
					
					jqDialogDetailsEvenement.dialog("widget").find(".ui-dialog-titlebar")
						.css("color", event.color);
					
					// Remplissage du template
					jqDialogDetailsEvenement.find("#dialog_details_evenement_hook").html(templateDialogDetails({
						strDateDebut: $.fullCalendar.formatDate(event.start, "dd/MM/yyyy hh:mm"),
						strDateFin: $.fullCalendar.formatDate(event.end, "dd/MM/yyyy hh:mm"),
						strSalles: event.strSalle,
						proprietaires: event.responsables,
						intervenants: event.intervenants,
						editable: event.editable
					}));
					
					if(event.editable) {
						jqDialogDetailsEvenement.find(".boutons_valider").css("display", "block");
					}
					else {
						jqDialogDetailsEvenement.find(".boutons_valider").css("display", "none");
					}
					
					// Positionnement de la dialog
					jqDialogDetailsEvenement.dialog("option", {
						position: {
							my: "center bottom",
							at: "top-10",
							of: jqElement
						},
						title: event.title
					});
					
					jqDialogDetailsEvenement.dialog("open");
				});
			},
			eventDragStop: function(event, jsEvent, ui, view) {
				if(!event.pendingUpdates) { // L'évènement est synchronisé avec le serveur
					
					// Copie profonde des dates (fullCalendar les modifie)
					oldDatesDrag[event.id] = { start: new Date(event.start.getTime()), end: new Date(event.end.getTime()) };
				}
			},
			eventResizeStop: function(event, jsEvent, ui, view) {
				if(!event.pendingUpdates) {
					oldDatesDrag[event.id] = { start: event.start, end: event.end };
				}
			},
			eventDrop: function(event, dayDelta, minuteDelta, allDay, revertFunc, jsEvent, ui, view) {
				// Evènements "toute la journée" non supportés
				if(allDay) {
					revertFunc();
				}
				else {			
					updateDatesEvenement(event, evenementGestion, revertFunc, me.jqCalendar, oldDatesDrag[event.id].start, oldDatesDrag[event.id].end);
				}
			},
			eventResize: function(event, dayDelta, minuteDelta, revertFunc) {
				updateDatesEvenement(event, evenementGestion, revertFunc, me.jqCalendar, oldDatesDrag[event.id].start, oldDatesDrag[event.id].end);
			},
			viewRender: function() {
				if(jqDatepicker) {
					var date = me.getDate();
					jqDatepicker.DatePickerSetDate(date, date);
				}
			}
			
		});
		
		// Ajout des listeners d'évènements aux dropdown de filtres
		var updateFiltres = function() { me.refetchEvents(); };
		$("#dropdown_filtre_matiere").change(updateFiltres);
		$("#dropdown_filtre_type").change(updateFiltres);
		$("#dropdown_filtre_responsable").change(updateFiltres);
	};
	
	/**
	 * Mise à jour des dates d'un évènement auprès de la base de données.
	 * Retarde toutes les mises à jour de 1.5sec et n'exécute que la dernière à l'expiration du délai, 
	 * même si l'utilisateur effectue plusieurs modifications
	 * 
	 * @param {Object} event Nouvel évènement à enregistrer
	 * @param {module:EvenementGestion} evenementGestion Gestionnaire d'évènements JS
	 * @param {function} revertFunc Fonction à appeler pour invalider la modification (en cas d'erreur)
	 * @param {jQuery} jqCalendar Objet jQuery de fullCalendar
	 * @param {Date} oldStart Ancienne date de début de l'évènement
	 * @param {Date} oldEnd Ancienne date de fin de l'évènement
	 */
	function updateDatesEvenement(event, evenementGestion, revertFunc, jqCalendar, oldStart, oldEnd) {
		
		event.loading = true;
		if(event.pendingUpdates) {
			event.pendingUpdates++;
		}
		else {
			event.pendingUpdates = 1;
		}
		
		// Attente que l'utilisateur fasse d'autres modifications (éventuellement) avant d'envoyer au serveur
		setTimeout(function() {
			event.pendingUpdates--;
			
			if(event.pendingUpdates == 0) {
				
				evenementGestion.modifierEvenement(event.id, function(resultCode) {
					
					if(resultCode == RestManager.resultCode_Success) {
						// Invalidation du cache aux anciennes dates de l'évènement
						evenementGestion.invalidateCache(oldStart, oldEnd);
					}
					else if(resultCode == RestManager.resultCode_NetworkError) {
						window.showToast("Erreur de mise à jour de l'évènement ; vérifiez votre connexion");
						revertFunc();
					}
					else if(resultCode == RestManager.resultCode_SalleOccupee) {
						window.showToast("Erreur de mise à jour de l'évènement : salle(s) occupée(s) pendant ce créneau");
						revertFunc();
					}
					else {
						window.showToast("Erreur de mise à jour de l'évènement ; code retour " + resultCode);
						revertFunc();
					}
					
					// Suppression de l'indicateur de chargement
					event.loading = false;
					jqCalendar.fullCalendar("updateEvent", event);
					
				}, event.start, event.end);
			}
		}, 1000);
	}
	
	/**
	 * Déplace la vue à la date fournie. Voir la documentation de fullCalendar pour gotoDate.
	 * @param {Date} date Date vers laquelle déplacer la vue
	 */
	Calendrier.prototype.gotoDate = function(date) {
		this.jqCalendar.fullCalendar("gotoDate", date);
	};
	
	/**
	 * Récupère la date en cours d'affichage dans le calendrier. voir la documentation de fullCalendar pour getDate
	 */
	Calendrier.prototype.getDate = function() {
		return this.jqCalendar.fullCalendar("getDate");
	};
	

	/**
	 * Rafraichit le calendrier
	 */
	Calendrier.prototype.refetchEvents = function() {
		this.jqCalendar.fullCalendar("refetchEvents");
	};
	
	/**
	 * Remplit une liste déroulante avec les clés/valeurs de l'objet fourni
	 * @param {jQuery} jqDropdown Objet jquery de liste déroulante
	 * @param {Object} objValues Objet contenant les clés/valeurs
	 */
	var remplirDropdown = function(jqDropdown, objValues) {
		var currentValue = jqDropdown.val();
		
		jqDropdown.find("option").remove();
		// Ajout d'un élément sans filtre
		jqDropdown.append("<option value=''>---</option>");
		var found = false;
		for(var key in objValues) {
			if(key == currentValue) {
				found = true;
				jqDropdown.append("<option selected='selected' value='" + key + "'>" + objValues[key] + "</option>");
			}
			else {
				jqDropdown.append("<option value='" + key + "'>" + objValues[key] + "</option>");
			}
		}
		
		if(!found) {
			jqDropdown.val('');
		}
	};

	
	/**
	 * Filtre les évènements en fonction des filtres sélectionnés,
	 * et remplit les listes déroulantes des filtres en fonction des évènements à afficher
	 * @param {Object} evenements Objet contenant les événements à filtrer
	 */
	Calendrier.prototype.filtrerMatiereTypeRespo = function(evenements) {
		// Récupération des filtres sélectionnés
		var jqFiltreMatiere = $("#dropdown_filtre_matiere");
		var jqFiltreType = $("#dropdown_filtre_type");
		var jqFiltreRespo = $("#dropdown_filtre_responsable");
		
		var filtreMatiere = jqFiltreMatiere.val();
		var filtreType = jqFiltreType.val();
		var filtreRespo = jqFiltreRespo.val();
		
		var matieres = Object(); // Clés et valeurs : nom de la matière
		var types = Object(); // Clés et valeurs : type
		var responsables = Object(); // Clé : id, valeur : prénom nom

		var res = new Array();
		
		for(var i=0, maxI = evenements.length; i<maxI; i++) {
			var okMatiere = false;
			for(var j=0, maxJ = evenements[i].matieres.length; j<maxJ; j++) {
				var matiere = evenements[i].matieres[j];
				matieres[matiere] = matiere;
				
				if(matiere === filtreMatiere) {
					okMatiere = true;
				}
			}
			
			var okType = false;
			for(var j=0, maxJ = evenements[i].types.length; j<maxJ; j++) {
				var type = evenements[i].types[j];
				types[type] = type;
				
				if(type === filtreType) {
					okType = true;
				}
			}
			
			var okRespo = false;
			for(var j=0, maxJ = evenements[i].responsables.length; j<maxJ; j++) {
				var respo = evenements[i].responsables[j];
				responsables[respo.id] = respo.prenom + " " + respo.nom;
				
				if(respo.id === parseInt(filtreRespo)) {
					okRespo = true;
				}
			}
			
			// Filtrage de l'évènement
			if((!filtreMatiere || okMatiere) && (!filtreType || okType) && (!filtreRespo || okRespo)) {
				res.push(evenements[i]);
			}
		}
		
		// Ajout des options déjà sélectionnés dans les dropdown
		if(filtreMatiere) {
			matieres[filtreMatiere] = filtreMatiere;
		}
		if(filtreType) {
			types[filtreType] = filtreType;
		}
		if(filtreRespo) {
			responsables[filtreRespo] = jqFiltreRespo.children("option:selected").text();
		}
		
		// Remplissage des listes déroulantes
		remplirDropdown(jqFiltreMatiere, matieres);
		remplirDropdown(jqFiltreType, types);
		remplirDropdown(jqFiltreRespo, responsables);
		
		return res;
	};

	return Calendrier;
});