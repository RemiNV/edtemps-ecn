BEGIN;

/* typeutilisateur */
	INSERT INTO edt.typeutilisateur(type_libelle) VALUES('enseignant');
	INSERT INTO edt.typeutilisateur(type_libelle) VALUES('eleve');
	INSERT INTO edt.typeutilisateur(type_libelle) VALUES('administration');

/* utilisateur */
	/* Utilisateurs vides */
	/* Tokens bidons pour retrouver facilement ensuite */
	INSERT INTO edt.utilisateur(utilisateur_token, utilisateur_nom, utilisateur_prenom) VALUES('1', 'ProfObjet', 'EtGelol'); /* Enseignant, prof OBJET et GELOL */
	INSERT INTO edt.utilisateur(utilisateur_token, utilisateur_nom, utilisateur_prenom) VALUES('2', 'EleveEI3', 'Info'); /* Elève EI3 Info */
	INSERT INTO edt.utilisateur(utilisateur_token, utilisateur_nom, utilisateur_prenom) VALUES('3', 'EleveEI1', 'GrK'); /* Elève EI1 groupe K*/
	INSERT INTO edt.utilisateur(utilisateur_token, utilisateur_nom, utilisateur_prenom) VALUES('4', 'ProfSportThere', 'EtSibad'); /* Enseignant, prof Sport, THERE et dSIBAD (logique !) */
	INSERT INTO edt.utilisateur(utilisateur_token, utilisateur_nom, utilisateur_prenom) VALUES('5', 'Admi', 'Nistration'); /* Administration */
	INSERT INTO edt.utilisateur(utilisateur_token, utilisateur_nom, utilisateur_prenom) VALUES('6', 'EleveEI3', 'Info2'); /* Elève EI3 Info */
	INSERT INTO edt.utilisateur(utilisateur_token, utilisateur_nom, utilisateur_prenom) VALUES('7', 'EleveEI1', 'GrL'); /* Elève EI1 groupe L, suit SIBAD */

/* administrateurs */
	INSERT INTO edt.administrateurs(admin_login, admin_password) VALUES('admin', '4747b3c121b96d7ead4a1b279f1982958e241f57691d3fe51c382b5b7b29446c');

/* Intervenant */
	INSERT INTO edt.utilisateur(utilisateur_nom, utilisateur_prenom) VALUES('Doe', 'John');

/* estdetype */
	INSERT INTO edt.estdetype(type_id, utilisateur_id)
		SELECT typeutilisateur.type_id, utilisateur.utilisateur_id FROM edt.typeutilisateur
		CROSS JOIN edt.utilisateur
		WHERE typeutilisateur.type_libelle='enseignant'
		AND utilisateur.utilisateur_token='1' LIMIT 1;

	INSERT INTO edt.estdetype(type_id, utilisateur_id)
		SELECT typeutilisateur.type_id, utilisateur.utilisateur_id FROM edt.typeutilisateur
		CROSS JOIN edt.utilisateur
		WHERE typeutilisateur.type_libelle='eleve'
		AND utilisateur.utilisateur_token='2' LIMIT 1;

	INSERT INTO edt.estdetype(type_id, utilisateur_id)
		SELECT typeutilisateur.type_id, utilisateur.utilisateur_id FROM edt.typeutilisateur
		CROSS JOIN edt.utilisateur
		WHERE typeutilisateur.type_libelle='enseignant'
		AND utilisateur.utilisateur_token='3' LIMIT 1;

	INSERT INTO edt.estdetype(type_id, utilisateur_id)
		SELECT typeutilisateur.type_id, utilisateur.utilisateur_id FROM edt.typeutilisateur
		CROSS JOIN edt.utilisateur
		WHERE typeutilisateur.type_libelle='administration'
		AND utilisateur.utilisateur_token='1' LIMIT 1;

/* matiere */
	INSERT INTO edt.matiere(matiere_nom) VALUES('OBJET');
	INSERT INTO edt.matiere(matiere_nom) VALUES('GELOL');
	INSERT INTO edt.matiere(matiere_nom) VALUES('dSIBAD');
	INSERT INTO edt.matiere(matiere_nom) VALUES('Sport');
	INSERT INTO edt.matiere(matiere_nom) VALUES('THERE');

/* proprietairematiere */

	INSERT INTO edt.proprietairematiere(utilisateur_id, matiere_id)
		SELECT utilisateur.utilisateur_id, matiere.matiere_id
		FROM edt.utilisateur CROSS JOIN edt.matiere
		WHERE matiere.matiere_nom='OBJET'
		OR matiere.matiere_nom='GELOL'
		AND utilisateur.utilisateur_token='1' LIMIT 2;

	INSERT INTO edt.proprietairematiere(utilisateur_id, matiere_id)
		SELECT utilisateur.utilisateur_id, matiere.matiere_id
		FROM edt.utilisateur CROSS JOIN edt.matiere
		WHERE matiere.matiere_nom='dSIBAD'
		OR matiere.matiere_nom='Sport'
		OR matiere.matiere_nom='THERE'
		AND utilisateur.utilisateur_token='4' LIMIT 3;


/* typecalendrier */
	INSERT INTO edt.typecalendrier(typecal_libelle) VALUES('TD');
	INSERT INTO edt.typecalendrier(typecal_libelle) VALUES('TP');
	INSERT INTO edt.typecalendrier(typecal_libelle) VALUES('CM');

