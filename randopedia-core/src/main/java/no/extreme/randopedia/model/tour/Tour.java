package no.extreme.randopedia.model.tour;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Tour implements Comparable<Tour>{
    private String id;
    private String name;
    private String areaId;
    private String clientAreaId;
    private String shortDescription;
    private String itinerary;
    private Integer timeOfYearFrom;
    private Integer timeOfYearTo;
    private Integer timingMin;
    private Integer timingMax;
    private Integer elevationGain;
    private Integer elevationLoss;
    private Integer elevationMax;
    private Integer aspect;
    private String accessPoint;
    private Integer accessPointElevation;
    private Integer grade;
    private boolean requiresTools;
    private String toolsDescription; 
    private boolean haveHazards;
    private String hazardsDescription;
    private Integer degreesMax;
    private List<Object> mapPaths;
    private Object mapGeoJson;
    private List<String> tags;
    private String portfolioImage;
    private String clientId;
    private Double centerLatitude;
    private Double centerLongitude;
    
    private List<String> actions;
    private Integer status;
    
    /**
     * Publishing comment, only used for updates, will not be persisted in the tour
     */
    @Transient
    private String publishComment;
    
    @JsonIgnore
    private List<TourImage> tourImages;
    
    @JsonIgnore
    private List<TourComment> tourComments;   
    
    @Id
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

	public String getArea() {
		return areaId;
	}

	public void setArea(String areaId) {
		this.areaId = areaId;
	}
	
    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }   	
	public String getItinerary() {
		return itinerary;
	}

	public void setItinerary(String itinerary) {
		this.itinerary = itinerary;
	}

	public Integer getTimeOfYearFrom() {
		return timeOfYearFrom;
	}

	public void setTimeOfYearFrom(Integer timeOfYearFrom) {
		this.timeOfYearFrom = timeOfYearFrom;
	}

	public Integer getTimeOfYearTo() {
		return timeOfYearTo;
	}

	public void setTimeOfYearTo(Integer timeOfYearTo) {
		this.timeOfYearTo = timeOfYearTo;
	}

	public Integer getTimingMin() {
		return timingMin;
	}

	public void setTimingMin(Integer timingMin) {
		this.timingMin = timingMin;
	}

	public Integer getTimingMax() {
		return timingMax;
	}

	public void setTimingMax(Integer timingMax) {
		this.timingMax = timingMax;
	}

	public Integer getElevationGain() {
		return elevationGain;
	}

	public void setElevationGain(Integer elevationGain) {
		this.elevationGain = elevationGain;
	}

	public Integer getElevationLoss() {
		return elevationLoss;
	}

	public void setElevationLoss(Integer elevationLoss) {
		this.elevationLoss = elevationLoss;
	}

	public Integer getAspect() {
		return aspect;
	}

	public void setAspect(Integer aspect) {
		this.aspect = aspect;
	}

	@JsonProperty("requiresTools")
	public boolean isRequiresTools() {
		return requiresTools;
	}

	@JsonProperty("requiresTools")
	public void setRequiresTools(boolean requiresTools) {
		this.requiresTools = requiresTools;
	}

	public String getToolsDescription() {
		return toolsDescription;
	}

	public void setToolsDescription(String toolsDescription) {
		this.toolsDescription = toolsDescription;
	}

	public String getAccessPoint() {
		return accessPoint;
	}

	public void setAccessPoint(String accessPoint) {
		this.accessPoint = accessPoint;
	}

	public Integer getAccessPointElevation() {
        return accessPointElevation;
    }

    public void setAccessPointElevation(Integer accessPointElevation) {
        this.accessPointElevation = accessPointElevation;
    }

    public Integer getGrade() {
		return grade;
	}

	public void setGrade(Integer grade) {
		this.grade = grade;
	}

	@JsonProperty("haveHazards")
	public boolean isHaveHazards() {
        return haveHazards;
    }

	@JsonProperty("haveHazards")
    public void setHaveHazards(boolean haveHazards) {
        this.haveHazards = haveHazards;
    }
    
    public String getHazardsDescription() {
        return hazardsDescription;
    }
    
    public void setHazardsDescription(String hazardsDescription) {
        this.hazardsDescription = hazardsDescription;
    }

    public Integer getDegreesMax() {
        return degreesMax;
    }

    public void setDegreesMax(Integer degreesMax) {
        this.degreesMax = degreesMax;
    }

    public Integer getElevationMax() {
        return elevationMax;
    }

    public void setElevationMax(Integer elevationMax) {
        this.elevationMax = elevationMax;
    }

    public List<Object> getMapPaths() {
        return mapPaths;
    }

    public void setMapPaths(List<Object> mapPaths) {
        this.mapPaths = mapPaths;
    }

    public Object getMapGeoJson() {
		return mapGeoJson;
	}

	public void setMapGeoJson(Object mapGeoJson) {
		this.mapGeoJson = mapGeoJson;
	}

	@JsonIgnore
    public List<TourImage> getTourImages() {
        return tourImages;
    }

    @JsonIgnore
    public void setTourImages(List<TourImage> tourImages) {
        this.tourImages = tourImages;
    }

    public void setPortfolioImage(String imageId) {
        this.portfolioImage = imageId;
    }
    
    public String getPortfolioImage(){
        return portfolioImage;
    }
    
    @Transient
    public List<String> getImages() {
        List<String> images = new ArrayList<String>();
        
        if(tourImages != null) {
            for(TourImage tourImage : tourImages) {
                images.add(tourImage.getId());
            }
        }
        
        return images;
    }
    
    @JsonIgnore
    public List<TourComment> getTourComments() {
        if(tourComments == null){
            tourComments = new ArrayList<TourComment>();
        }
        return tourComments;
    }

    @JsonIgnore
    public void setTourComments(List<TourComment> comments) {
        this.tourComments = comments;
    }
    
    @Transient
    public List<String> getComments() {
        List<String> comments = new ArrayList<String>();
        
        if(tourComments != null) {
            for(TourComment tourComment : tourComments) {
                comments.add(tourComment.get_Id().toString());
            }
        }
        
        return comments;
    }

    public List<String> getActions() {
        if(actions == null){
            actions = new ArrayList<String>();
        }
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	
    public String getPublishComment() {
        return publishComment;
    }
    
    public void setPublishComment(String publishComment) {
        this.publishComment = publishComment;
    }	
	
	@Override
	public boolean equals(Object object){
		if(object instanceof Tour) {
			return this.getId().equals(((Tour)object).getId());	
		}
		return false;
	}
	
	@Override
	public int hashCode() {
	    return this.getId().hashCode();
	}

	@Override
	public int compareTo(Tour otherTour) {
		return this.getName().toLowerCase().compareTo(otherTour.getName().toLowerCase());
	}

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientArea() {
        return clientAreaId;
    }

    public void setClientArea(String clientAreaId) {
        this.clientAreaId = clientAreaId;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Double getCenterLatitude() {
        return centerLatitude;
    }

    public void setCenterLatitude(Double centerLatitude) {
        this.centerLatitude = centerLatitude;
    }

    public Double getCenterLongitude() {
        return centerLongitude;
    }

    public void setCenterLongitude(Double centerLongitude) {
        this.centerLongitude = centerLongitude;
    }
}
