// JavaScript Document

//SETTING UP OUR POPUP
//0 means disabled; 1 means enabled;
var popupStatus = 0;

//loading popup with jQuery magic!
function loadPopup() {
	//loads popup only if it is disabled
	if(popupStatus==0) {
		var windowWidth = document.documentElement.clientWidth;
		var windowHeight = document.documentElement.clientHeight;
		var popupHeight = $("#popup_delete").height();
		var popupWidth = $("#popup_delete").width();
		//centering
		$("#popup_delete").css({
			"position": "absolute",
			"top": windowHeight/2-popupHeight/2,
			"left": windowWidth/2-popupWidth/2
		});
		//only need force for IE6

		$("#background_popup").css({
			"height": windowHeight
		});
		
		$("#background_popup").css( {
			"opacity": "0.7"
		});
		$("#background_popup").fadeIn("slow");
		$("#popup_delete").fadeIn("slow");
		popupStatus = 1;
	}
}


//disabling popup with jQuery magic!
function disablePopup() {
	//disables popup only if it is enabled
	if(popupStatus==1) {
		$("#background_popup").fadeOut("slow");
		$("#popup_delete").fadeOut("slow");
		popupStatus = 0;
	}
}


function ShowItem (itemID) {
  var x = document.getElementById(itemID);
  if (x)
    x.style.visibility = "visible";
  return true;
}

function HideItem (itemID) { 
  var x = document.getElementById(itemID);
  if (x)
     x.style.visibility = "hidden";
  return true;
}

function dspToggleAnnotationForm() {
	var formBody = document.getElementById('annotation_form');
	if (formBody.style.display == 'block') {
        formBody.style.display = 'none';
	} else if (formBody.style.display == 'none') {
		formBody.style.display = 'block';
	} else if (formBody != null) {
		formBody.style.display = 'none';
	}
}

function dspToggleAnnotation() {
    var annoBody = document.getElementById('annotation_body');
	if (annoBody.style.display == 'block') {
		annoBody.style.display = 'none';	
	} else if (annoBody.style.display == 'none') { 
		annoBody.style.display = 'block';	
	} else if (annoBody != null) {
		annoBody.style.display = 'none';	
	}
}

function dspToggleProvenance() {
    var provBody = document.getElementById('provenance_body');
	if (provBody.style.display == 'block') {
		provBody.style.display = 'none';	
	} else if (provBody.style.display == 'none') { 
		provBody.style.display = 'block';	
	} else if (provBody != null) {
		provBody.style.display = 'none';	
	}
}


/* The new implementation for Annotation feature */

/* This function is used to popup the annotation list foe document node */
function ShowAnnotation(NodeId) {
   
	$.ajax({
				type: "GET",    
				url: "http://localhost:8082/CIAWFB/" + NodeId + "/annotation?action=show",   
				success: function(data)
				{
					$("#"+NodeId).append(data);   
				},
				error: function (XMLHttpRequest, textStatus, errorThrown)
				{
                     alert("Server Error! : " + errorThrown);
                } 

		  });
}

/* This function is used to add new annotation */
function AddAnnotation(NodeId) {
  
	var annoText = $('.InputText').val();
	if(annoText.length != 0)
	{
		$.ajax({
				type: "GET",    
				url: "http://localhost:8082/CIAWFB/" + NodeId + "/annotation?action=insert&annotate=" + annoText,   
				success: function(data)
				{
					$("#"+NodeId).append(data);   
				},
				error: function (XMLHttpRequest, textStatus, errorThrown)
				{
                     alert("Server Error! : " + errorThrown);
                } 
		  });
	}
	else
		alert ("Please enter annotation text!");
}

/* This function is used to delete existing annotation */
function DeleteAnnotation(NodeId) {
 
	var urlComponents = NodeId.split( '/' );
	var div_id= urlComponents[0]+"_row_"+urlComponents[1];
	
	$.ajax({
				type: "GET",   
				url: "http://localhost:8082/CIAWFB/" + urlComponents[0] + "/annotation?action=delete&annotation_id=" + urlComponents[1],
				success: function(data)
				{
					  $("#"+div_id).remove();
				},
				error: function (XMLHttpRequest, textStatus, errorThrown)
				{
                      alert("Delete Error! : " + errorThrown);
                } 

		  });
		  
}

