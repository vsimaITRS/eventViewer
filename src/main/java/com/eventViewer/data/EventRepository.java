package com.eventViewer.data;

import com.eventViewer.api.model.Event;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;



@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

	@Query("SELECT DISTINCT gateway FROM Event e")
	public List findDistinctGateways();
	
	@Query("SELECT DISTINCT probe FROM Event e")
	public List findDistinctProbes();
	
	@Query("SELECT DISTINCT sampler FROM Event e WHERE probe = :probe")
	public List findDistinctSamplersByProbe(@Param("probe") String probe);

	@Query("SELECT COUNT(severity) FROM Event e WHERE sampler = :sampler AND probe = :probe AND severity = :severity")
	public int findEventCountByProbeSamplerSeverity(@Param("probe") String probe, @Param("sampler") String sampler, @Param("severity") String severity);
	
}

