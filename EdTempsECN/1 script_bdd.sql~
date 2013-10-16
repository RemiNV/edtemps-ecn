
CREATE TABLE edt.Salle (
                salle_id INTEGER NOT NULL,
                salle_batiment VARCHAR NOT NULL,
                salle_niveau INTEGER NOT NULL,
                salle_numero INTEGER NOT NULL,
                salle_capacite INTEGER NOT NULL,
                CONSTRAINT salle_id PRIMARY KEY (salle_id)
);


CREATE TABLE edt.Materiel (
                materiel_id INTEGER NOT NULL,
                materiel_nom VARCHAR NOT NULL,
                CONSTRAINT materiel_id PRIMARY KEY (materiel_id)
);


CREATE TABLE edt.ContientMateriel (
                salle_id INTEGER NOT NULL,
                materiel_id INTEGER NOT NULL,
                contientMateriel_quantite INTEGER NOT NULL,
                CONSTRAINT salle_id_materiel_id PRIMARY KEY (salle_id, materiel_id)
);


CREATE TABLE edt.TypeCalendrier (
                typeCal_id INTEGER NOT NULL,
                typeCal_libelle VARCHAR NOT NULL,
                CONSTRAINT typecal_id PRIMARY KEY (typeCal_id)
);


CREATE TABLE edt.GroupedeParticipant (
                groupeParticipant_id INTEGER NOT NULL,
                groupeParticipant_nom VARCHAR NOT NULL,
                groupeParticipant_rattachementAutorise BOOLEAN NOT NULL,
                groupedeParticipant_id_parent INTEGER NOT NULL,
                CONSTRAINT groupeparticipant_id PRIMARY KEY (groupeParticipant_id)
);


CREATE TABLE edt.Evenement (
                eve_id INTEGER NOT NULL,
                eve_nom VARCHAR NOT NULL,
                eve_dateDebut TIMESTAMP NOT NULL,
                eve_dateFin TIMESTAMP NOT NULL,
                CONSTRAINT eve_id PRIMARY KEY (eve_id)
);


CREATE TABLE edt.ALieuenSalle (
                eve_id INTEGER NOT NULL,
                salle_id INTEGER NOT NULL,
                CONSTRAINT eve_id_salle_id PRIMARY KEY (eve_id, salle_id)
);


CREATE TABLE edt.NecessiteMateriel (
                eve_id INTEGER NOT NULL,
                materiel_id INTEGER NOT NULL,
                CONSTRAINT eve_id_materiel_id PRIMARY KEY (eve_id, materiel_id)
);


CREATE TABLE edt.Matiere (
                matiere_id INTEGER NOT NULL,
                matiere_nom VARCHAR NOT NULL,
                CONSTRAINT matiere_id PRIMARY KEY (matiere_id)
);


CREATE TABLE edt.Calendrier (
                cal_id INTEGER NOT NULL,
                matiere_id INTEGER NOT NULL,
                cal_nom VARCHAR NOT NULL,
                typeCal_id INTEGER NOT NULL,
                CONSTRAINT cal_id PRIMARY KEY (cal_id)
);


CREATE TABLE edt.CalendrierAppartientGroupe (
                groupeParticipant_id INTEGER NOT NULL,
                cal_id INTEGER NOT NULL,
                CONSTRAINT groupeparticipant_id_cal_id PRIMARY KEY (groupeParticipant_id, cal_id)
);


CREATE TABLE edt.EvenementAppartient (
                eve_id INTEGER NOT NULL,
                cal_id INTEGER NOT NULL,
                CONSTRAINT eve_id_cal_id PRIMARY KEY (eve_id, cal_id)
);


CREATE TABLE edt.Utilisateur (
                utilisateur_id SERIAL NOT NULL,
                utilisateur_url_ical VARCHAR,
				utilisateur_id_ldap INTEGER UNIQUE,
				utilisateur_token TEXT,
				utilisateur_token_expire TIMESTAMP,
                CONSTRAINT utilisateur_id PRIMARY KEY (utilisateur_id)
);


