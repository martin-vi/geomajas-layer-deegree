package org.deegree;

import javax.xml.namespace.QName;

import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.filter.expression.ValueReference;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.opengis.filter.And;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.Id;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNotEqualTo;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;



public class DeegreeVisitor implements FilterVisitor {

	/* logic operators */ 
	@Override
	public Object visit(And filter, Object extraData) {
		return new org.deegree.filter.logical.And(null);
	}

	@Override
	public Object visit(Not filter, Object extraData) {
		return new org.deegree.filter.logical.Not(null);
	}

	@Override
	public Object visit(Or filter, Object extraData) {
		return new org.deegree.filter.logical.Or(null);
	}	

	/* PropertyIsSOMETHING operator */
	@Override
	public Object visit(PropertyIsBetween filter, Object extraData) {
		return new org.deegree.filter.comparison.PropertyIsBetween(null, null, null, false, null);
	}

	@Override
	public Object visit(PropertyIsEqualTo filter, Object extraData) {
		return new org.deegree.filter.comparison.PropertyIsEqualTo(null, null, null, null);
	}

	@Override
	public Object visit(PropertyIsNotEqualTo filter, Object extraData) {
		return new org.deegree.filter.comparison.PropertyIsNotEqualTo(null, null, null, null);
	}

	@Override
	public Object visit(PropertyIsGreaterThan filter, Object extraData) {
		return new org.deegree.filter.comparison.PropertyIsGreaterThan(null, null, null, null);
	}

	@Override
	public Object visit(PropertyIsGreaterThanOrEqualTo filter, Object extraData) {
		return new org.deegree.filter.comparison.PropertyIsLessThanOrEqualTo(null, null, null, null);
	}

	@Override
	public Object visit(PropertyIsLessThan filter, Object extraData) {
		return new org.deegree.filter.comparison.PropertyIsLessThan(null, null, null, null);
	}

	@Override
	public Object visit(PropertyIsLessThanOrEqualTo filter, Object extraData) {
		return new org.deegree.filter.comparison.PropertyIsLessThanOrEqualTo(null, null, null, null);
	}

	@Override
	public Object visit(PropertyIsLike filter, Object extraData) {
		return new org.deegree.filter.comparison.PropertyIsLike(null, null, null, null, null, null, null);
	}

	@Override
	public Object visit(PropertyIsNull filter, Object extraData) {
		return new org.deegree.filter.comparison.PropertyIsNull(null, null);
	}

	@Override
	public Object visit(BBOX filter, Object extraData) {
		Envelope envelope = null;
		try {
			envelope = new GeometryFactory().createEnvelope(
							filter.getMinX(), filter.getMaxX(),
							filter.getMinY(), filter.getMaxY(),
							CRSManager.lookup( "EPSG:4326" ));
		} catch (UnknownCRSException e) {
			e.printStackTrace();
		}
		
        String NS = "http://www.polymap.org/";
        QName qnameProp = new QName(NS, "the_geom", "polymap" );
        ValueReference prop = new ValueReference(qnameProp);
		
		return new org.deegree.filter.spatial.BBOX( prop, envelope );
	}

	@Override
	public Object visit(ExcludeFilter filter, Object extraData) {
		return null;
	}

	@Override
	public Object visit(IncludeFilter filter, Object extraData) {
		return null;
	}

	@Override
	public Object visit(Id filter, Object extraData) {
		return new org.deegree.filter.IdFilter("");
	}

	@Override
	public Object visit(Beyond filter, Object extraData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Contains filter, Object extraData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Crosses filter, Object extraData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Disjoint filter, Object extraData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(DWithin filter, Object extraData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Equals filter, Object extraData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Intersects filter, Object extraData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Overlaps filter, Object extraData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Touches filter, Object extraData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Within filter, Object extraData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitNullFilter(Object extraData) {
		throw new UnsupportedOperationException("visit(Object userData)");
	}
	
	
}
