package com.eventViewer.api.controller;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
	
	
	ExecutorService executorService;

    @Autowired
    private EventRepository eventRepository;

    /**
     * Request method which returns a list of all events in the database.
     * @param start - Optional<Long> which limits the events returned to those after the start value if it is specified.
     * @param end - Optional<Long> which limits the events returned to those before the end value if it is specified
     * @return ResponseEntity<List<Events>> - a ResponseEntity object containing a list of Event objects from records in the event table
     * 										and a HTTPStatus code.
     */
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
    
    /**
     * Request method which formats event data into a hierarchy that may be consumed by d3.js Zoomable Sunburst visuals
     * @param start- Optional<Long> which limits the events in the selection to those after the start value if it is specified
     * @param end - Optional<Long> which limits the events in the selection to those before the end value if it is specified
     * @return ResponseEntity<String> - a ResponseEntity object containing a String representing a hierarchy of event data in a valid format
     * 									and a HTTPStatus code.
     * @throws JSONException - An exception thrown in the case of invalid JSON 
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    @RequestMapping(value = "/zoomableSunburstJSON",
    		produces = { MediaType.APPLICATION_JSON_VALUE },
    		method = RequestMethod.GET)
    public ResponseEntity<String> getZoomableSunburstJSON(@RequestParam("start") Optional<Long> start,
    													  @RequestParam("end")   Optional<Long> end) throws JSONException, InterruptedException, ExecutionException {
    	
    	String formattedJSON;
     	
        if(start != null && end != null)
        	formattedJSON = formatVisual("zoomableSunburst", start, end);
        else if(start != null && end == null)
        	formattedJSON = formatVisual("zoomableSunburst", start, null);
        else if(start == null && end != null)
        	formattedJSON = formatVisual("zoomableSunburst", null, end);
        else
        	formattedJSON = formatVisual("zoomableSunburst", null, null);
    	
    	return new ResponseEntity<String>(formattedJSON, HttpStatus.OK);
    }
    
    
    /**
     * Request method which formats event data into a hierarchy that may be consumed by d3.js Collapsible Tree visuals
     * @param start- Optional<Long> which limits the events in the selection to those after the start value if it is specified
     * @param end - Optional<Long> which limits the events in the selection to those before the end value if it is specified
     * @return ResponseEntity<String> - a ResponseEntity object containing a String representing a hierarchy of event data in a valid format
     * 									and a HTTPStatus code.
     * @throws JSONException - An exception thrown in the case of invalid JSON 
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    @RequestMapping(value = "/collapsibleTreeJSON",
    		produces = { MediaType.APPLICATION_JSON_VALUE },
    		method = RequestMethod.GET)
    public ResponseEntity<String> getcollapsibleTreeJSON(@RequestParam("start") Optional<Long> start,
    													  @RequestParam("end")   Optional<Long> end) throws JSONException, InterruptedException, ExecutionException {
    	
    	String formattedJSON;
    	    	
       	if(start != null && end != null)
       		formattedJSON = formatVisual("collapsibleTree", start, end);
       	else if(start != null && end == null)
       		formattedJSON = formatVisual("collapsibleTree", start, null);
       	else if(start == null && end != null)
       		formattedJSON = formatVisual("collapsibleTree", null, end);
       	else
       		formattedJSON = formatVisual("collapsibleTree", null, null);
    	
    		return new ResponseEntity<String>(formattedJSON, HttpStatus.OK);
    }

    /**
     * Request method which formats event data into a hierarchy that may be consumed by d3.js Calendar visuals
     * @return ResponseEntity<String> - a ResponseEntity object containing a String representing a hierarchy of event data in a valid format
     * 									and a HTTPStatus code.
     * @throws JSONException - An exception thrown in the case of invalid JSON 
     */
    @RequestMapping(value = "/calendarJSON",
    		produces = { MediaType.APPLICATION_JSON_VALUE },
    		method = RequestMethod.GET)
    public ResponseEntity<String> getCalendarJSON() throws JSONException {
    	
    	String formattedJSON;

    	formattedJSON = formatCalendarData();
    			    	
    	return new ResponseEntity<String>(formattedJSON, HttpStatus.OK);
    }
    
    
    /**
     * Request method which formats event data into a hierarchy that may be consumed by d3.js Calendar visuals
     * @param start - String which limits the events in the selection to those after the start value
     * @param end- String which limits the events in the selection to those before the end value
     * @return ResponseEntity<List<Event>> - ResponseEntity object containing a list of event objects that occurred between
     * 										the specified times and an HttpStatus code
     */
    @RequestMapping(value = "/start/{start}/end/{end}",
    		produces = { MediaType.APPLICATION_JSON_VALUE },
    		method = RequestMethod.GET)
    public ResponseEntity<List<Event>> getAllEventsBetweenTwoTimes(@PathVariable("start") String start,
    		@PathVariable("end") String end) {
    	
    	List<Event> events = eventRepository.findEventsBetweenTimes(Long.parseLong(start), Long.parseLong(end));
    	    	
    	if(events.isEmpty())
    		return new ResponseEntity(HttpStatus.NO_CONTENT);
        return new ResponseEntity<List<Event>>(events, HttpStatus.OK);
    }
    
    
    /**
     * Request method which finds the number of distinct gateways that had logged events between an optional range of times.
     * @param start- Optional<Long> which limits the events in the selection to those after the start value if it is specified.
     * @param end - Optional<Long> which limits the events in the selection to those before the end value if it is specified.
     * @return ResponseEntity<Integer> - ResponseEntity object containing an Integer that represents the number of distinct gateways found
     * 									and a HttpStatus code.
     */
    @RequestMapping(value = "gateways/count",
    		produces = { MediaType.APPLICATION_JSON_VALUE },
    		method = RequestMethod.GET)
    public ResponseEntity<Integer> getGatewayCountBetweenOptionalTimes(@RequestParam("start") Optional<Long> start,
    		@RequestParam("end") Optional<Long> end) {
    	
    	int gatewayCount;

    	if(start != null && end != null)
    		gatewayCount = eventRepository.findGatewayCountBetweenOptionalTimes(start, end);
    	else if(start != null && end == null)
    		gatewayCount = eventRepository.findGatewayCountBetweenOptionalTimes(start, null);
    	else if(start == null && end != null)
    		gatewayCount = eventRepository.findGatewayCountBetweenOptionalTimes(null, end);
    	else
    		gatewayCount = eventRepository.findGatewayCountBetweenOptionalTimes(null, null);
    	    	
        return new ResponseEntity<Integer>(gatewayCount, HttpStatus.OK);
    }
    
    /**
     * Request method which finds the number of distinct probes that had logged events between an optional range of times.
     * @param start- Optional<Long> which limits the events in the selection to those after the start value if it is specified.
     * @param end - Optional<Long> which limits the events in the selection to those before the end value if it is specified.
     * @return ResponseEntity<Integer> - ResponseEntity object containing an Integer that represents the number of distinct probes found
     * 									and a HttpStatus code.
     */
    @RequestMapping(value = "probes/count",
    		produces = { MediaType.APPLICATION_JSON_VALUE },
    		method = RequestMethod.GET)
    public ResponseEntity<Integer> getProbeCountBetweenOptionalTimes(@RequestParam("start") Optional<Long> start,
    		@RequestParam("end") Optional<Long> end) {
    	
    	int probeCount;

    	if(start != null && end != null)
    		probeCount = eventRepository.findProbeCountBetweenOptionalTimes(start, end);
    	else if(start != null && end == null)
    		probeCount = eventRepository.findProbeCountBetweenOptionalTimes(start, null);
    	else if(start == null && end != null)
    		probeCount = eventRepository.findProbeCountBetweenOptionalTimes(null, end);
    	else
    		probeCount = eventRepository.findProbeCountBetweenOptionalTimes(null, null);
    	    	
        return new ResponseEntity<Integer>(probeCount, HttpStatus.OK);
    }
    
    /**
     * 
     * Request method which finds the number of events that had been logged between an optional range of times.
     * @param start- Optional<Long> which limits the events in the selection to those after the start value if it is specified.
     * @param end - Optional<Long> which limits the events in the selection to those before the end value if it is specified.
     * @return ResponseEntity<Integer> - ResponseEntity object containing an Integer that represents the number of events logged
     * 									and a HttpStatus code.
     */
    @RequestMapping(value = "count",
    		produces = { MediaType.APPLICATION_JSON_VALUE },
    		method = RequestMethod.GET)
    public ResponseEntity<Integer> getEventCountBetweenOptionalTimes(@RequestParam("start") Optional<Long> start,
    		@RequestParam("end") Optional<Long> end) {
    	
    	int eventCount;

    	if(start != null && end != null)
    		eventCount = eventRepository.findEventCountBetweenOptionalTimes(start, end);
    	else if(start != null && end == null)
    		eventCount = eventRepository.findEventCountBetweenOptionalTimes(start, null);
    	else if(start == null && end != null)
    		eventCount = eventRepository.findEventCountBetweenOptionalTimes(null, end);
    	else
    		eventCount = eventRepository.findEventCountBetweenOptionalTimes(null, null);
    	    	
        return new ResponseEntity<Integer>(eventCount, HttpStatus.OK);
    }
    
    /**
     * Request method which finds the number of events of a particular severity that had been logged between an optional range of times.
     * @param start- Optional<Long> which limits the events in the selection to those after the start value if it is specified.
     * @param end - Optional<Long> which limits the events in the selection to those before the end value if it is specified.
     * @param severity - String which represents the particular severity that events should have
     * @return ResponseEntity<Integer> - ResponseEntity object containing an Integer that represents the number of events logged
     * 									with the specified severity and a HttpStatus code.
     */
    @RequestMapping(value = "{severity}/count",
    		produces = { MediaType.APPLICATION_JSON_VALUE },
    		method = RequestMethod.GET)
    public ResponseEntity<Integer> getEventCountBetweenOptionalTimes(@RequestParam("start") Optional<Long> start,
    		@RequestParam("end") Optional<Long> end, @PathVariable String severity) {
    	
    	if(severity.equals("critical"))
			severity = "2";
		else if(severity.equals("warning"))
			severity = "1";
		else if(severity.equals("OK"))
			severity = "0";
		else if(severity.equals("undefined"))
			severity = "-1";
    	
    	int eventCount;
    	
    	if(start != null && end != null)
    		eventCount = eventRepository.findEventCountBySeverityBetweenOptionalTimes(start, end, severity);
    	else if(start != null && end == null)
    		eventCount = eventRepository.findEventCountBySeverityBetweenOptionalTimes(start, null, severity);
    	else if(start == null && end != null)
    		eventCount = eventRepository.findEventCountBySeverityBetweenOptionalTimes(null, end, severity);
    	else
    		eventCount = eventRepository.findEventCountBySeverityBetweenOptionalTimes(null, null, severity);
    	    	    	
        return new ResponseEntity<Integer>(eventCount, HttpStatus.OK);
    }
    
    /**
     * Request method which finds the number of distinct samplers that had been logged between an optional range of times.
     * @param start- Optional<Long> which limits the events in the selection to those after the start value if it is specified.
     * @param end - Optional<Long> which limits the events in the selection to those before the end value if it is specified.
     * @return ResponseEntity<Integer> - ResponseEntity object containing an Integer that represents the number of distinct samplers
     * 									and a HttpStatus code.
     */
    @RequestMapping(value = "samplers/count",
    		produces = { MediaType.APPLICATION_JSON_VALUE },
    		method = RequestMethod.GET)
    public ResponseEntity<Integer> getSamplerCountBetweenOptionalTimes(@RequestParam("start") Optional<Long> start,
    		@RequestParam("end") Optional<Long> end) {
    	
    	int samplerCount;

    	if(start != null && end != null)
    		samplerCount = eventRepository.findSamplerCountBetweenOptionalTimes(start, end);
    	else if(start != null && end == null)
    		samplerCount = eventRepository.findSamplerCountBetweenOptionalTimes(start, null);
    	else if(start == null && end != null)
    		samplerCount = eventRepository.findSamplerCountBetweenOptionalTimes(null, end);
    	else
    		samplerCount = eventRepository.findSamplerCountBetweenOptionalTimes(null, null);
    
        return new ResponseEntity<Integer>(samplerCount, HttpStatus.OK);
    }
    
     
    
    
	/**
	 * Request method which finds the events logged of a particular gateway, probe, and sampler.
     * @return ResponseEntity<Integer> - ResponseEntity object containing an Integer that represents the number of distinct samplers 
     * 									of a particular probe and a HttpStatus code.
	 */
    @RequestMapping(value = "/gateways/{gateway}/probes/{probe}/samplers/{sampler}", produces = { MediaType.APPLICATION_JSON_VALUE }, method = RequestMethod.GET)
    public ResponseEntity<?> getEventsByGatewayProbeSampler(@PathVariable("gateway") String gateway, @PathVariable("probe") String probe, 
    				@PathVariable("sampler") String sampler, @RequestParam("start") Optional<Long> start,
    				@RequestParam("end") Optional<Long> end) {
    	
    	List<Event> events; 
    	    	
    	if(start != null && end != null)
    		events = eventRepository.findEventsByGatewayProbeSampler(gateway, probe, sampler, start, end);
    	else if(start != null && end == null)
    		events = eventRepository.findEventsByGatewayProbeSampler(gateway, probe, sampler, start, null);
    	else if(start == null && end != null)
    		events = eventRepository.findEventsByGatewayProbeSampler(gateway, probe, sampler, null, end);
    	else
    		events = eventRepository.findEventsByGatewayProbeSampler(gateway, probe, sampler, null, null);
    	    	
    	if(events.isEmpty())
    		return new ResponseEntity(HttpStatus.NO_CONTENT);
        return new ResponseEntity<List<Event>>(events, HttpStatus.OK);
    }
    
    /**
     * Helper method which formats event data into a valid representation for the specified visual type (only for zoomable sunburst or collapsible tree).
     * @param visual - String that represents the type of d3.js visual.
     * @param start- Optional<Long> which limits the events in the selection to those after the start value if it is specified.
     * @param end - Optional<Long> which limits the events in the selection to those before the end value if it is specified.
     * @return String representing the event data formatted for the specified visual type.
     * @throws JSONException - Exception which catches any invalid JSON.
     */
    ExecutorService executor = Executors.newFixedThreadPool(20);
    public String formatVisual(String visualType, Optional<Long> start, Optional<Long> end) throws JSONException, InterruptedException, ExecutionException {
    	    	
    	JSONObject visualRoot = new JSONObject();
    	
    	if(visualType.equals("zoomableSunburst"))
    		visualRoot.put("name", "flare");
    	else if(visualType.equals("collapsibleTree"))
    		visualRoot.put("name", "gateways");
			
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
					
					List<String> samplers = eventRepository.findDistinctSamplersByProbeAndGateway(netProbeName, gatewayName, start, end);
					
					for(String samplerName : samplers) {
						
						//for implementation of multi-threading
//						executor.execute(new Runnable() {
//				            @Override
//				            public void run() {
//				                try {
//				           			Future<String> future = getSamplerData(samplerName, netProbeChildren, gatewayName, netProbeName, start, end);
//									
//								} catch (InterruptedException | ExecutionException e) {
//									e.printStackTrace();
//								}
//				            }
//				        });
						
						JSONArray samplerChildren = new JSONArray();
						
						JSONObject sampler = new JSONObject();

						sampler.put("name", samplerName);
						sampler.put("children", samplerChildren);
						
						netProbeChildren.put(sampler);
						
						JSONObject undefined = new JSONObject();
						JSONObject OK = new JSONObject();
						JSONObject warning = new JSONObject();
						JSONObject critical = new JSONObject();
						
						samplerChildren.put(undefined);
						samplerChildren.put(OK);
						samplerChildren.put(warning);
						samplerChildren.put(critical);

						//for implementation of multi-threading
//						Future<Integer> undefinedFuture = eventRepository.findEventCountBySeverity(gatewayName, netProbeName, samplerName, "-1", start, end);
//						Future<Integer> OKFuture = eventRepository.findEventCountBySeverity(gatewayName, netProbeName, samplerName, "0", start, end);
//						Future<Integer> warningFuture = eventRepository.findEventCountBySeverity(gatewayName, netProbeName, samplerName, "1", start, end);
//						Future<Integer> criticalFuture = eventRepository.findEventCountBySeverity(gatewayName, netProbeName, samplerName, "2", start, end);
						
						int undefinedCount = eventRepository.findEventCountByGatewayProbeSamplerSeverityBetweenOptionalTimes(gatewayName, netProbeName, samplerName, "-1", start, end);
						int OKCount = eventRepository.findEventCountByGatewayProbeSamplerSeverityBetweenOptionalTimes(gatewayName, netProbeName, samplerName, "0", start, end);
						int warningCount = eventRepository.findEventCountByGatewayProbeSamplerSeverityBetweenOptionalTimes(gatewayName, netProbeName, samplerName, "1", start, end);
						int criticalCount = eventRepository.findEventCountByGatewayProbeSamplerSeverityBetweenOptionalTimes(gatewayName, netProbeName, samplerName, "2", start, end);
						
						undefined.put("name", "undefined");			
						undefined.put("size", undefinedCount);
				
						OK.put("name", "OK");
						OK.put("size", OKCount);

						warning.put("name", "warning");
						warning.put("size", warningCount);

						critical.put("name", "critical");
						critical.put("size", criticalCount);
						

					}
				}
			}

			visualRoot.put("children", flareChildren);
			return visualRoot.toString();
    }
    
    //For implementation of multi-threading
