package it.smartcommunitylab.csengine.common;

public enum ExpAttr {
	title("title"),
	description("description"),
	location("location"), //GeoJsonPoint
	dateFrom("dateFrom"),
	dateTo("dateTo"),
	validityFrom("validityFrom"),
	validityTo("validityTo"),
	competences("competences"), //List<Compatence_attr>
	organisation("organisation"); //Organisation_attr
	
	public final String label;
	
	private ExpAttr(String label) {
    this.label = label;
	}

}
