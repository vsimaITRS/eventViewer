package com.eventViewer.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.eventViewer.api.model.Event;
import com.eventViewer.data.EventRepository;

@CrossOrigin(origins = "http://localhost", maxAge = 3600)
@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @RequestMapping(value = "/",
    		produces = { MediaType.APPLICATION_JSON_VALUE },
    		method = RequestMethod.GET)
    public ResponseEntity<List<Event>> getAllEvents() {
    	
    	List<Event> events = eventRepository.findAll();
    	if(events.isEmpty())
    		return new ResponseEntity(HttpStatus.NO_CONTENT);
        return new ResponseEntity<List<Event>>(events, HttpStatus.OK);
    }
    
    //GET DISTINCT GATEWAYS FROM ALL EVENTS
    @RequestMapping(value = "/gateways", method = RequestMethod.GET)
    public ResponseEntity<List<String>> getDistinctGateways() {
    	
    	List<String> gateways = eventRepository.findDistinctGateways();
    	if(gateways.isEmpty())
    		return new ResponseEntity(HttpStatus.NO_CONTENT);    	
    	return new ResponseEntity<List<String>>(gateways, HttpStatus.OK);
    }

    //GET DISTINCT PROBES FROM ALL EVENTS
    @RequestMapping(value = "/probes", method = RequestMethod.GET)
    public ResponseEntity<List<String>> getDistinctProbes() {
    	
    	List<String> probes = eventRepository.findDistinctProbes();
    	if(probes.isEmpty())
    		return new ResponseEntity(HttpStatus.NO_CONTENT);    	
    	return new ResponseEntity<List<String>>(probes, HttpStatus.OK);
    }
    
    //GET DISTINCT SAMPLERS FOR A SPECIFIC PROBE
    
    @RequestMapping(value = "/probes/{probe}/samplers", method = RequestMethod.GET)
    public ResponseEntity<List<String>> getDistinctSamplersByProbeName(@PathVariable("probe") String probe) {
    	
    	List<String> samplers = eventRepository.findDistinctSamplersByProbe(probe);
    	if(samplers.isEmpty())
    		return new ResponseEntity(HttpStatus.NO_CONTENT);
    	
    	return new ResponseEntity<List<String>>(samplers, HttpStatus.OK);
    }
    
    //GET EVENT COUNT BASED ON SPECIFIC PROBE, SAMPLER, AND SEVERITY
    @RequestMapping(value = "/probes/{probe}/samplers/{sampler}/severity/{severity}", method = RequestMethod.GET)
    public ResponseEntity<?> getEventCountByProbeSamplerSeverity(@PathVariable("probe") String probe, @PathVariable("sampler") String sampler, @PathVariable("severity") String severity) {
    	
    	int count = eventRepository.findEventCountByProbeSamplerSeverity(probe, sampler, severity);
    	return new ResponseEntity(count, HttpStatus.OK);
    }
}
