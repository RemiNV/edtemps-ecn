define(["RestManager", "lib/fullcalendar.translated.min"], function(RestManager) {

	function Calendrier(eventsSource, ajoutEvenement) {
		var me = this;

		this.jqCalendar = $("#calendar");
		// Initialisation du calendrier
		this.jqCalendar.fullCalendar({
			weekNumbers: true,
			weekNumberTitle: "Sem.",
			firstDay: 1,
			editable: true,
			defaultView: "agendaWeek",
			timeFormat: "HH'h'(mm)",
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
			height: Math.max(window.innerHeight - 110, 500),
			windowResize: function(view) {
				me.jqCalendar.fullCalendar("option", "height", Math.max(window.innerHeight - 110, 500));
			},
			events: eventsSource,
			dayClick: function(date, allDay, jsEvent, view) {
				
				// Durée d'1h par défaut
				var dateFin = new Date(date.getTime() + 1000 * 3600);
				
				ajoutEvenement.show(date, dateFin, null);
			}
		});
		
		// Ajout des listeners d'évènements aux dropdown de filtres
		var updateFiltres = function() { me.refetchEvents(); };
		$("#dropdown_filtre_matiere").change(updateFiltres);
		$("#dropdown_filtre_type").change(updateFiltres);
		$("#dropdown_filtre_responsable").change(updateFiltres);
	};
	
	

	/**
	 * Rafraichit le calendrier
	 */
	Calendrier.prototype.refetchEvents = function() {
		this.jqCalendar.fullCalendar("refetchEvents");
	};
	
	/**
	 * Remplit une liste déroulante avec les clés/valeurs de l'objet fourni
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
	 * @param evenements
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
				
				if(matiere == filtreMatiere) {
					okMatiere = true;
				}
			}
			
			var okType = false;
			for(var j=0, maxJ = evenements[i].types.length; j<maxJ; j++) {
				var type = evenements[i].types[j];
				types[type] = type;
				
				if(type == filtreType) {
					okType = true;
				}
			}
			
			var okRespo = false;
			for(var j=0, maxJ = evenements[i].responsables.length; j<maxJ; j++) {
				var respo = evenements[i].responsables[j];
				responsables[respo.id] = respo.prenom + " " + respo.nom;
				
				if(respo.id == filtreRespo) {
					okRespo = true;
				}
			}
			
			// Filtrage de l'évènement
			if((!filtreMatiere || okMatiere) && (!filtreType || okType) && (!filtreRespo || okRespo)) {
				res.push(evenements[i]);
			}
		}
		
		// Remplissage des listes déroulantes
		remplirDropdown(jqFiltreMatiere, matieres);
		remplirDropdown(jqFiltreType, types);
		remplirDropdown(jqFiltreRespo, responsables);
		
		return res;
	};

	return Calendrier;
});