/* This function is used to close the annotation popup */
function CloseAnnotation() {
 
	$("#AnnotationTable").remove();
	
}


/* The new implementation for Visualization of provenance feature */

/* This is the main handler called by user action on "Provenance Graphs" link 
   It calls the other handlers to process the request for provenance chart and list*/
function ProvenanceGraphs() {
	
    if ( typeof ProvenanceGraphs.isDisplayed == 'undefined' ) {
		 ProvenanceGraphs.isDisplayed=1;
	}
	else if (  ProvenanceGraphs.isDisplayed == 1)
	{
		 ProvenanceGraphs.isDisplayed =0;
		 $(".ProvenanceGraphTable").hide();
		 return;
	}

	var myDate = null;
	$("#ProvenanceStartDate").datepicker( "setDate"  , myDate);
	$("#ProvenanceEndDate").datepicker( "setDate"  , myDate);
	var username = ""; 
	var startDate = "";
	var endDate = "";

	ProvenanceCharts(startDate, endDate);
	ProvenanceList(username, startDate, endDate);
	 ProvenanceGraphs.isDisplayed = 1;
	 $(".ProvenanceGraphTable").show();
}

/* This handler is written for handling provenance chart request
   It generates the requesst for provenance chart and 
   on complition creates the pie chart and bar graph */
function ProvenanceCharts(startDate, endDate) {

	$.ajax({
				type: "GET",   
				url: "http://localhost:8082/CIAWFB/provenanceStructure?displayStr=charts&username=\"\"&startDate="+startDate+"&endDate="+endDate,
				success: function(result){
					var api = new jGCharts.Api();  
					var graphData = result.split(";");
					var userList = graphData[0].split(",");
					var countList = graphData[1].split(",");
					
					var opt = {   
						data : countList,
						type : 'p',
						axis_labels : userList
					};	

					$("#ProvenancePieChart").html("");

					jQuery('<img>') 
					.attr('src', api.make(opt)) 
					.appendTo("#ProvenancePieChart");	
					

					var opt = {   
						data : countList,
						size : '400x250', 
						type : 'bvg', 
						legend : userList
					};
					$("#ProvenanceLineChart").html("");
					jQuery('<img>') 
					.attr('src', api.make(opt)) 
					.appendTo("#ProvenanceLineChart");	
					}
		  });
}

/* This handler is written for handling visualization of provenance list request */
function ProvenanceList (username, startDate, endDate) {
	$.ajax({
				type: "GET",   
				url: "http://localhost:8082/CIAWFB/provenanceStructure?displayStr=list&username="+username+"&startDate="+startDate+"&endDate="+endDate,
				success: function(result){
						$("#ProvenanceList").html("")	;
						$("#ProvenanceList").append(result);
					}
		  });

}

/* This handler handles the request when user changes the dates */
function RequestProvenanceByDate(startDate, endDate) 
{
	ProvenanceCharts(startDate, endDate);
	var index = document.getElementById('ProUserOptions').selectedIndex;
	var username = document.getElementById('ProUserOptions').options[index].text;
	ProvenanceList(username, startDate, endDate);
}

/* This handler is written for handling the request when user changes the username*/
function ProvenanceSelectedUsername() {
	var index = document.getElementById('ProUserOptions').selectedIndex;
	var username = document.getElementById('ProUserOptions').options[index].text;

	if($('#ProvenanceEndDate').val() != "" && $('#ProvenanceStartDate').val() != "")
	{
		var startDate = $('#ProvenanceStartDate').val();
		var endDate = $('#ProvenanceEndDate').val();
		ProvenanceList(username, startDate, endDate);
	}
	else
	{
		ProvenanceList(username, "", "");
	}
}


/* This handler is written to create and initialise the object of datepicker witget when the page is being loaded 
   It handles the date for annotation as well as provenance graphs*/
