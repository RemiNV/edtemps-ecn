
/**
 * @module MultiWidget
 */
define([ "jquery", "jqueryui" ], function() {
	
	/**
	 * Widget de formulaire duplicable sur plusieurs lignes en cliquant sur un bouton.
	 * Voir les propriétés statiques AUTOCOMPLETE_INIT et AUTOCOMPLETE_VAL pour initialiser un widget autocomplete.
	 *
	 * @param jqControl Objet jQuery à utiliser comme contrôle duplicable
	 * @param getValFunction Fonction permettant de récupérer les valeurs d'un contrôle. Prend en paramètres le contrôle et renvoie sa valeur, ou null pour ne rien renvoyer dans le tableau de résultats
	 * @param initControl Fonction d'initialisation de chaque contrôle, à l'ajout ou l'initialisation de la première ligne
	 * @param forceFirstValue Valeur à assigner au premier contrôle de la liste, qui sera désactivé ; objet avec les attributs label et value
	 */
	var MultiWidget = function(jqControl, getValFunction, initControl, forceFirstValue) {
		
		var me = this;
		
		this.getValFunction = getValFunction;
		this.initControl = initControl;
		
		// Wrapping du contrôle dans une div globale
		jqControl.addClass("multiwidget_entry").wrap("<div class='multiwidget'></div>");
		this.jqDiv = jqControl.parent();
		
		// Wrapping de chaque ligne
		jqControl.wrap("<div class='multiwidget_line'></div>");
		
		// Création de l'élément à ajouter à chaque clic sur le bouton "ajouter"
		this.newLine = jqControl.parent().clone();
		var jqBtnSupprimer = $("<img src='img/corbeille.png' alt='Supprimer la ligne' class='multiwidget_btn' />");
		this.newLine.append(jqBtnSupprimer);
		
		var jqBtnAjouter = $("<img src='img/ajout.png' alt='Ajouter une ligne' class='multiwidget_btn' />");
		jqControl.after(jqBtnAjouter);
		
		jqBtnAjouter.click(function() {
			me.ajouterLigne();
		});
		
		// Listener dupliqué par "clone"
		jqBtnSupprimer.click(function() {
			$(this).parent().remove();
		});
	
		// Initialisation de la première ligne
		if(initControl) {
			initControl(jqControl);
		}
		
		if(forceFirstValue) {
			jqControl.val(forceFirstValue.label)
				.attr("data-label", forceFirstValue.label)
				.attr("data-val", forceFirstValue.value)
				.attr("disabled", "disabled");
		}
	};
	
	MultiWidget.prototype.ajouterLigne = function() {
		// Clonage listeners compris
		var newLine = this.newLine.clone(true);
		this.jqDiv.append(newLine);
		
		if(this.initControl) {
			this.initControl(newLine.find(".multiwidget_entry"));
		}
	};
	
	/**
	 * Renvoie les valeurs entrées dans les contrôles (tableau de valeurs).
	 * Fait appel au paramètre getValFunction du constructeur.
	 * @return Tableau des valeurs saisies */
	MultiWidget.prototype.val = function() {
		var res = new Array();
		var me = this;
		this.jqDiv.find(".multiwidget_entry").each(function() {
			var val = me.getValFunction($(this)); 
			if(val != null) {
				res.push(val);
			}
		});
		
		return res;
	};
	
	/**
	 * Fonction de récupération de valeurs à fournir au constructeur de MultiWidget
	 * pour un autocomplete
	 */
	MultiWidget.AUTOCOMPLETE_VAL = function(jqElem) {
		var val = jqElem.attr("data-val");
		return val !== "" ? val : null;
	};
	
	/**
	 * Fonction qui créé une fonction d'initialisation à fournir au constructeur de MultiWidget pour un autocomplete 
	 * @param source Paramètre source de l'autocomplete
	 * @param minLength Paramètre minLength de l'autocomplete
	 */
	MultiWidget.AUTOCOMPLETE_INIT = function(source, minLength) { 
		return function(jqElem) {
			var inputAutocompletion = $("<input type='text' disabled='disabled' class='input_autocomplete_overlay' />");
			jqElem.autocomplete({
				source: source,
				autoFocus: true,
				delay: 0,
				minLength: minLength,
				focus: function(event, ui) {
					jqElem.attr("data-val", ui.item.value);
					jqElem.attr("data-label", ui.item.label);
					
					var valeurEntree = jqElem.val();
					var prefixeAutocomplete = ui.item.label.substring(0, valeurEntree.length);
					if(valeurEntree.toLowerCase() == prefixeAutocomplete.toLowerCase()) {
						inputAutocompletion.val(ui.item.label);
						jqElem.val(prefixeAutocomplete);
					}
					else {
						inputAutocompletion.val("");
					}
					
					return false;
				},
				change: function(event, ui) {
					jqElem.val(jqElem.attr("data-label"));
				},
				close: function(event, ui) {
					inputAutocompletion.val("");
				}
			});
			
			var posElem = jqElem.position();
			inputAutocompletion.css({ top: posElem.top, left: posElem.left });
			jqElem.after(inputAutocompletion);
			jqElem.addClass("input_autocomplete");
		};
	};
	
	return MultiWidget;
});
