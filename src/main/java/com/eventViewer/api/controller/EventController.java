package com.eventViewer.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.eventViewer.data.EventRepository;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public List getAllEvents() {
        return eventRepository.findAll();
    }

}
