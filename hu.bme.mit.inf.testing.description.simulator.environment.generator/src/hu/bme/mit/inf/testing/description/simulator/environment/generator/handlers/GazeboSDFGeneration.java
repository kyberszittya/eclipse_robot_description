package hu.bme.mit.inf.testing.description.simulator.environment.generator.handlers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.Axis;
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.CollisionMesh;
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.ComplexModel;
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.Cube;
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.Cylinder;
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.ExteriorModel;
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.Inertial;
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.Joint;
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.JointLimit;
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.Link;
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.LinkPhysicalSettings;
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.Mesh;
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.Model;
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.ModelMesh;
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.Orientation;
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.PhysicalSettings;
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.Point2D;
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.PolyLine;
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.Pose;
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.Position;
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.Primitive;
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.Road;
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.Sphere;
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.VisualMesh;
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.World;

public class GazeboSDFGeneration {
	protected static DocumentBuilder doc_builder;
	protected Document document;
	protected World world;
	protected Element sdf_root;
	protected Element world_element;
	// Count of building blocks
	private static class BuildingBlockCount {
		protected int cnt_link;
		protected int cnt_models;
		protected int cnt_roads;
		protected int cnt_joints;
		
		public BuildingBlockCount() {
			resetCount();
		}
		
		public void resetCount() {
			cnt_link = 0;
			cnt_models = 0;
			cnt_roads = 0;
			cnt_joints = 0;
		}
	}
	BuildingBlockCount cnt_block;
	
	public GazeboSDFGeneration() {
		cnt_block = new BuildingBlockCount();		
	}
	
	public Document getDocumentRoot() {
		return document;
	}
	
	public void setWorldEcore(World w) {
		this.world = w;
	}
	
	public void initialize() throws ParserConfigurationException {
		if (doc_builder==null) {
			doc_builder = DocumentBuilderFactory.newInstance().newDocumentBuilder(); 
		}
		document = doc_builder.newDocument();
		sdf_root = document.createElement("sdf");
		sdf_root.setAttribute("version", "1.5");
		document.appendChild(sdf_root);
	}
	
	public void serializeExteriorModel(String path, Element worldElement, Element includeElement) {
		final Element uriElement = document.createElement("uri");
		uriElement.setTextContent(path);
		includeElement.appendChild(uriElement);
	}
	
	public void serializeExteriorModel(String path, Element worldElement) {
		final Element includeElement = document.createElement("include");
		serializeExteriorModel(path, worldElement, includeElement);
		worldElement.appendChild(includeElement);
	}
	
	public void serializeWorld(World w, boolean default_world) throws InvalidWorldModel, InvalidLinkModel, InvalidRoadModel, InvalidJointModel {
		setWorldEcore(w);
		world_element = document.createElement("world");
		world_element.setAttribute("name", w.getName());
		sdf_root.appendChild(world_element);
		if (default_world) {
			// Include sun and ground plane
			serializeExteriorModel("model://ground_plane",world_element);
			serializeExteriorModel("model://sun",world_element);
		}
		for (Model m: w.getModel()) {
			serializeModel(m);
			cnt_block.cnt_models++;
		}
		for (Road r: w.getRoad()) {
			serializeRoad(r);
			cnt_block.cnt_roads++;
		}
	}
	
	public void serializeDefaultWorld(World w) throws InvalidWorldModel, InvalidLinkModel, InvalidRoadModel, InvalidJointModel {
		serializeWorld(w, true);
	}
	
	public void serializeZeroPose(Element element) {
		final Element pose_element = document.createElement("pose");
		pose_element.setTextContent("0 0 0 0 0 0");
		element.appendChild(pose_element);
	}
	
