package com.eventViewer.api.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
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
        		formattedJSON = formatZoomableSunburst(start, end);
        	else if(start != null && end == null)
        		formattedJSON = formatZoomableSunburst(start, null);
        	else if(start == null && end != null)
        		formattedJSON = formatZoomableSunburst(null, end);
        	else
        		formattedJSON = formatZoomableSunburst(null, null);
    		return new ResponseEntity<String>(formattedJSON, HttpStatus.OK);
    	}
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
    @RequestMapping(value = "/gateways/{gateway}/probes/{probe}/samplers/{sampler}/severity/{severity}", method = RequestMethod.GET)
    public ResponseEntity<?> getEventCountByProbeSamplerSeverity(@PathVariable("gateway") String gateway, @PathVariable("probe") String probe, @PathVariable("sampler") String sampler, @PathVariable("severity") String severity) {
    	
    	int count = eventRepository.findEventCountBySeverity(gateway, probe, sampler, severity, null, null);
    	return new ResponseEntity(count, HttpStatus.OK);
    }
    
    
    public String formatZoomableSunburst(Optional<Long> start, Optional<Long> end) throws JSONException {
    
    	JSONObject flare = new JSONObject();
			flare.put("name", "flare");
			
			JSONArray flareChildren = new JSONArray();
			
			List<String> gateways = eventRepository.findDistinctGateways(start, end);
			
			JSONArray gatewayChildren = new JSONArray();
			
			for(String gatewayName : gateways)	{
				
				JSONObject gateway = new JSONObject();
				gateway.put("name", gatewayName);
				gateway.put("children", gatewayChildren);
				
				flareChildren.put(gateway);
				
				List<String> netProbes = eventRepository.findDistinctProbesByGateway(gatewayName, start, end);
				
				JSONArray netProbeChildren = new JSONArray();
				
				for(String netProbeName : netProbes) {
					
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
			
			flare.put("children", flareChildren);
			
			return flare.toString();
    }
    
    
//    public Set findDistinctGateways(List<Event> events) {
//    	
//    	List<String> gatewayList = new ArrayList();
//    	
//    	for(Event e : events) 
//    		gatewayList.add(e.getGateway());
//    	
//    	return new HashSet(gatewayList);
//    }
//    
//    public Set findDistinctNetProbesByGateway(List<Event> events, String gateway) {
//
//    	List<String> netProbeList = new ArrayList();
//    	
//    	for(Event e : events) 
//    		if(e.getGateway().equals(gateway))
//    			netProbeList.add(e.getGateway());
//    	
//    	return new HashSet(netProbeList);
//    }
    

    
}
