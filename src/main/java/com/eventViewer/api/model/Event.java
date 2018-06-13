package com.eventViewer.api.model;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "event_table")
public class Event {

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	private int ref;
    private Long timestamp;
    private int node_ref;
    private String varname;
    private String severity;
    private String description;
    private String gateway;
    private String probe;
    private String managed_entity;
    private String sampler;
    private String type;
    private String dataview;
    private String headline;
    private String rowname;
    private String columnname;

    public Event(){}

    public Event(int id, Long time, int nodeRef, String name, String severity, String description, String gateway,
                 String probe, String managedEntity, String sampler, String type, String dataview, String headline,
                 String rowname, String columnname) {
        this.ref = id;
        this.timestamp = time;
        this.node_ref = nodeRef;
        this.varname = name;
        this.severity = severity;
        this.description = description;
        this.gateway = gateway;
        this.probe = probe;
        this.managed_entity = managedEntity;
        this.sampler = sampler;
        this.type = type;
        this.dataview = dataview;
        this.headline = headline;
        this.rowname = rowname;
        this.columnname = columnname;
    }

    public int getRef() {
		return ref;
	}

	public void setRef(int ref) {
		this.ref = ref;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public int getNode_ref() {
		return node_ref;
	}

	public void setNode_ref(int node_ref) {
		this.node_ref = node_ref;
	}

	public String getVarname() {
		return varname;
	}

	public void setVarname(String varname) {
		this.varname = varname;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getGateway() {
		return gateway;
	}

	public void setGateway(String gateway) {
		this.gateway = gateway;
	}

	public String getProbe() {
		return probe;
	}

	public void setProbe(String probe) {
		this.probe = probe;
	}

	public String getManaged_entity() {
		return managed_entity;
	}

	public void setManaged_entity(String managed_entity) {
		this.managed_entity = managed_entity;
	}

	public String getSampler() {
		return sampler;
	}

	public void setSampler(String sampler) {
		this.sampler = sampler;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDataview() {
		return dataview;
	}

	public void setDataview(String dataview) {
		this.dataview = dataview;
	}

	public String getHeadline() {
		return headline;
	}

	public void setHeadline(String headline) {
		this.headline = headline;
	}

	public String getRowname() {
		return rowname;
	}

	public void setRowname(String rowname) {
		this.rowname = rowname;
	}

	public String getColumnname() {
		return columnname;
	}

	public void setColumnname(String columnname) {
		this.columnname = columnname;
	}

}
