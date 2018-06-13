package com.eventViewer.data;

import com.eventViewer.api.model.Event;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

	public List findDistinctSamplerByProbe(String probe);

}