$(function() {
			$(".ProvenanceGraphTable").hide();
			$(".AnnotationGraphTable").hide();

			if (document.getElementById('ProUserOptions') != null)
			{
				document.getElementById('ProUserOptions').options[0].selected = true;
			}
			if (document.getElementById('AnnUserOptions') != null)
			{
				document.getElementById('AnnUserOptions').options[0].selected = true;
			}
			
			var myDate = null;

			// for provenance dates
			$('#ProvenanceStartDate').datepicker({
				dateFormat: 'dd/mm/yy',
				//onChangeMonthYear: function(year, month, inst) { alert('a'); },
				onSelect: function(startDate, inst) {
					if ($('#ProvenanceEndDate').val() != "")
					{
						var endDate = $('#ProvenanceEndDate').val();
						RequestProvenanceByDate(startDate, endDate);
					}
				}

			});

			$('#ProvenanceEndDate').datepicker({
				dateFormat: 'dd/mm/yy',
				onSelect: function(endDate, inst) { 
						if($('#ProvenanceStartDate').val() != "")
						{
							var startDateTemp= $('#ProvenanceStartDate').datepicker("getDate");
							var endDateTemp = $('#ProvenanceEndDate').datepicker("getDate");
							var startDate = $('#ProvenanceStartDate').val();
							if (startDateTemp < endDateTemp)
							{
								RequestProvenanceByDate(startDate, endDate);
							}
							else
							{
								alert ("Please select the end date again as end date should not be before start date.");
								onClose: $('#ProvenanceEndDate').val('');
							}
						}
						else
						{
							var endDate = $('#ProvenanceEndDate').val();
							if (endDate < startDate)
							{
								alert ("Please select the start date first.");
							    onClose: $('#ProvenanceEndDate').val('');
							}
						}
					}

			});


			// for annotation dates
			$('#AnnotationStartDate').datepicker({
				dateFormat: 'dd/mm/yy',
				//onChangeMonthYear: function(year, month, inst) { alert('a'); },
				onSelect: function(startDate, inst) {
					if ($('#AnnotationEndDate').val() != "")
					{
						var endDate = $('#AnnotationEndDate').val();
						RequestAnnotationByDate(startDate, endDate);
					}
				}

			});

			$('#AnnotationEndDate').datepicker({
				dateFormat: 'dd/mm/yy',
				onSelect: function(endDate, inst) { 
						if($('#AnnotationStartDate').val() != "")
						{
							var startDateTemp= $('#AnnotationStartDate').datepicker("getDate");
							var endDateTemp = $('#AnnotationEndDate').datepicker("getDate");
							var startDate = $('#AnnotationStartDate').val();
							if (startDateTemp < endDateTemp)
							{
								RequestAnnotationByDate(startDate, endDate);
							}
							else
							{
								alert ("Please select the end date again as end date should not be before start date.");
								onClose: $('#AnnotationEndDate').val('');
							}
						}
						else
						{
							var endDate = $('#AnnotationEndDate').val();
							if (endDate < startDate)
							{
								alert ("Please select the start date first.");
							    onClose: $('#AnnotationEndDate').val('');
							}
						}
					}

			});



	});


/* The new implementation for Visualization of annotation feature */

/* This is the main handler called by user action on "Annotation Graphs" link 
   It calls the other handlers to process the request for annotation chart and list*/
function AnnotationGraphs() {
	 if ( typeof AnnotationGraphs.isDisplayed == 'undefined' ) {
			 AnnotationGraphs.isDisplayed=1;
		}
		else if (  AnnotationGraphs.isDisplayed == 1)
		{
			 AnnotationGraphs.isDisplayed =0;
			 $(".AnnotationGraphTable").hide();
			 return;
		}

		var myDate = null;
		$("#AnnotationStartDate").datepicker( "setDate"  , myDate);
		$("#AnnotationEndDate").datepicker( "setDate"  , myDate);
		var username = ""; 
		var startDate = "";
		var endDate = "";

		AnnotationCharts(startDate, endDate);
		AnnotationList(username, startDate, endDate);
		 AnnotationGraphs.isDisplayed = 1;
		 $(".AnnotationGraphTable").show();
}

