package org.deegree;

import java.util.List;
import java.util.Map;


import org.geomajas.configuration.FeatureInfo;
import org.geomajas.global.GeomajasException;
import org.geomajas.layer.LayerException;
import org.geomajas.layer.VectorLayerService;
import org.geomajas.layer.feature.Attribute;
import org.geomajas.layer.feature.InternalFeature;
import org.geomajas.service.GeoService;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.vividsolutions.jts.geom.Envelope;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/org/geomajas/spring/geomajasContext.xml",
		"/layerDeegreeWfs.xml"})
public class TestDeegreeLayer {

	@Autowired
	@Qualifier("layerWfs")
	private DeegreeLayer layerWfs;
	
	@Autowired
	private VectorLayerService vectorLayerService;
	
	@Autowired
	private FeatureInfo vectorFeatureInfo;

	@Autowired
	private GeoService geoService;
	
	@Autowired
	private org.geomajas.security.SecurityManager securityManager;

	private CoordinateReferenceSystem crs;
	
	/*
	 * using deegree 3.3.1 wfs with gml data
	 * test reference data on gist.github available: 
	 * FlNaturDNEU.xml  -> https://gist.github.com/martin-vi/2e8e62803559e653c6d3
	 * FlNaturDNEU.xsd 	-> https://gist.github.com/martin-vi/3e282dd0f1fe40cda8c5 
	 */
    @Before
    public void initialize() throws LayerException {
    	
    	// allow everything
    	securityManager.createSecurityContext(null);
    	// init crs from spring config "org.deegree.DeegreeLayer"
    	crs = geoService.getCrs2(layerWfs.getLayerInfo().getCrs());
     }
	
    /*
     * test if any features exist
     */
	@Test
	public void testDeegreeWFSLayer() throws GeomajasException {
		
		List<InternalFeature> features = vectorLayerService.getFeatures(
				"layerWfs", this.crs, Filter.INCLUDE, null, VectorLayerService.FEATURE_INCLUDE_ALL);
		
		Assert.assertNotNull(features);
		Assert.assertFalse(features.isEmpty());
	}
	
	/*
	 * look for the needed geometry property in "org.geomajas.configuration.FeatureInfo"
	 */
	@Test
	public void testVectorFeatureInfo() {
		
		String testString = "geometryProperty";
		Assert.assertTrue( testString.equals( vectorFeatureInfo.getGeometryType().getName() ) );
	}
	
	/*
	 * try to access and return the configured attributes from "org.geomajas.configuration.FeatureInfo"
	 */
	@Test
	public void testAttributesDeegreeWFSLayer() throws LayerException, GeomajasException {
		
		List<InternalFeature> features = vectorLayerService.getFeatures(
				"layerWfs", this.crs, Filter.INCLUDE, null, VectorLayerService.FEATURE_INCLUDE_ALL);
		
		Assert.assertNotNull(features);
		Assert.assertFalse(features.isEmpty());
		
		InternalFeature singleFeature = features.get(0);
		Map<String, Attribute> attrs = singleFeature.getAttributes();
		Assert.assertFalse(attrs.isEmpty());
		for (String key : attrs.keySet() ) {
			Assert.assertFalse( attrs.get(key).isEmpty() );
			System.out.println("key : \"" + key + "\", value : \"" + attrs.get(key) +"\"");
		}
		
	}
	
	/*
	 * access the wfs with a bounding box filter, should return a feature subset of 9 items
	 */
	@Test
	public void testFilterBBoxDeegreeWFSLayer() throws GeomajasException, CQLException {
		
		Filter bboxFilter = ECQL.toFilter("BBOX(geometryProperty, 13.361, 50.859, 13.440, 50.927 )");
		
		List<InternalFeature> features = vectorLayerService.getFeatures(
				"layerWfs", this.crs, bboxFilter, null, VectorLayerService.FEATURE_INCLUDE_ALL);
		
		Assert.assertNotNull( features );
		Assert.assertTrue( features.size() == 9 );
		
		System.out.println( bboxFilter.toString() );
		System.out.println( features.toString() );
	}
	
	/*
	 * validate the boundingbox of all features
	 */
	@Test
	public void testBoundsDeegreeWFSLayer() throws LayerException {
		
		System.out.println( layerWfs.getBounds() );
		
		String wfsBounds = String.format( "%s %s %s %s",
				layerWfs.getBounds().getMinX(),
				layerWfs.getBounds().getMinY(),
				layerWfs.getBounds().getMaxX(),
				layerWfs.getBounds().getMaxY() );
		
		Assert.assertTrue( wfsBounds.equals("12.65911 50.661907 13.612227 51.213652") );

	}
	
	@Test
	public void testFilteredBounds() throws LayerException, CQLException {
		
		// ECQL Filter
		Filter bboxFilter = ECQL.toFilter("BBOX(geometryProperty, 13.361, 50.859, 13.440, 50.927 )");
		// testing Envelope
		Envelope bboxEnvelope = new Envelope(13.361, 13.440, 50.859, 50.927);
		
		Envelope featureEnvelope = layerWfs.getBounds(bboxFilter);
		System.out.println( featureEnvelope );
		
		Envelope intersectEnvelope = featureEnvelope.intersection(bboxEnvelope);
		System.out.println( intersectEnvelope );
				
		Assert.assertNotNull( intersectEnvelope );
		
	}

	
//	@Test
//	public void testReadAllFeatures() throws GeomajasException, CQLException {
//		Filter filter = CQL.toFilter("attName >= 5");
//		layerWfs.getElements(filter, 0, Integer.MAX_VALUE);	//Filter.INCLUDE
//	}
//	
//	

	
}
