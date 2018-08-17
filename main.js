var urlStart = "http://192.168.2.86:8080/api/events"; //beginning of endpoints


$(document).ready(function() {
	
	//Select the dropdown button that contains a list of optional visuals
	//and set an 'onclick' function which draws the appropriate visual when chosen.
    $('.dropdown').each(function (key, dropdown) {
        var $dropdown = $(dropdown);
        $dropdown.find('.dropdown-menu a').on('click', function() {
			
			var url = ""
			
			//change the dropdown button's text to show the selected visual
            $dropdown.find('button').text($(this).text()).append(' <span class="caret"></span>');
			
			
			//create the request url for the selected visual and draw the visual
			if($dropdown.find('button').text() == "Zoomable Sunburst "){

				//accounts for the specified date and time (if specified)
				url = getDateTimeURLForDiagram("zoomableSunburstJSON");

				getDataFromRequest(url).then(function(result){
					drawZoomableSunburst(result);
				});
			}
			else if($dropdown.find('button').text() == "Collapsible Tree "){
				
				url = getDateTimeURLForDiagram("collapsibleTreeJSON");
			
				getDataFromRequest(url).then(function(result){
					drawCollapsibleTree(result);
				});
			}
			else if($dropdown.find('button').text() == "Calendar "){
				
				getDataFromRequest(urlStart + "/calendarJSON").then(function(result){
					drawCalendar(result);
				});
			}
        });
    });
	
	//gives 'update' button functionality to redraw the selected visual based on dates and times specified
	$('#update-visual').on('click', function() {
		
		var $dropdown = $('.dropdown');
		var url = "";
		
		if($dropdown.find('button').text() == "Zoomable Sunburst "){
				
			url = getDateTimeURLForDiagram("zoomableSunburstJSON");
			
			getDataFromRequest(url).then(function(result){
				drawZoomableSunburst(result);
			});
		}
		else if($dropdown.find('button').text() == "Collapsible Tree "){
				
			url = getDateTimeURLForDiagram("collapsibleTreeJSON");
			
			getDataFromRequest(url).then(function(result){
				drawCollapsibleTree(result);
			});
		}
	})
});

//converts date to timestamp
function toTimestamp(strDate){
	var date = Date.parse(strDate);
	return date/1000;
}

//accounts for time zone differences
function convertUTCDateToLocalDate(date) {
    return new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate(),  date.getHours(), 
								date.getMinutes(), date.getSeconds()));
}