/* calendrier */
	INSERT INTO edt.calendrier(matiere_id, cal_nom, typecal_id)
		SELECT matiere.matiere_id, 'OBJET TD', typecalendrier.typecal_id
		FROM edt.matiere CROSS JOIN edt.typecalendrier
		WHERE matiere.matiere_nom='OBJET'
		AND typecalendrier.typecal_libelle='TD' LIMIT 1;

	INSERT INTO edt.calendrier(matiere_id, cal_nom, typecal_id)
		SELECT matiere.matiere_id, 'GELOL TD', typecalendrier.typecal_id
		FROM edt.matiere CROSS JOIN edt.typecalendrier
		WHERE matiere.matiere_nom='GELOL'
		AND typecalendrier.typecal_libelle='TD' LIMIT 1;

	INSERT INTO edt.calendrier(matiere_id, cal_nom, typecal_id)
		SELECT matiere.matiere_id, 'dSIBAD TD', typecalendrier.typecal_id
		FROM edt.matiere CROSS JOIN edt.typecalendrier
		WHERE matiere.matiere_nom='dSIBAD'
		AND typecalendrier.typecal_libelle='TD' LIMIT 1;

	INSERT INTO edt.calendrier(matiere_id, cal_nom, typecal_id)
		SELECT matiere.matiere_id, 'Sport groupes K,L', typecalendrier.typecal_id
		FROM edt.matiere CROSS JOIN edt.typecalendrier
		WHERE matiere.matiere_nom='Sport'
		AND typecalendrier.typecal_libelle='TP' LIMIT 1;

	INSERT INTO edt.calendrier(cal_nom) VALUES('Réunions d''information EI1');

	INSERT INTO edt.calendrier(matiere_id, cal_nom, typecal_id)
		SELECT matiere.matiere_id, 'THERE CM Promo B', typecalendrier.typecal_id
		FROM edt.matiere CROSS JOIN edt.typecalendrier
		WHERE matiere.matiere_nom='THERE'
		AND typecalendrier.typecal_libelle='CM' LIMIT 1;

	INSERT INTO edt.calendrier(cal_nom) VALUES('PGROU projet emploi du temps');
	
		/* Calendriers pour le test de la classe EvenementGestion */
	INSERT INTO edt.calendrier(matiere_id, cal_nom, typecal_id)
		SELECT matiere.matiere_id, 'testEvenementGestion1', typecalendrier.typecal_id
		FROM edt.matiere CROSS JOIN edt.typecalendrier
		WHERE matiere.matiere_nom='THERE'
		AND typecalendrier.typecal_libelle='CM' LIMIT 1;
	
	INSERT INTO edt.calendrier(matiere_id, cal_nom, typecal_id)
		SELECT matiere.matiere_id, 'testEvenementGestion2', typecalendrier.typecal_id
		FROM edt.matiere CROSS JOIN edt.typecalendrier
		WHERE matiere.matiere_nom='THERE'
		AND typecalendrier.typecal_libelle='CM' LIMIT 1;
	
	INSERT INTO edt.calendrier(matiere_id, cal_nom, typecal_id)
		SELECT matiere.matiere_id, 'testEvenementGestionPere', typecalendrier.typecal_id
		FROM edt.matiere CROSS JOIN edt.typecalendrier
		WHERE matiere.matiere_nom='THERE'
		AND typecalendrier.typecal_libelle='CM' LIMIT 1;
	
	INSERT INTO edt.calendrier(matiere_id, cal_nom, typecal_id)
		SELECT matiere.matiere_id, 'testEvenementGestionFils', typecalendrier.typecal_id
		FROM edt.matiere CROSS JOIN edt.typecalendrier
		WHERE matiere.matiere_nom='THERE'
		AND typecalendrier.typecal_libelle='CM' LIMIT 1;
	

/* proprietairecalendrier */
	INSERT INTO edt.proprietairecalendrier(utilisateur_id, cal_id)
		SELECT utilisateur.utilisateur_id, calendrier.cal_id
		FROM edt.utilisateur CROSS JOIN edt.calendrier
		WHERE utilisateur.utilisateur_token='1'
		AND calendrier.cal_nom='OBJET TD' LIMIT 1;

	INSERT INTO edt.proprietairecalendrier(utilisateur_id, cal_id)
		SELECT utilisateur.utilisateur_id, calendrier.cal_id
		FROM edt.utilisateur CROSS JOIN edt.calendrier
		WHERE utilisateur.utilisateur_token='1'
		AND calendrier.cal_nom='GELOL TD' LIMIT 1;

	INSERT INTO edt.proprietairecalendrier(utilisateur_id, cal_id)
		SELECT utilisateur.utilisateur_id, calendrier.cal_id
		FROM edt.utilisateur CROSS JOIN edt.calendrier
		WHERE utilisateur.utilisateur_token='4'
		AND calendrier.cal_nom='dSIBAD TD' LIMIT 1;

	INSERT INTO edt.proprietairecalendrier(utilisateur_id, cal_id)
		SELECT utilisateur.utilisateur_id, calendrier.cal_id
		FROM edt.utilisateur CROSS JOIN edt.calendrier
		WHERE utilisateur.utilisateur_token='4'
		AND calendrier.cal_nom='Sport groupes K,L' LIMIT 1;

	INSERT INTO edt.proprietairecalendrier(utilisateur_id, cal_id)
		SELECT utilisateur.utilisateur_id, calendrier.cal_id
		FROM edt.utilisateur CROSS JOIN edt.calendrier
		WHERE utilisateur.utilisateur_token='5'
		AND calendrier.cal_nom='Réunions d''information EI1' LIMIT 1;

	INSERT INTO edt.proprietairecalendrier(utilisateur_id, cal_id)
		SELECT utilisateur.utilisateur_id, calendrier.cal_id
		FROM edt.utilisateur CROSS JOIN edt.calendrier
		WHERE utilisateur.utilisateur_token='4'
		AND calendrier.cal_nom='THERE CM Promo B' LIMIT 1;

	/* 2 propriétaires pour ce calendrier */
	INSERT INTO edt.proprietairecalendrier(utilisateur_id, cal_id)
		SELECT utilisateur.utilisateur_id, calendrier.cal_id
		FROM edt.utilisateur CROSS JOIN edt.calendrier
		WHERE utilisateur.utilisateur_token='6' OR utilisateur.utilisateur_token='2'
		AND calendrier.cal_nom='PGROU projet emploi du temps' LIMIT 2;