CREATE TABLE edt.ProprietaireGroupedeParticipant (
                utilisateur_id INTEGER NOT NULL,
                groupeParticipant_id INTEGER NOT NULL,
                CONSTRAINT proprietaire_utilisateur_id_groupeparticipant_id PRIMARY KEY (utilisateur_id, groupeParticipant_id)
);


CREATE TABLE edt.AbonneGroupeParticipant (
                utilisateur_id INTEGER NOT NULL,
                groupeParticipant_id INTEGER NOT NULL,
                abonnementGroupeParticipant_obligatoire BOOLEAN NOT NULL,
                CONSTRAINT abonnement_utilisateur_id_groupeparticipant_id PRIMARY KEY (utilisateur_id, groupeParticipant_id)
);


CREATE TABLE edt.ResponsableEvenement (
                utilisateur_id INTEGER NOT NULL,
                eve_id INTEGER NOT NULL,
                CONSTRAINT responsable_utilisateur_id_eve_id PRIMARY KEY (utilisateur_id, eve_id)
);


CREATE TABLE edt.IntervenantEvenement (
                utilisateur_id INTEGER NOT NULL,
                eve_id INTEGER NOT NULL,
                CONSTRAINT intervenant_utilisateur_id_eve_id PRIMARY KEY (utilisateur_id, eve_id)
);


CREATE TABLE edt.ProprietaireCalendrier (
                utilisateur_id INTEGER NOT NULL,
                cal_id INTEGER NOT NULL,
                CONSTRAINT utilisateur_id_cal_id PRIMARY KEY (utilisateur_id, cal_id)
);


CREATE TABLE edt.ProprietaireMatiere (
                utilisateur_id INTEGER NOT NULL,
                matiere_id INTEGER NOT NULL,
                CONSTRAINT matiere_id_utilisateur_id PRIMARY KEY (utilisateur_id, matiere_id)
);


CREATE TABLE edt.TypeUtilisateur (
                type_id INTEGER NOT NULL,
                type_libelle VARCHAR NOT NULL,
                CONSTRAINT type_id PRIMARY KEY (type_id)
);


CREATE TABLE edt.EstDeType (
                type_id INTEGER NOT NULL,
                utilisateur_id INTEGER NOT NULL,
                CONSTRAINT type_id_utilisateur_id PRIMARY KEY (type_id, utilisateur_id)
);


CREATE TABLE edt.Droits (
                droits_id INTEGER NOT NULL,
                droits_libelle VARCHAR NOT NULL,
                CONSTRAINT droits_id PRIMARY KEY (droits_id)
);


CREATE TABLE edt.ALeDroitDe (
                type_id INTEGER NOT NULL,
                droits_id INTEGER NOT NULL,
                CONSTRAINT type_id_droits_id PRIMARY KEY (type_id, droits_id)
);


