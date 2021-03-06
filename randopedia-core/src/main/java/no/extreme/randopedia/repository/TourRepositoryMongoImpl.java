package no.extreme.randopedia.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import no.extreme.randopedia.model.tag.Tag;
import no.extreme.randopedia.model.tour.Tour;
import no.extreme.randopedia.model.tour.TourAction;
import no.extreme.randopedia.model.tour.TourComment;
import no.extreme.randopedia.model.tour.TourImage;
import no.extreme.randopedia.model.tour.TourStatus;
import no.extreme.randopedia.service.FileWriterService;
import no.extreme.randopedia.utils.ImageUtils;
import no.extreme.randopedia.utils.RandoNameUtils;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class TourRepositoryMongoImpl implements TourRepository {

    @Autowired
    MongoOperations mongoOperations;
    @Value("${webapp.client.directory}")
    private String WEBAPP_CLIENT_DIRECTORY;
    @Value("${tourimages.directory}")
    private String TOURIMAGES_DIRECTORY;
    @Autowired
    private FileWriterService fileWriterService;


    private Criteria onlyPublishedCriteria = Criteria.where("status").is(TourStatus.PUBLISHED);
    
    /**
     * Creates a new tour or updates an existing tour. If id doesn't exist, a new document is inserted.
     */
    @Override
    public void saveTour(Tour tour) {
        // Set array to null to avoid getting keys with null/empty values in database
        if(tour.getTourImages() != null && tour.getTourImages().isEmpty()){
            tour.setTourImages(null);
        }
        
        if(tour.getClientId() == null) {
            String clientId = findUniqueClientId(tour);
            tour.setClientId(clientId);
        }
        
        mongoOperations.save(tour);
    }
    
    /**
     * Returns all published tours
     */
    @Override
    public List<Tour> findAllTours() {
        List<Tour> tours = mongoOperations.find(new Query(onlyPublishedCriteria), Tour.class);
        sortComments(tours);
        return tours;
    }
    
    @Override
    public List<Tour> findAllToursIgnoreStatus() {
        List<Tour> tours = mongoOperations.findAll(Tour.class);
        sortComments(tours);
        return tours;
    }
    
    /**
     * Returns the specified tour
     */
    @Override
	public Tour findTourById(String tourId) {
        Tour tour = mongoOperations.findById(tourId, Tour.class);
        
        if(tour != null) {
            sortComments(tour);
        }
        
        return tour;
    }
    
    @Override
    public Tour findTourByIdAndStatus(String tourId, int status){
        Criteria criteria = Criteria.where("id").is(tourId).and("status").is(status);
        Query query = Query.query(criteria);
    	return mongoOperations.findOne(query, Tour.class);
    }
    
    @Override
    public List<Tour> findToursByStatus(int status){
        Criteria criteria = Criteria.where("status").is(status);
        Query query = Query.query(criteria);
        return mongoOperations.find(query, Tour.class);
    }
    
    @Override
    public Tour findTourByClientIdAndStatus(String tourId, int status) {
        Criteria criteria = Criteria.where("clientId").is(tourId).and("status").is(status);
        Query query = Query.query(criteria);
        return mongoOperations.findOne(query, Tour.class);
    }

    /**
     * Returns published tours that match the search string
     */
    @Override
    public List<Tour> findToursByQuery(String searchString) {
        List<Tour> toursByName = new ArrayList<Tour>();
        List<Tour> toursByArea = new ArrayList<Tour>();
        Criteria criteria = Criteria.where("name").regex(searchString, "i");
        Query query = Query.query(criteria);
        query.addCriteria(onlyPublishedCriteria);
        toursByName = mongoOperations.find(query, Tour.class);
        
        SortedSet<Tour> allTours = new TreeSet<Tour>();
        allTours.addAll(toursByArea);
        allTours.addAll(toursByName);
        return new ArrayList<Tour>(allTours);
    }

    /**
     * Returns all published tours in the specified areas
     */
    @Override
    public List<Tour> findToursByAreaId(List<String> areaIds) {
        Criteria areaCriteria = Criteria.where("areaId").in(areaIds);
        Query areaQuery = Query.query(areaCriteria);
        areaQuery.addCriteria(onlyPublishedCriteria);
        List<Tour> tours = mongoOperations.find(areaQuery, Tour.class);
        sortComments(tours);
        return tours;
    }

    @Override
    public void addCommentToTour(Tour tour, TourComment comment) {
        List<TourComment> comments = tour.getTourComments();
        if(comments == null) {
            comments = new ArrayList<TourComment>();
        }
        comment.setTime(new Date().getTime());
        comments.add(comment);
        tour.setTourComments(comments);
        
        mongoOperations.save(tour);
    }

    @Override
    public TourImage getTourImage(String imageId) {
        Tour tour = getTourFromImageId(imageId);
        
        if(tour == null){
            return null;
        }
        
        for(TourImage img : tour.getTourImages()){
            if(img.getId().equals(imageId)){
                return img;
            }
        }
        
        return null;
    }
    
    @Override
    public List<TourImage> getTourImages(Object[] ids) {
        List<ObjectId> objectIds = new ArrayList<ObjectId>();
        for(Object id : ids) {
            ObjectId objectId = new ObjectId((String)id);
            objectIds.add(objectId);
        }
        Criteria criteria = Criteria.where("tourImages._id").in(objectIds);
        Query query = Query.query(criteria);
        Tour tour = mongoOperations.findOne(query, Tour.class);
        
        if(tour == null){
            return null;
        }
        
        return tour.getTourImages();
    }
    
    @Override
    public void addImageToTour(Tour tour, TourImage image) throws IOException {
        List<TourImage> images = tour.getTourImages();
        if(images == null) {
            images = new ArrayList<TourImage>();
        }
        
        byte[] imageBytes = ImageUtils.getImageBytesFromBase64(image.getImageData());
        
        ObjectId imageId = ObjectId.get();
        image.set_Id(imageId);
        String fileName = WEBAPP_CLIENT_DIRECTORY + "/" + TOURIMAGES_DIRECTORY + "/" + tour.getClientId() + "_" + imageId.toString() + ".jpg";
        String databaseFileName = TOURIMAGES_DIRECTORY + "/" + tour.getClientId() + "_" + imageId.toString() + ".jpg";
        fileWriterService.writeFile(fileName, imageBytes);
        image.setImageFile(databaseFileName);
        images.add(image);
        image.setImageData(null);
        tour.setTourImages(images);  
        
        saveTour(tour);
    }

    @Override
    public TourImage updateImageOnTour(Tour tour, String imageId, TourImage image) {
        List<TourImage> images = tour.getTourImages();
        int index = getIndexOfImage(images, imageId);
        if(index != -1){
            image.set_Id(new ObjectId(imageId));
            
            if(image.isPortfolio()){
                images.remove(index);
                images.add(0, image);
                index = 0;
                for(TourImage img : images){
                    if(!img.getId().equals(image.getId())){
                        img.setPortfolio(false);
                    }
                }
                tour.setPortfolioImage(image.getId());
            }
            else {
                images.set(index, image);
            }

            tour.setTourImages(images);
            saveTour(tour);
            
            return images.get(index);
        }
        return null;
    }
    
    @Override
    public void deleteImageFromTour(String imageId) {
        Tour tour = getTourFromImageId(imageId);
        if(tour == null){
            return;
        }
        
        List<TourImage> images = tour.getTourImages();
        int index = getIndexOfImage(images, imageId);
        if(index != -1){
            TourImage image = images.get(index);
            String fileName = WEBAPP_CLIENT_DIRECTORY + "/" + image.getImageFile();
            fileWriterService.deleteFile(fileName);
            images.remove(index);
            if(images.size() == 0){
                images = null;
            }
            tour.setTourImages(images);
            mongoOperations.save(tour);
        }
    }
    
    @Override
    public Tour getTourFromImageId(String imageId) {
        Criteria criteria = Criteria.where("tourImages._id").is(new ObjectId(imageId));
        Query query = Query.query(criteria);
        return mongoOperations.findOne(query, Tour.class);
    }

    
    @Override
    public List<Tour> findDrafts(String userId) {
        Criteria criteria = Criteria.where("userId").is(userId);
        Query query = Query.query(criteria);
        List<TourAction> actions = mongoOperations.find(query, TourAction.class);
        
        if(actions == null) {
        	return null;
        }
        
        List<Tour> tours = new ArrayList<Tour>();
        for(TourAction action : actions){
        	Tour tour = findTourByIdAndStatus(action.getTourId(), TourStatus.DRAFT);
        	if(tour != null && !containsTour(tours, tour.getId())){
        		tours.add(tour);	
        	}
        }
        
        return tours;
    } 

    
    /**
     * Returns lite tours (only basic props included in returned tours)
     */
    @Override
    public List<Tour> getLiteTours() {
        Query query = new Query(onlyPublishedCriteria);
        query.fields().include("mapGeoJson");
        query.fields().include("name");
        query.fields().include("grade");
        query.fields().include("elevationLoss");
        query.fields().include("elevationGain");
        query.fields().include("timingMin");
        query.fields().include("timingMax");
        query.fields().include("shortDescription");
        query.fields().include("clientId");
        return mongoOperations.find(query, Tour.class);
    }
    
    @Override
    public Tour getRandomTour() {
    	List<Tour> result = getIdsForAllToursWithImages();
    	
    	int count = result.size();
    	if(count == 0) {
    	    return null;
    	}
    	
    	Random r = new Random();
    	int random = r.nextInt(count);
    	Tour tour = mongoOperations.findById(result.get(random).getId(), Tour.class);
    	
    	// If portfolio is not set on tour, first image is returned as portfolio
    	if(tour.getPortfolioImage() == null && tour.getTourImages().size() > 0) {
    	    tour.setPortfolioImage(tour.getTourImages().get(0).getId());
    	}
    	
    	return tour;
    }
    
    public List<Tour> getIdsForAllToursWithImages() {
        Criteria imagesNotNullCriteria = Criteria.where("tourImages").ne(null);
        Query query = new Query();
        query.addCriteria(imagesNotNullCriteria);
        query.addCriteria(onlyPublishedCriteria);
        query.fields().include("_id");
        return mongoOperations.find(query, Tour.class); 
    }
    
    private boolean containsTour(List<Tour> tours, String tourId){
    	for(Tour tour : tours) {
    		if(tour.getId().equals(tourId)){
    			return true;
    		}
    	}
    	return false;
    }
    
    private int getIndexOfImage(List<TourImage> images, String imageId){
        if(images == null || imageId == null){
            return -1;
        }
        for(int i = 0; i < images.size(); i++){
            String id = images.get(i).getId();
            if(id.equals(imageId)){
                return i;
            }
        }
        return -1;
    }
    
    private void sortComments(List<Tour> tours){
        for(Tour tour: tours){
            sortComments(tour);
        }
    }
    
    private void sortComments(Tour tour){
        Collections.sort(tour.getTourComments(), new Comparator<TourComment>() {
            @Override
            public int compare(TourComment o1, TourComment o2) {
                int res = new Date(o1.getTime()).compareTo(new Date(o2.getTime()));
                if(res > 0){
                    return -1;
                }
                else if(res < 0){
                    return 1;
                }
                return 0;
            }
        });
    }

    @Override
    public Tour findTourByClientId(String clientId) {
        Tour tour = mongoOperations.findOne(new Query(
                Criteria.where("clientId").is(clientId)), Tour.class, "tour");
        return tour;
    }
    
    private String findUniqueClientId(Tour tour) {
        String startClientId = RandoNameUtils.getTextId(tour.getName());
        String clientId = startClientId;
        int startIter = 1;
        Tour duplicate = findTourByClientId(startClientId);
        while(duplicate != null) {
            clientId = startClientId + "_" + startIter;
            duplicate = findTourByClientId(clientId);
            startIter++;
        }
        return clientId;
    }

    @Override
    public List<Tour> findToursByTag(String tagId) {
        Criteria tagCriteria = Criteria.where("tags").in(tagId);
        Query query = new Query();
        query.addCriteria(tagCriteria);
        query.addCriteria(onlyPublishedCriteria);
        return mongoOperations.find(query, Tour.class);
    }

    @Override
    public List<Tag> findAllTags() {
        List<Tag> tags = mongoOperations.findAll(Tag.class);
        
        Collections.sort(tags, new Comparator<Tag>() {
            @Override
            public int compare(Tag o1, Tag o2) {
                return (Integer.valueOf(o2.getValue())).compareTo((Integer.valueOf(o1.getValue())));
            }
        });
        
        return tags;
    }

    @Override
    public List<Tour> findToursByCoordinate(Long mapCenterLat, Long mapCenterLong, Long zoomLevel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Tour> findToursByCoordinate(
            Double topLeftLatitude,
            Double topLeftLongitude,
            Double bottomRightLatitude,
            Double bottomRightLongitude) {
        
        Criteria criteria = Criteria.where("mapGeoJson").exists(true)
                .andOperator(
                        Criteria.where("centerLatitude").lte(topLeftLatitude),
                        Criteria.where("centerLongitude").gte(topLeftLongitude),
                        Criteria.where("centerLatitude").gte(bottomRightLatitude),
                        Criteria.where("centerLongitude").lte(bottomRightLongitude));
        Query query = new Query();
        query.addCriteria(criteria);
        
        return mongoOperations.find(query, Tour.class);
    }

    @Override
    public List<Tour> findToursByUser(String userId) {
        Criteria criteria = Criteria.where("userId").is(userId);
        Query query = Query.query(criteria);
        List<TourAction> actions = mongoOperations.find(query, TourAction.class);
        
        Set<String> actionTourIds = new HashSet<String>();
        for(TourAction action : actions) {
            actionTourIds.add(action.getTourId());
        }
        
        Criteria tagCriteria = Criteria.where("id").in(actionTourIds);
        Query tourQuery = new Query();
        tourQuery.addCriteria(tagCriteria);
        tourQuery.addCriteria(onlyPublishedCriteria);
        return mongoOperations.find(tourQuery, Tour.class);
    }
    
}

