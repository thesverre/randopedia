App.GpxFileUploadView = Ember.View.extend({
    templateName: 'gpx-file-upload-view',
    settings: {
        fileInputElementId: '#gpxFileInputElement'
    },

    change: function (evt) {
        if (window.File && window.FileReader && window.FileList && window.Blob) {
            var self = this;
            var input = evt.target;
            if (input.files) {
                for (var i = 0; i < input.files.length; i++) {
                    self.readFile(input.files[i]);
                }
            }
        } else {
            App.Alerts.showErrorMessage('Could not import gpx file. Most likely because your browser does not support the File API.');
        }
    },

    readFile: function (data) {
        var self = this;

        if (data) {
            var reader = new FileReader();
            reader.onloadend = function () {
                self.saveGpxFile(reader);
            };
            reader.readAsText(data);
        }
    },

    saveGpxFile: function (reader) {
        var self = this;
        var parser = new DOMParser();
        var xmlDoc = parser.parseFromString(reader.result, "application/xml");
        var geojson = toGeoJSON.gpx(xmlDoc);
        self.get('parentView').importGeoJson(geojson);
    },

    actions: {
        openFileDialog: function () {
            $(this.settings.fileInputElementId).click();
        }
    }
});

App.TourEditMapView = Ember.View.extend({
    settings: {
        mapRootElementId: '#tourEditMapRootElement',
        defaultZoomLevel: 3,
        detailedZoomLevel: 13,
        defaultMapCenter: new google.maps.LatLng(46.5, 8.5)
    },

    templateName: 'tourmapedit-view',
    mapRootElement: null,
    map: null,
    currentMapPolylines: [],
    summitPointMarker: null,
    drawingManager: null,
    selectedPolylines: [],
    mousePositionLat: null,
    mousePositionLng: null,
    loadingGpxData: false,
    gpxDataWasLoaded: false,
    gpxDataIsInvalid: false,
    allTours: [],
    draftPathType: App.Fixtures.MapSymbolTypes.UP_DOWN_TRACK,

    didInsertElement: function() {
        this.initMap();
    },

    parseGeoJson: function() {
        var self = this;

        var tourMapObjects = App.GeoHelper.getGoogleObjectsFromTourGeoJson(self.get('controller.model.mapGeoJson'), true);

        tourMapObjects.forEach(function (mapObject) {
            if (mapObject.get('rando_type') === App.Fixtures.MapSymbolTypes.SUMMIT_POINT) {
                self.setupMarkerListeners(mapObject);
                self.set('summitPointMarker', mapObject);
            } else {
                // Assume paths
                self.setupPolylineListeners(mapObject);
                self.get('currentMapPolylines').push(mapObject);
            }
            mapObject.setMap(self.get('map'));
        });
    },

    saveGeoJson: function() {
        var self = this;
        var mapObjects = [];

        self.get('currentMapPolylines').forEach(function (polyline) {
            mapObjects.push(polyline);
        });

        if (self.get('summitPointMarker')) {
            mapObjects.push(self.get('summitPointMarker'));
        }

        var geojson = App.GeoHelper.getGeoJsonFromGoogleObjects(mapObjects);
        self.get('controller').send('updateGeoJson', geojson);
    },

    importGeoJson: function(geojson) {
        var self = this;

        if (!App.GeoHelper.validateGeoJson(geojson)) {
            self.set('gpxDataIsInvalid', true);
            return;
        }

        geojson = App.GeoHelper.cleanImportedGeoJson(geojson);

        // TODO: Refactor out to helper method(s)
        var minDistanceBetweenPoints = 40;
        geojson.features.forEach(function(feature) {
            var geometry = feature.geometry;

            if (geometry.type === "LineString") {
                var coordinatesToBeDeleted = [];
                var prevCoord = null;
                // console.log('BEFORE: ' + geometry.coordinates.length);
                for (var i = 0; i < geometry.coordinates.length; i++) {

                    var currentCoord = geometry.coordinates[i];

                    if (!prevCoord) {
                        prevCoord = currentCoord;
                        continue;
                    }

                    var pt1 = { type: 'Point', coordinates: [prevCoord[0], prevCoord[1]] };
                    var pt2 = { type: 'Point', coordinates: [currentCoord[0], currentCoord[1]] };

                    var distance = gju.pointDistance(pt1, pt2);

                    if (distance < minDistanceBetweenPoints && (geometry.coordinates.length - 1)) {
                        coordinatesToBeDeleted.push(currentCoord);
                    } else {
                        prevCoord = geometry.coordinates[i];
                    }
                }

                coordinatesToBeDeleted.forEach(function(coord) {
                    var index = geometry.coordinates.indexOf(coord);
                    if (index != -1) {
                        geometry.coordinates.splice(index, 1);
                    }
                });

                //  console.log('AFTER: ' + geometry.coordinates.length);
            }
        });

        self.set('loadingGpxData', true);
        self.get('controller').send('updateGeoJson', geojson);

        setTimeout(function() {
            self.parseGeoJson();
            self.setZoomAndCenter();
            self.set('loadingGpxData', false);
            self.set('gpxDataWasLoaded', true);
            self.set('gpxDataIsInvalid', false);
        }, 500);
    },

    setZoomAndCenter: function () {
        // If tour has paths they are used to set bounds (both zoom and center)
        // If no paths but summit point is set, this is used for map center
        // Else, zoom and center are reset to defaults

        var self = this;
        var lines = self.get('currentMapPolylines');

        if (lines && lines.length > 0) {
            var bounds = new google.maps.LatLngBounds();
            for (var i = 0; i < lines.length; i++) {
                for (var j = 0; j < lines[i].getPath().length; j++) {
                    bounds.extend(lines[i].getPath().getArray()[j]);
                }
            }
            self.get('map').fitBounds(bounds);

        } else if (this.get('summitPointMarker')) {
            self.get('map').setZoom(self.settings.detailedZoomLevel);
            self.get('map').setCenter(self.get('summitPointMarker').position);

        } else {
            self.get('map').setZoom(self.settings.defaultZoomLevel);
            self.get('map').setCenter(self.settings.defaultMapCenter);
        }
    },

    setMapSize: function() {
        var self = this;
        var newWidth = $('.mapContainer').width();
        var newHeight = 800;
        self.get('mapRootElement').css({ width: newWidth + 'px', height: newHeight + 'px' });
    },

    setDrawingMode: function(type) {
        var self = this;
        var dm = self.get('drawingManager');

        switch (type) {
        case App.Fixtures.MapSymbolTypes.UP_DOWN_TRACK:
            dm.polylineOptions.strokeColor = App.Fixtures.MapObjectStyles.DEFAULT_PATH_COLOR;
            dm.setDrawingMode(google.maps.drawing.OverlayType.POLYLINE);
            break;
        case App.Fixtures.MapSymbolTypes.UP_TRACK:
            dm.polylineOptions.strokeColor = App.Fixtures.MapObjectStyles.UP_PATH_COLOR;
            dm.setDrawingMode(google.maps.drawing.OverlayType.POLYLINE);
            break;
        case App.Fixtures.MapSymbolTypes.DOWN_TRACK:
            dm.polylineOptions.strokeColor = App.Fixtures.MapObjectStyles.DOWN_PATH_COLOR;
            dm.setDrawingMode(google.maps.drawing.OverlayType.POLYLINE);
            break;
        case App.Fixtures.MapSymbolTypes.SUMMIT_POINT:
            dm.setDrawingMode(google.maps.drawing.OverlayType.MARKER);
            break;
        default:
            dm.setDrawingMode(null);
            break;
        }
    },

    onPolylinePathChanged: function() {
        var self = this;
        if (!self.get('drawingManager')) {
            return;
        }
        self.get('drawingManager').setDrawingMode(null);
        self.saveGeoJson();
    },

    onPolylineClick: function(polyline) {
        var self = this;
        if (polyline.strokeColor === App.Fixtures.MapObjectStyles.SELECTED_PATH_COLOR) {
            switch (polyline.get('rando_type')) {
            case App.Fixtures.MapSymbolTypes.UP_DOWN_TRACK:
                polyline.setOptions({ strokeColor: App.Fixtures.MapObjectStyles.DEFAULT_PATH_COLOR, strokeWeight: App.Fixtures.MapObjectStyles.DEFAULT_PATH_WIDTH });
                break;
            case App.Fixtures.MapSymbolTypes.UP_TRACK:
                polyline.setOptions({ strokeColor: App.Fixtures.MapObjectStyles.UP_PATH_COLOR, strokeWeight: App.Fixtures.MapObjectStyles.DEFAULT_PATH_WIDTH });
                break;
            case App.Fixtures.MapSymbolTypes.DOWN_TRACK:
                polyline.setOptions({ strokeColor: App.Fixtures.MapObjectStyles.DOWN_PATH_COLOR, strokeWeight: App.Fixtures.MapObjectStyles.DEFAULT_PATH_WIDTH });
                break;
            }
            self.get('selectedPolylines').removeObject(polyline);
        } else {
            polyline.setOptions({ strokeColor: App.Fixtures.MapObjectStyles.SELECTED_PATH_COLOR, strokeWeight: App.Fixtures.MapObjectStyles.SELECTED_PATH_WIDTH });
            self.get('selectedPolylines').insertAt(0, polyline);
        }
    },

    setupMarkerListeners: function (marker) {
        var self = this;
        google.maps.event.addListener(marker, "dragend", function () {
            self.saveGeoJson();
        });
    },

    setupPolylineListeners: function(polyline) {
        var self = this;
        var path = polyline.getPath();

        google.maps.event.addListener(path, 'set_at', function() {
            self.onPolylinePathChanged();
        });
        google.maps.event.addListener(path, 'insert_at', function() {
            self.onPolylinePathChanged();
        });
        google.maps.event.addListener(polyline, 'click', function() {
            self.onPolylineClick(this);
        });
    },

    addSummitPointMarker: function(marker) {
        var self = this;

        if (self.get('summitPointMarker')) {
            return;
        }

        marker.set('rando_type', App.Fixtures.MapSymbolTypes.SUMMIT_POINT);
        self.set('summitPointMarker', marker);
        self.setupMarkerListeners(self.get('summitPointMarker'));
        self.saveGeoJson();
        self.get('drawingManager').setDrawingMode(null);
    },

    addPolyline: function(polyline) {
        var self = this;
        self.get('currentMapPolylines').push(polyline);
        self.saveGeoJson();
    },

    // todo: Work in progress on "show other tours" feature
    //startShowOtherTours: function () {
    //    var self = this;

    //    if (self.get('allTours').length > 0) {
    //        self.showOtherTours();
    //    }

    //    self.set('loadingAllTours', true);
    //    self.get('store').findQuery('tour', { liteTours: true }).then(function (tours) {
    //        self.set('allTours', tours);
    //        self.set('loadingAllTours', false);
    //        self.showOtherTours();
    //    }, function (error) {
    //        App.Alerts.showErrorMessage('Error when loading tours');
    //        self.set('loadingAllTours', false);
    //    });
    //},

    //showOtherTours: function () {
    //    var tourPaths = [];

    //    self.get('allTours').forEach(function(tour) {
    //        var mapObjects = App.GeoHelper.getGoogleObjectsFromTourGeoJson(tour.get('mapGeoJson'));
    //        mapObjects.forEach(function(mapObject) {
    //            if (mapObject.get('rando_type') !== App.Fixtures.MapSymbolTypes.SUMMIT_POINT) {
    //                tourPaths.push(mapObject);
    //                mapObject.setMap(self.get('map'));
    //            }
    //        });
    //    });

    //    self.set('otherToursPaths', tourPaths);
    //},

    //hideOtherTours: function() {
        
    //},

    actions: {
        clearDrawingMode: function() {
            this.get('drawingManager').setDrawingMode(null);
        },
        drawUpDownTrack: function() {
            this.setDrawingMode(App.Fixtures.MapSymbolTypes.UP_DOWN_TRACK);
        },
        drawUpTrack: function() {
            this.setDrawingMode(App.Fixtures.MapSymbolTypes.UP_TRACK);
        },
        drawDownTrack: function() {
            this.setDrawingMode(App.Fixtures.MapSymbolTypes.DOWN_TRACK);
        },
        drawSummitPoint: function() {
            this.setDrawingMode(App.Fixtures.MapSymbolTypes.SUMMIT_POINT);
        },
        deleteRoutes: function() {
            var self = this;
            self.get('selectedPolylines').forEach(function(line) {
                line.setMap(null);
                var index = self.get('currentMapPolylines').indexOf(line);
                self.get('currentMapPolylines').splice(index, 1);
            });

            self.get('selectedPolylines').clear();

            self.saveGeoJson();
        },
        closeGpxImportModal: function() {
            this.set('gpxDataWasLoaded', false);
        },
        updatePathsType: function () {
            var self = this;
            self.get('selectedPolylines').forEach(function (polyline) {
                App.GeoHelper.setPolylineDefaultOptions(polyline, self.get('draftPathType'));
            });

            self.saveGeoJson();

            self.send('clearSelectedPolylines');
            self.set('draftPolyLineType', App.Fixtures.MapSymbolTypes.UP_DOWN_TRACK);
        },
        clearSelectedPolylines: function() {
            this.set('selectedPolylines', []);
        }
    },

    hasSummitPointMarker: function() {
        return this.get('summitPointMarker') !== null;
    }.property('summitPointMarker'),

    isDeletePathsDisabled: function () {
        return !this.get('haveSelectedPaths');
    }.property('selectedPolylines.[]'),

    haveSelectedPaths: function () {
        return this.get('selectedPolylines').length > 0;
    }.property('selectedPolylines.[]'),

    initMap: function () {
        var self = this;

        self.set('currentMapPolylines', []);
        self.set('selectedPolylines', []);

        self.set('mapRootElement', self.$(self.settings.mapRootElementId));

        var mapOptions = {
            mapTypeId: google.maps.MapTypeId.TERRAIN,
            mapTypeControl: true,
            mapTypeControlOptions: {
                style: google.maps.MapTypeControlStyle.DROPDOWN_MENU
            },
            zoomControl: true,
            zoomControlOptions: {
                style: google.maps.ZoomControlStyle.SMALL
            },
            scaleControl: true,
            scaleControlOptions: {
                position: google.maps.ControlPosition.TOP_LEFT
            },
            scrollwheel: true,
            panControl: true,
            streetViewControl: false,
            overviewMapControl: false,
            rotateControl: false
        };

        self.set('map', new google.maps.Map(self.get('mapRootElement').get(0), mapOptions));

        self.set('drawingManager', new google.maps.drawing.DrawingManager({
            drawingMode: null,
            drawingControl: true,
            drawingControlOptions: {
                position: google.maps.ControlPosition.TOP_CENTER,
                drawingModes: []
            },
            markerOptions: {
                draggable: true
            },
            polylineOptions: {
                strokeColor: App.Fixtures.MapObjectStyles.DEFAULT_PATH_COLOR,
                strokeWeight: App.Fixtures.MapObjectStyles.DEFAULT_PATH_WIDTH,
                editable: true,
                zIndex: 1,
                geodesic: true
            }
        }));

        self.get('drawingManager').setMap(self.get('map'));

        self.setMapSize();
        self.parseGeoJson();
        self.setZoomAndCenter();

        var redrawMap = function () {
            self.setMapSize();
            google.maps.event.trigger(self.get('map'), 'resize');
            self.setZoomAndCenter();
        };

        google.maps.event.addListener(self.get('drawingManager'), "markercomplete", function (marker) {
            self.addSummitPointMarker(marker);
        });

        google.maps.event.addListener(self.get('drawingManager'), 'polylinecomplete', function (polyline) {

            if (polyline.strokeColor === App.Fixtures.MapObjectStyles.DEFAULT_PATH_COLOR) {
                polyline.set('rando_type', App.Fixtures.MapSymbolTypes.UP_DOWN_TRACK);
            } else if (polyline.strokeColor === App.Fixtures.MapObjectStyles.UP_PATH_COLOR) {
                polyline.set('rando_type', App.Fixtures.MapSymbolTypes.UP_TRACK);
            } else if (polyline.strokeColor === App.Fixtures.MapObjectStyles.DOWN_PATH_COLOR) {
                polyline.set('rando_type', App.Fixtures.MapSymbolTypes.DOWN_TRACK);
            }

            self.get('drawingManager').setDrawingMode(null);

            self.addPolyline(polyline);
            self.setupPolylineListeners(polyline);
        });

        google.maps.event.addListener(self.get('map'), 'mousemove', function (event) {
            self.set('mousePositionLat', App.GeoHelper.roundCoordinate(event.latLng.lat()));
            self.set('mousePositionLng', App.GeoHelper.roundCoordinate(event.latLng.lng()));
        });

        // Hook up to window resize event to do implicit resize on map canvas
        $(window).on('resize', redrawMap);
    },
});
