package hu.bme.mit.inf.testing.description.simulator.environment.generator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import hu.bme.mit.inf.testing.description.simulator.environment.generator.handlers.InvalidJointModel;
import hu.bme.mit.inf.testing.description.simulator.environment.generator.handlers.InvalidLinkModel;
import hu.bme.mit.inf.testing.description.simulator.environment.generator.handlers.InvalidRoadModel;
import hu.bme.mit.inf.testing.description.simulator.environment.generator.handlers.InvalidWorldModel;

class TestExceptions {

	/**
	 * TEST description: generate a robotic arm description with self looping joint
	 */
	@Test
	void testSelfLoop() {
		assertThrows(InvalidJointModel.class, ()->{
			CommonFunctions.testWithFile("./model/error/RobotArmSelfLoop.xmi");
		});
	}
	
	/**
	 * TEST description: road has no width throw error
	 */
	@Test
	void testRoadNoWidth() {
		assertThrows(InvalidRoadModel.class, ()->{
			CommonFunctions.testWithFile("./model/error/TestWorldRoadNoWidth.xmi");
		});
	}
	
	/**
	 * TEST description: generate a robotic arm description with a single joint link
	 */
	@Test
	void testRoadNoControlVertices() {
		assertThrows(InvalidRoadModel.class, ()->{
			CommonFunctions.testWithFile("./model/error/TestWorldRoadNoControlVertices.xmi");
		});
	}
	
	/**
	 * TEST description: sphere has zero radius
	 */
	@Test
	void testSphereZeroRadius() {
		assertThrows(InvalidLinkModel.class, ()->{
			CommonFunctions.testWithFile("./model/error/TestWorldRadiusZero.xmi");
		});
	}
	
	/**
	 * TEST description: cuba has one dimension zero
	 */
	@Test
	void testCubeZeroDimension() {
		assertThrows(InvalidLinkModel.class, ()->{
			CommonFunctions.testWithFile("./model/error/TestWorldCubeZeroDimension.xmi");
		});
	}
	
	/**
	 * TEST description: model has no name
	 */
	@Test
	void testModelNoName() {
		assertThrows(InvalidWorldModel.class, ()->{
			CommonFunctions.testWithFile("./model/error/TestWorldNoModelName.xmi");
		});
	}
	
	/**
	 * TEST description: null child and parent
	 */
	@Test
	void testRoadNullChildParent() {
		assertThrows(InvalidJointModel.class, ()->{
			CommonFunctions.testWithFile("./model/error/RobotArmNullJointChildParent.xmi");
		});
	}
	
	
}