/* This handler is written for handling annotation chart request
   It generates the requesst for annotation chart and 
   on complition creates the pie chart and bar graph */
function AnnotationCharts(startDate, endDate) {
	$.ajax({
				type: "GET",   
				url: "http://localhost:8082/CIAWFB/annotationStructure?displayStr=charts&username=\"\"&startDate="+startDate+"&endDate="+endDate,
				success: function(result){
					var api = new jGCharts.Api();  
					var graphData = result.split(";");
					var userList = graphData[0].split(",");
					var countList = graphData[1].split(",");
					
					var opt = {   
						data : countList,
						type : 'p',
						axis_labels : userList
					};	

					$("#AnnotationPieChart").html("");

					jQuery('<img>') 
					.attr('src', api.make(opt)) 
					.appendTo("#AnnotationPieChart");	
					

					var opt = {   
						data : countList,
						size : '400x250', 
						type : 'bvg', 
						legend : userList
					};
					$("#AnnotationLineChart").html("");
					jQuery('<img>') 
					.attr('src', api.make(opt)) 
					.appendTo("#AnnotationLineChart");	
					}
		  });

}

/* This handler is written for handling visualization of annotation list request */
function AnnotationList(username, startDate, endDate) {
	$.ajax({
				type: "GET",   
				url: "http://localhost:8082/CIAWFB/annotationStructure?displayStr=list&username="+username+"&startDate="+startDate+"&endDate="+endDate,
				success: function(result){
					
						$("#AnnotationList").html("")	;
						$("#AnnotationList").append(result);
					}
		  });

}

/* This handler handles the request when user changes the dates */
function RequestAnnotationByDate(startDate, endDate) {
	AnnotationCharts(startDate, endDate);
	var index = document.getElementById('AnnUserOptions').selectedIndex;
	var username = document.getElementById('AnnUserOptions').options[index].text;
	AnnotationList(username, startDate, endDate);
}

/* This handler is written for handling the request when user changes the username*/
function AnnotationSelectedUsername() {
	var index = document.getElementById('AnnUserOptions').selectedIndex;
	var username = document.getElementById('AnnUserOptions').options[index].text;

	if($('#AnnotationEndDate').val() != "" && $('#AnnotationStartDate').val() != "")
	{
		var startDate = $('#AnnotationStartDate').val();
		var endDate = $('#AnnotationEndDate').val();
		AnnotationList(username, startDate, endDate);
	}
	else
	{
		AnnotationList(username, "", "");
	}
}


/* This handler handles the request for comparing two provenance records */
function CompareVersions(nodeID) {
	
	var length = $("input[name='versionComparison']:checked").length;
	if(length != 2)
		alert("Please select only 2 versions..");
	else
	{
		var arrayObj = new Array();
		arrayObj[0] = $("input[name='versionComparison']:checked")[0].value;
		arrayObj[1] = $("input[name='versionComparison']:checked")[1].value;
	
		$.ajax({
				type: "GET",   
				url: "http://localhost:8082/CIAWFB/" + nodeID + "/provenanceComparison?displayStr=comparison&version1=" + arrayObj[0] + "&version2=" + arrayObj[0],
				success: function(result){
					
						$("#comparisonResult").append(result);
						var o=diffString(escape($('#one').text()),escape($('#two').text()));
						o=o.replace(/\r/gm,'<br />');
						o=o.replace(/\t/gm,'&nbsp;&nbsp;&nbsp;&nbsp;');
						//$('#out').innerHTML=o
						
						$("#comparisonResult").append(o);

					}
		  }); 
	}

}


/* This is the JS Diff JavaScript library used directly for comparing two provenance records
   More information on this library will get on http://blog.lotusnotes.be/domino/archive/2007-10-29-js-diff.html
   */
