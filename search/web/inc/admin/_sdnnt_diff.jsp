<%--
    Sync results dialog
--%>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>

<%@ page isELIgnored="false"%>

	<style>
		<!--

			.app-container {
				width: 1000px;
			}

			.app-table {
				border-width: 1px 0 0 1px;
				border-style: solid;
				border-color: silver;
				width: 100%;
			}

			.app-table th {
				font-size: 11px;
				text-align: left;
				vertical-align: top;
			}

			.app-table th,
			.app-table td {
				padding: 5px;
				border-width: 0 1px 1px 0;
				border-style: solid;
				border-color: silver;
			}

			.app-icon-txt {
				display: flex;
				align-items: center;
			}

			.app-icon-txt .ui-icon {
				margin-right: 2px;
			}

			.app-table-in {
				background-color: rgb(232, 232, 232);
				padding: 0 !important;
			}

			.app-table-in .app-table {
				border: none;
			}

			.app-table-in .app-table th:last-child,
			.app-table-in .app-table td:last-child {
				border-right: none;
			}

			.app-table-in .app-table tr:last-child td {
				border-bottom: none;
			}
			._sdnnt_controls div.buttons{
				float: right;
			}
			._sdnnt_controls>div.buttons>a {
				float: left;
			}
			.buttons{
				margin-bottom: 5px;
			}
			.buttons>a{
				margin-right:3px;
			}
			.buttons>a>.ui-button-text{
				padding:3px;
			}

			
			-->
	</style>
	
	<script>
			
		var _sdnntdata = {'table':[], partial:{},'expanded':{},'page':0,'size':50,'total':10000 };
		
		function _sdnntload() {
			if (_sdnntdata.page == 0) {
				$("#_sdnntfirst").hide();
				$("#_sdnntprev").hide();
			} else {
				$("#_sdnntfirst").show();
				$("#_sdnntprev").show();
			}
			var url = "api/v5.0/admin/sdnnt/sync?page="+_sdnntdata.page+"&rows="+_sdnntdata.size;
	        $.get(url, function(data) {
				_sdnntdata.table = data.docs;
				_sdnntdata.total = data.numFound;
				
				if ((_sdnntdata.page+1)*_sdnntdata.size >= _sdnntdata.total) {
					$("#_sdnntlast").hide();
					$("#_sdnntnext").hide();
				} else {
					$("#_sdnntlast").show();
					$("#_sdnntnext").show();
				}
				
				
				$("#_sdnnt_date").html(_sdnntdata.table[0].fetched);
	
				var trs = map(function(doc) { 
					var td1 = '<td><a target="_blank" href="handle"'+doc.pid+'\>'+doc.pid+'</a></td>'					
					var td2 = '<td><a target="_blank" href="https://sdnnt.nkp.cz/sdnnt/search?q="'+doc.catalog+'\>'+doc.catalog+"</a></td>"					
					var td3 = "<td>"+doc.title+"</td>"					
					var td31 = "<td>"+doc.real_kram_date+"</td>"					
					var td32 = "<td>"+doc.real_kram_model+"</td>"					

					var td4 = '<td><div class="app-icon-txt">'
					
					
					if (doc.sync_actions.includes('add_dnnto')) {
						td4 += '<span class="ui-icon ui-icon-plus"></span><strong>dnnto</strong>';
					}						
					if (doc.sync_actions.includes('add_dnntt')) {
						td4 += '<span class="ui-icon ui-icon-plus"></span><strong>dnntt</strong>';
					}
					if (doc.sync_actions.includes('remove_dnnto')) {
						td4 += '<span class="ui-icon ui-icon-trash"></span><strong>dnnto</strong>';
					}
					if (doc.sync_actions.includes('remove_dnntt')) {
						td4 += '<span class="ui-icon ui-icon-trash"></span><strong>dnntt</strong>';
					}
					if (doc.sync_actions.includes('change_dnnto_dnntt')) {
						td4 += '<strong>dntto</strong><span class="ui-icon ui-icon-arrowthick-1-e"></span><strong>dnntt</strong>';
					}
					if (doc.sync_actions.includes('change_dnntt_dnnto')) {
						td4 += '<strong>dnntt</strong><span class="ui-icon ui-icon-arrowthick-1-e"></span><strong>dnnto</strong>';
					}
					if (doc.sync_actions.includes('change_dnntt_dnnto')) {
						td4 += '<strong>dnntt</strong><span class="ui-icon ui-icon-arrowthick-1-e"></span><strong>dnnto</strong>';
					}
					
					if (doc.sync_actions.includes('partial_change')) {
						var id = doc.id;
						var ahrefid = "a_"+_hash_id(doc.id);
						td4 += '<a id="'+ahrefid+'" class="ui-icon ui-icon-arrowthickstop-1-s" href="'+"javascript:_sdnntpartial('"+id+"');\"> </a>";
						td4 += '<a href="javascript:_sdnntpartial(\''+id+'\');"> <strong>částečná změna</strong></a>';
					}
        
					td4 += '</div></td>';

					var td5 = '<td>'
					if (doc['process_uuid'] && doc['process_uuid']) {
						var uuids =  doc['process_uuid'];
						
						for (let i = 0; i < uuids.length; i++) {
							if (i>0) {
								td5 =td5 + "&nbsp;<b>|</b>&nbsp; ";
							}
							let htmlPostfix = '';
							let state = doc["process_uuid_details"][uuids[i]]['state'];
							if (state === 'FINISHED') {
								htmlPostfix = '<span class="ui-icon ui-icon-check">'+state+'</span>';
							} else {
								htmlPostfix = '<span class="ui-icon ui-icon-alert">'+state+'</span>';
							}
							td5 =td5 + "<a target=\"_blank\" href=\"inc/admin/_processes_outputs.jsp?uuid="+uuids[i]+"\">"+uuids[i].substring(0,5)+"</a> "+htmlPostfix;
						}
					}
					td5 = td5 + '</td>'



					var retval = "<tr>"+td1+td2+td3+td31+td32+td4+td5+"</tr>";
					if (doc.sync_actions.includes('partial_change')) {
						var id = _hash_id(doc.id);
						retval += '<tr class="app-table-in" id="'+id+'"></tr>';
					}				

					return retval;

	            }, _sdnntdata.table);

				var html = "";
				$.each(trs, function(i,val) {
					html += val;
				});            

				$("#_sdnntbody").html(html);
			});
		}
		
		function _sdnntleft() {
			if (_sdnntdata.page > 0) {
				_sdnntdata.page -= 1; 
				_sdnntload();
				_info();
			}
			
		}
		
		function _sdnntright() {
			if ((_sdnntdata.page+1)*_sdnntdata.size <= _sdnntdata.total) {
				_sdnntdata.page += 1;
				_sdnntload();
				_info();
			}
		}
		
		function _sdnntlast() {
			if ((_sdnntdata.page+1)*_sdnntdata.size < _sdnntdata.total) {
				var pages = Math.trunc( _sdnntdata.total / _sdnntdata.size );
				pages += _sdnntdata.total % _sdnntdata.size == 0 ? 1 : 0;
				_sdnntdata.page = pages;
				_sdnntload();
				_info();
			}
		}
		
		function _sdnntfirst() {
			if (_sdnntdata.page > 0) {
				_sdnntdata.page  = 0;
				_sdnntload();
				_info();
			}
		}
		
		function _applychanges() {
			var url = "api/v5.0/admin/sdnnt/sync/batches";
			$.get(url, function(data) {
				_sdnntload();
			});
		}
		
		function _hash_id(id) { return id.substring(24); }
		
		function _sdnntpartial(id) {
			$("#a_"+_hash_id(id)).toggleClass("ui-icon-arrowthickstop-1-s  ui-icon-arrowthickstop-1-n");
			if (!_sdnntdata.expanded[id]) {
				
				_sdnntdata.expanded[id] = true;
				var url = "api/v5.0/admin/sdnnt/sync/granularity/"+id;
				$.get(url, function(data) {
					
					_sdnntdata.partial=data;
					
					data[id].sort(function(a, b) {
						var dateA = a.real_kram_date || '';
						var dateB = b.real_kram_date || '';
						return dateA.localeCompare(dateB);
					});
						
					var innertable="<td colspan=\"5\">"
					innertable +="<table class=\"app-table\" cellpadding=\"0\" cellspacing=\"0\">";
					innertable += "<thead><tr><th>Pid</th> <th>Title</th> <th>Datum</th> <th>Model</th> <th>Navrhovaná změna</th><th>Proces</th></tr></thead>";
					innertable += "<tbody>";
					var trs = map(function(doc) { 
							var td1 = '<td><a target="_blank" href="handle/"'+doc.pid+'\>'+doc.pid+'</a></td>'					
							
							var td31 = "<td>"+doc.real_kram_date+"</td>"					
							var td32 = "<td>"+doc.real_kram_model+"</td>"					
							var td33 = "<td>"+(doc.real_kram_titles_search ? doc.real_kram_titles_search : "")+"</td>"					

							var td4 = '<td><div class="app-icon-txt">'
							
							if (doc.sync_actions.includes('add_dnnto')) {
								td4 += '<span class="ui-icon ui-icon-plus"></span><strong>dnnto</strong>';
							}						
							if (doc.sync_actions.includes('add_dnntt')) {
								td4 += '<span class="ui-icon ui-icon-plus"></span><strong>dnntt</strong>';
							}
							if (doc.sync_actions.includes('remove_dnnto')) {
								td4 += '<span class="ui-icon ui-icon-trash"></span><strong>dnnto</strong>';
							}
							if (doc.sync_actions.includes('remove_dnntt')) {
								td4 += '<span class="ui-icon ui-icon-trash"></span><strong>dnntt</strong>';
							}
							if (doc.sync_actions.includes('change_dnnto_dnntt')) {
								td4 += '<strong>dntto</strong><span class="ui-icon ui-icon-arrowthick-1-e"></span><strong>dnntt</strong>';
							}
							if (doc.sync_actions.includes('change_dnntt_dnnto')) {
								td4 += '<strong>dnntt</strong><span class="ui-icon ui-icon-arrowthick-1-e"></span><strong>dnnto</strong>';
							}
							if (doc.sync_actions.includes('change_dnntt_dnnto')) {
								td4 += '<strong>dnntt</strong><span class="ui-icon ui-icon-arrowthick-1-e"></span><strong>dnnto</strong>';
							}
							
				
							td4 += '</div></td>';
							
							var td5 = '<td>'
							if (doc['process_uuid']) {
								var uuids =  doc['process_uuid'];
								for (let i = 0; i < uuids.length; i++) {
									if (i>0) {
										td5 =td5 + "&nbsp;<b>|</b>&nbsp; ";
									}

									let htmlPostfix = '';
									let state = doc["process_uuid_details"][uuids[i]]['state'];
									if (state === 'FINISHED') {
										htmlPostfix = '<span class="ui-icon ui-icon-check">'+state+'</span>';
									} else {
										htmlPostfix = '<span class="ui-icon ui-icon-alert">'+state+'</span>';
									}

									//let state = doc["process_uuid_details"][uuids[i]]['state'];
									td5 += "<a target=\"_blank\" href=\"inc/admin/_processes_outputs.jsp?uuid="+uuids[i]+"\">"+uuids[i].substring(0,5) +"</a> "+htmlPostfix;
							}
							}
							td5 += '</td>'

							var retval = "<tr>"+td1+td33+td31+td32+td4+td5+"</tr>";

						return retval;

					}, data[id]);

					
					$.each(trs, function(i,val) {
						innertable += "<tr>"+val+"</tr>";
					});  

					innertable += "</tbody>";
					innertable += "</table>";
					innertable += "</td>";	
					
					$("#"+_hash_id(id)).append(innertable);	
				});
			} else {
				delete _sdnntdata.expanded[id];
				$("#"+_hash_id(id)).html("");	
			}
		}
		
		function _info() {
			//$("#_sdnnt_page").html("aktualni strana:"+_sdnntdata.page+", pocet zaznamu"+_sdnntdata.total+", velikost  strany "+_sdnntdata.size);	
			//_sdnntdata.table[0].fetched
			//$("#_sdnnt_page").html(_sdnntdata.table[0].fetched);
		}

		_info();
		_sdnntload();
	</script>

	<div>
		Datum a čas poslední synchronizace: <strong id="_sdnnt_date">x</strong>
	</div>
	
	<div class="_sdnnt_controls">
		<div class="buttons">  </div>	
		<div class="buttons">
			<a id="_sdnntfirst" title="first" href="javascript:_sdnntfirst();" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only" role="button"><span class="ui-button-text"><span class="ui-icon ui-icon-seek-prev">previous skip</span></span></a>
			<a id="_sdnntprev" title="prev" href="javascript:_sdnntleft();" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only" role="button"><span class="ui-button-text"><span class="ui-icon ui-icon-arrowthick-1-w">previous</span></span></a>
			<a id="_sdnntnext" title="next" href="javascript:_sdnntright();" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only" role="button"><span class="ui-button-text"><span class="ui-icon ui-icon-arrowthick-1-e">next</span></span></a>
            <a id="_sdnntlast" title="last"  href="javascript:_sdnntlast();" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only" role="button"><span class="ui-button-text"><span class="ui-icon ui-icon-seek-next">next</span></span></a>
    </div>
	</div>	
	
	
	<table class="app-table" cellpadding="0" cellspacing="0">
		<thead>
			<tr>
				<th>Pid</th>
				<th>Sdnt</th>
				<th>Titulek</th>
				<th>Datum/Rok</th>
				<th>Model</th>
				<th>Navrhovaná změna</th>
				<th>Proces</th>
			</tr>
		</thead>
		<tbody id="_sdnntbody">

		</tbody>
	</table>
	</div>