//    public Future<String> getSamplerData(String samplerName, JSONArray netProbeChildren, String gatewayName, String netProbeName, 
//    										Optional<Long> start, Optional<Long> end) throws JSONException, InterruptedException, ExecutionException {
//		
//			JSONArray samplerChildren = new JSONArray();
//			
//			JSONObject sampler = new JSONObject();
//
//			sampler.put("name", samplerName);
//			sampler.put("children", samplerChildren);
//			
//			netProbeChildren.put(sampler);
//			
//			JSONObject undefined = new JSONObject();
//			JSONObject OK = new JSONObject();
//			JSONObject warning = new JSONObject();
//			JSONObject critical = new JSONObject();
//			
//			samplerChildren.put(undefined);
//			samplerChildren.put(OK);
//			samplerChildren.put(warning);
//			samplerChildren.put(critical);
//
//			Future<Integer> undefinedFuture = eventRepository.findEventCountBySeverity(gatewayName, netProbeName, samplerName, "-1", start, end);
//			Future<Integer> OKFuture = eventRepository.findEventCountBySeverity(gatewayName, netProbeName, samplerName, "0", start, end);
//			Future<Integer> warningFuture = eventRepository.findEventCountBySeverity(gatewayName, netProbeName, samplerName, "1", start, end);
//			Future<Integer> criticalFuture = eventRepository.findEventCountBySeverity(gatewayName, netProbeName, samplerName, "2", start, end);
//			
//
//			
//			undefined.put("name", "undefined");			
//			undefined.put("size", undefinedFuture.get());
//	
//			OK.put("name", "OK");
//			OK.put("size", OKFuture.get());
//
//			warning.put("name", "warning");
//			warning.put("size", warningFuture.get());
//
//			critical.put("name", "critical");
//			critical.put("size", criticalFuture.get());
//			return null;
//			
//			
//	}
    

    
    

    /**
     * Helper method which converts a long representing a timestamp to a simple date.
     * @param time - long which represents the timestamp to be converted
     * @return String - represents the converted date
     */
    public String convertTime(long time){
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        return format.format(date);
    }
    
    
    /**
     * Helper method which formats event data into a valid representation for the calendar visual type.
     * @return String representing the event data formatted for the calendar visual type.
     * @throws JSONException - Exception which catches any invalid JSON.
     */
    public String formatCalendarData() throws JSONException {
    	
    	JSONArray array = new JSONArray();    			
    	

    	List<Long> timestamps = eventRepository.findAllTimestamps();

    	HashMap<String, Integer> map = new HashMap<>();
    	
    	for(Long e : timestamps) {
    		    	
	    	String date = convertTime(e*1000);
	    	
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


