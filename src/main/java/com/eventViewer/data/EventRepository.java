package com.eventViewer.data;

import com.eventViewer.api.model.Event;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;



@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
	
	@Query("FROM Event e WHERE (:start is NULL or e.timestamp >= :start) AND (:end is NULL or e.timestamp <= :end)")
	public List findEventsBetweenOptionalTimes(@Param("start") Optional<Long> start, @Param("end") Optional<Long> end);

	@Query("SELECT DISTINCT gateway FROM Event e WHERE (:start is NULL or e.timestamp >= :start) AND (:end is NULL or e.timestamp <= :end)")
	public List findDistinctGateways(@Param("start") Optional<Long> start, @Param("end") Optional<Long> end);
	
	@Query("SELECT DISTINCT probe FROM Event e WHERE (:start is NULL or e.timestamp >= :start) AND (:end is NULL or e.timestamp <= :end)")
	public List findDistinctProbes(@Param("start") Optional<Long> start, @Param("end") Optional<Long> end);
	
	@Query("SELECT DISTINCT probe FROM Event e WHERE gateway = :gateway AND (:start is NULL or e.timestamp >= :start) AND (:end is NULL or e.timestamp <= :end)")
	public List findDistinctProbesByGateway(@Param("gateway") String gateway, @Param("start") Optional<Long> start, @Param("end") Optional<Long> end);
	
	@Query("SELECT DISTINCT sampler FROM Event e WHERE probe = :probe AND (:start is NULL or e.timestamp >= :start) AND (:end is NULL or e.timestamp <= :end)")
	public List findDistinctSamplersByProbe(@Param("probe") String probe, @Param("start") Optional<Long> start, @Param("end") Optional<Long> end);

	@Query("SELECT COUNT(severity) FROM Event e WHERE gateway = :gateway AND sampler = :sampler AND probe = :probe AND severity = :severity AND (:start is NULL or e.timestamp >= :start) AND (:end is NULL or e.timestamp <= :end)")
	public int findEventCountBySeverity(@Param("gateway") String gateway, @Param("probe") String probe, @Param("sampler") String sampler, @Param("severity") String severity, @Param("start") Optional<Long> start, @Param("end") Optional<Long> end);
	
}