//This function creates a zoomable sunburst visual
function drawZoomableSunburst(root){
	
	
	//delete any extra tooltips that may have been created from any of the visuals, so that only one remains
//-------------------------------------------------------------------------------------------------------------
	var tooltips = d3.selectAll("#tooltip");
	if(tooltips.size() > 1){
		for (var i = 0; i < tooltips.size()-1; i++){
			d3.select('#tooltip').remove();
		}
	}

	//Set the opacity of all tooltips to zero
//-------------------------------------------------------------------------------------------------------------
	d3.selectAll('#tooltip').style('opacity', '0');
	
	//Breadcrumb dimensions: width, height, spacing, width of tip/tail.
//--------------------------------------------------------------------------------------------------
	var b = {
		w: 150,
		h: 25,
		s: 5,
		t: 10
	};	
	
	//Sunburst dimensions
//--------------------------------------------------------------------------------------------------
	var vWidth = 600;
	var vHeight = 600;
	var radius = Math.min(vWidth, vHeight) / 2;
	
	var x = d3.scale.linear()
		.range([0, 2 * Math.PI]);
	var y = d3.scale.linear()
		.range([0, radius]);
	
	var hue = d3.scale.category10();

	var luminance = d3.scale.sqrt()
		.domain([0, 1e6])
		.clamp(true)
		.range([90, 20]);

		

	//Enable start and end date inputs
//--------------------------------------------------------------------------------------------------
	enableDateTimeUpdate();
	
	//Clear any diagram container of any visuals that are being displayed
//--------------------------------------------------------------------------------------------------
	clearDiagramContainer();
	
	//Prepare our physical space
//--------------------------------------------------------------------------------------------------
	var svg = d3.select("#diagramContainer").append("svg")
		.attr("width", vWidth).attr("height", vHeight)
		.style('margin-bottom', '20px')
		.style('margin-left', '40px')
	   .append('g')
	    .attr("id", "container")
		.attr('transform', 'translate(' + vWidth / 2 + ',' + vHeight / 2 + ')');
		

	initializeBreadcrumbTrail();
			
	//Object determines the size of parent elements (the arcs of sunburst) based on the sum of the
	//sizes of its children
//--------------------------------------------------------------------------------------------------
	var partition = d3.layout.partition()
		.sort(function(a, b) { return d3.ascending(a.name, b.name); })
		.size([2 * Math.PI, radius])

	
	//calculates size of each arc, defining the four lines of the perimeter based on start, end angles
	//and inner, outer radii
//--------------------------------------------------------------------------------------------------
	var arc = d3.svg.arc()
		.startAngle(function(d) { return d.x; })
		.endAngle(function(d) { return d.x + d.dx - .01 / (d.depth + .5); })
		.innerRadius(function(d) { return radius / 3 * d.depth; })
		.outerRadius(function(d) { return radius / 3 * (d.depth + 1) - 1; });
		
		
		
		



	//organizes data into the sunburst pattern
	//ensures all 360 degrees of circle are used
	//ensures each slice is sized relative to others
//--------------------------------------------------------------------------------------------------
	partition
		.value(function(d) { return d.size; })
		.nodes(root)
		.forEach(function(d) {
			d._children = d.children;
			d.sum = d.value;
			d.key = key(d);
			d.fill = fill(d);
		});

    //Redefine the value function to use the previously-computed sum.
	partition
		.children(function(d, depth) { return depth < 2 ? d._children : null; })
		.value(function(d) { return d.sum; });

	var center = svg.append("circle")
		.attr("r", radius / 3)
		.on("click", zoomOut);

	center.append("title")
   	    .text("zoom out");

	var partitioned_data = partition.nodes(root).slice(1)

	var g = svg.selectAll("g")
		.append("g")

	var path = svg.selectAll("g")
	    .data(partitioned_data)
		.enter().append("path")
		.attr("d", arc)
		.style("fill", function(d) { return d.fill; })
		.each(function(d) { this._current = updateArc(d); })
		.on("click", zoomIn)
		.on("mouseover", mouseOverArc)
		.on("mousemove", mouseMoveArc)
		.on("mouseout", mouseOutArc);
		
	var texts = svg.selectAll("g")
		.data(partitioned_data)
		.enter().append("text")
		.on("click", zoomIn)
		.on("mouseover", mouseOverArc)
		.on("mousemove", mouseMoveArc)
		.on("mouseout", mouseOutArc)
		.filter(filter_min_arc_size_text)    	
		.attr("transform", function(d) {  return 'translate(' + arc.centroid(d) + ') rotate(' + computeTextRotation(d) + ')'; })
		.attr("text-anchor", "middle")
		.text(function(d,i) {return d.name === "root" ? "" : d.name})


	// Add the mouseleave handler to the bounding circle.
    d3.select("#container").on("mouseout", mouseOutArc);
		
		
	//Zoom functions
//--------------------------------------------------------------------------------------------------
	function zoomIn(p) {
		
		if (p.depth > 1) 
			p = p.parent;
		
		if (!p.children) {
			listEvents(p);
			return;
		}
		zoom(p, p);
	}
	
	function zoomOut(p) {
		
		if (!p.parent) 
			return;
		
		zoom(p.parent, p);
	}

	function zoom(root, p) {
		
		if (document.documentElement.__transition__) 
			return;
		
		// Rescale outside angles to match the new layout.
		var enterArc,
			exitArc,
			outsideAngle = d3.scale.linear().domain([0, 2 * Math.PI]);

		function insideArc(d) {
		    return p.key > d.key
			    ? {depth: d.depth - 1, x: 0, dx: 0} : p.key < d.key
			    ? {depth: d.depth - 1, x: 2 * Math.PI, dx: 0}
			    : {depth: 0, x: 0, dx: 2 * Math.PI};
		}

		function outsideArc(d) {
		    return {depth: d.depth + 1, x: outsideAngle(d.x), 
					dx: outsideAngle(d.x + d.dx) - outsideAngle(d.x)};
		}

		center.datum(root);

		// When zooming in, arcs enter from the outside and exit to the inside.
		// Entering outside arcs start from the old layout.
		if (root === p) 
			enterArc = outsideArc, exitArc = insideArc, outsideAngle.range([p.x, p.x + p.dx]);

		// When zooming out, arcs enter from the inside and exit to the outside.
		// Exiting outside arcs transition to the new layout.
		if (root !== p) 
			enterArc = insideArc, exitArc = outsideArc, outsideAngle.range([p.x, p.x + p.dx]);
		
		var new_data = partition.nodes(root).slice(1)

		path = path.data(new_data, function(d) { return d.key; });

		d3.transition().duration(d3.event.altKey ? 7500 : 750).each(function() {
		    
			path.exit().transition()
				.style("fill-opacity", function(d) { return d.depth === 1 + (root === p) ? 1 : 0; })
			    .attrTween("d", function(d) { return arcTween.call(this, exitArc(d)); })
			    .remove();
			  
		    path.enter().append("path")
			    .style("fill-opacity", function(d) { return d.depth === 2 - (root === p) ? 1 : 0; })
			    .style("fill", function(d) { return d.fill; })
			    .on("click", zoomIn)
				.on("mouseover", mouseOverArc)
			    .on("mousemove", mouseMoveArc)
			    .on("mouseout", mouseOutArc)
			    .each(function(d) { this._current = enterArc(d); });
				
		    path.transition()
			    .style("fill-opacity", 1)
			    .attrTween("d", function(d) { return arcTween.call(this, updateArc(d)); });
		});
		
		
		texts = texts.data(new_data, function(d) { return d.key; })
		 
		texts.exit().remove()    
		texts.enter().append("text")
		.on("click", zoomIn)
		.on("mouseover", mouseOverArc)
		.on("mousemove", mouseMoveArc)
		.on("mouseout", mouseOutArc)
		texts.style("opacity", 0)		
			.attr("transform", function(d) {  return 'translate(' + arc.centroid(d) + ') rotate(' + computeTextRotation(d) + ')'; })
			.attr("text-anchor", "middle")
		    .attr("dx", "0") 
		    .attr("dy", ".35em") 
		    .filter(filter_min_arc_size_text)    	
		    .text(function(d,i) {return d.name === "root" ? "" : d.name})
			.transition().delay(750).style("opacity", 1) 
    }

	
	
	
	
	//Filter and formatting functions
	//--------------------------------------------------------------------------------------------------
	function filter_min_arc_size_text(d, i) {
		return (d.dx*d.depth*radius/3)>14
	} 
		
	function format_number(x) { 
		return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ","); 
	}
	
	function format_description(d) {
		var description = d.name;
		return  /* '<b>' + d.name  + '</b></br>'+*/ d.name + '<br> (' + format_number(d.value) + ')';
	}

	
	
	
	
	//Tooltip description
	//--------------------------------------------------------------------------------------------------
	var tooltip = d3.select("body")
		.append("div")
		.attr("id", "tooltip")
		.style("position", "absolute")
		.style("z-index", "10")
		.style("opacity", 0);
	
	
	//Mouse and arc interaction functions
	//--------------------------------------------------------------------------------------------------
	function mouseOverArc(d) {
		
		d3.select(this).attr("stroke","black")
				 
		tooltip.html(format_description(d));
		
		var sequenceArray = getAncestors(d);
		updateBreadcrumbs(sequenceArray);
		
		return tooltip.transition()
			.duration(50)
			.style("opacity", 0.9);
	}

	function mouseOutArc(){
		d3.select(this).attr("stroke","")
		d3.select("#trail").style("visibility", "hidden");
		return tooltip.style("opacity", 0);
	}

	function mouseMoveArc (d) {
		return tooltip
			.style("top", (d3.event.pageY-10)+"px")
			.style("left", (d3.event.pageX+10)+"px");
	}
	
	
	
	
	
	
	
	
	// Given a node in a partition layout, return an array of all of its ancestor
	// nodes, highest first, but excluding the root.
	function getAncestors(node) {
	  var path = [];
	  var current = node;
	  while (current.parent) {
		path.unshift(current);
		current = current.parent;
	  }
	  return path;
	}

	function initializeBreadcrumbTrail() {
	  // Add the svg area.
	  var trail = d3
		.select("#sequence")
		.append("svg:svg")
		.attr("width", vWidth)
		.attr("height", 50)
		.attr("id", "trail");
	}

	// Generate a string that describes the points of a breadcrumb polygon.
	function breadcrumbPoints(d, i) {
	  var points = [];
	  points.push("0,0");
	  points.push(b.w + ",0");
	  points.push(b.w + b.t + "," + b.h / 2);
	  points.push(b.w + "," + b.h);
	  points.push("0," + b.h);
	  if (i > 0) {
		// Leftmost breadcrumb; don't include 6th vertex.
		points.push(b.t + "," + b.h / 2);
	  }
	  return points.join(" ");
	}

	// Update the breadcrumb trail to show the current sequence and percentage.
	function updateBreadcrumbs(nodeArray) {
		
	  // Data join; key function combines name and depth (= position in sequence).
	  var g = d3
		.select("#trail")
		.selectAll("g")
		.data(nodeArray, function(d) {
		  return d.name + d.depth;
		});

	  // Add breadcrumb and label for entering nodes.
	  var entering = g.enter().append("svg:g");

	  entering
		.append("svg:polygon")
		.attr("points", breadcrumbPoints)
		.style("fill", function(d) { return d.fill; });

	  entering
		.append("svg:text")
		.attr("x", (b.w + b.t) / 2)
		.attr("y", b.h / 2)
		.attr("dy", "0.35em")
		.attr("text-anchor", "middle")
		.text(function(d) {
		  return d.name;
		});

	  // Set position for entering and updating nodes.
	  g.attr("transform", function(d, i) {
		return "translate(" + i * (b.w + b.s) + ", 0)";
	  });

	  // Remove exiting nodes.
	  g.exit().remove();

	  // Make the breadcrumb trail visible, if it's hidden.
	  d3.select("#trail").style("visibility", "");
	}
	
	
	
	
	
	
	
	
	//--------------------------------------------------------------------------------------------------
	function key(d) {
		var k = [], p = d;
		while (p.depth) k.push(p.name), p = p.parent;
		return k.reverse().join(".");
	}

	function fill(d) {
		var p = d;
		while (p.depth > 1) p = p.parent;
		var c = d3.lab(hue(p.name));
		c.l = luminance(d.sum);
		return c;
	}

	//transitions selected arcs from their current angles to the specified new angles
	function arcTween(b) {
		var i = d3.interpolate(this._current, b);
		this._current = i(0);
		return function(t) {
			return arc(i(t));
	    };
	}

	function updateArc(d) {
		return {depth: d.depth, x: d.x, dx: d.dx};
	}
	
	//determines the angle the text should be rotated so that it is positioned according to its location
	//around the arc
	function computeTextRotation(d) {
		var angle=(d.x +d.dx/2)*180/Math.PI - 90;	
		return (angle > 90) ? 180 + angle : angle;
	}
	d3.select(self.frameElement).style("height", vHeight + "px");
}


















