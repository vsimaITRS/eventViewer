package com.eventViewer.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.datetime.joda.DateTimeParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eventViewer.api.model.Event;
import com.eventViewer.data.EventRepository;

@CrossOrigin(origins = "http://localhost", maxAge = 3600)
@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @RequestMapping(value = "",
    		produces = { MediaType.APPLICATION_JSON_VALUE },
    		method = RequestMethod.GET)
    public ResponseEntity<List<Event>> getAllEvents(@RequestParam("start") Optional<Long> start,
			  @RequestParam("end")   Optional<Long> end) {
    	
    	List<Event> events; 
    	
    	if(start != null && end != null)
    		events = eventRepository.findEventsBetweenOptionalTimes(start, end);
    	else if(start != null && end == null)
    		events = eventRepository.findEventsBetweenOptionalTimes(start, null);
    	else if(start == null && end != null)
    		events = eventRepository.findEventsBetweenOptionalTimes(null, end);
    	else
    		events = eventRepository.findAll();
    	    	
    	if(events.isEmpty())
    		return new ResponseEntity(HttpStatus.NO_CONTENT);
        return new ResponseEntity<List<Event>>(events, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/zoomableSunburstJSON",
    		produces = { MediaType.APPLICATION_JSON_VALUE },
    		method = RequestMethod.GET)
    public ResponseEntity<String> getZoomableSunburstJSON(@RequestParam("start") Optional<Long> start,
    													  @RequestParam("end")   Optional<Long> end) throws JSONException {
    	
    	String formattedJSON;
    	
    	if(false)
    		return new ResponseEntity(HttpStatus.NO_CONTENT);
    	else {
        	if(start != null && end != null)
        		formattedJSON = formatDiagram("zoomableSunburst", start, end);
        	else if(start != null && end == null)
        		formattedJSON = formatDiagram("zoomableSunburst", start, null);
        	else if(start == null && end != null)
        		formattedJSON = formatDiagram("zoomableSunburst", null, end);
        	else
        		formattedJSON = formatDiagram("zoomableSunburst", null, null);
    	}
    	
    		return new ResponseEntity<String>(formattedJSON, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/collapsibleTreeJSON",
    		produces = { MediaType.APPLICATION_JSON_VALUE },
    		method = RequestMethod.GET)
    public ResponseEntity<String> getcollapsibleTreeJSON(@RequestParam("start") Optional<Long> start,
    													  @RequestParam("end")   Optional<Long> end) throws JSONException {
    	
    	String formattedJSON;
    	
    	if(false)
    		return new ResponseEntity(HttpStatus.NO_CONTENT);
    	else {
        	if(start != null && end != null)
        		formattedJSON = formatDiagram("collapsibleTree", start, end);
        	else if(start != null && end == null)
        		formattedJSON = formatDiagram("collapsibleTree", start, null);
        	else if(start == null && end != null)
        		formattedJSON = formatDiagram("collapsibleTree", null, end);
        	else
        		formattedJSON = formatDiagram("collapsibleTree", null, null);
    	}
    	
    		return new ResponseEntity<String>(formattedJSON, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/calendarViewJSON",
    		produces = { MediaType.APPLICATION_JSON_VALUE },
    		method = RequestMethod.GET)
    public ResponseEntity<String> getZoomableSunburstJSON() throws JSONException {
    	
    	String formattedJSON;
    	
    	if(false)
    		return new ResponseEntity(HttpStatus.NO_CONTENT);
    	else
    		formattedJSON = formatCalendarView();
    	
    		return new ResponseEntity<String>(formattedJSON, HttpStatus.OK);
    	
    }
    
    @RequestMapping(value = "/start/{start}/end/{end}",
    		produces = { MediaType.APPLICATION_JSON_VALUE },
    		method = RequestMethod.GET)
    public ResponseEntity<List<Event>> getAllEventsInSelectedCalendarDay(@PathVariable("start") String start,
    		@PathVariable("end") String end) {
    	
    	List<Event> events = eventRepository.findEventsBetweenTimes(Long.parseLong(start), Long.parseLong(end));
    	    	
    	if(events.isEmpty())
    		return new ResponseEntity(HttpStatus.NO_CONTENT);
        return new ResponseEntity<List<Event>>(events, HttpStatus.OK);
    }
    
    
    //GET DISTINCT GATEWAYS FROM ALL EVENTS
    @RequestMapping(value = "/gateways", method = RequestMethod.GET)
    public ResponseEntity<List<String>> getDistinctGateways() {
    	
    	List<String> gateways = eventRepository.findDistinctGateways(null, null);
    	if(gateways.isEmpty())
    		return new ResponseEntity(HttpStatus.NO_CONTENT);    	
    	return new ResponseEntity<List<String>>(gateways, HttpStatus.OK);
    }

    //GET DISTINCT PROBES FROM ALL EVENTS
    @RequestMapping(value = "/probes", method = RequestMethod.GET)
    public ResponseEntity<List<String>> getDistinctProbes() {
    	
    	List<String> probes = eventRepository.findDistinctProbes(null, null);
    	if(probes.isEmpty())
    		return new ResponseEntity(HttpStatus.NO_CONTENT);    	
    	return new ResponseEntity<List<String>>(probes, HttpStatus.OK);
    }
    
    //GET DISTINCT SAMPLERS FOR A SPECIFIC PROBE
    @RequestMapping(value = "/probes/{probe}/samplers", method = RequestMethod.GET)
    public ResponseEntity<List<String>> getDistinctSamplersByProbeName(@PathVariable("probe") String probe) {
    	
    	List<String> samplers = eventRepository.findDistinctSamplersByProbe(probe, null, null);
    	if(samplers.isEmpty())
    		return new ResponseEntity(HttpStatus.NO_CONTENT);
    	
    	return new ResponseEntity<List<String>>(samplers, HttpStatus.OK);
    }
    
    //GET EVENT COUNT BASED ON SPECIFIC PROBE, SAMPLER, AND SEVERITY
    @RequestMapping(value = "/gateways/{gateway}/probes/{probe}/samplers/{sampler}/severity/{severity}", produces = { MediaType.APPLICATION_JSON_VALUE }, method = RequestMethod.GET)
    public ResponseEntity<?> getEventsByEntity(@PathVariable("gateway") String gateway, @PathVariable("probe") String probe, 
    				@PathVariable("sampler") String sampler, @PathVariable("severity") String severity, @RequestParam("start") Optional<Long> start,
    				@RequestParam("end") Optional<Long> end) {
    	
    	if(severity.equals("critical"))
    			severity = "2";
    	else if(severity.equals("warning"))
			severity = "1";
    	else if(severity.equals("OK"))
			severity = "0";
    	else if(severity.equals("undefined"))
			severity = "-1";
    	
    	List<Event> events; 
    	    	
    	if(start != null && end != null)
    		events = eventRepository.findEventsByEntity(gateway, probe, sampler, severity, start, end);
    	else if(start != null && end == null)
    		events = eventRepository.findEventsByEntity(gateway, probe, sampler, severity, start, null);
    	else if(start == null && end != null)
    		events = eventRepository.findEventsByEntity(gateway, probe, sampler, severity, null, end);
    	else
    		events = eventRepository.findEventsByEntity(gateway, probe, sampler, severity, null, null);
    	    	
    	if(events.isEmpty())
    		return new ResponseEntity(HttpStatus.NO_CONTENT);
        return new ResponseEntity<List<Event>>(events, HttpStatus.OK);
    }
    
    
    public String formatDiagram(String diagram, Optional<Long> start, Optional<Long> end) throws JSONException {
    	
    	JSONObject diagramRoot = new JSONObject();
    	
    	if(diagram.equals("zoomableSunburst"))
    		diagramRoot.put("name", "flare");
    	else if(diagram.equals("collapsibleTree"))
    		diagramRoot.put("name", "gateways");
			
			JSONArray flareChildren = new JSONArray();
			
			List<String> gateways = eventRepository.findDistinctGateways(start, end);
			
			for(String gatewayName : gateways)	{
				
				JSONArray gatewayChildren = new JSONArray();
				JSONObject gateway = new JSONObject();
				gateway.put("name", gatewayName);
				gateway.put("children", gatewayChildren);
				
				flareChildren.put(gateway);
				
				List<String> netProbes = eventRepository.findDistinctProbesByGateway(gatewayName, start, end);
				
				
				for(String netProbeName : netProbes) {
					
					JSONArray netProbeChildren = new JSONArray();
					JSONObject netProbe = new JSONObject();
					netProbe.put("name", netProbeName);
					netProbe.put("children", netProbeChildren);
					
					gatewayChildren.put(netProbe);
					
					List<String> samplers = eventRepository.findDistinctSamplersByProbe(netProbeName, start, end);
					
					for(String samplerName : samplers) {
						
						JSONArray samplerChildren = new JSONArray();
						
						JSONObject sampler = new JSONObject();
						sampler.put("name", samplerName);
						sampler.put("children", samplerChildren);
						
						netProbeChildren.put(sampler);
						
						JSONObject undefined = new JSONObject();
						JSONObject OK = new JSONObject();
						JSONObject warning = new JSONObject();
						JSONObject critical = new JSONObject();
						
						undefined.put("name", "undefined");
						undefined.put("size", eventRepository.findEventCountBySeverity(gatewayName, netProbeName, samplerName, "-1", start, end));
						
						OK.put("name", "OK");
						OK.put("size", eventRepository.findEventCountBySeverity(gatewayName, netProbeName, samplerName, "0", start, end));
						
						warning.put("name", "warning");
						warning.put("size", eventRepository.findEventCountBySeverity(gatewayName, netProbeName, samplerName, "1", start, end));
						
						critical.put("name", "critical");
						critical.put("size", eventRepository.findEventCountBySeverity(gatewayName, netProbeName, samplerName, "2", start, end));
						
						samplerChildren.put(undefined);
						samplerChildren.put(OK);
						samplerChildren.put(warning);
						samplerChildren.put(critical);
											
					}
				}
			}
			
			diagramRoot.put("children", flareChildren);
			
			return diagramRoot.toString();
    }
    
//    public String formatCollapsibleTree() {
//    	
//    	JSONArray array = new JSONArray();
//    	
//    	List records = eventRepository.findTimestampsAndSeveritys();
//    	
//    	
//    	JSONObject record = new JSONObject();
//    
//    	return null;
//    }
    
    public String convertTime(long time){
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        return format.format(date);
    }
    
    public String formatCalendarView() throws JSONException {
    	
    	JSONArray array = new JSONArray();
    	List<Event> events = eventRepository.findAll();
    	
    	HashMap<String, Integer> map = new HashMap<>();
    	
    	for(Event e : events) {
    		
	    	long timestamp = e.getTimestamp();
	    	String date = convertTime(timestamp*1000);
	    	
    		if(map.containsKey(date))
    			map.put(date, map.get(date) + 1);
    		else
    			map.put(date, 1);
    	}
    	
		for(Map.Entry<String, Integer> e : map.entrySet()) {
			
	    	JSONObject record = new JSONObject();
	    	
	    	record.put("date", e.getKey());
	    	record.put("value", e.getValue());
	    	array.put(record);
		}
    	
    	return array.toString();
    }
}