	public void serializeRoad(Road r) throws InvalidRoadModel {
		final Element roadElement = document.createElement("road");
		final Element widthElement = document.createElement("width");
		if (r.getWidth()<=0.0) {
			throw new InvalidRoadModel();
		}
		if (r.getControlvertices().size()==0) {
			throw new InvalidRoadModel();
		}
		roadElement.setAttribute("name", r.getName());
		widthElement.setTextContent(Double.toString(r.getWidth()));
		roadElement.appendChild(widthElement);
		for (Position p: r.getControlvertices()) {
			Element pointElement = document.createElement("point");
			StringBuilder sb = new StringBuilder();
			sb.append(p.getX());
			sb.append(" ");
			sb.append(p.getY());
			sb.append(" ");
			sb.append(p.getZ());
			pointElement.setTextContent(sb.toString());
			roadElement.appendChild(pointElement);
		}
		
		world_element.appendChild(roadElement);
	}
	
	public void serializePose(Element element, Pose p) {
		final Element pose_element = document.createElement("pose");
		final StringBuilder sb = new StringBuilder();
		final Position pos = p.getPosition();
		sb.append(pos.getX());
		sb.append(" ");
		sb.append(pos.getY());
		sb.append(" ");
		sb.append(pos.getZ());
		final Orientation ori = p.getOrientation();
		sb.append(" ");
		sb.append(ori.getRoll());
		sb.append(" ");
		sb.append(ori.getPitch());
		sb.append(" ");
		sb.append(ori.getYaw());
		pose_element.setTextContent(sb.toString());
		element.appendChild(pose_element);
	}
	
	public void serializeExternalMesh(ModelMesh m, Element mesh_element) {
		Element geometry_element = document.createElement("geometry");
		Element meshElement = document.createElement("mesh");
		Element uriElement = document.createElement("uri");
		Element scaleElement = document.createElement("scale");
		if (m.getScale()!=null) {
			StringBuilder sb = new StringBuilder();
			sb.append(m.getScale().getSx());
			sb.append(" ");
			sb.append(m.getScale().getSy());
			sb.append(" ");
			sb.append(m.getScale().getSz());
			scaleElement.setTextContent(sb.toString());
		}else {
			scaleElement.setTextContent("1 1 1");
		}
		uriElement.setTextContent(m.getPath());
		meshElement.appendChild(uriElement);
		meshElement.appendChild(scaleElement);
		geometry_element.appendChild(meshElement);
		mesh_element.appendChild(geometry_element);
	}
	
	public void serializePolyLine(PolyLine polyline, Element geometryElement) {
		Element polylineElement = document.createElement("polyline");
		for (Point2D p: polyline.getPoints()) {
			Element pointElement = document.createElement("point");
			StringBuilder sb = new StringBuilder();
			sb.append(p.getX());
			sb.append(" ");
			sb.append(p.getY());
			pointElement.setTextContent(sb.toString());
			polylineElement.appendChild(pointElement);
		}
		Element heightElement = document.createElement("height");
		polylineElement.appendChild(heightElement);
		heightElement.setTextContent(Double.toString(polyline.getHeight()));
		geometryElement.appendChild(polylineElement);
	}
	
	public void serializeCylinder(Cylinder cylinder, Element geometryElement) {
		Element cylinder_element = document.createElement("cylinder");
		Element radius_element = document.createElement("radius");
		radius_element.setTextContent(Double.toString(cylinder.getRadius()));
		Element length_element = document.createElement("length");
		length_element.setTextContent(Double.toString(cylinder.getHeight()));
		cylinder_element.appendChild(length_element);
		cylinder_element.appendChild(radius_element);
		geometryElement.appendChild(cylinder_element);
	}
	
