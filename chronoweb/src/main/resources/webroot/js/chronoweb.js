
let validationURL;
let captureURL;
let queryURL;
let captureJobList;
let nextPageToken;
let nextResourcePageToken = '';
let resourceTypeLocal;
let resourceLocal;

// Server connection check and Show status
function serverConn(url, circle) {
	$.ajax({
		url: url,
		crossOrigin: true,
	}).done(() => {
		$(circle).removeClass('bg-danger').addClass("bg-success");
	}).fail(() => {
		$(circle).removeClass('bg-success').addClass("bg-danger");
	});
}

function getData(gammas){
	
	nodeSet = new Set();
	edgeSet = new Set();
	
	for(let gamma in gammas ){
		let path = gammas[gamma]['path'];
		let times = gamma['times'];
		for(let i = 0 ; i < path.length ; i++){
			nodeSet.add(path[i]);
			if(i != 0){
				edgeSet.add(path[i-1] + "," + path[i]);
			}
		}
	}
	
	node = [];
	for(let n of nodeSet){
		node.push({id: n, group: 1});
	}
	edge = [];
	for(let e of edgeSet){
		earr =	e.split(',');
		edge.push({source: earr[0], target: earr[1], value: 1});
	}
	
	data = {};
	data.nodes = node;
	data.links = edge; 
	return data;
}

function getData2(source, gammas){
	data = {};
	data.source = null;
	
	for(let gamma in gammas ){
		let path = gammas[gamma]['path'];
		let times = gamma['times'];
		for(let i = 0 ; i < path.length ; i++){
			nodeSet.add(path[i]);
			if(i != 0){
				edgeSet.add(path[i-1] + "," + path[i]);
			}
		}
	}
}

function visualize(url, containerID) {

	$.ajax({
		type: "GET",
		url: url,
		crossOrigin: true,
	}).done((result) => {
		
		data = getData(result["gamma"]);
		
		$("#" + containerID).html("");

		// Specify the dimensions of the chart.
		const width = 928;
		const height = 600;
		// Specify the color scale.
		const color = d3.scaleOrdinal(d3.schemeCategory10);

		// The force simulation mutates links and nodes, so create a copy
		// so that re-evaluating this cell produces the same result.

		//data = {
		//	nodes: [
		//		{ id: "a", group: 1 }, { id: "b", group: 1 }, { id: "c", group: 1 }
		//	],
		//	links: [
		//		{ source: "a", target: "b", value: 1 }, { source: "b", target: "c", value: 1 }, { source: "c", target: "a", value: 1 }
		//	]
		//}

		const links = data.links.map(d => ({ ...d }));
		const nodes = data.nodes.map(d => ({ ...d }));

		// Create a simulation with several forces.
		const simulation = d3.forceSimulation(nodes)
			.force("link", d3.forceLink(links).id(d => d.id))
			.force("charge", d3.forceManyBody())
			.force("center", d3.forceCenter(width / 2, height / 2))
			.on("tick", ticked);

		// Create the SVG container.
		const svg = d3.create("svg")
			.attr("width", width)
			.attr("height", height)
			.attr("viewBox", [0, 0, width, height])
			.attr("style", "max-width: 100%; height: auto;");

		// Add a line for each link, and a circle for each node.
		const link = svg.append("g")
			.attr("stroke", "#999")
			.attr("stroke-opacity", 0.6)
			.selectAll()
			.data(links)
			.join("line")
			.attr("stroke-width", d => Math.sqrt(d.value));

		const node = svg.append("g")
			.attr("stroke", "#fff")
			.attr("stroke-width", 1.5)
			.selectAll()
			.data(nodes)
			.join("circle")
			.attr("r", 5)
			.attr("fill", d => color(d.group));

		node.append("title")
			.text(d => d.id);

		// Add a drag behavior.
		node.call(d3.drag()
			.on("start", dragstarted)
			.on("drag", dragged)
			.on("end", dragended));

		// Set the position attributes of links and nodes each time the simulation ticks.
		function ticked() {
			link
				.attr("x1", d => d.source.x)
				.attr("y1", d => d.source.y)
				.attr("x2", d => d.target.x)
				.attr("y2", d => d.target.y);

			node
				.attr("cx", d => d.x)
				.attr("cy", d => d.y);
		}

		// Reheat the simulation when drag starts, and fix the subject position.
		function dragstarted(event) {
			if (!event.active) simulation.alphaTarget(0.3).restart();
			event.subject.fx = event.subject.x;
			event.subject.fy = event.subject.y;
		}

		// Update the subject (dragged node) position during drag.
		function dragged(event) {
			event.subject.fx = event.x;
			event.subject.fy = event.y;
		}

		// Restore the target alpha so the simulation cools after dragging ends.
		// Unfix the subject position now that it’s no longer being dragged.
		function dragended(event) {
			if (!event.active) simulation.alphaTarget(0);
			event.subject.fx = null;
			event.subject.fy = null;
		}

		// When this cell is re-run, stop the previous simulation. (This doesn’t
		// really matter since the target alpha is zero and the simulation will
		// stop naturally, but it’s a good practice.)
		// invalidation.then(() => simulation.stop());

		// Append the SVG element.
		$("#" + containerID).append(svg.node());

	}).fail((result) => {
		console.log(result);
	});
}

