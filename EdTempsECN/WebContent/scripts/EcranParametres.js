/**
 * Module d'affichage de la page de paramètres, avec ses différents onglets (abonnements, mes agendas, mes groupes...)
 * Associé au HTML templates/page_parametres.html
 * @module EcranParametres
 */
define(["RestManager", "GroupeGestion", "CalendrierGestion", "DialogCreationCalendrier", "DialogCreationGroupeParticipants", "DialogDetailGroupeParticipants", "DialogGererGroupeParticipants", "davis",
        "jqueryui", "jquerymultiselect", "jquery", "underscore"], function(RestManager, GroupeGestion, CalendrierGestion, DialogCreationCalendrier, 
        		DialogCreationGroupeParticipants, DialogDetailGroupeParticipants, DialogGererGroupeParticipants, Davis) {
	
	/**
	 * @constructor
	 * @alias module:EcranParametres
	 */
	var EcranParametres = function(restManager) {
		this.restManager = restManager;
 		this.groupeGestion = new GroupeGestion(this.restManager);
 		this.calendrierGestion = new CalendrierGestion(this.restManager);
 		this.dialogCreationCalendrier = new DialogCreationCalendrier(this.restManager, this, $("#form_creer_calendrier"));
 		this.dialogCreationGroupeParticipants = new DialogCreationGroupeParticipants(this.restManager, $("#form_creer_groupe"));
 		this.dialogDetailGroupeParticipants = new DialogDetailGroupeParticipants(this.restManager, $("#dialog_detail_groupe"));
 		this.dialogGererGroupeParticipants = new DialogGererGroupeParticipants(this.restManager, this, $("#dialog_gerer_groupe"));
 		//Variable contenant les calendriers dont l'utilisateur est propriétaire
 		this.listeCalendriers = new Object();
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
		
		this.initMesGroupes();
		
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
						+ gpe.nom ;
					// Information groupeUnique
					if (gpe.estCalendrierUnique) {
						html += " (Calendrier)";
					}						
					else {
						html += " (Groupe)";
					}
					html += '</option>';
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
					html += '>' + gpe.nom ;
					// Information GroupeUnique
					if (gpe.estCalendrierUnique) {
						html += " (Calendrier)";
					}						
					else {
						html += " (Groupe)";
					}
					html += '</option>';
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
							// Si aucune erreur, on remet à jour les abonnements indirects 
							// NB : le déplacement de l'élément d'une liste à l'autre est fait par la bibliothèque
							if(resultCode == RestManager.resultCode_Success) {
								// On supprime les infos bulles de tous les agendas disponibles
								$( '.ms-selection li' ).each(function() {
									$(this).removeClass("abonnement_indirect");
									$(this).removeAttr("title");
								});		
								// On ajoute les infos-bulles sur les groupes liés (parent ou fils) aux abonnements directs (= éléments <li> ayant la classe .ms-selectable, tout en étant affichés) 
								$( '.ms-selectable li' ).each(function() {
									if ( $(this).css("display")  != "none") {
										var idGpe = $(this).attr("id").replace("-selectable", "");
										var nomGpe = $(this).text();
										me.afficheAbonnementsIndirectes(idGpe, nomGpe);
									}
								});	
							}
							// En cas d'erreur, on affiche un message et replace l'élément sélectionné dans les abonnements de l'utilisateur
							else {
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
							// Si aucune erreur, on modifie seulement les abonnements indirects 
							// NB : le déplacement de l'élément d'une liste à l'autre est fait par la bibliothèque 
							if(resultCode == RestManager.resultCode_Success) {
								var nomGroupe = $("#" + idgroupe + "-selectable").text();
								me.afficheAbonnementsIndirectes(idgroupe, nomGroupe);	
						 	}
							// En cas d'erreur, on affiche un message et replace l'élément sélectionné dans les "Agendas disponibles"
							else {
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
		
		// Listener du bouton de creation d'un nouveau calendrier
		var me = this;
		$("#btn_creer_calendrier").click(function() {
			me.dialogCreationCalendrier.show(false);
		});

		// Affiche la liste des calendriers de l'utilisateur
		this.afficheListeMesCalendriers();
	};
	

	/**
	 * Initialisation de l'onglet "Mes groupes de participants"
	 */
	EcranParametres.prototype.initMesGroupes = function() {
		var me = this;

		// Listener pour le bouton d'ajout
		$("#btn_creer_groupe").click(function() {
			var dialog = me.dialogCreationGroupeParticipants;
			dialog.show("Créer un groupe de participants", "Créer", null, function() {
				dialog.ajouterGroupe(
					dialog.jqChampNom.val(),
					dialog.jqCreationGroupeForm.find("#form_creer_groupe_parent").val(),
					dialog.jqCreationGroupeForm.find("#form_creer_groupe_rattachement").is(':checked'),
					dialog.jqCreationGroupeForm.find("#form_creer_groupe_cours").is(':checked'),
					dialog.multiWidgetProprietaires.val(),
					function () {
						me.afficheListeMesGroupes();
					}
				);
			});
		});
		
		// Affiche liste des groupes de participants
		this.afficheListeMesGroupes();
		
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
	
	
	/**
	 * Met en valeur les demande de rattachement qui sont en attente de validation
	 */
	EcranParametres.prototype.miseEnValeurGroupesEnAttenteRattachement = function() {
		var me=this;

		// Cacher la bulle d'information
		$("#bulle_information").html("").hide();
		
		this.groupeGestion.queryGroupesEnAttenteRattachement(function(resultCode, data) {
			var nbGroupes = data.length;

			// Ajout des boutons "Gérer" et mise en surbrillance les lignes
			var dejaMisEnValeur = new Object();
			if (nbGroupes > 0) {
				for (var i=0; i<nbGroupes; i++) {
					if (!dejaMisEnValeur[data[i].parentIdTmp]) {
						$("#tbl_mes_groupes_ligne_"+data[i].parentIdTmp).addClass("tbl_mes_groupes_ligne_importante").attr("title", "Des demandes de rattachement sont en attente de validation pour ce groupe. Cliquez sur 'Gérer' pour les traiter.");
						$("#tbl_mes_groupes_ligne_"+data[i].parentIdTmp+" .tbl_mes_groupes_boutons").prepend("<input type='button' data-id='"+data[i].parentIdTmp+"' class='button tbl_mes_groupes_boutons_gerer' value='Gérer' />");
						dejaMisEnValeur[data[i].parentIdTmp] = true;
					}
				}
			}
			
			// Listeners pour les boutons gérer
			$(".tbl_mes_groupes_boutons_gerer").click(function() {
				var listeRattachementAttenteValidations = new Array();
				for (var i=0, maxI=data.length; i<maxI; i++) {
					if (data[i].parentIdTmp==$(this).attr("data-id")) {
						listeRattachementAttenteValidations.push(data[i]);
					}
				}
				me.dialogGererGroupeParticipants.show(listeRattachementAttenteValidations);
			});

		});

	};

	
	/**
	 * Affiche la liste des groupes dans l'onglet Mes groupes de participants
	 */
	EcranParametres.prototype.afficheListeMesGroupes = function() {
		var me=this;
		
		// Affichage d'un message de chargement
		$("#tbl_mes_groupes_chargement").css("display", "block");
		$("#tbl_mes_groupes_chargement_message").html("Récupération de mes groupes de participants ...");
		
		// Récupération des groupes de l'utilisateur
		this.groupeGestion.queryGroupesUtilisateurProprietaire(function (resultCode, data) {
			
			// Suppression du message de chargement
			$("#tbl_mes_groupes_chargement").css("display", "none");

			if(resultCode == RestManager.resultCode_Success) {

				if (data.listeGroupes.length>0) {
					// Ecriture du tableau dans la page
					var listMesGroupesTemplate = 
						"<% _.each(groupes, function(groupe) { %> <tr id='tbl_mes_groupes_ligne_<%= groupe.id %>' <% if(groupe.parentIdTmp>0) { %> class='tbl_mes_groupes_ligne_importante' title='En attente de validation pour le rattachement' <% } %>>" +
							"<td class='tbl_mes_groupes_groupe' data-id='<%= groupe.id %>'><%= groupe.nom %></td>" +
							"<td class='tbl_mes_groupes_boutons'>" +
								"<input type='button' data-id='<%= groupe.id %>' class='button tbl_mes_groupes_boutons_modifier' value='Modifier' />" +
								"<input type='button' class='button tbl_mes_groupes_boutons_supprimer' data-id='<%= groupe.id %>' value='Supprimer' />" +
							"</td>" +
						"</tr> <% }); %>";

					$("#tbl_mes_groupes").html(_.template(listMesGroupesTemplate, {groupes: data.listeGroupes}));
					
					// Listeners pour les lignes
					$(".tbl_mes_groupes_groupe").click(function() {
						me.dialogDetailGroupeParticipants.show($(this).attr("data-id"));
					});

					// Listeners pour les boutons Supprimer
					$(".tbl_mes_groupes_boutons_supprimer").click(function() {
						if(confirm("Etes-vous sur de vouloir supprimer le groupe '"+$(this).parents("tr").find(".tbl_mes_groupes_groupe").html()+"' ?")) {
							me.groupeGestion.querySupprimerGroupes($(this).attr("data-id"), function () {
								if (resultCode == RestManager.resultCode_Success) {
									window.showToast("Le groupe a été supprimé avec succès.");
									me.afficheListeMesGroupes();
								} else {
									window.showToast("La suppression du groupe a échoué ; vérifiez votre connexion.");
								}
							});
						}
					});
					
					// Listeners pour les boutons Modifier
					$(".tbl_mes_groupes_boutons_modifier").click(function() {
						// Récupération des informations complètes sur le groupe
						me.groupeGestion.queryGetGroupeComplet($(this).attr("data-id"), function(resultCode, data) {
							if (resultCode == RestManager.resultCode_Success) {
								var dialog = me.dialogCreationGroupeParticipants;
								dialog.show("Modifier un groupe de participants", "Modifier", data.groupe, function() {
									dialog.modifierGroupe(
										dialog.idGroupeModification,
										dialog.jqChampNom.val(),
										dialog.jqCreationGroupeForm.find("#form_creer_groupe_parent").val(),
										dialog.jqCreationGroupeForm.find("#form_creer_groupe_rattachement").is(':checked'),
										dialog.jqCreationGroupeForm.find("#form_creer_groupe_cours").is(':checked'),
										dialog.multiWidgetProprietaires.val(),
										function() {
											me.afficheListeMesGroupes();
										}
									);
								});
							} else {
								window.showToast("La récupération des informations sur le groupe a échoué ; vérifiez votre connexion.");
							}
						});
					});
					
					// Mise en valeur des groupes qui ont des demandes de rattachement en attente de validation
					me.miseEnValeurGroupesEnAttenteRattachement();

				} else {
					$("#tbl_mes_groupes").html("<tr><td>Vous n'avez aucun groupes de participants</td></tr>");
				}

		 	} else {
				// En cas d'erreur, on affiche un message
				window.showToast("La récupération des groupes a échoué ; vérifiez votre connexion.");
			}
		});

	};

	/**
	 * Affiche la liste des calendriers dans l'onglet "Mes calendriers"
	 */
	EcranParametres.prototype.afficheListeMesCalendriers = function() {
		var me=this;
		
		// Création du template pour la liste des calendriers
		var listMesCalendriersTemplate = 
			"<% _.each(calendriers, function(calendrier) { %> " +
			"<tr data-id='<%= calendrier.id %>'>" +
				"<td><%= calendrier.nom %></td>" +
				"<td><%= calendrier.matiere %></td>" +
				"<td><%= calendrier.type %></td>" +
				"<td class='tbl_mes_calendriers_boutons'>" +
					"<input type='button' data-id='<%= calendrier.id %>' class='button tbl_mes_calendriers_boutons_modifier' value='Modifier' />" +
					"<input type='button' class='button tbl_mes_calendriers_boutons_supprimer' data-id='<%= calendrier.id %>' value='Supprimer' />" +
				"</td>" +
			"</tr> <% }); %>";
		
		// Récupération des groupes de l'utilisateur
		me.calendrierGestion.queryCalendrierUtilisateurProprietaire(function (resultCode, data) {
			
			if(resultCode != RestManager.resultCode_Success) {
				window.showToast("La récupération des calendriers a échoué ; vérifiez votre connexion.");
			}
			else {
				if (data.listeCalendriers.length == 0) {
					$("#tbl_mes_calendriers").html("<tr><td>Vous n'avez aucun groupes de participants</td></tr>");
				}
				else {
					// Enregistrement de la liste des calendriers
					me.listeCalendriers = data.listeCalendriers;
					// Ecriture du tableau dans la page, en utilisant le template
					$("#tbl_mes_calendriers").html(_.template(listMesCalendriersTemplate, {calendriers: data.listeCalendriers}));
					
					// Listeners pour les boutons "modifier"
					$(".tbl_mes_calendriers_boutons_modifier").click(function() {
						var idCalendrierEnQuestion = $(this).parent().parent().attr("data-id");
						var calendrierAModifier = new Object();
						//recherche du calendrier dans la liste
						for (var i = 0, maxI=me.listeCalendriers.length ; i < maxI ; i++) {
							if (me.listeCalendriers[i].id == idCalendrierEnQuestion) {
								calendrierAModifier = me.listeCalendriers[i];
								break;
							}
						}
						//appel au dialog de creation/modification de calendrier
						me.dialogCreationCalendrier.show(true, calendrierAModifier);
					});
	
					// Listeners pour les boutons "supprimer"
					$(".tbl_mes_calendriers_boutons_supprimer").click(function() {
						if(confirm("Etes-vous sur de vouloir supprimer le calendrier '" + $(this).parent().siblings().first().text()+"' ?")) {
							me.calendrierGestion.supprimerCalendrier($(this).parent().parent().attr("data-id"), function () {
								if (resultCode == RestManager.resultCode_Success) {
									window.showToast("Le calendrier a été supprimé avec succès.");
									me.afficheListeMesCalendriers();
								} else {
									window.showToast("La suppression du calendrier a échoué ; vérifiez votre connexion.");
								}
							});
						}	
					});
	
				}
		 	} 
			
		});
	};

	
	return EcranParametres;
});