function drawCollapsibleTree(data){
	
	d3.selectAll('#tooltip').remove();
	
	var margin = {top: 20, right: 120, bottom: 20, left: 120},
    width = 960 - margin.right - margin.left,
    height = 800 - margin.top - margin.bottom;

	var i = 0,
		duration = 750,
		root;

	var tree = d3.layout.tree()
		.size([height, width]);

	var diagonal = d3.svg.diagonal()
		.projection(function(d) { return [d.y, d.x]; });
		
	enableDateTimeUpdate();	

	clearDiagramContainer();
	
	var svg = d3.select("#diagramContainer").append("svg")
		.attr("width", width + margin.right + margin.left)
		.attr("height", height + margin.top + margin.bottom)
	  .append("g")
		.attr("transform", "translate(" + margin.left + "," + margin.top + ")");

	  root = data;
	  root.x0 = height / 2;
	  root.y0 = 0;

    function collapse(d) {
		if (d.children) {
			d._children = d.children;
			d._children.forEach(collapse);
			d.children = null;
		}
	}

    root.children.forEach(collapse);
    update(root);


	d3.select(self.frameElement).style("height", "800px");

	function update(source) {

	// Compute the new tree layout.
	var nodes = tree.nodes(root).reverse(),
		links = tree.links(nodes);
	  
	  

	// Normalize for fixed-depth.
	nodes.forEach(function(d) { d.y = d.depth * 180; });

	// Update the nodes…
	var node = svg.selectAll("g.node")
		.data(nodes, function(d) { return d.id || (d.id = ++i); });

	// Enter any new nodes at the parent's previous position.
	var nodeEnter = node.enter().append("g")
		.attr("class", "node")
		.attr("transform", function(d) { return "translate(" + source.y0 + "," + source.x0 + ")"; })
		.on("click", click);

	nodeEnter.append("circle")
		.attr("r", 1e-6)
		.style("fill", function(d) { return d._children ? "blue" : "#fff"; });

	nodeEnter.append("text")
		.attr("x", function(d) { return d.children || d._children ? -14 : 14; })
		.attr("dy", "-.35em")
		.attr("text-anchor", function(d) { return d.children || d._children ? "end" : "start"; })
		//.attr("text-anchor", "middle")
		.text(function(d) { return d.name; })
		.style("fill-opacity", 1e-6)
		.style('fill', 'white');
	  
	nodeEnter.append("text")
		.attr("x", function(d) { return d.children || d._children ? -14 : 14; })
		.attr("dy", ".55em")
		.attr("text-anchor", function(d) { return d.children || d._children ? "end" : "start"; })
		.text(function(d) { return d.size; })
		.style("fill-opacity", 1e-6)
		.style('fill', 'white');

	// Transition nodes to their new position.
	var nodeUpdate = node.transition()
		.duration(duration)
		.attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; });

	nodeUpdate.select("circle")
		.attr("r", 7.5)
		.style("fill", function(d) { return d._children ? "blue" : "#fff"; });

	nodeUpdate.selectAll("text")
		.style("fill-opacity", 1);

	// Transition exiting nodes to the parent's new position.
	var nodeExit = node.exit().transition()
		.duration(duration)
		.attr("transform", function(d) { return "translate(" + source.y + "," + source.x + ")"; })
		.remove();

	nodeExit.select("circle")
		.attr("r", 1e-6);

	nodeExit.selectAll("text")
		.style("fill-opacity", 1e-6);

	// Update the links…
	var link = svg.selectAll("path.link")
		.data(links, function(d) { return d.target.id; });

	// Enter any new links at the parent's previous position.
	link.enter().insert("path", "g")
		.attr("class", "link")
		.attr("d", function(d) {
			var o = {x: source.x0, y: source.y0};
			return diagonal({source: o, target: o});
		});

	// Transition links to their new position.
	link.transition()
		.duration(duration)
		.attr("d", diagonal);

	// Transition exiting nodes to the parent's new position.
	link.exit().transition()
		.duration(duration)
		.attr("d", function(d) {
			var o = {x: source.x, y: source.y};
			return diagonal({source: o, target: o});
		})
		.remove();

	// Stash the old positions for transition.
	nodes.forEach(function(d) {
		d.x0 = d.x;
		d.y0 = d.y;
	});
	}

	// Toggle children on click.
	function click(d) {
	  if (d.children) {
		d._children = d.children;
		d.children = null;
	  } else {
			if(d._children == null)
				listEvents(d);
			d.children = d._children;
			d._children = null;
	  }
	  update(d);
	}
}






