/**
 * Module de gestion de la dialog de détails d'un événement
 * @module DialogDetailsEvenement
 */
define(["RestManager", "text!../templates/dialog_details_evenement.tpl", "jquery"], function(RestManager, tplDialogDetailsEvenement) {

	/**
	 * @constructor
	 * @alias module:DialogDetailsEvenement
	 */
	var DialogDetailsEvenement = function(jqDialog, evenementGestion, dialogAjoutEvenement, dialogRepeter, refetchCallback) {
		
		this.jqDialog = jqDialog;
		this.evenement = null; // Mémorise l'événement pour lequel la dialog dialogDetailsEvenement est ouverte
		this.template = _.template(tplDialogDetailsEvenement);
		
		var closeDialogDetailsCallback = function(event) {
			if(!jqDialog.dialog("isOpen")) {
				return;
			}
			
			// On n'est pas à l'intérieur d'une dialog
			var jqTarget = $(event.target);
			if(!jqTarget.is(".ui_dialog, .fc-event, .evenement_groupe") 
					&& jqTarget.closest(".ui-dialog").length == 0
					&& jqTarget.closest(".fc-event").length == 0
					&& jqTarget.closest(".evenement_groupe").length == 0) {
				jqDialog.dialog("close");
				return false;
			}
		};
		
		jqDialog.dialog({
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
		
		var me = this;
		jqDialog.find("#btnModifierEvenement").click(function() {
			jqDialog.dialog("close");
			dialogAjoutEvenement.showEdit(me.evenement);
		});
		
		jqDialog.find("#btnSupprimerEvenement").click(function() {
			
			confirm("Etes-vous sûr(e) de vouloir supprimer l'événement : " + me.evenement.title + " ?", function() {
				evenementGestion.supprimerEvenement(me.evenement, function(resultCode) {
					if(resultCode == RestManager.resultCode_Success) {
						jqDialog.dialog("close");
						refetchCallback();
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
		
		// Bouton présent dans le HTML selon la page
		jqDialog.find("#btnRepeterEvenement").click(function(e) {
			dialogRepeter.show(me.evenement);
		});
		
	};
	
	DialogDetailsEvenement.prototype.show = function(event, jqSource) {
		this.evenement = event;
		
		this.jqDialog.dialog("widget").find(".ui-dialog-titlebar")
			.css("background-color", event.color);
		
		// Remplissage du template
		this.jqDialog.find("#dialog_details_evenement_hook").html(this.template({
			strDateDebut: $.fullCalendar.formatDate(event.start, "dd/MM/yyyy hh:mm"),
			strDateFin: $.fullCalendar.formatDate(event.end, "dd/MM/yyyy hh:mm"),
			strSalles: event.strSalle,
			proprietaires: event.responsables,
			intervenants: event.intervenants,
			editable: event.editable
		}));
		
		if(event.editable) {
			this.jqDialog.find(".boutons_valider").css("display", "block");
		}
		else {
			this.jqDialog.find(".boutons_valider").css("display", "none");
		}
		
		// Positionnement de la dialog
		this.jqDialog.dialog("option", {
			position: {
				my: "center bottom",
				at: "top-10",
				of: jqSource
			},
			title: event.title
		});
		
		this.jqDialog.dialog("open");
	};
	
	DialogDetailsEvenement.prototype.isOpen = function() {
		return this.jqDialog.dialog("isOpen");
	};
	
	return DialogDetailsEvenement;
	
});