/* groupedeparticipant */

	INSERT INTO edt.groupeparticipant(groupeparticipant_nom, groupeparticipant_rattachementautorise, groupeparticipant_id_parent, groupeparticipant_estcours, groupeParticipant_estCalendrierUnique)
		VALUES('Elèves ingénieur', false, null, true, false);
		
	INSERT INTO edt.groupeparticipant(groupeparticipant_nom, groupeparticipant_rattachementautorise, groupeparticipant_id_parent, groupeparticipant_estcours, groupeParticipant_estCalendrierUnique)
		SELECT 'Electifs', false, groupeparticipant.groupeparticipant_id, false, false FROM edt.groupeparticipant WHERE groupeparticipant.groupeparticipant_nom='Elèves ingénieur' LIMIT 1;

	INSERT INTO edt.groupeparticipant(groupeparticipant_nom, groupeparticipant_rattachementautorise, groupeparticipant_id_parent, groupeparticipant_estcours, groupeParticipant_estCalendrierUnique)
		SELECT 'EI1', false, groupeparticipant.groupeparticipant_id, true, false FROM edt.groupeparticipant WHERE groupeparticipant.groupeparticipant_nom='Elèves ingénieur' LIMIT 1;

	INSERT INTO edt.groupeparticipant(groupeparticipant_nom, groupeparticipant_rattachementautorise, groupeparticipant_id_parent, groupeparticipant_estcours, groupeParticipant_estCalendrierUnique)
		SELECT 'EI1 Promo B', false, groupeparticipant.groupeparticipant_id, true, false FROM edt.groupeparticipant WHERE groupeparticipant.groupeparticipant_nom='EI1' LIMIT 1;

	INSERT INTO edt.groupeparticipant(groupeparticipant_nom, groupeparticipant_rattachementautorise, groupeparticipant_id_parent, groupeparticipant_estcours, groupeParticipant_estCalendrierUnique)
		SELECT 'EI1 Groupe K', true, groupeparticipant.groupeparticipant_id, true, false FROM edt.groupeparticipant WHERE groupeparticipant.groupeparticipant_nom='EI1 Promo B' LIMIT 1;

	INSERT INTO edt.groupeparticipant(groupeparticipant_nom, groupeparticipant_rattachementautorise, groupeparticipant_id_parent, groupeparticipant_estcours, groupeParticipant_estCalendrierUnique)
		SELECT 'EI1 Groupe L', true, groupeparticipant.groupeparticipant_id, true, false FROM edt.groupeparticipant WHERE groupeparticipant.groupeparticipant_nom='EI1 Promo B' LIMIT 1;

	INSERT INTO edt.groupeparticipant(groupeparticipant_nom, groupeparticipant_rattachementautorise, groupeparticipant_id_parent, groupeparticipant_estcours, groupeParticipant_estCalendrierUnique)
		SELECT 'EI3', false, groupeparticipant.groupeparticipant_id, true, false FROM edt.groupeparticipant WHERE groupeparticipant.groupeparticipant_nom='Elèves ingénieur' LIMIT 1;

	INSERT INTO edt.groupeparticipant(groupeparticipant_nom, groupeparticipant_rattachementautorise, groupeparticipant_id_parent, groupeparticipant_estcours, groupeParticipant_estCalendrierUnique)
		SELECT 'EI3 Info', true, groupeparticipant.groupeparticipant_id, true, false FROM edt.groupeparticipant WHERE groupeparticipant.groupeparticipant_nom='EI3' LIMIT 1;
	
		/* Groupes pour le test unitaire de la classe evenement gestion*/
	INSERT INTO edt.groupeparticipant(groupeparticipant_nom, groupeparticipant_rattachementautorise, groupeparticipant_id_parent, groupeparticipant_estcours, groupeParticipant_estCalendrierUnique)
		VALUES('testEvenementGestionPere', false, null, true, false);
	
	INSERT INTO edt.groupeparticipant(groupeparticipant_nom, groupeparticipant_rattachementautorise, groupeparticipant_id_parent, groupeparticipant_estcours, groupeParticipant_estCalendrierUnique)
		SELECT 'testEvenementGestion', true, groupeparticipant.groupeparticipant_id, true, false FROM edt.groupeparticipant WHERE groupeparticipant.groupeparticipant_nom='testEvenementGestionPere' LIMIT 1;
		
	INSERT INTO edt.groupeparticipant(groupeparticipant_nom, groupeparticipant_rattachementautorise, groupeparticipant_id_parent, groupeparticipant_estcours, groupeParticipant_estCalendrierUnique)
		SELECT 'testEvenementGestionFils', true, groupeparticipant.groupeparticipant_id, true, false FROM edt.groupeparticipant WHERE groupeparticipant.groupeparticipant_nom='testEvenementGestion' LIMIT 1;
	
	

	/* Groupes uniques des calendriers */
	INSERT INTO edt.groupeparticipant(groupeparticipant_nom, groupeparticipant_rattachementautorise, groupeparticipant_estCalendrierUnique) VALUES('OBJET TD', false, true);
	INSERT INTO edt.groupeparticipant(groupeparticipant_nom, groupeparticipant_rattachementautorise, groupeparticipant_estCalendrierUnique) VALUES('GELOL TD', false, true);
	INSERT INTO edt.groupeparticipant(groupeparticipant_nom, groupeparticipant_rattachementautorise, groupeparticipant_estCalendrierUnique) VALUES('dSIBAD TD', false, true);
	INSERT INTO edt.groupeparticipant(groupeparticipant_nom, groupeparticipant_rattachementautorise, groupeparticipant_estCalendrierUnique) VALUES('Sport groupes K,L', false, true);
	INSERT INTO edt.groupeparticipant(groupeparticipant_nom, groupeparticipant_rattachementautorise, groupeparticipant_estCalendrierUnique) VALUES('THERE CM Promo B', false, true);
	INSERT INTO edt.groupeparticipant(groupeparticipant_nom, groupeparticipant_rattachementautorise, groupeparticipant_estCalendrierUnique) VALUES('Réunions d''information EI1', false, true);
	INSERT INTO edt.groupeparticipant(groupeparticipant_nom, groupeparticipant_rattachementautorise, groupeparticipant_estCalendrierUnique) VALUES('PGROU projet emploi du temps', false, true);



/* proprietairegroupeparticipant */

	INSERT INTO edt.proprietairegroupeparticipant(utilisateur_id, groupeparticipant_id)
		SELECT utilisateur.utilisateur_id, groupeparticipant.groupeparticipant_id
		FROM edt.utilisateur CROSS JOIN edt.groupeparticipant
		WHERE utilisateur.utilisateur_token='5' AND 
			(groupeparticipant.groupeparticipant_nom='Elèves ingénieur' 
			OR groupeparticipant.groupeparticipant_nom='Electifs'
			OR groupeparticipant.groupeparticipant_nom='EI1'
			OR groupeparticipant.groupeparticipant_nom='EI1 Promo B'
			OR groupeparticipant.groupeparticipant_nom='EI1 Groupe K'
			OR groupeparticipant.groupeparticipant_nom='EI1 Groupe L'
			OR groupeparticipant.groupeparticipant_nom='EI3'
			OR groupeparticipant.groupeparticipant_nom='EI3 Info');

	/* Pas de propriétaire pour les groupes de calendrier unique : pas d'administration directe du groupe sans passer par son calendrier */