function escape(s){return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;')}


/* This handler is the part of JS Diff library mentioned above.
    More information on this library will get on http://blog.lotusnotes.be/domino/archive/2007-10-29-js-diff.html
	*/
function diffString(o,n){
    o=o.replace(/\s+$/,'');n=n.replace(/\s+$/,'')
    var out=diff(o==''?[]:o.split(/\s+/),n==''?[]:n.split(/\s+/)),str='',i,x=null,pre
    var oSpace=o.match(/\s+/g),nSpace=n.match(/\s+/g)
    if(oSpace==x)oSpace=['\n'];else oSpace.push('\n')
    if(nSpace==x)nSpace=['\n'];else nSpace.push('\n')
    if(out.n.length==0){for(i=0;i<out.o.length;i++)str+='<del>'+escape(out.o[i])+oSpace[i]+'</del>'}
    else{
        if(out.n[0].text==x){for(n=0;n<out.o.length && out.o[n].text==x;n++)str+='<del>'+escape(out.o[n])+oSpace[n]+'</del>'}
        for(i=0;i<out.n.length;i++){
            if(out.n[i].text==x)str+='<ins>'+escape(out.n[i])+nSpace[i]+'</ins>'
            else{
                pre=''
                for(n=out.n[i].row+1;n<out.o.length && out.o[n].text==x;n++){
                    pre+='<del>'+escape(out.o[n])+oSpace[n]+'</del>'
                }
                str+=" "+out.n[i].text+nSpace[i]+pre
            }
        }
    }
    return str
}

/* This handler is the part of JS Diff library mentioned above.
   More information on this library will get on http://blog.lotusnotes.be/domino/archive/2007-10-29-js-diff.html
*/
function diff(o,n){
    var ns={},os={},i,x=null
    for(i=0;i<n.length;i++){if(ns[n[i]]==x)ns[n[i]]={rows:[],o:x};ns[n[i]].rows.push(i)}
    for(i=0;i<o.length;i++){if(os[o[i]]==x)os[o[i]]={rows:[],n:x};os[o[i]].rows.push(i)}
    for(i in ns){
        if(ns[i].rows.length==1 && typeof(os[i])!='undefined' && os[i].rows.length==1){
            n[ns[i].rows[0]]={text:n[ns[i].rows[0]],row:os[i].rows[0]}
            o[os[i].rows[0]]={text:o[os[i].rows[0]],row:ns[i].rows[0]}
        }
    }
    for(i=0;i<n.length-1;i++){
        if(n[i].text!=x && n[i+1].text==x && n[i].row+1<o.length && o[n[i].row+1].text==x &&
        n[i+1]==o[n[i].row+1]){
            n[i+1]={text:n[i+1],row:n[i].row+1}
            o[n[i].row+1]={text:o[n[i].row+1],row:i+1}
        }
    }
    for(i=n.length-1;i>0;i--){
        if(n[i].text!=x && n[i-1].text==x && n[i].row>0 && o[n[i].row-1].text==x &&
        n[i-1]==o[n[i].row-1]){
            n[i-1]={text:n[i-1],row:n[i].row-1}
            o[n[i].row-1]={text:o[n[i].row-1],row:i-1}
        }
    }
    return {o:o,n:n}
}




/* This handler handles the request for displaying the provenance record in tree structure
   In this handler, it uses the JavaScript InfoVis Toolkit (JIT) library
   More information about JIT is available on http://thejit.org/
   It calls the init method of example1.js which is directly used from JIT library and
   modified it accordingly. */
function DisplayVersionTree(nodeID) {
	
	var length = $("input[name='versionComparison']:checked").length;
	if(length < 1)
		alert("Please select the version..");
	if(length > 1)
		alert("Please select only one version..");
	else
	{
		var arrayObj = new Array();
		arrayObj[0] = $("input[name='versionComparison']:checked")[0].value;
			
		$.ajax({
				type: "GET",   
				url: "http://localhost:8082/CIAWFB/" + nodeID + "/provenanceTree?versionId=" + arrayObj[0],
				success: function(result){
					
						$("#container").show();
						init();
					}
		  }); 
	}

}