var toolTipDescription = "";


async function drawCalendar(root) {

	d3.selectAll('#tooltip').remove();
	
	var minAndMaxYear = findMinAndMaxYearsOfData(root);
	
	var width = 900,
		height = 105,
		cellSize = 12; 
		week_days = ['Sun','Mon','Tue','Wed','Thu','Fri','Sat']
		month = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec']
		
	var day = d3.time.format("%w"),
		week = d3.time.format("%U"),
		percent = d3.format(".1%"),
		format = d3.time.format("%Y%m%d");
		parseDate = d3.time.format("%Y%m%d").parse;
			
	var color = d3.scale.linear()
		.domain([findMinCountEventsOfDay(root), findMaxCountEventsOfDay(root)])
		.range(["#ffffbf", "#a50026"]);

	clearDiagramContainer();
	
	disableDateTimeUpdate();
	
		
	var svg = d3.select("#diagramContainer")
		.selectAll("svg")
		.data(d3.range(minAndMaxYear[0], minAndMaxYear[1]+1))
		.enter().append("svg")
		.attr("width", '100%')
		.attr("data-height", '0.5678')
		.attr("viewBox",'0 0 900 105')
		.attr("class", "RdYlGn")
	  .append("g")
		.attr("transform", "translate(" + ((width - cellSize * 53) / 2) + "," + (height - cellSize * 7 - 1) + ")");
		
		
	//Give last calendar a margin so that it has some space from eventList
	var calendars = d3.select("#diagramContainer").selectAll("svg");
	calendars.filter(function(d, i) {
		return i === calendars.size() - 1;
	}).style('margin-bottom', '40px');
	//Give first calendar a margin on top
	calendars.filter(function(d, i) {
		return i === 0;
	}).style('margin-top', '20px');

	svg.append("text")
		.attr("transform", "translate(-38," + cellSize * 3.5 + ")rotate(-90)")
		.style("text-anchor", "middle")
		.text(function(d) { return d; })
		.style('fill', 'white');
	 
	for (var i=0; i<7; i++){    
		svg.append("text")
			.attr("transform", "translate(-5," + cellSize*(i+1) + ")")
			.style("text-anchor", "end")
			.attr("dy", "-.25em")
			.style('fill', 'white')
			.text(function(d) { return week_days[i]; }); 
	}

	var rect = svg.selectAll(".day")
		.data(function(d) { return d3.time.days(new Date(d, 0, 1), new Date(d + 1, 0, 1)); })
	    .enter()
		.append("rect")
		.attr("class", "day")
		.attr("width", cellSize)
		.attr("height", cellSize)
		.attr("x", function(d) { return week(d) * cellSize; })
		.attr("y", function(d) { return day(d) * cellSize; })
		.attr("fill",'#fff')
		.datum(format);

	var legend = svg.selectAll(".legend")
		  .data(month)
		  .enter().append("g")
		  .attr("class", "legend")
		  .attr("transform", function(d, i) { return "translate(" + (((i+1) * 50)+8) + ",0)"; });

	legend.append("text")
	   .attr("class", function(d,i){ return month[i] })
	   .style("text-anchor", "end")
	   .attr("dy", "-.25em")
	   .style('fill', 'white')
	   .text(function(d,i){ return month[i] });
	   
	svg.selectAll(".month")
		.data(function(d) { return d3.time.months(new Date(d, 0, 1), new Date(d + 1, 0, 1)); })
	  .enter().append("path")
		.attr("class", "month")
		.attr("id", function(d,i){ return month[i] })
		.attr("d", monthPath);
		
		
	root.forEach(function(d) {
	   d.value = parseInt(d.value);
	});

    var data = d3.nest()
		.key(function(d) { return d.date; })
		.rollup(function(d) { return d[0].value; })
		.map(root);		
		

		
   	var dataKeys = [];
	var dataTitle = "";
	
	var rects = rect.filter(function(d) { return d in data; })[0];
	
	
	for(var i in data){
		dataKeys.push(i);
	}
	
	
	//makes and sets a tooltip for each calendar day, also sets onclick function for each day and fills tile with the appropriate color
	async function appendDataTitles(dataKey, rect) {
		
		dataTitle = "";
		await getDataFromRequest(urlStart + "/gateways/count" + getURLForCalendarItem(dataKey))
				.then(
					async function(result){
						dataTitle += result + ")" + "<br>" + "probes(";
						
						await getDataFromRequest(urlStart + "/probes/count" + getURLForCalendarItem(dataKey))
							.then(
								async function(result){
									dataTitle += result + ")" + "<br>" + "samplers(";
									
									await getDataFromRequest(urlStart + "/samplers/count" + getURLForCalendarItem(dataKey))
										.then(
											async function(result){
												dataTitle += result + ")" + "<br>" + "events(";
												
													await getDataFromRequest(urlStart + "/count" + getURLForCalendarItem(dataKey))
														.then(
															async function(result){
																dataTitle += result + ")"+ "<br>" + "------critical(";
										
																await getDataFromRequest(urlStart + "/critical/count" + getURLForCalendarItem(dataKey))
																	.then(
																		async function(result){
																			dataTitle += result + ")"+ "<br>" + "-----warning(";
																			
																			await getDataFromRequest(urlStart + "/warning/count" + getURLForCalendarItem(dataKey))
																				.then(
																					async function(result){
																						dataTitle += result + ")"+ "<br>" + "----------OK(";
										
																						await getDataFromRequest(urlStart + "/OK/count" + getURLForCalendarItem(dataKey))
																							.then(
																								async function(result){
																									dataTitle += result + ")"+ "<br>" + "---undefined(";
																							
																										await getDataFromRequest(urlStart + "/undefined/count" + getURLForCalendarItem(dataKey))
																											.then(
																												async function(result){
																													dataTitle += result + ")";	
																
																													dataTitle = "date: " + dataKey.substring(4,6) + "-" + dataKey.substring(6,8)
																																+ "-" + dataKey.substring(0,4) + "<br>" + "gateways(" + dataTitle;
																													
																													rect
																														.setAttribute("fill", color(data[dataKey]))
																													rect	
																														.setAttribute('data-title', dataTitle)
																													rect
																														.onclick = async function(d) {  
																															await getDataFromRequest(urlStart + getURLForCalendarDay(dataKey))
																																.then(
																																	function(result){
																																		tabulateEvents(result); 
																																	}
																																);
																														} 
																												}
																											)
																								}
																							)
																					}
																				)
																		}
																	)														
															}
														)
											}
										)
								}
							)
					}
				)
	}
	
	for(var i = 0; i < rects.length; i++)
		await appendDataTitles(dataKeys[i], rects[i]);
		
	$("rect").tooltip({container: 'body', html: true, placement:'top'});
	

	function monthPath(t0) {
		var t1 = new Date(t0.getFullYear(), t0.getMonth() + 1, 0),
			d0 = +day(t0), w0 = +week(t0),
			d1 = +day(t1), w1 = +week(t1);
	    return "M" + (w0 + 1) * cellSize + "," + d0 * cellSize
			+ "H" + w0 * cellSize + "V" + 7 * cellSize
			+ "H" + w1 * cellSize + "V" + (d1 + 1) * cellSize
			+ "H" + (w1 + 1) * cellSize + "V" + 0
			+ "H" + (w0 + 1) * cellSize + "Z";
	} 
}
































