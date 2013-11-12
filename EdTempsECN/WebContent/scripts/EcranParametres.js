/**
 * Module d'affichage de la page de paramètres, avec ses différents onglets (abonnements, mes agendas, mes groupes...)
 * Associé au HTML templates/page_parametres.html
 * @module EcranParametres
 */
define(["RestManager", "GroupeGestion", "DialogCreationCalendrier", "DialogCreationGroupeParticipants", "lib/davis.min",
        "jqueryquicksearch", "jqueryui", "jquerymultiselect", "jquery"], function(RestManager, GroupeGestion, DialogCreationCalendrier, 
        		DialogCreationGroupeParticipants, Davis) {
	
	/**
	 * @constructor
	 * @alias module:EcranParametres
	 */
	var EcranParametres = function(restManager) {
		this.restManager = restManager;
 		this.groupeGestion = new GroupeGestion(this.restManager);
 		this.dialogCreationCalendrier = new DialogCreationCalendrier(this.restManager);
 		this.dialogCreationGroupeParticipants = new DialogCreationGroupeParticipants(this.restManager);
	};
	
	var idTabs = {
		"parametres/mes_abonnements": 0,
		"parametres/mes_agendas": 1,
		"parametres/mes_groupes": 2
	};
	
	/**
	 * Initialisation de l'écran
	 * 
	 * @param {string} tab Onglet à afficer : "mes_abonnements", "mes_agendas" ou "mes_groupes"
	 */
	EcranParametres.prototype.init = function(tab) {
		
		// Initialisaion de la navigation par tabs
		$("#tabs").tabs({
			activate: function(event, ui) {
				Davis.location.replace(ui.newPanel.get(0).id);
			},
			active: idTabs[tab]
		});
		
		// Initialisation des dialog
		$("#dialog_export").dialog({
			autoOpen: false,
			modal: true,
			width: 600
		});
		
		// Listeners
		$("#btn_parametres_retour").click(function() {
			Davis.location.assign("agenda");
		});
		
		this.initMesAbonnements();

		// A voir : est qu'on fait tout au démarrage de la page "Paramètres" ou lorsqu'on clique sur un onglet
		// -> tout au démarrage => 1 seul requete si on veut
		this.initMesCalendriers();
		
	};

	/**
	 * Initialisation de l'onglet "mes abonnements"
	 */
	EcranParametres.prototype.initMesAbonnements = function() {

		// Remplir les "abonnements" et "non abonnments" dans le MultiSelect
		this.groupeGestion.queryAbonnementsEtNonAbonnements(function(resultCode, data) {
			
			if(resultCode == RestManager.resultCode_Success) {
				
				// Variable recevant progressivement le code HTML à ajouter à l'élément MultiSelect
				var html = "";
				// Variable temporaire contenant le groupe en cours de lecture
				var gpe; 
				// Parcourt des groupes auxquels l'utilisateur n'est pas abonné
				for (var i = 0, maxI=data.groupesNonAbonnements.length ; i < maxI ; i++) {
					// Ajout du groupe dans "abonnements disponibles"
					gpe = data.groupesNonAbonnements[i];
					html += '<option value="' + gpe.id + '"'
						+ ' idparent="' + gpe.parentId + '"'
						+ ' selected="selected">' 
						+ gpe.nom + '</option>';
				}
				// Parcourt des groupes auxquels l'utilisateur est abonné 
				for (var i = 0, maxI=data.groupesAbonnements.length ; i < maxI ; i++) {
					// Ajout du groupe dans "mes abonnements"
					gpe = data.groupesAbonnements[i];
					html += '<option value="' + gpe.id + '"';
					// Si abonnementObligatoire, empecher le désabonnement
					if (gpe.abonnementObligatoire) {
						html += ' disabled="disabled"';
					}
					html += ' idparent="' + gpe.parentId + '"';
					html += '>' + gpe.nom + '</option>';
				}
			
				// Affichage	 
				$("#select-abonnements").html(html);
				
				// Paramètres de l'objet multiSelect
				$("#select-abonnements").multiSelect({
					selectableHeader: "<h3>Mes abonnements : </h3><input type='text' class='select-abonnements-filtre' autocomplete='off' placeholder='Filtrer...'>",
					selectionHeader: "<h3>Agendas disponibles : </h3><input type='text' class='select-abonnements-filtre' autocomplete='off' placeholder='Filtrer...'>",
					afterInit: function(ms){
						var me = this,
						$selectableSearch = me.$selectableUl.prev(),
						$selectionSearch = me.$selectionUl.prev(),
						selectableSearchString = '#'+me.$container.attr('id')+' .ms-elem-selectable:not(.ms-selected)',
						selectionSearchString = '#'+me.$container.attr('id')+' .ms-elem-selection.ms-selected';

						me.qs1 = $selectableSearch.quicksearch(selectableSearchString);
						me.qs2 = $selectionSearch.quicksearch(selectionSearchString);
				    },
					afterSelect: function(idgroupe){
						me.groupeGestion.seDesabonner(idgroupe, function(resultCode) {
							// En cas d'erreur, on affiche un message et replace l'élément sélectionné dans les abonnements de l'utilisateur
							if(resultCode != RestManager.resultCode_Success) {
								window.showToast("Le désabonnement a échoué ...");
								var idElementSelectable = "#" + idgroupe + "-selectable";
								var idElementSelection = "#" + idgroupe + "-selection";
								$(idElementSelection).css('display','none');
								$(idElementSelectable).css('display','list-item');
							}
						});
					},
					afterDeselect: function(idgroupe){
						 me.groupeGestion.sAbonner(idgroupe, function(resultCode) {
							// En cas d'erreur, on affiche un message et replace l'élément sélectionné dans les "Agendas disponibles"
							if(resultCode != RestManager.resultCode_Success) {
								window.showToast("L'abonnement a échoué ...");
								var idElementSelectable = "#" + idgroupe + "-selectable";
								var idElementSelection = "#" + idgroupe + "-selection";
								$(idElementSelectable).css('display','none');
								$(idElementSelection).css('display','list-item');
							}
						});
					}
				});	
				
				// Mise en forme des abonnements indirectes à ce groupe (=> parcours des abonnements directs)
				for (var i = 0, maxI=data.groupesAbonnements.length ; i < maxI ; i++) {
					me.afficheAbonnementsIndirectes(data.groupesAbonnements[i].id, data.groupesAbonnements[i].nom);
				}
			}
			
			else if(resultCode == RestManager.resultCode_NetworkError) {
				window.showToast("Erreur de chargement des groupes (auxquels vous êtes abonnés ou non) ; vérifiez votre connexion.");
			}

			else {
				window.showToast(resultCode + " Erreur de chargement des groupes (auxquels vous êtes abonnés ou non) ; votre session a peut-être expiré ?");
			}
		});
		
		// Ajout des listeners
		var me = this;
		$("#btn_export").click(function(event) {
			me.restManager.effectuerRequete("GET", "ical/token", { token: me.restManager.getToken() }, function(response) {
				
				if(response.resultCode == RestManager.resultCode_Success) {
					// Remplissage de la dialog d'export
					var urlIcal = document.location.origin + document.location.pathname + "ical/get?token=" + response.data.token;
					$("#dialog_export #txt_url_agenda_ical").html(urlIcal);
					$("#dialog_export #lien_export_gmail").attr("href", "http://www.google.com/calendar/render?cid=" + encodeURIComponent(urlIcal));
					
					var urlSansProtocole = urlIcal.substring(location.protocol.length);
					
					$("#dialog_export #lien_export_ical").attr("href", "webcal:" + urlSansProtocole);
					
					// Affichage de la dialog initialisée
					$("#dialog_export").dialog("open");
				}
				else if(response.resultCode == RestManager.resultCode_NetworkError) {
					window.showToast("Erreur de récupération de votre URL ICal : vérifiez votre connexion");
				}
				else {
					window.showToast("Erreur de récupération de votre URL ICal");
				}
				
			});
		});

	};
	
	/**
	 * Affiche l'onglet indiqué dans l'écran
	 * 
	 * @param {string} tab Onglet à afficher : "mes_abonnement", "mes_agendas" ou "mes_groupes" 
	 */
	EcranParametres.prototype.showTab = function(tab) {
		
		$("#tabs").tabs("option", "active", idTabs[tab]);
	};
	
	/**
	 * Initialisation de l'onglet "mes calendriers"
	 */
	EcranParametres.prototype.initMesCalendriers = function() {
		
		// Affichage des calendriers (utiliser template ?)
		
		// Listeners
		var me = this;
		$("#btn_creer_calendrier").click(function() {
			me.dialogCreationCalendrier.init();
		});

		$("#btn_creer_groupe").click(function() {
			me.dialogCreationGroupeParticipants.show();
		});

	};
	
	/**
	 * Permet d'ajouter l'info "abonné indirectement" aux parents et fils du groupe ayant l'id "id"
	 * Cette info est ajoutée sur les groupes de la liste "abonnements disponibles"
	 *
	 * @param : id
	 */
	EcranParametres.prototype.afficheAbonnementsIndirectes = function(id, nom) {
		this.afficheAbonnementsIndirectesFils(id, nom);
		this.afficheAbonnementsIndirectesParent(id, nom);
	};
	
	/**
	 * Permet d'ajouter l'info "abonné indirectement" aux parents du groupe ayant l'id "id"
	 * et le "nom nomGroupeAbonne"
	 * Cette info est ajoutée sur les groupes de la liste "abonnements disponibles"
	 * 
	 * Fonction récursive qui effectue l'opération sur le parent direct,
	 * puis fait appel à elle-même pour réitérer l'opération sur le parent.
	 * 
	 * @param : id = id du groupe pour lequel on va parcourir les parents 
	 * @param : nomGroupeAbonne = nom du groupe auquel on est abonné et duquel on déduit les abonnements indirectes (par les parents)
	 */
	EcranParametres.prototype.afficheAbonnementsIndirectesParent = function(id, nomGroupeAbonne) {
		//On cherche le parent (unique) de l'élément
		var idparent = $("#"+id+"-selection").attr("idparent");
		//Si l'élément a bien un parent
		if (idparent != "0") {
			var elementParent = $( "#" + idparent + "-selection");
			//On lui ajoute la classe "abonnement_indirect"
			elementParent.addClass("abonnement_indirect");
			//On lui ajoute (ou modifie) l'infobulle
			var infobulle = elementParent.attr("title");
			if (infobulle == undefined) {
				elementParent.attr("title", "Abonnement indirect via "+ nomGroupeAbonne);
			}	
			else {
				elementParent.attr("title", infobulle + " / " + nomGroupeAbonne);
			}
			//On réitère l'opération "afficheAbonnementsIndirectesParent" sur le parent
			this.afficheAbonnementsIndirectesParent(idparent, nomGroupeAbonne);
		}
	};
	
	/**
	 * Permet d'ajouter l'info "abonné indirectement" aux fils du groupe ayant l'id "id"
	 * et le nom "nomGroupeAbonne"
	 * Cette info est ajoutée sur les groupes de la liste "abonnements disponibles"
	 * 
	 * Fonction récursive qui effectue l'opération sur les fils directs,
	 * puis fait appel à elle-même pour réitérer l'opération sur chacun des fils.
	 * 
	 * @param : id = id du groupe pour lequel on va parcourir les fils
	 * @param : nomGroupeAbonne = nom du groupe auquel on est abonné et duquel on déduit les abonnements indirectes (par les fils)
	 */
	EcranParametres.prototype.afficheAbonnementsIndirectesFils = function(id, nomGroupeAbonne) {
		var me = this;
		//On parcourt les fils de l'élément
		if (id != 0) {
			$( '.ms-selection li[idparent="'+id+'"]' ).each(function() {
				//A chaque fils, on ajoute la classe "abonnementIndirect"
				$(this).addClass("abonnement_indirect");
				//On ajoute (ou modifie) l'info bulle du fils considéré
				var infobulle = $(this).attr("title");
				if (infobulle == undefined) {
					$(this).attr("title", "Abonnement indirect via "+ nomGroupeAbonne);
				}	
				else {
					$(this).attr("title", infobulle + " / " + nomGroupeAbonne);
				}
				//Pour chaque fils, on parcourt ses fils via un appel récursif
				var idfils = $(this).attr("id").replace("-selection", "");
				me.afficheAbonnementsIndirectesFils(idfils, nomGroupeAbonne);
			});
		}
	};
	
	return EcranParametres;
});