/* calendrierappartientgroupe */

	INSERT INTO edt.calendrierappartientgroupe(groupeparticipant_id, cal_id)
		SELECT groupeparticipant.groupeparticipant_id, calendrier.cal_id
		FROM edt.groupeparticipant CROSS JOIN edt.calendrier
		WHERE calendrier.cal_nom = 'OBJET TD'
		AND (groupeparticipant.groupeparticipant_nom='EI3 Info' OR groupeparticipant.groupeparticipant_nom='OBJET TD') LIMIT 2;

	INSERT INTO edt.calendrierappartientgroupe(groupeparticipant_id, cal_id)
		SELECT groupeparticipant.groupeparticipant_id, calendrier.cal_id
		FROM edt.groupeparticipant CROSS JOIN edt.calendrier
		WHERE calendrier.cal_nom = 'GELOL TD'
		AND (groupeparticipant.groupeparticipant_nom='EI3 Info' OR groupeparticipant.groupeparticipant_nom='GELOL TD') LIMIT 2;

	INSERT INTO edt.calendrierappartientgroupe(groupeparticipant_id, cal_id)
		SELECT groupeparticipant.groupeparticipant_id, calendrier.cal_id
		FROM edt.groupeparticipant CROSS JOIN edt.calendrier
		WHERE calendrier.cal_nom = 'dSIBAD TD'
		AND (groupeparticipant.groupeparticipant_nom='dSIBAD TD' OR groupeparticipant.groupeparticipant_nom='Electifs') LIMIT 2;

	INSERT INTO edt.calendrierappartientgroupe(groupeparticipant_id, cal_id)
		SELECT groupeparticipant.groupeparticipant_id, calendrier.cal_id
		FROM edt.groupeparticipant CROSS JOIN edt.calendrier
		WHERE calendrier.cal_nom = 'Sport groupes K,L'
		AND (groupeparticipant.groupeparticipant_nom='EI1 Groupe K' OR groupeparticipant.groupeparticipant_nom='EI1 Groupe L' OR groupeparticipant.groupeparticipant_nom='Sport groupes K,L') LIMIT 3;

	INSERT INTO edt.calendrierappartientgroupe(groupeparticipant_id, cal_id)
		SELECT groupeparticipant.groupeparticipant_id, calendrier.cal_id
		FROM edt.groupeparticipant CROSS JOIN edt.calendrier
		WHERE calendrier.cal_nom = 'THERE CM Promo B'
		AND (groupeparticipant.groupeparticipant_nom='EI1 Promo B' OR groupeparticipant.groupeparticipant_nom='THERE CM Promo B') LIMIT 2;

	INSERT INTO edt.calendrierappartientgroupe(groupeparticipant_id, cal_id)
		SELECT groupeparticipant.groupeparticipant_id, calendrier.cal_id
		FROM edt.groupeparticipant CROSS JOIN edt.calendrier
		WHERE calendrier.cal_nom = 'Réunions d''information EI1'
		AND (groupeparticipant.groupeparticipant_nom='EI1' OR groupeparticipant.groupeparticipant_nom='Réunions d''information EI1') LIMIT 2;

	INSERT INTO edt.calendrierappartientgroupe(groupeparticipant_id, cal_id)
		SELECT groupeparticipant.groupeparticipant_id, calendrier.cal_id
		FROM edt.groupeparticipant CROSS JOIN edt.calendrier
		WHERE calendrier.cal_nom = 'PGROU projet emploi du temps'
		AND groupeparticipant.groupeparticipant_nom='PGROU projet emploi du temps' LIMIT 1;

		/* Test de la classe EvenementGestion */
	INSERT INTO edt.calendrierappartientgroupe(groupeparticipant_id, cal_id)
		SELECT groupeparticipant.groupeparticipant_id, calendrier.cal_id
		FROM edt.groupeparticipant CROSS JOIN edt.calendrier
		WHERE calendrier.cal_nom = 'testEvenementGestion1'
		AND groupeparticipant.groupeparticipant_nom='testEvenementGestion' LIMIT 1;
		
	INSERT INTO edt.calendrierappartientgroupe(groupeparticipant_id, cal_id)
		SELECT groupeparticipant.groupeparticipant_id, calendrier.cal_id
		FROM edt.groupeparticipant CROSS JOIN edt.calendrier
		WHERE calendrier.cal_nom = 'testEvenementGestion2'
		AND groupeparticipant.groupeparticipant_nom='testEvenementGestion' LIMIT 1;

	INSERT INTO edt.calendrierappartientgroupe(groupeparticipant_id, cal_id)
		SELECT groupeparticipant.groupeparticipant_id, calendrier.cal_id
		FROM edt.groupeparticipant CROSS JOIN edt.calendrier
		WHERE calendrier.cal_nom = 'testEvenementGestionPere'
		AND groupeparticipant.groupeparticipant_nom='testEvenementGestionPere' LIMIT 1;
		
	INSERT INTO edt.calendrierappartientgroupe(groupeparticipant_id, cal_id)
		SELECT groupeparticipant.groupeparticipant_id, calendrier.cal_id
		FROM edt.groupeparticipant CROSS JOIN edt.calendrier
		WHERE calendrier.cal_nom = 'testEvenementGestionFils'
		AND groupeparticipant.groupeparticipant_nom='testEvenementGestionFils' LIMIT 1;



		
