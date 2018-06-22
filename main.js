$(document).ready(function() {
    $('.dropdown').each(function (key, dropdown) {
        var $dropdown = $(dropdown);
        $dropdown.find('.dropdown-menu a').on('click', function() {
            $dropdown.find('button').text($(this).text()).append(' <span class="caret"></span>');
			
			if($dropdown.find('button').text() == "Zoomable Sunburst "){
				
				var url = "http://192.168.2.86:8080/api/events/zoomableSunburstJSON";
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
			
				getDataFromRequest(url).then(function(result){
					createZoomableSunburst(result);
				});
			}
        });
    });
});


function toTimestamp(strDate){
	var datum = Date.parse(strDate);
	return datum/1000;
}

function convertUTCDateToLocalDate(date) {
    return new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate(),  date.getHours(), 
								date.getMinutes(), date.getSeconds()));
}


function createZoomableSunburst(root){


	var margin = {top: 350, right: 480, bottom: 350, left: 480},
		radius = Math.min(margin.top, margin.right, margin.bottom, margin.left) - 10;
		
	var hue = d3.scale.category20c();
	
	var luminance = d3.scale.sqrt()
		.domain([0, 1e6])
		.clamp(true)
		.range([90, 20]);
	
	var svg = d3.select("body").append("svg")
		.attr("width", margin.left + margin.right)
		.attr("height", margin.top + margin.bottom)
		.append("g")
		.attr("transform", "translate(" + margin.left + "," + margin.top + ")");
		
	var partition = d3.layout.partition()
		.sort(function(a, b) {return d3.ascending(a.name, b.name);})
		.size([2 * Math.PI, radius]);
		
	var arc = d3.svg.arc()
		.startAngle(function(d) { return d.x; })
		.endAngle(function(d) { return d.x + d.dx ; })
		.padAngle(.01)
		.padRadius(radius / 3)
		.innerRadius(function(d) { return radius / 3 * d.depth; })
		.outerRadius(function(d) { return radius / 3 * (d.depth + 1) - 1 });

	partition
		.value(function(d) { return d.size; })
		.nodes(root)
		.forEach(function(d) {
			d._children = d.children;
			d.sum = d.value;
			d.key = key(d);
			d.fill = fill(d);			
		});

	partition
		.children(function(d, depth) { return depth < 2 ? d._children : null; })
		.value(function(d) { return  d.sum; });

	var center = svg.append("circle")
		.attr("r", radius / 3)
		.on("click", zoomOut)
		
	center.append("title")
		.text("zoom out");
		
		  var path = svg.selectAll("path")
      .data(partition.nodes(root).slice(1))
    .enter().append("path")
      .attr("d", arc)
      .style("fill", function(d) { return d.fill; })
      .each(function(d) { this._current = updateArc(d); })
      .on("click", zoomIn);
	  
	  var text = svg.selectAll("text")
	  .data(partition.nodes(root).slice(1))
	  .enter().append("text")
	  .attr("transform", function(d) {
                return "translate(" + arc.centroid(d) + ")rotate(" + computeTextRotation(d) + ")"; })
            .attr("dx", "-20")
            .attr("dy", ".5em")
            .text(function(d) { return d.parent ? d.data.name : "" });
		};
	  
	  
  function zoomIn(p) {
    if (p.depth > 1) p = p.parent;
    if (!p.children) return;
    zoom(p, p);
  }
  function zoomOut(p) {
    if (!p.parent) return;
    zoom(p.parent, p);
  }
  // Zoom to the specified new root.
  function zoom(root, p) {
    if (document.documentElement.__transition__) return;
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
      return {depth: d.depth + 1, x: outsideAngle(d.x), dx: outsideAngle(d.x + d.dx) - outsideAngle(d.x)};
    }
    center.datum(root);
    // When zooming in, arcs enter from the outside and exit to the inside.
    // Entering outside arcs start from the old layout.
    if (root === p) enterArc = outsideArc, exitArc = insideArc, outsideAngle.range([p.x, p.x + p.dx]);
    path = path.data(partition.nodes(root).slice(1), function(d) { return d.key; });
    // When zooming out, arcs enter from the inside and exit to the outside.
    // Exiting outside arcs transition to the new layout.
    if (root !== p) enterArc = insideArc, exitArc = outsideArc, outsideAngle.range([p.x, p.x + p.dx]);
    d3.transition().duration(d3.event.altKey ? 7500 : 750).each(function() {
      path.exit().transition()
          .style("fill-opacity", function(d) { return d.depth === 1 + (root === p) ? 1 : 0; })
          .attrTween("d", function(d) { return arcTween.call(this, exitArc(d)); })
          .remove();
      path.enter().append("path")
          .style("fill-opacity", function(d) { return d.depth === 2 - (root === p) ? 1 : 0; })
          .style("fill", function(d) { return d.fill; })
          .on("click", zoomIn)
          .each(function(d) { this._current = enterArc(d); });
		 
		text.exit().transition()
          .attrTween("d", function(d) { return arcTweenText.call(this, exitArc(d)); })
          .remove();
		 
	  text.enter().append("text")
		.attr("transform", function(d) {
                return "translate(" + arc.centroid(d) + ")rotate(" + computeTextRotation(d) + ")"; })
            .attr("dx", "-20")
            .attr("dy", ".5em")
            .text(function(d) { return d.parent ? d.data.name : "" });

	  
	  
      path.transition()
          .style("fill-opacity", 1)
          .attrTween("d", function(d) { return arcTween.call(this, updateArc(d)); });
		  
		text.transition()
			.attrTween("d", function(d) { return arcTweenText.call(this, updateArc(d)); });
    });
  }
  
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
d3.select(self.frameElement).style("height", margin.top + margin.bottom + "px");