	public void serializePrimitiveMesh(Primitive p, Element mesh_element) throws InvalidLinkModel {
		Element geometry_element = document.createElement("geometry");
		if (p.getPose()!=null) {
			serializePose(mesh_element, p.getPose());
		}else {
			serializeZeroPose(mesh_element);
		}
		if (p instanceof Sphere) {
			Sphere s = (Sphere)p;
			Element sphere_element = document.createElement("sphere");
			Element radius_element = document.createElement("radius");
			radius_element.setTextContent(Double.toString(s.getRadius()));
			sphere_element.appendChild(radius_element);
			geometry_element.appendChild(sphere_element);
		}else if(p instanceof Cube) {
			Cube c = (Cube)p;
			Element cube_element = document.createElement("box");
			StringBuilder sb = new StringBuilder();
			sb.append(c.getWidth());
			sb.append(" ");
			sb.append(c.getDepth());
			sb.append(" ");
			sb.append(c.getHeight());
			Element size_element = document.createElement("size");
			size_element.setTextContent(sb.toString());
			cube_element.appendChild(size_element);
			geometry_element.appendChild(cube_element);
		} else if (p instanceof PolyLine) {
			serializePolyLine((PolyLine)p, geometry_element);
		} else if (p instanceof Cylinder){
			serializeCylinder((Cylinder)p, geometry_element);
		} else {
			throw new InvalidLinkModel();
		}
		mesh_element.appendChild(geometry_element);
	}
	
	public void serializeCollisionMesh(Mesh m, Element linkElement, String name) throws InvalidLinkModel {
		Element collision_element = document.createElement("collision");
		collision_element.setAttribute("name", name);
		linkElement.appendChild(collision_element);
		if (m instanceof Primitive) {
			serializePrimitiveMesh((Primitive)m, collision_element);
		}else if (m instanceof ModelMesh) {
			serializeExternalMesh((ModelMesh)m, collision_element);
		}else {
			throw new InvalidLinkModel();
		}
	}
	
	public void serializeCollisionMesh(CollisionMesh c, Element link_element) throws InvalidLinkModel {
		serializeCollisionMesh(c.getMesh(), link_element, c.getName());
	}
	
	public void serializeVisualMesh(VisualMesh v, Element link_element) throws InvalidLinkModel {
		Element visual_element = document.createElement("visual");
		if (v.getName().length()>0) {
			visual_element.setAttribute("name", v.getName());
		}else {
			throw new InvalidLinkModel();
		}
		link_element.appendChild(visual_element);
		if (v.getMesh() instanceof Primitive) {
			serializePrimitiveMesh((Primitive)v.getMesh(), visual_element);
		}else if (v.getMesh() instanceof ModelMesh) {
			serializeExternalMesh((ModelMesh)v.getMesh(), visual_element);
		}else {
			throw new InvalidLinkModel();
		}
	}
	
	public void serializeJointLimit(JointLimit jointlimit, Element axisElement) {
		Element limitElement = document.createElement("limit");
		Element upperLimitElement = document.createElement("upper");
		upperLimitElement.setTextContent(Double.toString(jointlimit.getUpper()));
		Element lowerLimitElement = document.createElement("lower");
		lowerLimitElement.setTextContent(Double.toString(jointlimit.getLower()));
		Element effortLimitElement = document.createElement("effort");
		effortLimitElement.setTextContent(Double.toString(jointlimit.getEffort()));
		Element velocityLimitElement = document.createElement("velocity");
		velocityLimitElement.setTextContent(Double.toString(jointlimit.getVelocity()));
		limitElement.appendChild(lowerLimitElement);
		limitElement.appendChild(upperLimitElement);
		limitElement.appendChild(velocityLimitElement);
		limitElement.appendChild(effortLimitElement);
		axisElement.appendChild(limitElement);
	}
	
	public void serializeAxis(Axis axis, Element jointElement) throws InvalidJointModel {
		Element axisElement = document.createElement("axis");
		Element xyzElement = document.createElement("xyz");
		if (axis.getAxis()!=null) {
			StringBuilder sb = new StringBuilder();
			sb.append(axis.getAxis().getX());
			sb.append(" ");
			sb.append(axis.getAxis().getY());
			sb.append(" ");
			sb.append(axis.getAxis().getZ());
			xyzElement.setTextContent(sb.toString());
		}else {
			xyzElement.setTextContent("0 0 1");
		}
		if (axis.getJointlimit()!=null) {
			serializeJointLimit(axis.getJointlimit(), axisElement);
		}else {
			throw new InvalidJointModel();
		}
		axisElement.appendChild(xyzElement);
		jointElement.appendChild(axisElement);
	}
	