/* abonnegroupeparticipant */

	INSERT INTO edt.abonnegroupeparticipant(utilisateur_id, groupeparticipant_id, abonnementgroupeparticipant_obligatoire)
	SELECT utilisateur.utilisateur_id, groupeparticipant.groupeparticipant_id, true
	FROM edt.groupeparticipant CROSS JOIN edt.utilisateur
	WHERE groupeparticipant.groupeparticipant_nom='EI3 Info' AND utilisateur.utilisateur_token='2' LIMIT 1;

	INSERT INTO edt.abonnegroupeparticipant(utilisateur_id, groupeparticipant_id, abonnementgroupeparticipant_obligatoire)
	SELECT utilisateur.utilisateur_id, groupeparticipant.groupeparticipant_id, false
	FROM edt.groupeparticipant CROSS JOIN edt.utilisateur
	WHERE groupeparticipant.groupeparticipant_nom='PGROU projet emploi du temps' AND utilisateur.utilisateur_token='2' LIMIT 1;

	INSERT INTO edt.abonnegroupeparticipant(utilisateur_id, groupeparticipant_id, abonnementgroupeparticipant_obligatoire)
	SELECT utilisateur.utilisateur_id, groupeparticipant.groupeparticipant_id, true
	FROM edt.groupeparticipant CROSS JOIN edt.utilisateur
	WHERE groupeparticipant.groupeparticipant_nom='EI3 Info' AND utilisateur.utilisateur_token='6' LIMIT 1;

	INSERT INTO edt.abonnegroupeparticipant(utilisateur_id, groupeparticipant_id, abonnementgroupeparticipant_obligatoire)
	SELECT utilisateur.utilisateur_id, groupeparticipant.groupeparticipant_id, false
	FROM edt.groupeparticipant CROSS JOIN edt.utilisateur
	WHERE groupeparticipant.groupeparticipant_nom='PGROU projet emploi du temps' AND utilisateur.utilisateur_token='6' LIMIT 1;

	INSERT INTO edt.abonnegroupeparticipant(utilisateur_id, groupeparticipant_id, abonnementgroupeparticipant_obligatoire)
	SELECT utilisateur.utilisateur_id, groupeparticipant.groupeparticipant_id, true
	FROM edt.groupeparticipant CROSS JOIN edt.utilisateur
	WHERE groupeparticipant.groupeparticipant_nom='EI1 Groupe K' AND utilisateur.utilisateur_token='3' LIMIT 1;

	INSERT INTO edt.abonnegroupeparticipant(utilisateur_id, groupeparticipant_id, abonnementgroupeparticipant_obligatoire)
	SELECT utilisateur.utilisateur_id, groupeparticipant.groupeparticipant_id, true
	FROM edt.groupeparticipant CROSS JOIN edt.utilisateur
	WHERE groupeparticipant.groupeparticipant_nom='EI1 Groupe L' AND utilisateur.utilisateur_token='7' LIMIT 1;

	INSERT INTO edt.abonnegroupeparticipant(utilisateur_id, groupeparticipant_id, abonnementgroupeparticipant_obligatoire)
	SELECT utilisateur.utilisateur_id, groupeparticipant.groupeparticipant_id, true
	FROM edt.groupeparticipant CROSS JOIN edt.utilisateur
	WHERE groupeparticipant.groupeparticipant_nom='dSIBAD TD' AND utilisateur.utilisateur_token='7' LIMIT 1;


/* materiel */

	INSERT INTO edt.materiel (materiel_nom) VALUES ('Ordinateur');
	INSERT INTO edt.materiel (materiel_nom) VALUES ('Vidéo-projecteur');
	INSERT INTO edt.materiel (materiel_nom) VALUES ('Ordinateur portable');

/* salle */

	INSERT INTO edt.salle(salle_batiment, salle_niveau, salle_nom, salle_numero, salle_capacite)
	VALUES('B', '2', 'Salle info B12', 12, 20);

	INSERT INTO edt.salle(salle_batiment, salle_niveau, salle_nom, salle_numero, salle_capacite)
	VALUES('B', '2', 'Salle info B13', 13, 18);

	INSERT INTO edt.salle(salle_batiment, salle_niveau, salle_nom, salle_numero, salle_capacite)
	VALUES('L', '1', 'Amphi L', null, 200);

	INSERT INTO edt.salle(salle_batiment, salle_niveau, salle_nom, salle_numero, salle_capacite)
	VALUES('C', '2', 'Salle C02', 1, 35);

	INSERT INTO edt.salle(salle_batiment, salle_niveau, salle_nom, salle_numero, salle_capacite)
	VALUES('D', '3', 'Salle D03', 1, 42);

/* contientmateriel */

	INSERT INTO edt.contientmateriel(salle_id, materiel_id, contientmateriel_quantite)
		SELECT salle.salle_id, materiel.materiel_id, 10
		FROM edt.salle CROSS JOIN edt.materiel
		WHERE salle.salle_nom='Salle info B12'
		AND materiel.materiel_nom='Ordinateur' LIMIT 1;

	INSERT INTO edt.contientmateriel(salle_id, materiel_id, contientmateriel_quantite)
		SELECT salle.salle_id, materiel.materiel_id, 9
		FROM edt.salle CROSS JOIN edt.materiel
		WHERE salle.salle_nom='Salle info B13'
		AND materiel.materiel_nom='Ordinateur' LIMIT 1;

	INSERT INTO edt.contientmateriel(salle_id, materiel_id, contientmateriel_quantite)
		SELECT salle.salle_id, materiel.materiel_id, 10
		FROM edt.salle CROSS JOIN edt.materiel
		WHERE salle.salle_nom='Salle C02'
		AND materiel.materiel_nom='Vidéoprojecteur' LIMIT 1;

/* evenement */

	INSERT INTO edt.evenement(eve_nom, eve_datedebut, eve_datefin) VALUES('OBJET', '2013-10-21 09:00:00', '2013-10-21 12:00:00');
	INSERT INTO edt.evenement(eve_nom, eve_datedebut, eve_datefin) VALUES('OBJET', '2013-10-23 14:00:00', '2013-10-23 17:00:00');

	INSERT INTO edt.evenement(eve_nom, eve_datedebut, eve_datefin) VALUES('GELOL', '2013-11-08 14:00:00', '2013-11-08 17:30:00');

	INSERT INTO edt.evenement(eve_nom, eve_datedebut, eve_datefin) VALUES('dSIBAD', '2013-10-21 08:00:00', '2013-10-21 10:00:00');
	INSERT INTO edt.evenement(eve_nom, eve_datedebut, eve_datefin) VALUES('dSIBAD', '2013-10-22 10:15:00', '2013-10-22 12:15:00');

	INSERT INTO edt.evenement(eve_nom, eve_datedebut, eve_datefin) VALUES('Sport', '2013-10-23 10:15:00', '2013-10-23 12:15:00');

	INSERT INTO edt.evenement(eve_nom, eve_datedebut, eve_datefin) VALUES('Réunion d''information alternance', '2013-09-23 10:15:00', '2013-09-23 12:15:00');

	INSERT INTO edt.evenement(eve_nom, eve_datedebut, eve_datefin) VALUES('THERE', '2013-10-24 13:45:00', '2013-10-24 15:45:00');

	INSERT INTO edt.evenement(eve_nom, eve_datedebut, eve_datefin) VALUES('Point d''avancement', '2013-10-23 17:00:00', '2013-10-23 18:00:00');

	/* Test de la classe EvenementGestion */
	INSERT INTO edt.evenement(eve_nom, eve_datedebut, eve_datefin) VALUES('TestEvenementGestion1', '2013-10-01 08:00:00', '2013-10-01 10:00:00');
	INSERT INTO edt.evenement(eve_nom, eve_datedebut, eve_datefin) VALUES('TestEvenementGestion2', '2013-10-01 10:15:00', '2013-10-01 12:15:00');
	INSERT INTO edt.evenement(eve_nom, eve_datedebut, eve_datefin) VALUES('TestEvenementGestion3', '2013-10-01 13:45:00', '2013-10-01 15:45:00');
	INSERT INTO edt.evenement(eve_nom, eve_datedebut, eve_datefin) VALUES('TestEvenementGestion4', '2013-10-01 16:00:00', '2013-10-01 18:00:00');
	INSERT INTO edt.evenement(eve_nom, eve_datedebut, eve_datefin) VALUES('TestEvenementGestion5', '2013-10-04 08:00:00', '2013-10-04 10:00:00');
	INSERT INTO edt.evenement(eve_nom, eve_datedebut, eve_datefin) VALUES('TestEvenementGestion6', '2013-10-04 10:15:00', '2013-10-04 12:15:00');
		
	
	
