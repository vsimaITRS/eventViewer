package com.eventViewer.data;

import com.eventViewer.api.model.Event;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;



@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
	
	@Query("SELECT timestamp FROM Event e")
	public List<Long> findAllTimestamps();
		
	@Query("FROM Event e WHERE (:start is NULL or e.timestamp >= :start) AND (:end is NULL or e.timestamp <= :end)")
	public List<Event> findEventsBetweenOptionalTimes(@Param("start") Optional<Long> start, @Param("end") Optional<Long> end);
	
	@Query("FROM Event e WHERE e.timestamp >= :start AND e.timestamp <= :end")
	public List<Event> findEventsBetweenTimes(@Param("start") Long start, @Param("end") Long end);

	@Query("SELECT DISTINCT gateway FROM Event e WHERE (:start is NULL or e.timestamp >= :start) AND (:end is NULL or e.timestamp <= :end)")
	public List<String> findDistinctGateways(@Param("start") Optional<Long> start, @Param("end") Optional<Long> end);
	
	@Query("SELECT DISTINCT probe FROM Event e WHERE gateway = :gateway AND (:start is NULL or e.timestamp >= :start) AND (:end is NULL or e.timestamp <= :end)")
	public List<String> findDistinctProbesByGateway(@Param("gateway") String gateway, @Param("start") Optional<Long> start, @Param("end") Optional<Long> end);
	
	@Query("SELECT DISTINCT sampler FROM Event e WHERE probe = :probe AND gateway = :gateway AND (:start is NULL or e.timestamp >= :start) AND (:end is NULL or e.timestamp <= :end)")
	public List<String> findDistinctSamplersByProbeAndGateway(@Param("probe") String probe, @Param("gateway") String gateway, @Param("start") Optional<Long> start, @Param("end") Optional<Long> end);

	//for implementation of multi-threading
//	@Query("SELECT COUNT(severity) FROM Event e WHERE gateway = :gateway AND sampler = :sampler AND probe = :probe AND severity = :severity AND (:start is NULL or e.timestamp >= :start) AND (:end is NULL or e.timestamp <= :end)")
//	@Async
//	public Future<Integer> findEventCountBySeverity(@Param("gateway") String gateway, @Param("probe") String probe, @Param("sampler") String sampler, @Param("severity") String severity, @Param("start") Optional<Long> start, @Param("end") Optional<Long> end);
	
	@Query("SELECT COUNT(severity) FROM Event e WHERE gateway = :gateway AND sampler = :sampler AND probe = :probe AND severity = :severity AND (:start is NULL or e.timestamp >= :start) AND (:end is NULL or e.timestamp <= :end)")
	public int findEventCountByGatewayProbeSamplerSeverityBetweenOptionalTimes(@Param("gateway") String gateway, @Param("probe") String probe, @Param("sampler") String sampler, @Param("severity") String severity, @Param("start") Optional<Long> start, @Param("end") Optional<Long> end);

	@Query("SELECT COUNT(DISTINCT gateway) FROM Event e WHERE (:start is NULL or e.timestamp >= :start) AND (:end is NULL or e.timestamp <= :end)")
	public int findGatewayCountBetweenOptionalTimes(@Param("start") Optional<Long> start, @Param("end") Optional<Long> end);
	
	@Query("SELECT COUNT(DISTINCT probe) FROM Event e WHERE (:start is NULL or e.timestamp >= :start) AND (:end is NULL or e.timestamp <= :end)")
	public int findProbeCountBetweenOptionalTimes(@Param("start") Optional<Long> start, @Param("end") Optional<Long> end);
	
	@Query("SELECT COUNT(DISTINCT sampler) FROM Event e WHERE (:start is NULL or e.timestamp >= :start) AND (:end is NULL or e.timestamp <= :end)")
	public int findSamplerCountBetweenOptionalTimes(@Param("start") Optional<Long> start, @Param("end") Optional<Long> end);
	
	@Query("SELECT COUNT(ref) FROM Event e WHERE (:start is NULL or e.timestamp >= :start) AND (:end is NULL or e.timestamp <= :end)")
	public int findEventCountBetweenOptionalTimes(@Param("start") Optional<Long> start, @Param("end") Optional<Long> end);
	
	@Query("SELECT COUNT(ref) FROM Event e WHERE (:start is NULL or e.timestamp >= :start) AND (:end is NULL or e.timestamp <= :end) AND severity = :severity")
	public int findEventCountBySeverityBetweenOptionalTimes(@Param("start") Optional<Long> start, @Param("end") Optional<Long> end, @Param("severity") String severity);
	
	@Query("FROM Event e WHERE gateway = :gateway AND sampler = :sampler AND probe = :probe AND severity = :severity AND (:start is NULL or e.timestamp >= :start) AND (:end is NULL or e.timestamp <= :end)")
	public List<Event> findEventsByEntity(@Param("gateway") String gateway, @Param("probe") String probe, @Param("sampler") String sampler, @Param("severity") String severity, @Param("start") Optional<Long> start, @Param("end") Optional<Long> end);
	
	@Query("FROM Event e WHERE gateway = :gateway AND sampler = :sampler AND probe = :probe AND (:start is NULL or e.timestamp >= :start) AND (:end is NULL or e.timestamp <= :end)")
	public List<Event> findEventsByGatewayProbeSampler(@Param("gateway") String gateway, @Param("probe") String probe, @Param("sampler") String sampler, @Param("start") Optional<Long> start, @Param("end") Optional<Long> end);
	
}