//Enables the inputs for start and end times.
function enableDateTimeUpdate(){
	d3.select('#start').attr('disabled', null);
	d3.select('#start-time').attr('disabled', null);
	d3.select('#end').attr('disabled', null);
	d3.select('#end-time').attr('disabled', null);
	d3.select('#update-visual').attr('disabled', null);
}

//Disables the inputs for start and end times.
function disableDateTimeUpdate(){
	d3.select('#start').attr('disabled', 'disabled');
	d3.select('#start-time').attr('disabled', 'disabled');
	d3.select('#end').attr('disabled', 'disabled');
	d3.select('#end-time').attr('disabled', 'disabled');
	d3.select('#update-visual').attr('disabled', 'disabled');
}

//Gets values from the start and end inputs and formats them into a string that an be appended to 
//the end of an httpRequest
function getDateTimeURLForDiagram(diagramType){
	
	var url = urlStart + "/" + diagramType;
	var $startDate = $('#start');
	var $endDate = $('#end');
	var $startTime = $('#start-time');
	var $endTime = $('#end-time');

	var startDateObj = new Date($startDate.val().toString() + ":" + ($startTime.val()));
	var endDateObj = new Date($endDate.val().toString() + ":" + ($endTime.val()));

	startDateObj = convertUTCDateToLocalDate(startDateObj);
	endDateObj = convertUTCDateToLocalDate(endDateObj);

	var start = toTimestamp(startDateObj);
	var end = toTimestamp(endDateObj);

	if(isNaN(start) == false && isNaN(end) == false)
		url += "?start=" + start + "&end=" + end;
	else if(isNaN(start) == false && isNaN(end))
		url += "?start=" + start;
	else if(isNaN(start) && isNaN(end) == false)
		url += "?end="+end;

	return url;
}

