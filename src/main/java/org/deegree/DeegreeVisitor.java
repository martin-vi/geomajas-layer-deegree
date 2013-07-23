package org.deegree;

import java.text.AttributedCharacterIterator.Attribute;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.cs.persistence.CRSStore;
import org.deegree.cs.refs.coordinatesystem.CRSRef;
import org.deegree.filter.MatchAction;
import org.deegree.filter.expression.ValueReference;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.standard.AbstractDefaultGeometry;
import org.deegree.geometry.standard.DefaultEnvelope;
import org.deegree.filter.expression.ValueReference;
import org.geomajas.layer.LayerException;
import org.hamcrest.core.IsInstanceOf;
import org.opengis.filter.And;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.Filter;
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
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.helpers.NamespaceSupport;

import com.vividsolutions.jts.geom.Geometry;


public class DeegreeVisitor implements FilterVisitor {

	private final Logger log = LoggerFactory.getLogger(DeegreeVisitor.class);
	
	private DeegreeFeatureModel deegreeModel;
	private int srid;
	private String geomName;

	private CRSRef deegreeCRS;

	public DeegreeVisitor(DeegreeFeatureModel deegreeModel) throws LayerException {
		this.deegreeModel = deegreeModel;
		srid = deegreeModel.getSrid();

		new org.deegree.cs.persistence.CRSManager();
		//TODO hard coded geomajas viewport crs
        this.deegreeCRS = CRSManager.getCRSRef("EPSG:" + this.srid);
		
		geomName = deegreeModel.getGeometryAttributeName();
		if ( geomName == null) {
			//TODO default fallback
			geomName = "geometry";
		}
		
	}
	
	/* logic operators */ 
	@Override
	public Object visit(And filter, Object extraData) {
		org.deegree.filter.Operator dfilter = null;
		
		for (Filter element : filter.getChildren()) {
			if (dfilter == null) {
				dfilter = (org.deegree.filter.Operator) element.accept(this, extraData);
				//c = (Criterion) element.accept(this, userData)
				
			} else {
				//c = Restrictions.and(c, (Criterion) element.accept(this, userData));
				dfilter =  new org.deegree.filter.logical.And(
							dfilter, (org.deegree.filter.Operator) element.accept(this, extraData)
						);
			}	
		}
		return dfilter;
	}

	@Override
	public Object visit(Not filter, Object extraData) {
		org.deegree.filter.Operator dfilter = (org.deegree.filter.Operator) filter.getFilter().accept(this, extraData);
		return new org.deegree.filter.logical.Not(dfilter); // TODO TEST!!!
	}

	@Override
	public Object visit(Or filter, Object extraData) {
		org.deegree.filter.Operator dfilter = null;
		for (Filter element : filter.getChildren()) {
			if (dfilter == null) {
				dfilter = (org.deegree.filter.Operator) element.accept(this, extraData);
			} else {
				dfilter = new org.deegree.filter.logical.Or( dfilter, (org.deegree.filter.Operator) element.accept(this, extraData) );
			}
		}
		return dfilter; // TODO TEST!!!
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

	    // attr name
	    PropertyName expr = (PropertyName) filter.getExpression();
	    org.deegree.filter.Expression propName =  new ValueReference( 
	    			this.deegreeModel.getNSQName( expr.getPropertyName() )
	    		);
	    
	    org.deegree.filter.expression.Literal lit = new org.deegree.filter.expression.Literal( filter.getLiteral() );
		
	    // Expression testValue, Expression pattern, String wildCard, String singleChar,String escapeChar, Boolean matchCase, MatchAction matchAction )
		return new org.deegree.filter.comparison.PropertyIsLike(
				propName, lit, filter.getWildCard(), 
				filter.getSingleChar(),filter.getEscape(),
				true, org.deegree.filter.MatchAction.ALL);
	}

	@Override
	public Object visit(PropertyIsNull filter, Object extraData) {
		return new org.deegree.filter.comparison.PropertyIsNull(null, null);
	}

	@SuppressWarnings("deprecation")
	@Override
	public Object visit(BBOX filter, Object extraData) {
		Envelope envelope = new GeometryFactory().createEnvelope(
							filter.getMinX(),filter.getMinY(),  
							filter.getMaxX(),filter.getMaxY(),
							this.deegreeCRS );
		
        /*
        String NS = "http://www.polymap.org/";
        QName qnameProp = new QName(NS, "the_geom", "polymap" );
        ValueReference prop = new ValueReference(qnameProp);
        */
		
		
		QName qnameProp = this.deegreeModel.getNSQName( this.deegreeModel.getGeometryAttributeName() );
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
		/*
		String idName;
		try {
			idName = featureModel.getEntityMetadata().getIdentifierPropertyName();
		} catch (LayerException e) {
			log.warn("Cannot read idName, defaulting to 'id'", e);
			idName = HIBERNATE_ID;
		}
		Collection<?> c = (Collection<?>) castLiteral(filter.getIdentifiers(), idName);
		return Restrictions.in(idName, c);
		*/
		
		String idName;		
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
		QName qnameProp = this.deegreeModel.getNSQName( this.deegreeModel.getGeometryAttributeName() ); 
        ValueReference prop = new ValueReference(qnameProp);
        
		return new org.deegree.filter.spatial.Intersects(prop, asGeometry( getLiteralValue( filter.getExpression2() ) ) );
	}

	/**
	 * Get the literal value for an expression.
	 * 
	 * @param expression expression
	 * @return literal value
	 */
	private Object getLiteralValue(Expression expression) {
		if (!(expression instanceof Literal)) {
			throw new IllegalArgumentException("Expression " + expression + " is not a Literal.");
		}
		return ((Literal) expression).getValue();
	}
	/**
	 * Mapping of the JTS geometry object to the deegree AbstractDefaultGeometry type
	 * @param com.vividsolutions.jts.geom.Geometry
	 * @return org.deegree.geometry.standard.AbstractDefaultGeometry
	 */
	private org.deegree.geometry.standard.AbstractDefaultGeometry asGeometry(Object geometry) {
		
		Geometry geom = (Geometry) geometry;
		
		if (geom instanceof Geometry) {

			AbstractDefaultGeometry deegreeGeom = new DefaultEnvelope(null, null);
			deegreeGeom = deegreeGeom.createFromJTS(geom, this.deegreeCRS); // geom, org.deegree.cs.coordinatesystems.ICRS crs			
			
			return deegreeGeom;
		} else {
			throw new IllegalStateException("Cannot handle " + geometry + " as geometry.");
		}
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
