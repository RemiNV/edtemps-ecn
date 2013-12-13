/**
 * Module de MultiWidget utilisé pour les sélections de propriétaires dans les boîtes de dialogue
 * @module MultiWidget
 */
define([ "jquery", "jqueryui" ], function() {
	
	/**
	 * Options qui peuvent être fournies au MultiWidget. Chaque paramètre est facultatif.
	 * @typedef {Object} MultiWidgetParams
	 * @property {function} getValFunction - Fonction permettant de récupérer les valeurs d'un contrôle. Prend en paramètres le contrôle et renvoie sa valeur, ou null pour ne rien renvoyer dans le tableau de résultats
	 * @property {function} initControl - Fonction d'initialisation de chaque contrôle, à l'ajout ou l'initialisation de la première ligne
	 * @property {function} setFunction - Fonction d'assignation de valeur à un champ. Reçoit un objet jQuery et une valeur en paramètres. jQuery.val() utilisé si non fourni. La valeur peut être null pour une valeur vide.
	 * @property {number} width - Largeur à assigner au contrôle
	 * @property {boolean} disabled - Indique si le contrôle doit être en lecture seule
	 */
	
	/**
	 * Widget de formulaire duplicable sur plusieurs lignes en cliquant sur un bouton.
	 * Voir les propriétés statiques AUTOCOMPLETE_INIT et AUTOCOMPLETE_VAL pour initialiser un widget autocomplete.
	 * La valeur des objets manipulés doivent OBLIGATOIREMENT être des entiers (typiquement des identifiants)
	 * Il est possible d'ajouter l'attribut "tooltip" aux items. Cette valeur sera affiché en infobulle lors du passage de la souris sur celui-ci
	 *
	 * @param {jQuery} jqControl Objet jQuery à utiliser comme contrôle duplicable
	 * @param {MultiWidgetParams} options Options à utiliser
	 */
	var MultiWidget = function(jqControl, options) {
		
		var me = this;
		
		this.getValFunction = options.getValFunction;
		this.initControl = options.initControl;
		this.setFunction = options.setFunction;
		this.disabled = options.disabled === true;
		
		if(options.width) {
			jqControl.css("width", options.width);
		}
		
		// Wrapping du contrôle dans une div globale
		jqControl.addClass("multiwidget_entry").wrap("<div class='multiwidget'></div>");
		this.jqDiv = jqControl.parent();
		
		// Wrapping de chaque ligne
		jqControl.wrap("<div class='multiwidget_line'></div>");
		
		// Création de l'élément à ajouter à chaque clic sur le bouton "ajouter"
		this.newLine = jqControl.parent().clone();
		console.log(jqControl.width());
		var jqBtnSupprimer = $("<img src='img/corbeille.png' alt='Supprimer la ligne' class='multiwidget_btn' />");
		this.newLine.append(jqBtnSupprimer);
		
		var jqBtnAjouter = $("<img src='img/ajout.png' alt='Ajouter une ligne' class='multiwidget_btn' />");
		jqControl.after(jqBtnAjouter);
		
		jqBtnAjouter.click(function() {
			if(!me.disabled) {
				me.ajouterLigne();
			}
		});
		
		// Listener dupliqué par "clone"
		jqBtnSupprimer.click(function() {
			if(!$(this).siblings(".multiwidget_entry").attr("disabled")) {
				$(this).parent().remove();
			}
		});
	
		// Initialisation de la première ligne
		if(options.initControl) {
			options.initControl(jqControl);
		}
	};
	
	/**
	 * Remplace les valeurs du widget par celles fournies.
	 * Les valeurs peuvent être des objets quelconques (suivant le paramètres setFunction), mais si elles
	 * contiennent un attribut readOnly = true, elles sont non modifiables.
	 * @param {Object[]} values Valeurs à utiliser pour le widget
	 */
	MultiWidget.prototype.setValues = function(values) {
		
		this.clear();
		
		// Ajout des lignes manquantes
		for(var i=0; i<values.length; i++) {
			
			// Première ligne déjà renseignée
			if(i != 0) {
				this.ajouterLigne();
			}
				
			var jqNewElem = this.jqDiv.find(".multiwidget_entry:last");
			if(values[i].readOnly) {
				jqNewElem.addClass("read_only").attr("disabled", "disabled");
			}
			
			if(this.setFunction) {
				this.setFunction(jqNewElem, values[i]);
			}
			else {
				jqNewElem.val(values[i]);
			}
		}
	};
	
	/**
	 * Définit le multiwidget comme en lecture seule
	 * @param {boolean} disabled Le multiwidget doit être mis en lecture seule
	 */
	MultiWidget.prototype.setDisabled = function(disabled) {
		this.disabled = disabled;
		if(disabled) {
			this.jqDiv.find(".multiwidget_entry").attr("disabled", "disabled");
		}
		else {
			this.jqDiv.find(".multiwidget_entry:not(.read_only)").removeAttr("disabled");
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
			var val = me.getValFunction ? me.getValFunction($(this)) : $(this).val(); 
			if(val != null) {
				res.push(val);
			}
		});
		
		return res;
	};
	
	/**
	 * Vide le contenu du multiwidget.
	 * Si l'option clearFunction n'a pas été fournie, utilise jquery.val("") pour vider le premier contrôle.
	 */
	MultiWidget.prototype.clear = function() {
		this.jqDiv.find(".multiwidget_line:not(:first)").remove();
		
		var firstElem = this.jqDiv.find(".multiwidget_entry").removeAttr("disabled");
		
		if(this.setFunction) {
			this.setFunction(firstElem, null);
		}
		else {
			firstElem.val("");
		}
	};
	
	/**
	 * Fonction de création des options à fournir au MultiWidget pour créer un autocomplete.
	 * 
	 * Exemple d'utilisation : new MultiWidget($("#id"), MultiWidget.AUTOCOMPLETE_OPTIONS(["aze", "rty"], 3));
	 * 
	 * @param {object} source Paramètre source de l'autocomplete
	 * @param {number} minLength Paramètre minLength de l'autocomplete
	 * @param {number} width Paramètre width du MultiWidget
	 */
	MultiWidget.AUTOCOMPLETE_OPTIONS = function(source, minLength, width) {
		return {
			getValFunction: function(jqElem) {
				var val = parseInt(jqElem.attr("data-val"));
				return isNaN(val) ? null : val;
			},
			setFunction: function(jqElem, val) {
				
				if(val === null) {
					jqElem.val("")
						.removeAttr("data-val")
						.removeAttr("data-label");
				}
				else {
					jqElem.val(val.label)
						.attr("data-val", val.value)
						.attr("data-label", val.label);
				}
			},
			width: width,
			initControl: function(jqElem) {
				var inputAutocompletion = $("<input type='text' disabled='disabled' class='input_autocomplete_overlay' />");
				inputAutocompletion.css({
					width: jqElem.css("width")
				});
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
					},
					select: function(event, ui) {
						inputAutocompletion.val("");
						jqElem.val(jqElem.attr("data-label"));
						return false;
					}
				}).data("ui-autocomplete")._renderItem = function (ul, item) {
					return $("<li>")
						.attr("data-value", item.value)
						.attr("title", item.tooltip==null ? "" : item.tooltip)
						.append($("<a>").text(item.label))
						.appendTo(ul);
				};
				
				jqElem.after(inputAutocompletion);
				
				// Pour prendre de la place étant donné que l'input est en position absolute :
				inputAutocompletion.after($("<div class='autocomplete_placeholder'></div>").width(jqElem.outerWidth())); 
				jqElem.addClass("input_autocomplete");
			}
		};
	};
	
	return MultiWidget;
});
