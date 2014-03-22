<table class="tbl_planning_groupes" style="max-height: <%= groupes.length * 150%>px">
	<tr class="planning_groupes_header">
		<th class="planning_groupes_num_semaine" rowspan="2">
			Semaine <span class="semaine"></span>
		</th>
		<%
		for(var i=0; i<6; i++) {
		%>
		<th class="jour"></th>
		<%
		}
		%>
	</tr>
	<tr class="planning_groupes_ligne_horaires">
		<%
		for(var i=0; i<6; i++) {
		%>
		<td>
			<div class="horaire_creneau">10</div>
			<div class="horaire_creneau">12</div>
			<div class="horaire_creneau">14</div>
			<div class="horaire_creneau">16</div>
			<div class="horaire_creneau">18</div>
		</td>
		<%
		}
		%>
	</tr>
	<%
	for(var idGroupe in groupes) {
	%>
	<tr id="ligne_groupe_<%= idGroupe %>" style="height: <%= 100/(nbGroupes + 1) %>%;">
		<td class="case_nom_groupe"><%= groupes[idGroupe] %></td>
		<%		
		for(var j=0; j<6; j++) {
		%>
		<td class="jour">
			<%
			for(var k=0; k<6; k++) {
			%>
			<div class="division_creneau"></div>
			<%
			}
			%>
		</td>
		<%
		}
		%>
	</tr>
	<%
	}
	%>
	
</table>