	public void serializeJoint(Joint joint, Element modelElement) throws InvalidJointModel {
		Element jointElement = document.createElement("joint");
		jointElement.setAttribute("type", joint.getType().getLiteral());
		if (joint.getName()==null) {
			joint.setName("joint_"+cnt_block.cnt_joints);
		}
		else {
			if (joint.getName().length() == 0) {
				joint.setName("joint_"+cnt_block.cnt_joints);
			}
		}
		jointElement.setAttribute("name", joint.getName());
		if (joint.getParent()!=null && joint.getChild()!=null) {
			if (joint.getParent()==joint.getChild()) {
				throw new InvalidJointModel();
			}
			Element parentElement = document.createElement("parent");
			parentElement.setTextContent(joint.getParent().getName());
			Element childElement = document.createElement("child");
			childElement.setTextContent(joint.getChild().getName());
			jointElement.appendChild(parentElement);
			jointElement.appendChild(childElement);
		}else {
			throw new InvalidJointModel();
		}
		for (Axis a: joint.getAxis()) {
			serializeAxis(a, jointElement);
		}
		if (joint.getPose()!=null) {
			serializePose(jointElement, joint.getPose());
		} else {
			serializeZeroPose(jointElement);
		}
		modelElement.appendChild(jointElement);
	}
	
	public void serializeDefaultPhysicalSettings(Element modelElement) {
		Element staticElement = document.createElement("static");
		staticElement.setTextContent(Boolean.toString(false));
		
		modelElement.appendChild(staticElement);
	}
	
	public void serializePhysicalSettings(PhysicalSettings phys, Element modelElement) {
		Element staticElement = document.createElement("static");
		staticElement.setTextContent(Boolean.toString(phys.isStatic()));
		
		modelElement.appendChild(staticElement);
	}
	
	public void setupPrimitiveInertia(Primitive p, 
			LinkPhysicalSettings phys, Element inertia_element) throws InvalidLinkModel {
		// Set zero-values to all inertia values
		double ixx = 0.0;
		double ixy = 0.0;
		double ixz = 0.0;
		double iyy = 0.0;
		double iyz = 0.0;
		double izz = 0.0;
		final double m = phys.getMass();
		// Due to rescaling we shall need volume too
		double v = 0.0;
		final Element ixxElement = document.createElement("ixx");
		final Element ixyElement = document.createElement("ixy");
		final Element ixzElement = document.createElement("ixz");
		final Element iyyElement = document.createElement("iyy");
		final Element iyzElement = document.createElement("iyz");
		final Element izzElement = document.createElement("izz");
		// We are considering a solid primitive
		if (!phys.isHollow()) {
			if (p instanceof Sphere) {
				double radius = ((Sphere) p).getRadius();
				if (radius==0.0) {
					throw new InvalidLinkModel();
				}
				v = 4.0/3.0*Math.PI*radius*radius*radius;
				ixx = 0.4*m*radius/v;
				iyy = 0.4*m*radius/v;
				izz = 0.4*m*radius/v;
			}else if(p instanceof Cube) {
				final double a = ((Cube) p).getWidth();
				final double b = ((Cube) p).getDepth();
				final double c = ((Cube) p).getHeight();
				v = a*b*c;
				if (v==0.0) {
					throw new InvalidLinkModel();
				}
				ixx = 1.0/12.0*m*(b*b+c*c)/v;
				iyy = 1.0/12.0*m*(a*a+c*c)/v;
				izz = 1.0/12.0*m*(a*a+b*b)/v;
			}else if(p instanceof Cylinder) {
				double radius = ((Cylinder) p).getRadius();
				double height = ((Cylinder) p).getHeight();
				if (radius==0.0) {
					throw new InvalidLinkModel();
				}
				v = radius*radius*height*Math.PI;
				ixx = 1.0/12.0*m*(3.0*radius*radius+height*height)/v;
				iyy = 1.0/12.0*m*(3.0*radius*radius+height*height)/v;
				izz = 0.5*m*radius*radius/v;
			}
		}
		// We are considering a hollow primitive
		else {
			if (p instanceof Sphere) {
				double radius = ((Sphere) p).getRadius();
				if (radius==0.0) {
					throw new InvalidLinkModel();
				}
				v = 4.0/3.0*Math.PI*radius*radius*radius;
				ixx = 2.0/3.0*m*radius/v;
				iyy = 2.0/3.0*m*radius/v;
				izz = 2.0/3.0*m*radius/v;
			}
		}
		ixxElement.setTextContent(Double.toString(ixx));
		ixyElement.setTextContent(Double.toString(ixy));
		ixzElement.setTextContent(Double.toString(ixz));
		iyyElement.setTextContent(Double.toString(iyy));
		iyzElement.setTextContent(Double.toString(iyz));
		izzElement.setTextContent(Double.toString(izz));
		// Wrap up inertia
		inertia_element.appendChild(ixxElement);
		inertia_element.appendChild(ixyElement);
		inertia_element.appendChild(ixzElement);
		inertia_element.appendChild(iyyElement);
		inertia_element.appendChild(iyzElement);
		inertia_element.appendChild(izzElement);
	}
	
