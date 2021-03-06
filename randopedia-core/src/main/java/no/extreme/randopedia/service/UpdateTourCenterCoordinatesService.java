package no.extreme.randopedia.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import no.extreme.randopedia.helper.GeoHelper;
import no.extreme.randopedia.model.tour.Tour;
import no.extreme.randopedia.repository.TourRepository;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdateTourCenterCoordinatesService {

    @Autowired
    TourRepository tourRepository;
    
    Logger logger = LoggerFactory.getLogger(UpdateTagCloudService.class);

    public int updateAllToursCenterCoordinates() throws JsonGenerationException, JsonMappingException, IOException {
        List<Tour> tours = tourRepository.findAllTours();
        
        int toursUpdated = 0;
        
        for(Tour tour : tours) {
            updateTourCenterCoordinate(tour);
        }
        return toursUpdated;
    }

    public boolean updateSingleTourCenterCoordinate(Tour tour) throws JsonGenerationException, JsonMappingException, JsonProcessingException, IOException {
        boolean updated = updateTourCenterCoordinate(tour);
        return updated;
    }
    
    private boolean updateTourCenterCoordinate(Tour tour) throws IOException,
            JsonGenerationException, JsonMappingException,
            JsonProcessingException {
        Object mapGeoJson = tour.getMapGeoJson();
        
        boolean didUpdate = false;
                    
        if(mapGeoJson != null) {
            List<Double> latitudes = new ArrayList<Double>();
            List<Double> longitudes = new ArrayList<Double>();
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(mapGeoJson);
            JsonNode node = mapper.readTree(json);
            JsonNode features = node.get("features");
            if(features != null){
                if(features.isArray()) {
                    for(JsonNode feature : features) {
                        JsonNode geometry = feature.get("geometry");
                        for(JsonNode coordinate : geometry.get("coordinates")) {
                            JsonNode longitude = coordinate.get(0);
                            JsonNode latitude = coordinate.get(1);
                            longitudes.add(longitude.getDoubleValue());
                            latitudes.add(latitude.getDoubleValue());
                        }
                    }
                }
            }
            didUpdate = true;
            
            List<Double> centerCoordinate = GeoHelper.getCenterCoordinate(latitudes, longitudes);
            tour.setCenterLatitude(centerCoordinate.get(0));
            tour.setCenterLongitude(centerCoordinate.get(1));
            tourRepository.saveTour(tour);
            logger.debug(centerCoordinate.get(0).toString());
            logger.debug(centerCoordinate.get(1).toString());
        }
    return didUpdate;
    }
}