//Returns an array containing the earliest year that an event was logged and puts it in the first position
//and the last year that an event was logged in the second position.
function findMinAndMaxYearsOfData(data) {

	var years = [];
	var minYear = 0;
	var maxYear = 0;
	var minAndMaxYear = [];

	for (var e in data){
		years[e] = (data[e].date.substring(0,4));
	}

	minAndMaxYear[0] = Math.min.apply(null, years);
	minAndMaxYear[1] = Math.max.apply(null, years);
	
	return minAndMaxYear;
}

//Returns the count of events from the day with the most events.
function findMaxCountEventsOfDay(data) {
	var values = [];
	for(var e in data){
		values[e] = data[e].value;
	}
	return Math.max.apply(null, values);
}

//Returns the count of events from the day with the least events.
function findMinCountEventsOfDay(data) {
	var values = [];
	for(var e in data){
		values[e] = data[e].value;
	}
	return Math.min.apply(null, values);
}

//formats the start and end parameters for any request url that requires start and end times
function getURLForCalendarDay(date){
	
	var year        = date.substring(0,4);
	var month       = date.substring(4,6);
	var day         = date.substring(6,8);
	var nextDay		= parseInt(day) + 1;

	var date        = new Date(year, month-1, day);
	var secondDate  = new Date(year, month-1, nextDay.toString());

	return ("/start/" + 
				(date.getTime() / 1000) + "/end/" + 
				(secondDate.getTime() / 1000));
}

