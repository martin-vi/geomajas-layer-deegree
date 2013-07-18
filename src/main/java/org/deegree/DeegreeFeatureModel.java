package org.deegree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.feature.GenericFeature;
import org.deegree.geometry.standard.AbstractDefaultGeometry;
import org.geomajas.configuration.VectorLayerInfo;
import org.geomajas.layer.LayerException;
import org.geomajas.layer.entity.EntityAttributeService;
import org.geomajas.layer.entity.EntityMapper;
import org.geomajas.layer.feature.Attribute;
import org.geomajas.layer.feature.FeatureModel;
import org.geomajas.layer.feature.attribute.StringAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.springframework.beans.factory.annotation.Autowired;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class DeegreeFeatureModel implements FeatureModel {
	
	private VectorLayerInfo vectorLayerInfo;
	
	private int srid;
	
	private String nameSpaceURI;

	/* Entity mapping for Attributes */
//	@Autowired
//	private EntityAttributeService entityMappingService;
//	private EntityMapper entityMapper;
	
	//private Object dICRS;
	
	public DeegreeFeatureModel( int srid, String nameSpaceURI ) throws LayerException  {
		super();
		this.srid = srid;
		//this.dICRS = org.deegree.cs.coordinatesystems.ICRS;
		
		this.nameSpaceURI = nameSpaceURI;
	}
	
	public String getNameSpaceURI() {
		return this.nameSpaceURI;
	}
	
	@Override
	public void setLayerInfo(VectorLayerInfo vectorLayerInfo)
			throws LayerException {
		this.vectorLayerInfo = vectorLayerInfo;
	}

	@SuppressWarnings({ "rawtypes", "unused" })
	@Override
					// Object org.deegree.feature.GenericFeature , String "gml_id"
	public Attribute getAttribute(Object feature, String name)
			throws LayerException {
		GenericFeature dFeature = (org.deegree.feature.GenericFeature) feature;
		
		//TODO hard coded ns 
		String NS = "http://org.maptools.org/"; 
		
		//List<Property> prop = dFeature.getProperties( new QName(NS, name, "ogr" ) );
		TypedObjectNode value = null;
		List<Property> prop = dFeature.getProperties();
		for (Property p : prop) {
			if ( p.getName().getLocalPart().equals(name) ) {
				value = p.getValue();
				break;
			}
		}
		
		/* TODO convert deegree value to geomajas value */
		System.out.println(value);
		
		/*org.deegree.feature.Feature deegreeFeature = (org.deegree.feature.Feature) feature;
		 * deegreeFeature.getProperties(QName.valueOf(name));
		 */
		Attribute geomajasAttr = new StringAttribute();
		return null;
		
		//{gml_id=null, Gebietsname=null}
	}

	@Override
	public Map<String, Attribute> getAttributes(Object feature) throws LayerException {
		
		// at the moment return an empty attribute map
		HashMap<String, Attribute> attribs = new HashMap<String, Attribute>();
		return attribs;
	}

	@Override
	public String getId(Object feature) throws LayerException {
		org.deegree.feature.Feature deegreeFeature = (org.deegree.feature.Feature) feature;
		return deegreeFeature.getId();
	}

	@Override
	public Geometry getGeometry(Object feature) throws LayerException {
		org.deegree.feature.Feature deegreeFeature = (org.deegree.feature.Feature) feature;
		
		// TODO get all geometries
		//org.deegree.commons.tom.gml.property.Property geometry = deegreeFeature.getGeometryProperties().get(0);
		org.deegree.geometry.Geometry geometry = org.deegree.filter.utils.FilterUtils.getGeometryValue(
					deegreeFeature.getGeometryProperties().get(0).getValue()
				);
		AbstractDefaultGeometry deegreeGeometry = (AbstractDefaultGeometry)geometry;
		//geometry.getJTSGeometry()
		
		Geometry jtsGeometry = deegreeGeometry.getJTSGeometry();
		
		//jtsGeometry = new WKTReader().read(geometry.toString());
		//TypedObjectNode a = geometry.getValue();
		
		//jtsGeometry = (com.vividsolutions.jts.geom.Geometry)geometry;
		
		jtsGeometry.setSRID(srid);
		return (Geometry) jtsGeometry.clone();
	}

	@Override
	public void setAttributes(Object feature, Map<String, Attribute> attributes)
			throws LayerException {
		// do nothing here ...
	}

	@Override
	public void setGeometry(Object feature, Geometry geometry)
			throws LayerException {
		// do nothing here ...
	}

	@Override
	public Object newInstance() throws LayerException {
		// do nothing here ...	
		return null;
	}

	@Override
	public Object newInstance(String id) throws LayerException {
		// do nothing here ...	
		return null;
	}

	@Override
	public int getSrid() throws LayerException {
		return this.srid;
	}

	@Override
	public String getGeometryAttributeName()  {
		return vectorLayerInfo.getFeatureInfo().getGeometryType().getName();
	}

	@Override
	public boolean canHandle(Object feature) {
		return feature instanceof SimpleFeature;
	}

}