	public void setupExplicitInertia(LinkPhysicalSettings phys, Element linkElement) {
		final Element inertialElement = document.createElement("inertial");
		final Element inertiaElement = document.createElement("inertia");
		final Element ixxElement = document.createElement("ixx");
		final Element ixyElement = document.createElement("ixy");
		final Element ixzElement = document.createElement("ixz");
		final Element iyyElement = document.createElement("iyy");
		final Element iyzElement = document.createElement("iyz");
		final Element izzElement = document.createElement("izz");
		final Element massElement = document.createElement("mass");
		massElement.setTextContent(Double.toString(phys.getMass()));
		inertialElement.appendChild(massElement);
		Inertial inertial = phys.getInertial();
		ixxElement.setTextContent(Double.toString(inertial.getIxx()));
		ixyElement.setTextContent(Double.toString(inertial.getIxy()));
		ixzElement.setTextContent(Double.toString(inertial.getIxz()));
		iyyElement.setTextContent(Double.toString(inertial.getIyy()));
		iyzElement.setTextContent(Double.toString(inertial.getIyz()));
		izzElement.setTextContent(Double.toString(inertial.getIzz()));
		// Wrap up inertia
		inertiaElement.appendChild(ixxElement);
		inertiaElement.appendChild(ixyElement);
		inertiaElement.appendChild(ixzElement);
		inertiaElement.appendChild(iyyElement);
		inertiaElement.appendChild(iyzElement);
		inertiaElement.appendChild(izzElement);
		inertialElement.appendChild(inertiaElement);
		linkElement.appendChild(inertialElement);
	}
	
	public void setupInertia(Mesh m, LinkPhysicalSettings phys, Element linkElement) throws InvalidLinkModel {
		// https://en.wikipedia.org/wiki/List_of_moments_of_inertia
		final Element inertialElement = document.createElement("inertial");
		final Element inertiaElement = document.createElement("inertia");
		inertialElement.appendChild(inertiaElement);
		final Element massElement = document.createElement("mass");
		massElement.setTextContent(Double.toString(phys.getMass()));
		inertialElement.appendChild(massElement);
		if (m instanceof Primitive) {
			setupPrimitiveInertia((Primitive)m, phys, inertiaElement);
		}else {
			throw new InvalidLinkModel();
		}
		linkElement.appendChild(inertialElement);
	}
	
