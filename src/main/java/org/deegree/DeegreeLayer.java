package org.deegree;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.Feature;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.spatial.BBOX;
import org.deegree.geometry.GeometryFactory;
import org.deegree.protocol.ows.exception.OWSExceptionReport;
import org.deegree.protocol.wfs.client.GetFeatureResponse;
import org.deegree.protocol.wfs.client.WFSClient;
import org.deegree.protocol.wfs.client.WFSFeatureCollection;
import org.deegree.protocol.wfs.metadata.WFSFeatureType;
import org.geomajas.annotation.Api;
import org.geomajas.configuration.VectorLayerInfo;
import org.geomajas.layer.LayerException;
import org.geomajas.layer.VectorLayer;
import org.geomajas.layer.feature.FeatureModel;
import org.geomajas.service.GeoService;
import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

@Api
public class DeegreeLayer implements VectorLayer {
	
	private static final Operator[] Operator = null;

	private Logger LOG = LoggerFactory.getLogger( DeegreeLayer.class );

	private WFSClient client;
	
	private String url;
	
	private String featureTypeName;
	
	private String featureGeometyPropertyName;
	
	private WFSFeatureType featureType;

	private String getCapabilityRequest = "?service=WFS&request=GetCapabilities&version=1.1.0";

	private org.deegree.geometry.Envelope featureBBox;

	private ValueReference featureProp;

	private DeegreeFeatureModel featureModel;
	
	@Autowired
	private GeoService geoService; // used for csr stuff 

	@Autowired
	private VectorLayerInfo layerInfo;

	private CoordinateReferenceSystem crs;

	private String id;

	private int srid;
	

	@PostConstruct
	public void initLayer() throws OWSExceptionReport, XMLStreamException, IOException, LayerException {


		//	    EPSG:31466 (Gauss-Krüger 2. Meridian)
		//	    EPSG:31467 (Gauss-Krüger 3. Meridian)
		//	    EPSG:4326 (WGS84)
		//	    EPSG:4258 (ETRS89, geographische Koordinaten)
		//	    EPSG:900913 (MERCATOR (GOOGLE))
		//	    EPSG:25832 (ETRS89, UTM, Zone 32)
		//	    EPSG:25833 (ETRS89, UTM, Zone 33)
		//		crs = geoService.getCrs2("EPSG:900913");
				
		/* configured crs */
		crs = geoService.getCrs2(layerInfo.getCrs());
		srid = geoService.getSridFromCrs(this.crs);
			
		// TODO - check if get getCapabilityRequest (query part) is already in url ...
		URL fullUrl = new URL( url + getCapabilityRequest );
		LOG.debug("WFS request url: " + fullUrl.toString());
		
		client = new WFSClient(fullUrl);
		
		List<WFSFeatureType> fTypes = client.getFeatureTypes();
        for (WFSFeatureType fType: fTypes) {
        	if ( featureTypeName.equals(fType.getName().getLocalPart()) ) {
        		this.featureType = fType;
        		LOG.debug(
        				String.format( "feature type %s found", this.featureType.toString() ) );
        		break;
        	}
        }
        
        if ( this.featureType.equals(null) ) {
        	throw new IllegalArgumentException(
        			String.format( "feature type %s not found, check WFS GetCapabilities \"%s\"",
        					this.featureTypeName, fullUrl.toString() ) );
        }
        
        this.featureBBox = this.featureType.getWGS84BoundingBox();
        LOG.debug( "feature type WGS84 bounding box " + this.featureBBox.toString() );
        
        // TODO feature property needed?
        QName featureQName = this.featureType.getName();
        
        //geometry name
        // this.featureGeometyPropertyName
        //	OR
        // this.layerInfo.getFeatureInfo().getGeometryType().getName()
        
        QName propertyQName = new QName(
        		featureQName.getNamespaceURI(), this.layerInfo.getFeatureInfo().getGeometryType().getName(), featureQName.getPrefix() );
        featureProp = new ValueReference( propertyQName );
        LOG.debug( featureProp.toString() );		
        //this.featureType.getName().valueOf(this.featureGeometyPropertyName) );
        
        
        // TODO get CRS from client or featureType
        // from config, should be 31468
        featureModel = new DeegreeFeatureModel( this.srid, featureQName.getNamespaceURI() ); // geoService.getSridFromCrs(deegreeWFScrs)
        featureModel.setLayerInfo(this.layerInfo);

        
	}
	
