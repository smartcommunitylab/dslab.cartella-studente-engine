package it.smartcommunitylab.csengine.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;

@Document
public class Activity {
	@Id
	private String id;
	private String title;
	private String description;
	private String entityType;
	private Map<String, DataView> views = new HashMap<>();
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate dateFrom;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate dateTo;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate validityFrom;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate validityTo;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDate getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(LocalDate dateFrom) {
		this.dateFrom = dateFrom;
	}

	public LocalDate getDateTo() {
		return dateTo;
	}

	public void setDateTo(LocalDate dateTo) {
		this.dateTo = dateTo;
	}

	public LocalDate getValidityFrom() {
		return validityFrom;
	}

	public void setValidityFrom(LocalDate validityFrom) {
		this.validityFrom = validityFrom;
	}

	public LocalDate getValidityTo() {
		return validityTo;
	}

	public void setValidityTo(LocalDate validityTo) {
		this.validityTo = validityTo;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public Map<String, DataView> getViews() {
		return views;
	}

	public void setViews(Map<String, DataView> views) {
		this.views = views;
	}
	
}
