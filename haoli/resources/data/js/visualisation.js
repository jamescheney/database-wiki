
  function codeAddress(add) {
  
	var geocoder;
	var map;
    geocoder = new google.maps.Geocoder();
    var myOptions = {
      zoom: 8,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    }
    map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
	
    var address = add.innerHTML.trim();
    geocoder.geocode( { 'address': address}, function(results, status) {
      if (status == google.maps.GeocoderStatus.OK) {
        map.setCenter(results[0].geometry.location);
        var marker = new google.maps.Marker({
            map: map, 
            position: results[0].geometry.location,
			title: address
        });
      } else {
        alert("Geocode was not successful for the following reason: " + status);
      }
    });
  }

/*
function initialize(address) {
	
	var point = address.innerHTML.split(",");
	var lat = point[0].trim();
	var lng = point[1].trim();
    var latlng = [new google.maps.LatLng(lat, lng)];
    var myOptions = {
      zoom: 8,
      center: latlng[0],
      mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    var map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
	
	for (var i = 0; i<latlng.length; i++) {
   	  var marker = new google.maps.Marker({
          position: latlng[i], 
          map: map,
          title:address.innerHTML
	    });
	  google.maps.event.addListener(marker, 'click', function() {
	    map.setZoom(8);
	  });
	}
}
*/

google.load("visualization", "1", {packages:["corechart", 'intensitymap']});
google.setOnLoadCallback(drawChartLine);
window.onload = getSelect;


function getSelect() {
	var chartType = document.getElementById("chart_type").options[document.getElementById("chart_type").options.selectedIndex].value;
	var id1 = document.getElementById("para1").options[document.getElementById("para1").options.selectedIndex].value;
	var id2 = document.getElementById("para2").options[document.getElementById("para2").options.selectedIndex].value;
	var p1 = document.getElementById("para1").options[document.getElementById("para1").options.selectedIndex].text;
	var p2 = document.getElementById("para2").options[document.getElementById("para2").options.selectedIndex].text;
	
	if(chartType == 'bar') {
		drawChartBar(p1, p2, id1, id2);
	}
	else if(chartType == 'column') {
		drawChartColumn(p1, p2, id1, id2);
	}
	else if(chartType == 'line') {
		drawChartLine(p1, p2, id1, id2);
	}
	else if(chartType == 'pie') {
		drawChartPie(p1, p2, id1, id2);
	}
	else if(chartType == 'scatter') {
		drawChartScatter(p1, p2, id1, id2);
	}
	else if(chartType == 'intensity') {
		drawChartIntensity(p1, p2, id1, id2);
	}
	else if(chartType == 'map') {
		drawChartMap(p1, id1);
	}
	else if(chartType == 'latlng') {
		drawChartLatlng(p1, p2, id1, id2);
	}	
}

function drawChartPie(p1, p2, id1, id2) {
        var data = new google.visualization.DataTable();
		
		var array = chartInit(id1, id2);
		
        data.addColumn('string', p1);
        data.addColumn('number', p2);
		
        data.addRows(array.length);
		for(var i=0; i<array.length; i++) {
		
			data.setValue(i, 0, ''+array[i][0]);
			data.setValue(i, 1, parseInt(array[i][1]));
		}		
		
        var chart = new google.visualization.PieChart(document.getElementById('chart_canvas'));
        chart.draw(data, {width: 450, height: 300, title: 'My Daily Activities'});
}

function drawChartBar(p1, p2, id1, id2) {
        var data = new google.visualization.DataTable();
		
		var array = chartInit(id1, id2);
		
        data.addColumn('string', p1);
        data.addColumn('number', p2);
		
        data.addRows(array.length);
		for(var i=0; i<array.length; i++) {
			
			data.setValue(i, 0, ''+array[i][0]);
			data.setValue(i, 1, parseInt(array[i][1]));
		}		
		
        var chart = new google.visualization.BarChart(document.getElementById('chart_canvas'));
        chart.draw(data, {width: 400, height: 240, title: 'Test',
                          hAxis: {title: p2, titleTextStyle: {color: 'red'}},
                          vAxis: {title: p1, titleTextStyle: {color: 'red'}}
                         });
}

function drawChartColumn(p1, p2, id1, id2) {
        var data = new google.visualization.DataTable();
		
		var array = chartInit(id1, id2);
		
        data.addColumn('string', p1);
        data.addColumn('number', p2);
		
        data.addRows(array.length);
		for(var i=0; i<array.length; i++) {
			
			data.setValue(i, 0, ''+array[i][0]);
			data.setValue(i, 1, parseInt(array[i][1]));
		}		
		
        var chart = new google.visualization.ColumnChart(document.getElementById('chart_canvas'));
        chart.draw(data, {width: 400, height: 240, title: 'Test',
                          hAxis: {title: p1, titleTextStyle: {color: 'red'}},
                          vAxis: {title: p2, titleTextStyle: {color: 'red'}}
                         });
}