/* responsableevenement */

	INSERT INTO edt.responsableevenement(utilisateur_id, eve_id)
		SELECT utilisateur.utilisateur_id, evenement.eve_id
		FROM edt.utilisateur CROSS JOIN edt.evenement
		WHERE utilisateur.utilisateur_token='1'
		AND (evenement.eve_nom='OBJET' OR evenement.eve_nom='GELOL');

	INSERT INTO edt.responsableevenement(utilisateur_id, eve_id)
		SELECT utilisateur.utilisateur_id, evenement.eve_id
		FROM edt.utilisateur CROSS JOIN edt.evenement
		WHERE utilisateur.utilisateur_token='4'
		AND (evenement.eve_nom='Sport' OR evenement.eve_nom='dSIBAD' OR evenement.eve_nom='THERE');

	INSERT INTO edt.responsableevenement(utilisateur_id, eve_id)
		SELECT utilisateur.utilisateur_id, evenement.eve_id
		FROM edt.utilisateur CROSS JOIN edt.evenement
		WHERE utilisateur.utilisateur_token='5'
		AND evenement.eve_nom='Réunion d''information alternance';

	INSERT INTO edt.responsableevenement(utilisateur_id, eve_id)
		SELECT utilisateur.utilisateur_id, evenement.eve_id
		FROM edt.utilisateur CROSS JOIN edt.evenement
		WHERE utilisateur.utilisateur_token='6'
		AND evenement.eve_nom='Point d''avancement';

	INSERT INTO edt.responsableevenement(utilisateur_id, eve_id)
		SELECT utilisateur.utilisateur_id, evenement.eve_id
		FROM edt.utilisateur CROSS JOIN edt.evenement
		WHERE utilisateur.utilisateur_token='1'
		AND evenement.eve_nom='testEvenementGestion1';

	INSERT INTO edt.responsableevenement(utilisateur_id, eve_id)
		SELECT utilisateur.utilisateur_id, evenement.eve_id
		FROM edt.utilisateur CROSS JOIN edt.evenement
		WHERE utilisateur.utilisateur_token='1'
		AND evenement.eve_nom='testEvenementGestion2';
		
	INSERT INTO edt.responsableevenement(utilisateur_id, eve_id)
		SELECT utilisateur.utilisateur_id, evenement.eve_id
		FROM edt.utilisateur CROSS JOIN edt.evenement
		WHERE utilisateur.utilisateur_token='1'
		AND evenement.eve_nom='testEvenementGestion3';
		
	INSERT INTO edt.responsableevenement(utilisateur_id, eve_id)
		SELECT utilisateur.utilisateur_id, evenement.eve_id
		FROM edt.utilisateur CROSS JOIN edt.evenement
		WHERE utilisateur.utilisateur_token='1'
		AND evenement.eve_nom='testEvenementGestion4';
		
	INSERT INTO edt.responsableevenement(utilisateur_id, eve_id)
		SELECT utilisateur.utilisateur_id, evenement.eve_id
		FROM edt.utilisateur CROSS JOIN edt.evenement
		WHERE utilisateur.utilisateur_token='1'
		AND evenement.eve_nom='testEvenementGestion5';

	INSERT INTO edt.responsableevenement(utilisateur_id, eve_id)
		SELECT utilisateur.utilisateur_id, evenement.eve_id
		FROM edt.utilisateur CROSS JOIN edt.evenement
		WHERE utilisateur.utilisateur_token='1'
		AND evenement.eve_nom='testEvenementGestion6';


		