//formats the start and end parameters for any request url that has optional start and end times
function getURLForCalendarItem(date){
	
	var year        = date.substring(0,4);
	var month       = date.substring(4,6);
	var day         = date.substring(6,8);
	var nextDay		= parseInt(day) + 1;

	var date        = new Date(year, month-1, day);
	var secondDate  = new Date(year, month-1, nextDay.toString());
	
	return ("?start=" + 
				(date.getTime() / 1000)  + "&end=" + 
				(secondDate.getTime() / 1000));
}


//returns data from an httpRequest
async function getDataFromRequest(url) {
	let response = await fetch(url);

	if(response.status == 200)
		return response.json();
}

//removes any svg or data tables present so a new one can take its place
function clearDiagramContainer() {
	//remove svg if one exists
	if(!(d3.select('body').select('svg').empty()))
		d3.selectAll('svg').remove()
	//remove table if exists
	if(!(d3.select('body').select('.clusterize').empty()))
		d3.select('.clusterize').remove()
}

//creates a request url for a specific child node of a visual and then sends the request and
//creates a table which displays the data returned
function listEvents(p) {
	var url = "";
	var parentCount = 0;
	var types = ["gateways", "probes", "samplers", "severity"];
	
	var tempObj = p;
	//count number of parents besides root parent
	while(tempObj.parent.parent){
		parentCount++;
		tempObj = tempObj.parent;
	}

	while(p.parent){
/* 		if(p.name == "critical")
			url = types[parentCount] + "/" + 2 + "/" + url;
		else if(p.name == "warning")
			url = types[parentCount] + "/" + 1 + "/" + url;
		else if(p.name == "OK")
			url = types[parentCount] + "/" + 0 + "/" + url;
		else if(p.name == "undefined")
			url = types[parentCount] + "/" + -1 + "/" + url;
		else
			url = types[parentCount] + "/" + p.name + "/" + url; */
		
		if(p.name == "critical")
			url = url;
		else if(p.name == "warning")
			url = url;
		else if(p.name == "OK")
			url = url;
		else if(p.name == "undefined")
			url = url;
		else
			url = types[parentCount] + "/" + p.name + "/" + url; 
		
			
		p = p.parent;
		parentCount--;
	}
	
	url = urlStart + "/" + url;
	getDataFromRequest(url).then(function(result){
		tabulateEvents(result);
	}); 
}