function getGammaTableSources(url, sourceContainerID, sourceMenuID, visualizeButtonID, containerID) {
	$.ajax({
		type: "GET",
		url: url,
		crossOrigin: true,
	}).done((result) => {
		$("#" + sourceContainerID).html("");

		for (let key in result['gammaSources']) {
			var newItem = $('<a class="dropdown-item" href="#">' + result['gammaSources'][key] + '</a>');
			newItem.click(function() {
				$("#" + sourceMenuID).text($(this).text());
				$("#" + visualizeButtonID).removeClass('invisible');
				$("#" + visualizeButtonID).click(function() {
					visualize(url + "/" + result['gammaSources'][key], containerID);
				});
				// getGammaTablePrograms(url, programContainerID, programMenuID);
			});
			$("#" + sourceContainerID).append(newItem);
		}
	}).fail((result) => {
		console.log(result);
	});
}

function getGammaTablePrograms(url, edgeLabel, programContainerID, programMenuID, sourceContainerID, sourceMenuID, visualizeButtonID, containerID) {
	$.ajax({
		type: "GET",
		url: url,
		crossOrigin: true,
	}).done((result) => {

		$("#" + programContainerID).html("");
		for (var key in result) {

			var newItem = $('<a class="dropdown-item" href="#">' + result[key] + '</a>');
			newItem.click(function() {
				$("#" + programMenuID).text($(this).text());
				getGammaTableSources(url + "/" + result[key] + "/" + $("#" + edgeLabel).val(), sourceContainerID, sourceMenuID, visualizeButtonID, containerID);
			});
			$("#" + programContainerID).append(newItem);
		}
	}).fail((result) => {
		console.log(result);
	});
}

function getGammaTableStartTimes(url, edgeLabel, timeContainerID, timeMenuID, programContainerID, programMenuID, sourceContainerID, sourceMenuID, visualizeButtonID, containerID) {
	$("#" + visualizeButtonID).addClass('invisible');
	$.ajax({
		type: "GET",
		url: url,
		crossOrigin: true,
	}).done((result) => {
		$("#" + timeContainerID).html("");
		for (key in result) {
			var newItem = $('<a class="dropdown-item" href="#">' + result[key] + '</a>');
			newItem.click(function() {
				$("#" + timeMenuID).text($(this).text());
				getGammaTablePrograms(url + "/" + result[key], edgeLabel, programContainerID, programMenuID, sourceContainerID, sourceMenuID, visualizeButtonID, containerID);
			});
			$("#" + timeContainerID).append(newItem);
		}
	}).fail((result) => {
		console.log(result);
	});
}