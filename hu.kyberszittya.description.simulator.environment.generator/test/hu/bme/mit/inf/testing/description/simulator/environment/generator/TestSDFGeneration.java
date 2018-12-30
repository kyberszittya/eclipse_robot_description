package hu.bme.mit.inf.testing.description.simulator.environment.generator;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.Test;
import org.omg.PortableInterceptor.SUCCESSFUL;
import org.xml.sax.SAXException;

import hu.bme.mit.inf.testing.description.simulator.environment.generator.handlers.GazeboSDFGeneration;
import hu.bme.mit.inf.testing.description.simulator.environment.generator.handlers.InvalidJointModel;
import hu.bme.mit.inf.testing.description.simulator.environment.generator.handlers.InvalidLinkModel;
import hu.bme.mit.inf.testing.description.simulator.environment.generator.handlers.InvalidRoadModel;
import hu.bme.mit.inf.testing.description.simulator.environment.generator.handlers.InvalidWorldModel;
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.EnvironmentmodelPackage;
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.World;

class TestSDFGeneration {
	
	
	
	/**
	 * TEST description: generate an SDF world of a sphere and cube
	 */
	@Test
	void testBasicWorld() {
		try {
			CommonFunctions.testWithFile("./model/simple/TestWorld.xmi");
		} catch (InvalidRoadModel | InvalidJointModel | InvalidWorldModel | InvalidLinkModel e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Error during execution: "+e);
		}
	}
	
	/**
	 * TEST description: generate an SDF world with a road and two spheres
	 */
	@Test
	void testBasicWorldRoad() {
		try {
			CommonFunctions.testWithFile("./model/simple/TestWorldRoad.xmi");
		}catch (InvalidRoadModel | InvalidJointModel | InvalidWorldModel | InvalidLinkModel e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Error during execution: "+e);
		}
	}
	
	
	/**
	 * TEST description: generate a world with a Toyota Prius external model and a sphere
	 */
	@Test
	void testBasicWorldRoadVehicle() {
		try {
			CommonFunctions.testWithFile("./model/simple/TestWorldExterior.xmi");
		}catch (InvalidRoadModel | InvalidJointModel | InvalidWorldModel | InvalidLinkModel e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Error during execution: "+e);
		}
		
	}
	
	/**
	 * TEST description: generate a world with explicitly set collision mesh and visual mesh 
	 */
	@Test
	void testExplicitCollisionMesh() {
		try {
			CommonFunctions.testWithFile("./model/simple/ExplicitCollisionModel.xmi");
		}catch (InvalidRoadModel | InvalidJointModel | InvalidWorldModel | InvalidLinkModel e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Error during execution: "+e);
		}
	}
	
	/**
	 * TEST description: generate a world with a complex mesh geometry loaded externally
	 */
	@Test
	void testExternalMesh() {
		try {
			CommonFunctions.testWithFile("./model/simple/ComplexModelMesh.xmi");
		} catch (InvalidRoadModel | InvalidJointModel | InvalidWorldModel | InvalidLinkModel e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Error during execution: "+e);
		}
	}
	
	/**
	 * TEST description: generate a world with a complex mesh geometry loaded externally with scale
	 */
	@Test
	void testExternalMeshScale() {
		try {
			CommonFunctions.testWithFile("./model/simple/ComplexModelMeshScaled.xmi");
		} catch (InvalidRoadModel | InvalidJointModel | InvalidWorldModel | InvalidLinkModel e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Error during execution: "+e);
		}
	}
	
	/**
	 * TEST description: generate a robotic arm description with a single joint link
	 */
	@Test
	void testRobotArm() {
		try {
			CommonFunctions.testWithFile("./model/simple/RobotArm.xmi");
		} catch (InvalidRoadModel | InvalidJointModel | InvalidWorldModel | InvalidLinkModel e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Error during execution: "+e);
		}
	}
	
	/**
	 * TEST description: generate a robotic arm description with a single joint link
	 */
	@Test
	void testRobotArmLinkTranslated() {
		try {
			CommonFunctions.testWithFile("./model/simple/RobotArmLinkTranslated.xmi");
		} catch(InvalidRoadModel | InvalidJointModel | InvalidWorldModel | InvalidLinkModel e) {
			e.printStackTrace();
			fail("Error during execution: "+e);
		}
	}
	
	/**
	 * TEST description: generate a robotic arm description with undefined axis
	 */
	@Test
	void testRobotArmUndefinedAxis() {
		try {
			CommonFunctions.testWithFile("./model/simple/RobotArmAxisUndefined.xmi");
		} catch(InvalidRoadModel | InvalidJointModel | InvalidWorldModel | InvalidLinkModel e) {
			e.printStackTrace();
			fail("Error during execution: "+e);
		}
	}
	
	/**
	 * TEST description: generate robot arm with undefined links and joints
	 */
	@Test
	void testRobotArmNamesUndefined() {
		try {
			CommonFunctions.testWithFile("./model/simple/RobotArmNamesUndefined.xmi");
		} catch(InvalidRoadModel | InvalidJointModel | InvalidWorldModel | InvalidLinkModel e) {
			e.printStackTrace();
			fail("Error during execution: "+e);
		}
	}
	
	/**
	 * TEST description: generate a polyline
	 */
	@Test
	void testPolyLine() {
		try {
			CommonFunctions.testWithFile("./model/simple/TestWorldPolyLine.xmi");	
		}catch(InvalidRoadModel | InvalidJointModel | InvalidWorldModel | InvalidLinkModel e) {
			e.printStackTrace();
			fail("Error during execution: "+e);
		}
	}
}
