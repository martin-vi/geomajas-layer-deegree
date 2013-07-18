package org.deegree;

import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.geomajas.configuration.FeatureInfo;
import org.geomajas.configuration.NamedStyleInfo;
import org.geomajas.configuration.client.ClientMapInfo;
import org.geomajas.configuration.client.ClientVectorLayerInfo;
import org.geomajas.geometry.Bbox;
import org.geomajas.global.GeomajasException;
import org.geomajas.layer.LayerException;
import org.geomajas.layer.VectorLayer;
import org.geomajas.layer.VectorLayerService;
import org.geomajas.layer.feature.Attribute;
import org.geomajas.layer.feature.InternalFeature;
import org.geomajas.service.GeoService;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
	
	@Test
	public void testReadAllFeatures() throws GeomajasException, CQLException {
		Filter filter = CQL.toFilter("attName >= 5");
		layerWfs.getElements(filter, 0, Integer.MAX_VALUE);	//Filter.INCLUDE
	}
	
	@Test
	public void testWFSLayerBBox() throws LayerException {
		layerWfs.getBounds();
	}
	
	@Test
	public void testVectorFeatureInfo() {
		System.out.println(vectorFeatureInfo.getGeometryType().getName());
	}
	
	@Test
	public void testGeomajasLayer() throws LayerException, GeomajasException {
		
		securityManager.createSecurityContext(null);
		
		
		@SuppressWarnings("deprecation")
		List<InternalFeature> features = vectorLayerService.getFeatures(
				"layerWfs",
				geoService.getCrs(layerWfs.getLayerInfo().getCrs()),
				Filter.INCLUDE,
				null,
				VectorLayerService.FEATURE_INCLUDE_GEOMETRY);
		
		Assert.assertNotNull(features);
		Assert.assertFalse(features.isEmpty());
		
		InternalFeature singleFeature = features.get(0);
		Map<String, Attribute> attrs = singleFeature.getAttributes();
		System.out.println("Attributes: " + attrs);
	}
	
	@Test
	public void testCQLFilter() throws CQLException {
		
		/*Filter bboxFilter = CQL.toFilter("BBOX(the_geom, " +
				"POLYGON ((14.054227941176457 50.30683519635355, " +
				"14.054227941176457 50.79547083733429," +
				"14.773897058823515 50.79547083733429, " +
				"14.773897058823515 50.30683519635355, " +
				"14.054227941176457 50.30683519635355)) )" );*/
		Filter bboxFilter = ECQL.toFilter("BBOX(geometryProperty, 13.361, 50.859, 13.440, 50.927 )");
		
		System.out.println(bboxFilter.toString());
		
	}
	
}