function arcTweenText(a, i) {
        var oi = d3.interpolate({ x0: a.x0s, x1: a.x1s }, a);
        function tween(t) {
            var b = oi(t);
            return "translate(" + arc.centroid(b) + ")rotate(" + computeTextRotation(b) + ")";
        }
        return tween;
    }

function computeTextRotation(d) {
        var angle = (d.x0 + d.x1) / Math.PI * 90;
        // Avoid upside-down labels
        return (angle < 120 || angle > 270) ? angle : angle + 180;  // labels as rims
        //return (angle < 180) ? angle - 90 : angle + 90;  // labels as spokes
    }

	/* var width = 960,
		height = 700,
		radius = Math.min(width, height) / 2;

	var x = d3.scale.linear()
		.range([0, 2 * Math.PI]);

	var y = d3.scale.linear()
		.range([0, radius]);

	var color = d3.scale.category20c();

	var svg = d3.select("body").append("svg")
		.attr("width", width)
		.attr("height", height)
	  .append("g")
		.attr("transform", "translate(" + width / 2 + "," + (height / 2 + 10) + ")");

	var partition = d3.layout.partition()
		.value(function(d) { return d.size; });

	var arc = d3.svg.arc()
		.startAngle(function(d) { return Math.max(0, Math.min(2 * Math.PI, x(d.x))); })
		.endAngle(function(d) { return Math.max(0, Math.min(2 * Math.PI, x(d.x + d.dx))); })
		.innerRadius(function(d) { return Math.max(0, y(d.y)); })
		.outerRadius(function(d) { return Math.max(0, y(d.y + d.dy)); });


    var g = svg.selectAll("g")
        .data(partition.nodes(root))
      .enter().append("g");

    var path = g.append("path")
      .attr("d", arc)
      .style("fill", function(d) { return color((d.children ? d : d.parent).name); })
      .on("click", click);

    var text = g.append("text")
      .attr("transform", function(d) { return "rotate(" + computeTextRotation(d) + ")"; })
      .attr("x", function(d) { return y(d.y); })
      .attr("dx", "6") // margin
      .attr("dy", ".35em") // vertical-align
	  .attr("visibility",function(d) { return d.dx < 0.01? "hidden" : "visible"})
      .text(function(d) { return d.name; });

    function click(d) {
      // fade out all text elements
      text.transition().attr("opacity", 0);

      path.transition()
        .duration(750)
        .attrTween("d", arcTween(d))
        .each("end", function(e, i) {
            // check if the animated element's data e lies within the visible angle span given in d
            if (e.x >= d.x && e.x < (d.x + d.dx)) {
              // get a selection of the associated text element
              var arcText = d3.select(this.parentNode).select("text");
              // fade in the text element and recalculate positions
              arcText.transition().duration(750)
                .attr("opacity", 1)
                .attr("transform", function() { return "rotate(" + computeTextRotation(e) + ")" })
                .attr("x", function(d) { return y(d.y); });
            }
        });
    } */

/* 	d3.select(self.frameElement).style("height", height + "px");
	
	// Interpolate the scales!
	function arcTween(d) {
	  var xd = d3.interpolate(x.domain(), [d.x, d.x + d.dx]),
		  yd = d3.interpolate(y.domain(), [d.y, 1]),
		  yr = d3.interpolate(y.range(), [d.y ? 20 : 0, radius]);
	  return function(d, i) {
		return i
			? function(t) { return arc(d); }
			: function(t) { x.domain(xd(t)); y.domain(yd(t)).range(yr(t)); return arc(d); };
	  };
	}

	function computeTextRotation(d) {
	    return (x(d.x + d.dx / 2) - Math.PI / 2) / Math.PI * 180;
	} */

}
async function getDataFromRequest(url) {

	let response = await fetch(url);

	if(response.status == 200) {
	
		return response.json();
	}
}


