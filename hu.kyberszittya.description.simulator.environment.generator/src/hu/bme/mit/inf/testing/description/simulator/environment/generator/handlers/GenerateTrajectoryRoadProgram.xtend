package hu.bme.mit.inf.testing.description.simulator.environment.generator.handlers

import hu.kyberszittya.auto.track.model.track.TrackPackage
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.resource.ResourceSet
import hu.kyberszittya.auto.track.model.track.Track
import hu.kyberszittya.spline.generation.transformation.math.CatmullRomSpline
import java.util.Map
import java.util.HashMap
import java.util.List
import java.util.ArrayList
import hu.kyberszittya.spline.generation.transformation.math.Vec3
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.World
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.EnvironmentmodelFactory
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.PolyLine
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.Model
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.ComplexModel
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.Link
import org.eclipse.emf.ecore.util.EcoreUtil
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.VisualMesh
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.CollisionMesh
import javax.xml.transform.Transformer
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.Result
import javax.xml.transform.TransformerFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Source

class GenerateTrajectoryRoadProgram {
	
	public def static void main(String[] args){
		val String path = 
				"C:\\Users\\kyberszittya\\ros-eclipse-ws\\eclipse_robot_description\\hu.kyberszittya.description.simulator.environment.generator\\model\\roadenv\\TestCircularTrack.xmi";
		val ResourceSet resset = new ResourceSetImpl();
		resset.getResourceFactoryRegistry().getExtensionToFactoryMap()
			.put(Resource.Factory.Registry.DEFAULT_EXTENSION,
					new XMIResourceFactoryImpl());
		resset.getPackageRegistry().put(TrackPackage.eNS_URI,
				TrackPackage.eINSTANCE);
		val URI uri = URI.createFileURI(path); 
		val Resource resource = resset.getResource(uri, true);
		// Initialize spline
		val Map<Track, CatmullRomSpline> csplines = new HashMap
		// Iterate on track
		val GazeboSDFGeneration sdf_gen = new GazeboSDFGeneration()
		
		val World root = EnvironmentmodelFactory.eINSTANCE.createWorld
		root.name = "world_1"
		 
		resource.allContents.filter[it instanceof Track].forEach[ track, n_track |
			val List<Vec3> points = new ArrayList()
			(track as Track).segment.forEach[ segment |
				val p = segment.pose.position
				points.add(new Vec3(p.x, p.y, p.z))
			]
			val spline = new CatmullRomSpline(points, true, 0.5, 0.5)
			spline.initialize
			csplines.put(track as Track, spline)
			val PolyLine road_mesh = EnvironmentmodelFactory.eINSTANCE.createPolyLine
			val PolyLine interior_wall = EnvironmentmodelFactory.eINSTANCE.createPolyLine
			val PolyLine exterior_wall = EnvironmentmodelFactory.eINSTANCE.createPolyLine
			for (var double t = spline.minT; t < spline.maxT; t+=0.3){
				// TODO: ordering messed up, correct it
				val v = spline.rt(t)
				val p0 = EnvironmentmodelFactory.eINSTANCE.createPoint2D
				val ndr = spline.drt_normalized(t)
				p0.x = v.x
				p0.y = v.y
				val p1 = EnvironmentmodelFactory.eINSTANCE.createPoint2D
				p1.x = v.x + ndr.y * 6.5
				p1.y = v.y - ndr.x * 6.5
				road_mesh.points.add(p0)
				road_mesh.points.add(p1)
				// Exterior wall setup
				val p0_ew = EnvironmentmodelFactory.eINSTANCE.createPoint2D
				p0_ew.x = v.x
				p0_ew.y = v.y
				val p1_ew = EnvironmentmodelFactory.eINSTANCE.createPoint2D
				p1_ew.x = v.x + ndr.y * 0.2
				p1_ew.y = v.y - ndr.x * 0.2
				exterior_wall.points.add(p0_ew)
				exterior_wall.points.add(p1_ew)
				// Interior wall setup
				val p0_iw = EnvironmentmodelFactory.eINSTANCE.createPoint2D
				p0_iw.x = v.x + ndr.y * 6.3
				p0_iw.y = v.y - ndr.x * 6.3
				val p1_iw = EnvironmentmodelFactory.eINSTANCE.createPoint2D
				p1_iw.x = v.x + ndr.y * 6.5
				p1_iw.y = v.y - ndr.x * 6.5
				interior_wall.points.add(p0_iw)
				interior_wall.points.add(p1_iw)
			}
			road_mesh.height = 0.1
			interior_wall.height = 2.0
			exterior_wall.height = 2.0
			val ComplexModel m = EnvironmentmodelFactory.eINSTANCE.createComplexModel
			m.name = '''南_track蓓road_model'''
			val Link l = EnvironmentmodelFactory.eINSTANCE.createLink
			l.name = '''南_track蓓road_link'''
			// Setup phys
			val phys = EnvironmentmodelFactory.eINSTANCE.createPhysicalSettings
			phys.static = true
			m.physicalsettings = phys
			l.linkphysicalsettings = EnvironmentmodelFactory.eINSTANCE.createLinkPhysicalSettings
			l.linkphysicalsettings.mass = 1.0
			// Add road mesh
			val VisualMesh v_road_mesh = EnvironmentmodelFactory.eINSTANCE.createVisualMesh
			v_road_mesh.name = '''viz_南_track蓓road'''
			v_road_mesh.mesh = EcoreUtil.copy(road_mesh)
			val CollisionMesh c_road_mesh = EnvironmentmodelFactory.eINSTANCE.createCollisionMesh
			c_road_mesh.name = '''coll_南_track蓓road'''
			c_road_mesh.mesh = EcoreUtil.copy(road_mesh)
			l.visualmesh.add(v_road_mesh)
			l.collisionmesh.add(c_road_mesh)
			// Add exterior wall to link
			val VisualMesh v_exterior_mesh = EnvironmentmodelFactory.eINSTANCE.createVisualMesh
			v_exterior_mesh.name = '''viz_南_track蓓exterior_road'''
			v_exterior_mesh.mesh = EcoreUtil.copy(exterior_wall)
			val CollisionMesh c_exterior_mesh = EnvironmentmodelFactory.eINSTANCE.createCollisionMesh
			c_exterior_mesh.name = '''coll_南_track蓓exteriror_road'''
			c_exterior_mesh.mesh = EcoreUtil.copy(exterior_wall)
			l.visualmesh.add(v_exterior_mesh)
			l.collisionmesh.add(c_exterior_mesh)
			// Add interior wall to link
			val VisualMesh v_interior_mesh = EnvironmentmodelFactory.eINSTANCE.createVisualMesh
			v_interior_mesh.name = '''viz_南_track蓓interior_road'''
			v_interior_mesh.mesh = EcoreUtil.copy(interior_wall)
			val CollisionMesh c_interior_mesh = EnvironmentmodelFactory.eINSTANCE.createCollisionMesh
			c_interior_mesh.name = '''coll_南_track蓓interor_road'''
			c_interior_mesh.mesh = EcoreUtil.copy(interior_wall)
			l.visualmesh.add(v_interior_mesh)
			l.collisionmesh.add(c_interior_mesh)
			// Wrapping up
			root.model.add(m)
			m.link.add(l)
			
		]
		// Generate world
		sdf_gen.initialize()
		sdf_gen.serializeDefaultWorld(root)
		val Transformer t = TransformerFactory.newInstance().newTransformer();
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		
		t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		val Source source = new DOMSource(sdf_gen.getDocumentRoot());
	    val Result result = new StreamResult(System.out);
	    t.transform(source, result);
	}
	
}