/* evenementappartient */

	INSERT INTO edt.evenementappartient(eve_id, cal_id)
		SELECT evenement.eve_id, calendrier.cal_id
		FROM edt.evenement CROSS JOIN edt.calendrier
		WHERE evenement.eve_nom='OBJET' AND evenement.eve_datedebut='2013-10-21 09:00:00' AND calendrier.cal_nom='OBJET TD' LIMIT 1;

	INSERT INTO edt.evenementappartient(eve_id, cal_id)
		SELECT evenement.eve_id, calendrier.cal_id
		FROM edt.evenement CROSS JOIN edt.calendrier
		WHERE evenement.eve_nom='OBJET' AND evenement.eve_datedebut='2013-10-23 14:00:00' AND calendrier.cal_nom='OBJET TD' LIMIT 1;

	INSERT INTO edt.evenementappartient(eve_id, cal_id)
		SELECT evenement.eve_id, calendrier.cal_id
		FROM edt.evenement CROSS JOIN edt.calendrier
		WHERE evenement.eve_nom='GELOL' AND evenement.eve_datedebut='2013-11-08 14:00:00' AND calendrier.cal_nom='GELOL TD' LIMIT 1;

	INSERT INTO edt.evenementappartient(eve_id, cal_id)
		SELECT evenement.eve_id, calendrier.cal_id
		FROM edt.evenement CROSS JOIN edt.calendrier
		WHERE evenement.eve_nom='dSIBAD' AND evenement.eve_datedebut='2013-10-21 08:00:00' AND calendrier.cal_nom='dSIBAD TD' LIMIT 1;

	INSERT INTO edt.evenementappartient(eve_id, cal_id)
		SELECT evenement.eve_id, calendrier.cal_id
		FROM edt.evenement CROSS JOIN edt.calendrier
		WHERE evenement.eve_nom='dSIBAD' AND evenement.eve_datedebut='2013-10-22 10:15:00' AND calendrier.cal_nom='dSIBAD TD' LIMIT 1;

	INSERT INTO edt.evenementappartient(eve_id, cal_id)
		SELECT evenement.eve_id, calendrier.cal_id
		FROM edt.evenement CROSS JOIN edt.calendrier
		WHERE evenement.eve_nom='Sport' AND evenement.eve_datedebut='2013-10-23 10:15:00' AND calendrier.cal_nom='Sport groupes K,L' LIMIT 1;

	INSERT INTO edt.evenementappartient(eve_id, cal_id)
		SELECT evenement.eve_id, calendrier.cal_id
		FROM edt.evenement CROSS JOIN edt.calendrier
		WHERE evenement.eve_nom='Réunion d''information alternance' AND evenement.eve_datedebut='2013-09-23 10:15:00' AND calendrier.cal_nom='Réunions d''information EI1' LIMIT 1;
		
	INSERT INTO edt.evenementappartient(eve_id, cal_id)
		SELECT evenement.eve_id, calendrier.cal_id
		FROM edt.evenement CROSS JOIN edt.calendrier
		WHERE evenement.eve_nom='THERE' AND evenement.eve_datedebut='2013-10-24 13:45:00' AND calendrier.cal_nom='THERE CM Promo B' LIMIT 1;

	INSERT INTO edt.evenementappartient(eve_id, cal_id)
		SELECT evenement.eve_id, calendrier.cal_id
		FROM edt.evenement CROSS JOIN edt.calendrier
		WHERE evenement.eve_nom='Point d''avancement' AND calendrier.cal_nom='PGROU projet emploi du temps' LIMIT 1;

	INSERT INTO edt.evenementappartient(eve_id, cal_id)
		SELECT evenement.eve_id, calendrier.cal_id
		FROM edt.evenement CROSS JOIN edt.calendrier
		WHERE evenement.eve_nom='testEvenementGestion1' AND calendrier.cal_nom='testEvenementGestion1' LIMIT 1;

	INSERT INTO edt.evenementappartient(eve_id, cal_id)
		SELECT evenement.eve_id, calendrier.cal_id
		FROM edt.evenement CROSS JOIN edt.calendrier
		WHERE evenement.eve_nom='testEvenementGestion2' AND calendrier.cal_nom='testEvenementGestion1' LIMIT 1;
		
	INSERT INTO edt.evenementappartient(eve_id, cal_id)
		SELECT evenement.eve_id, calendrier.cal_id
		FROM edt.evenement CROSS JOIN edt.calendrier
		WHERE evenement.eve_nom='testEvenementGestion3' AND calendrier.cal_nom='testEvenementGestion2' LIMIT 1;
		
	INSERT INTO edt.evenementappartient(eve_id, cal_id)
		SELECT evenement.eve_id, calendrier.cal_id
		FROM edt.evenement CROSS JOIN edt.calendrier
		WHERE evenement.eve_nom='testEvenementGestion4' AND calendrier.cal_nom='testEvenementGestion2' LIMIT 1;

	INSERT INTO edt.evenementappartient(eve_id, cal_id)
		SELECT evenement.eve_id, calendrier.cal_id
		FROM edt.evenement CROSS JOIN edt.calendrier
		WHERE evenement.eve_nom='testEvenementGestion5' AND calendrier.cal_nom='testEvenementGestionPere' LIMIT 1;

	INSERT INTO edt.evenementappartient(eve_id, cal_id)
		SELECT evenement.eve_id, calendrier.cal_id
		FROM edt.evenement CROSS JOIN edt.calendrier
		WHERE evenement.eve_nom='testEvenementGestion6' AND calendrier.cal_nom='testEvenementGestionFils' LIMIT 1;

/* alieuensalle */

	INSERT INTO edt.alieuensalle(eve_id, salle_id)
		SELECT evenement.eve_id, salle.salle_id
		FROM edt.evenement CROSS JOIN edt.salle
		WHERE evenement.eve_nom='OBJET' AND evenement.eve_datedebut='2013-10-21 09:00:00' AND salle.salle_nom='Salle D03' LIMIT 1;

	/* Evènement dans 2 salles */
	INSERT INTO edt.alieuensalle(eve_id, salle_id)
		SELECT evenement.eve_id, salle.salle_id
		FROM edt.evenement CROSS JOIN edt.salle
		WHERE evenement.eve_nom='OBJET' AND evenement.eve_datedebut='2013-10-23 14:00:00' AND (salle.salle_nom='Salle info B12' OR salle.salle_nom='Salle info B13') LIMIT 2;

	INSERT INTO edt.alieuensalle(eve_id, salle_id)
		SELECT evenement.eve_id, salle.salle_id
		FROM edt.evenement CROSS JOIN edt.salle
		WHERE evenement.eve_nom='GELOL' AND evenement.eve_datedebut='2013-11-08 14:00:00' AND salle.salle_nom='Salle D03' LIMIT 1;

	INSERT INTO edt.alieuensalle(eve_id, salle_id)
		SELECT evenement.eve_id, salle.salle_id
		FROM edt.evenement CROSS JOIN edt.salle
		WHERE evenement.eve_nom='dSIBAD' AND evenement.eve_datedebut='2013-10-21 08:00:00' AND salle.salle_nom='Salle C02' LIMIT 1;

	INSERT INTO edt.alieuensalle(eve_id, salle_id)
		SELECT evenement.eve_id, salle.salle_id
		FROM edt.evenement CROSS JOIN edt.salle
		WHERE evenement.eve_nom='dSIBAD' AND evenement.eve_datedebut='2013-10-22 10:15:00' AND salle.salle_nom='Salle C02' LIMIT 1;

	/* Pas de salle précisée pour le sport (on pourrait mettre le gymnase, mais c'est pour tester le cas sans salle) */

	INSERT INTO edt.alieuensalle(eve_id, salle_id)
		SELECT evenement.eve_id, salle.salle_id
		FROM edt.evenement CROSS JOIN edt.salle
		WHERE evenement.eve_nom='Réunion d''information alternance' AND evenement.eve_datedebut='2013-09-23 10:15:00' AND salle.salle_nom='Amphi L' LIMIT 1;

	INSERT INTO edt.alieuensalle(eve_id, salle_id)
		SELECT evenement.eve_id, salle.salle_id
		FROM edt.evenement CROSS JOIN edt.salle
		WHERE evenement.eve_nom='THERE' AND evenement.eve_datedebut='2013-10-24 13:45:00' AND salle.salle_nom='Amphi L' LIMIT 1;

	INSERT INTO edt.alieuensalle(eve_id, salle_id)
		SELECT evenement.eve_id, salle.salle_id
		FROM edt.evenement CROSS JOIN edt.salle
		WHERE evenement.eve_nom='Point d''avancement' AND salle.salle_nom='Salle D03' LIMIT 1;
	
	/* Données pour le test de evenementGestion */
	INSERT INTO edt.alieuensalle(eve_id, salle_id)
		SELECT evenement.eve_id, salle.salle_id
		FROM edt.evenement CROSS JOIN edt.salle
		WHERE evenement.eve_nom='testEvenementGestion1' AND salle.salle_nom='Salle D03' LIMIT 1;
	
	INSERT INTO edt.alieuensalle(eve_id, salle_id)
		SELECT evenement.eve_id, salle.salle_id
		FROM edt.evenement CROSS JOIN edt.salle
		WHERE evenement.eve_nom='testEvenementGestion2' AND salle.salle_nom='Salle D03' LIMIT 1;
	
	INSERT INTO edt.alieuensalle(eve_id, salle_id)
		SELECT evenement.eve_id, salle.salle_id
		FROM edt.evenement CROSS JOIN edt.salle
		WHERE evenement.eve_nom='testEvenementGestion3' AND salle.salle_nom='Salle D03' LIMIT 1;
	
	INSERT INTO edt.alieuensalle(eve_id, salle_id)
		SELECT evenement.eve_id, salle.salle_id
		FROM edt.evenement CROSS JOIN edt.salle
		WHERE evenement.eve_nom='testEvenementGestion4' AND salle.salle_nom='Salle D03' LIMIT 1;
	
	INSERT INTO edt.alieuensalle(eve_id, salle_id)
		SELECT evenement.eve_id, salle.salle_id
		FROM edt.evenement CROSS JOIN edt.salle
		WHERE evenement.eve_nom='testEvenementGestion5' AND salle.salle_nom='Salle D03' LIMIT 1;
	
	INSERT INTO edt.alieuensalle(eve_id, salle_id)
		SELECT evenement.eve_id, salle.salle_id
		FROM edt.evenement CROSS JOIN edt.salle
		WHERE evenement.eve_nom='testEvenementGestion6' AND (salle.salle_nom='Salle D03' OR salle.salle_nom='Salle info B12') LIMIT 2;
	
	