ALTER TABLE edt.ALieuenSalle ADD CONSTRAINT salle_alieuensalle_fk
FOREIGN KEY (salle_id)
REFERENCES edt.Salle (salle_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.ContientMateriel ADD CONSTRAINT salle_contientmateriel_fk
FOREIGN KEY (salle_id)
REFERENCES edt.Salle (salle_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.NecessiteMateriel ADD CONSTRAINT materiel_necessitemateriel_fk
FOREIGN KEY (materiel_id)
REFERENCES edt.Materiel (materiel_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.ContientMateriel ADD CONSTRAINT materiel_contientmateriel_fk
FOREIGN KEY (materiel_id)
REFERENCES edt.Materiel (materiel_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.Calendrier ADD CONSTRAINT typecalendrier_calendrier_fk
FOREIGN KEY (typeCal_id)
REFERENCES edt.TypeCalendrier (typeCal_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.AbonneGroupeParticipant ADD CONSTRAINT groupedeparticipant_abonnegroupeparticipant_fk
FOREIGN KEY (groupeParticipant_id)
REFERENCES edt.GroupedeParticipant (groupeParticipant_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.ProprietaireGroupedeParticipant ADD CONSTRAINT groupedeparticipant_proprietairegroupedeparticipant_fk
FOREIGN KEY (groupeParticipant_id)
REFERENCES edt.GroupedeParticipant (groupeParticipant_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.CalendrierAppartientGroupe ADD CONSTRAINT groupedeparticipant_calendrierappartiengroupe_fk
FOREIGN KEY (groupeParticipant_id)
REFERENCES edt.GroupedeParticipant (groupeParticipant_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.GroupedeParticipant ADD CONSTRAINT groupedeparticipant_groupedeparticipant_fk
FOREIGN KEY (groupedeParticipant_id_parent)
REFERENCES edt.GroupedeParticipant (groupeParticipant_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.IntervenantEvenement ADD CONSTRAINT evenement_intervenantevenement_fk
FOREIGN KEY (eve_id)
REFERENCES edt.Evenement (eve_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.ResponsableEvenement ADD CONSTRAINT evenement_responsableevenement_fk
FOREIGN KEY (eve_id)
REFERENCES edt.Evenement (eve_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.EvenementAppartient ADD CONSTRAINT evenement_evenementappartient_fk
FOREIGN KEY (eve_id)
REFERENCES edt.Evenement (eve_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.NecessiteMateriel ADD CONSTRAINT evenement_necessitemateriel_fk
FOREIGN KEY (eve_id)
REFERENCES edt.Evenement (eve_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.ALieuenSalle ADD CONSTRAINT evenement_alieuensalle_fk
FOREIGN KEY (eve_id)
REFERENCES edt.Evenement (eve_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.ProprietaireMatiere ADD CONSTRAINT matiere_estproprietaire_fk
FOREIGN KEY (matiere_id)
REFERENCES edt.Matiere (matiere_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.Calendrier ADD CONSTRAINT matiere_calendrier_fk
FOREIGN KEY (matiere_id)
REFERENCES edt.Matiere (matiere_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.ProprietaireCalendrier ADD CONSTRAINT calendrier_proprietairecalendrier_fk
FOREIGN KEY (cal_id)
REFERENCES edt.Calendrier (cal_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.EvenementAppartient ADD CONSTRAINT calendrier_evenementappartient_fk
FOREIGN KEY (cal_id)
REFERENCES edt.Calendrier (cal_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.CalendrierAppartientGroupe ADD CONSTRAINT calendrier_calendrierappartiengroupe_fk
FOREIGN KEY (cal_id)
REFERENCES edt.Calendrier (cal_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.EstDeType ADD CONSTRAINT utilisateur_estdetype_fk
FOREIGN KEY (utilisateur_id)
REFERENCES edt.Utilisateur (utilisateur_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.ProprietaireMatiere ADD CONSTRAINT utilisateur_estproprietaire_fk
FOREIGN KEY (utilisateur_id)
REFERENCES edt.Utilisateur (utilisateur_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.ProprietaireCalendrier ADD CONSTRAINT utilisateur_proprietairecalendrier_fk
FOREIGN KEY (utilisateur_id)
REFERENCES edt.Utilisateur (utilisateur_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.IntervenantEvenement ADD CONSTRAINT utilisateur_intervenantevenement_fk
FOREIGN KEY (utilisateur_id)
REFERENCES edt.Utilisateur (utilisateur_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.ResponsableEvenement ADD CONSTRAINT utilisateur_responsableevenement_fk
FOREIGN KEY (utilisateur_id)
REFERENCES edt.Utilisateur (utilisateur_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.AbonneGroupeParticipant ADD CONSTRAINT utilisateur_abonnegroupeparticipant_fk
FOREIGN KEY (utilisateur_id)
REFERENCES edt.Utilisateur (utilisateur_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.ProprietaireGroupedeParticipant ADD CONSTRAINT utilisateur_proprietairegroupedeparticipant_fk
FOREIGN KEY (utilisateur_id)
REFERENCES edt.Utilisateur (utilisateur_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.ALeDroitDe ADD CONSTRAINT typeutilisateur_aledroitde_fk
FOREIGN KEY (type_id)
REFERENCES edt.TypeUtilisateur (type_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.EstDeType ADD CONSTRAINT typeutilisateur_estdetype_fk
FOREIGN KEY (type_id)
REFERENCES edt.TypeUtilisateur (type_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE edt.ALeDroitDe ADD CONSTRAINT droits_aledroitde_fk
FOREIGN KEY (droits_id)
REFERENCES edt.Droits (droits_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;
