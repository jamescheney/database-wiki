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

