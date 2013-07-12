package org.deegree;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.geometry.standard.AbstractDefaultGeometry;
import org.geomajas.configuration.VectorLayerInfo;
import org.geomajas.layer.LayerException;
import org.geomajas.layer.feature.Attribute;
import org.geomajas.layer.feature.FeatureModel;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class DeegreeFeatureModel implements FeatureModel {
	
	private VectorLayerInfo vectorLayerInfo;
	
	private int srid;
	
	public DeegreeFeatureModel( int srid ) throws LayerException  {
		super();
		this.srid = srid;
	}
	
	@Override
	public void setLayerInfo(VectorLayerInfo vectorLayerInfo)
			throws LayerException {
		this.vectorLayerInfo = vectorLayerInfo;
	}

	@Override
	public Attribute getAttribute(Object feature, String name)
			throws LayerException {
		/*org.deegree.feature.Feature deegreeFeature = (org.deegree.feature.Feature) feature;
		 * deegreeFeature.getProperties(QName.valueOf(name));
		 */
		return null;
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
		AbstractDefaultGeometry test = (AbstractDefaultGeometry)geometry;
		//geometry.getJTSGeometry()
		
		Geometry jtsGeometry = test.getJTSGeometry();
		
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
	public String getGeometryAttributeName() throws LayerException {
		
		return "dummy string ...";
	}

	@Override
	public boolean canHandle(Object feature) {
		return feature instanceof SimpleFeature;
	}

}