//helper method which returns a table with the columns specified
function tabulate(data, columns) {

	if(!(d3.select('body').select('#dataTable').empty()))
		d3.select('#dataTable').remove();
	
	var table = d3.select('body')
		.append('table')
		.attr("id", "dataTable")
		.attr("class", "table table-striped");
	var thead = table.append('thead')
		.attr("class", "thead-dark");
	var	tbody = table.append('tbody');

	// append the header row
	thead.append('tr')
			.selectAll('th')
			.data(columns).enter()
			.append('th')
			.text(function (column) { return column; });

	// create a row for each object in the data
	var rows = tbody.selectAll('tr')
			.data(data)
			.enter()
			.append('tr');
			

	// create a cell in each row for each column
	var cells = rows.selectAll('td')
			.data(function (row) {
				return columns.map(function (column) {
					return {column: column, value: row[column]};
				});
			})
			.enter()
			.append('td')
			.text(function (d) { return d.value; });
			
	return table;
}

//appends rows containing event data to the table
function tabulateEvents(data){
	
	var dataRows = [];
	for(var i = 0; i < data.length; i++){
		
		dataRows.push(				
			'<tr><td>'+data[i].ref+'</td><td>'+data[i].timestamp+'</td><td>'+data[i].node_ref+'</td><td>'+
					data[i].varname+'</td><td class="sev">'+data[i].severity+'</td><td>'+data[i].description+'</td><td>'+data[i].gateway+'</td><td>'+
					data[i].probe+'</td><td>'+data[i].managed_entity+'</td><td>'+data[i].sampler+'</td><td>'+data[i].type+'</td><td>'+data[i].dataview+'</td><td>'+
					data[i].headline+'</td><td>'+data[i].rowname+'</td><td>'+data[i].columnname+'</td></tr>'
		);
	} 
	
	if(d3.select('body').select('.clusterize').empty())
		createEventListTable();

	var clusterize = new Clusterize({
	  rows: dataRows,
	  scrollId: 'scroll-area',
	  contentId: 'content-area',
	  rows_in_block: 10
	}); 	
	

}



 
//creates a table with columns for each data in an event
function createEventListTable(){
	
	var columns = ['ref', 'timestamp', 'node_ref', 'varname', 'severity',
		'description', 'gateway', 'probe', 'managed_entity', 'sampler',
		'type', 'dataview', 'headline', 'rowname', 'columnname'];
	
	var cluster = d3.select('body')
		.append('div').attr('class', 'clusterize');
		
	var headerTable = cluster
		.append('table')
		.attr('class', 'headers-area');
	
	var scrollArea = cluster
		.append('div')
		.attr('class', 'clusterize-scroll')		
		.attr('id', 'scroll-area');
		
	var table = scrollArea
		.append('table')
		.attr('id', 'eventTable')
	
	//Make header
	var headerRow = headerTable
		.append('thead').append('tr');
		
	var th = headerRow.selectAll('th')
		.data(columns).enter()
		.append('th').text(function(d) { return d; })
		
			
	var tbody = table.append('tbody')
		.attr('class', 'clusterize-content')
		.attr('id', 'content-area')
}