/* intervenantevenement */

	/* Je ne mets pas le prof en intervenant ici, flemme */
	INSERT INTO edt.intervenantevenement(utilisateur_id, eve_id)
		SELECT utilisateur.utilisateur_id, evenement.eve_id
		FROM edt.utilisateur CROSS JOIN edt.evenement
		WHERE utilisateur.utilisateur_nom='Doe' AND utilisateur.utilisateur_prenom='John'
		AND evenement.eve_nom='Réunion d''information alternance';

	INSERT INTO edt.intervenantevenement(utilisateur_id, eve_id)
		SELECT utilisateur.utilisateur_id, evenement.eve_id
		FROM edt.utilisateur CROSS JOIN edt.evenement
		WHERE utilisateur.utilisateur_nom='Doe' AND utilisateur.utilisateur_prenom='John'
		AND evenement.eve_nom='testEvenementGestion1';

	INSERT INTO edt.intervenantevenement(utilisateur_id, eve_id)
		SELECT utilisateur.utilisateur_id, evenement.eve_id
		FROM edt.utilisateur CROSS JOIN edt.evenement
		WHERE utilisateur.utilisateur_nom='Doe' AND utilisateur.utilisateur_prenom='John'
		AND evenement.eve_nom='testEvenementGestion2';

		
	INSERT INTO edt.intervenantevenement(utilisateur_id, eve_id)
		SELECT utilisateur.utilisateur_id, evenement.eve_id
		FROM edt.utilisateur CROSS JOIN edt.evenement
		WHERE utilisateur.utilisateur_nom='Doe' AND utilisateur.utilisateur_prenom='John'
		AND evenement.eve_nom='testEvenementGestion3';

		
	INSERT INTO edt.intervenantevenement(utilisateur_id, eve_id)
		SELECT utilisateur.utilisateur_id, evenement.eve_id
		FROM edt.utilisateur CROSS JOIN edt.evenement
		WHERE utilisateur.utilisateur_nom='Doe' AND utilisateur.utilisateur_prenom='John'
		AND evenement.eve_nom='testEvenementGestion4';

		
	INSERT INTO edt.intervenantevenement(utilisateur_id, eve_id)
		SELECT utilisateur.utilisateur_id, evenement.eve_id
		FROM edt.utilisateur CROSS JOIN edt.evenement
		WHERE utilisateur.utilisateur_nom='Doe' AND utilisateur.utilisateur_prenom='John'
		AND evenement.eve_nom='testEvenementGestion5';

		
	INSERT INTO edt.intervenantevenement(utilisateur_id, eve_id)
		SELECT utilisateur.utilisateur_id, evenement.eve_id
		FROM edt.utilisateur CROSS JOIN edt.evenement
		WHERE utilisateur.utilisateur_nom='Doe' AND utilisateur.utilisateur_prenom='John'
		AND evenement.eve_nom='testEvenementGestion6';

		
/* droits */
INSERT INTO edt.droits(droits_id, droits_libelle) VALUES (1, 'CREER_GROUPE');
INSERT INTO edt.droits(droits_id, droits_libelle) VALUES (2, 'RATTACHER_CALENDRIER_GROUPE');
INSERT INTO edt.droits(droits_id, droits_libelle) VALUES (3, 'CREER_GROUPE_COURS');
INSERT INTO edt.droits(droits_id, droits_libelle) VALUES (4, 'CHOISIR_PROPRIETAIRES_EVENEMENT');
INSERT INTO edt.droits(droits_id, droits_libelle) VALUES (5, 'LIMITE_CALENDRIERS_ETENDUE');

/* aledroitde */

	/* L'enseignant et l'administration peuvent tout faire */
	INSERT INTO edt.aledroitde(type_id, droits_id) VALUES (1, 1);
	INSERT INTO edt.aledroitde(type_id, droits_id) VALUES (1, 2);
	INSERT INTO edt.aledroitde(type_id, droits_id) VALUES (1, 3);
	INSERT INTO edt.aledroitde(type_id, droits_id) VALUES (1, 4);

	INSERT INTO edt.aledroitde(type_id, droits_id) VALUES (3, 1);
	INSERT INTO edt.aledroitde(type_id, droits_id) VALUES (3, 2);
	INSERT INTO edt.aledroitde(type_id, droits_id) VALUES (3, 3);
	INSERT INTO edt.aledroitde(type_id, droits_id) VALUES (3, 4);

	/* L'étudiant peut tout faire sauf créer un groupe de cours */
	INSERT INTO edt.aledroitde(type_id, droits_id) VALUES (2, 1);
	INSERT INTO edt.aledroitde(type_id, droits_id) VALUES (2, 2);
	INSERT INTO edt.aledroitde(type_id, droits_id) VALUES (2, 4);






COMMIT;