	public void serializeLink(Link l, Element model_element) throws InvalidWorldModel, InvalidLinkModel {
		final Element link_element = document.createElement("link");
		if (l.getName() == null) {
			l.setName("link_"+cnt_block.cnt_link);
		}else {
			if (l.getName().length()==0) {
				l.setName("link_"+cnt_block.cnt_link);
			}
		}
		link_element.setAttribute("name", l.getName());
		// #SECTION Pose setup
		if (l.getPose()!=null) {
			serializePose(link_element, l.getPose());
		}else {
			// Initialize pose to zero
			serializeZeroPose(link_element);
		}
		// #SECTION mesh setup
		final boolean is_visual = l.getVisualmesh().size() > 0;
		final boolean is_collision = l.getCollisionmesh().size() > 0;
		final boolean is_inertial = l.getLinkphysicalsettings().getInertial()!=null;
		if (!is_visual && !is_collision && is_inertial) {
			throw new InvalidWorldModel();
		}
		for (VisualMesh v: l.getVisualmesh()) {
			serializeVisualMesh(v, link_element);
		}
		for (CollisionMesh c: l.getCollisionmesh()) {
			serializeCollisionMesh(c, link_element);
		}
		// Collision geometry is not explicitly set we may use the visual representation 
		if (is_visual && !is_collision) {
			for (VisualMesh v: l.getVisualmesh()) {
				serializeCollisionMesh(v.getMesh(), link_element, "coll_"+v.getName());
			}
		}
		// At this point only the first item is considered
		// TODO: join inertia
		if (!is_inertial) {
			if (is_visual && !is_collision) {
				// No collision explicitly set, try to set it
				setupInertia(l.getVisualmesh().get(0).getMesh(),
						l.getLinkphysicalsettings(), link_element);
			}
			else if (!is_visual && is_collision) {
				// We might have a collision with no apparent visual
				setupInertia(l.getCollisionmesh().get(0).getMesh(), 
						l.getLinkphysicalsettings(), link_element);
			}
		}else {
			// 
			setupExplicitInertia(l.getLinkphysicalsettings(), link_element);
		}
		// Wrap-up
		model_element.appendChild(link_element);
		
	}
	
	public void serializeModel(Model m) throws InvalidWorldModel, InvalidLinkModel, InvalidJointModel {
		Element model_element;
		cnt_block.resetCount();
		if (m.getName().length()==0) {
			throw new InvalidWorldModel();
		}
		if (m instanceof ComplexModel) {
			model_element = document.createElement("model");
			model_element.setAttribute("name", m.getName());
			ComplexModel cm = (ComplexModel)m;
			
			for (Link l: cm.getLink()) {
				serializeLink(l, model_element);
				cnt_block.cnt_link++;
				// Construct tree
				
			}
			for (Joint j: cm.getJoint()) {
				serializeJoint(j, model_element);
				cnt_block.cnt_joints++;
			}
			world_element.appendChild(model_element);			
		}
		else {
			ExteriorModel em = (ExteriorModel)m;
			model_element = document.createElement("include");
			serializeExteriorModel(em.getFilename(), world_element, model_element);			
			world_element.appendChild(model_element);
		}
		if (m.getPose()!=null) {
			serializePose(model_element, m.getPose());
		}else {
			serializeZeroPose(model_element);
		}
		if (m.getPhysicalsettings()!=null) {
			serializePhysicalSettings(m.getPhysicalsettings(), model_element);
		}else {
			serializeDefaultPhysicalSettings(model_element);
		}
	}
	
	public boolean validateCurrentDocumentXSD() throws SAXException, IOException {
		// Validate against SDFormat XSD
		URL schemaFile = new URL("http://sdformat.org/schemas/root.xsd");
		SchemaFactory schemaFactory = SchemaFactory
			    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = schemaFactory.newSchema(schemaFile);
		Validator validator = schema.newValidator();
		validator.validate(new DOMSource(document));
		return true;
	}
}