	public void setLayerInfo(VectorLayerInfo layerInfo) throws LayerException {
		this.layerInfo = layerInfo;
		if (null != featureModel) {
			featureModel.setLayerInfo(getLayerInfo());
		}
	}
	
	
	@Override
	public VectorLayerInfo getLayerInfo() {
		return this.layerInfo;
	}

	@Override
	public CoordinateReferenceSystem getCrs() {
		return this.crs;
	}

	@Override
	public String getId() {
		return this.id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	

	@Override
	public boolean isCreateCapable() {
		return false;
	}

	@Override
	public boolean isUpdateCapable() {
		return false;
	}

	@Override
	public boolean isDeleteCapable() {
		return false;
	}

	@Override
	public FeatureModel getFeatureModel() {
		return featureModel;
	}

	@Override
	public Object create(Object feature) throws LayerException {
		return null;
	}

	@Override
	public Object saveOrUpdate(Object feature) throws LayerException {
		return null;
	}

	@Override
	public Object read(String featureId) throws LayerException {
		return null;
	}

	@Override
	public void delete(String featureId) throws LayerException {
		// do nothing here
	}

	@Override
	public Iterator<?> getElements(Filter filter, int offset, int maxResultSize) throws LayerException {
		try {		
			
			org.deegree.filter.Filter dFilter = null;
			

			//DummyFilter
		    /*
			org.deegree.filter.Filter deegreeFilter = null;
		    org.deegree.geometry.Envelope envelopeBox = null;
			envelopeBox = new GeometryFactory().createEnvelope(13.361, 50.859, 13.440, 50.927, CRSManager.lookup( "EPSG:4326" ));
	        deegreeFilter = new OperatorFilter( new BBOX( this.featureProp, envelopeBox ) );
			*/
			
			// visitor pattern filter usage 
			if (filter != null) {
				if (filter != Filter.INCLUDE) {
			        DeegreeVisitor deegreeVisitor = new DeegreeVisitor( this.featureModel );
			        
			        org.deegree.filter.Operator op = (org.deegree.filter.Operator) filter.accept(deegreeVisitor, null);
			        if (op != null) {
			        	dFilter = new OperatorFilter( op );
			        }
				}
			}
						
			// do getFeature request
		    GetFeatureResponse<Feature> result = null;
	        //result = client.getFeatures(this.featureType.getName(), deegreeFilter);
	        result = client.getFeatures(this.featureType.getName(), dFilter);
			
			// iterate over features
            WFSFeatureCollection<Feature> wfsFc = result.getAsWFSFeatureCollection();
            Iterator<Feature> iter = wfsFc.getMembers();
            
            return iter;
			
			
		} catch (OWSExceptionReport e) {
			
			//e.printStackTrace();
			throw new LayerException(e);
		} catch (IOException e) {
			throw new LayerException(e);
		} catch (XMLParsingException e) {
			throw new LayerException(e);
		} catch (XMLStreamException e) {
			throw new LayerException(e);
		} catch (UnknownCRSException e) {
			throw new LayerException(e);
		} catch (TransformationException e) {
			throw new LayerException(e);
		}
	}

	@Override
	public Envelope getBounds(Filter filter) throws LayerException {
		getElements(filter, 0, Integer.MAX_VALUE);
		
		return null;
	}

	@Override
	public Envelope getBounds() throws LayerException {
		org.deegree.geometry.Envelope dEnvelope = this.featureBBox.getEnvelope();
		LOG.debug( this.featureBBox.getEnvelope().getMax().toString() );
		
		Envelope BBox = new Envelope(
				new Coordinate(dEnvelope.getMin().get0(), dEnvelope.getMin().get1()),
				new Coordinate(dEnvelope.getMax().get0(), dEnvelope.getMax().get1()) );
		
		LOG.debug( 
				String.format("bbox layer \"%s\" min :  %f %f max : %f %f",
						this.featureType.getName().getLocalPart(),
						BBox.getMinX(), BBox.getMinY(),
						BBox.getMaxX(), BBox.getMaxY() ) ); 
		
		return BBox;
	}

	/* spring bean property getters and setters */
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getFeatureTypeName() {
		return featureTypeName;
	}

	public void setFeatureTypeName(String featureTypeName) {
		this.featureTypeName = featureTypeName;
	}

	public String getFeatureGeometyPropertyName() {
		return featureGeometyPropertyName;
	}

	public void setFeatureGeometyPropertyName(String featureGeometyPropertyName) {
		this.featureGeometyPropertyName = featureGeometyPropertyName;
	}

}