function drawChartLine(p1, p2, id1, id2) {
        var data = new google.visualization.DataTable();
		
		var array = chartInit(id1, id2);
		
        data.addColumn('string', p1);
        data.addColumn('number', p2);
		
        data.addRows(array.length);
		for(var i=0; i<array.length; i++) {
		
			data.setValue(i, 0, ''+array[i][0]);
			data.setValue(i, 1, parseInt(array[i][1]));
		}		
		
        var chart = new google.visualization.LineChart(document.getElementById('chart_canvas'));
        chart.draw(data, {width: 400, height: 240, title: 'Test',						
                          hAxis: {title: p1, titleTextStyle: {color: 'red'}},
                          vAxis: {title: p2, titleTextStyle: {color: 'red'}}
                         });
}

function drawChartScatter(p1, p2, id1, id2) {
        var data = new google.visualization.DataTable();
		
		var array = chartInit(id1, id2);
		
        data.addColumn('number', p1);
        data.addColumn('number', p2);
		
        data.addRows(array.length);
		for(var i=0; i<array.length; i++) {
		
			data.setValue(i, 0, parseInt(array[i][0]));
			data.setValue(i, 1, parseInt(array[i][1]));
		}		
		
        var chart = new google.visualization.ScatterChart(document.getElementById('chart_canvas'));
        chart.draw(data, {width: 400, height: 240,							
                          title: 'Scatter Test',
                          hAxis: {title: p1},
                          vAxis: {title: p2},
                          legend: 'none'
                         });
}

function drawChartMap(p1, id1) {
  
	var array = chartInit(id1, id1);
	var geocoder;
	var map;
    geocoder = new google.maps.Geocoder();
    var myOptions = {
      zoom: 8,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    }
    map = new google.maps.Map(document.getElementById("chart_canvas"), myOptions);
	
	for(var i=0; i<array.length; i++) {
		var address = array[i][0];
		geocoder.geocode( { 'address': address}, function(results, status) {
			if (status == google.maps.GeocoderStatus.OK) {
				map.setCenter(results[0].geometry.location);
				var marker = new google.maps.Marker({
					map: map, 
					position: results[0].geometry.location,
					title: address
				});
				google.maps.event.addListener(marker, 'click', function() {
					map.setZoom(8);
				});
			}
		});
	}
  }
  
function drawChartLatlng(p1, p2, id1, id2) {	
	
	var array = chartInit(id1, id2);
	var lat = 0;
	var lng = 0;
	var latlng = new Array(array.length);
    for(var j = 0; j<latlng.length; j++) {
		lat = parseInt(array[j][0]);
		lng = parseInt(array[j][1]);
		latlng[j] = new google.maps.LatLng(lat, lng);
	}
    var myOptions = {
      zoom: 8,
      center: latlng[0],
      mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    var map = new google.maps.Map(document.getElementById("chart_canvas"), myOptions);
	
	for (var i = 0; i<latlng.length; i++) {
   	  var marker = new google.maps.Marker({
          position: latlng[i], 
          map: map,
          title: array[i][0]+","+array[i][1]
	    });
	  google.maps.event.addListener(marker, 'click', function() {
	    map.setZoom(8);
	  });
	}
}

function drawChartIntensity(p1, p2, id1, id2) {
        var data = new google.visualization.DataTable();
		
		var array = chartInit(id1, id2);
		
        data.addColumn('string', p1);
        data.addColumn('number', p2);
		
        data.addRows(array.length);
		for(var i=0; i<array.length; i++) {
		
			data.setValue(i, 0, ''+array[i][0]);
			data.setValue(i, 1, parseInt(array[i][1]));
		}		
		
        var chart = new google.visualization.IntensityMap(document.getElementById('chart_canvas'));
        chart.draw(data, {});
}

function changeSelect() {
	var chartType = document.getElementById("chart_type").options[document.getElementById("chart_type").options.selectedIndex].value;
	if(chartType == 'map') {
		document.getElementById("para2").disabled = true;
	}
	else{		
		document.getElementById("para2").disabled = false;
	}
}



//chart visualisation

function chartInit(id1, id2) {
	var table1 =document.getElementById("data"+id1);  
	var table2 =document.getElementById("data"+id2);
	var rows1 = table1.rows;
	var rows2 = table2.rows;
	
	var n = rows1.length;
	var array=new Array(n);
	
	for(var i=0; i<n; i++) {
		array[i] = new Array(2);
		var cells1 = rows1[i].cells;
		var cells2 = rows2[i].cells;
		array[i][0] = cells1[0].innerHTML.trim();
		array[i][1] = cells2[0].innerHTML.trim();
	}
	
	return array;
}