package hu.bme.mit.inf.testing.description.simulator.environment.generator;

import static org.junit.jupiter.api.Assertions.fail;

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
import org.xml.sax.SAXException;

import hu.bme.mit.inf.testing.description.simulator.environment.generator.handlers.GazeboSDFGeneration;
import hu.bme.mit.inf.testing.description.simulator.environment.generator.handlers.InvalidJointModel;
import hu.bme.mit.inf.testing.description.simulator.environment.generator.handlers.InvalidLinkModel;
import hu.bme.mit.inf.testing.description.simulator.environment.generator.handlers.InvalidRoadModel;
import hu.bme.mit.inf.testing.description.simulator.environment.generator.handlers.InvalidWorldModel;
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.EnvironmentmodelPackage;
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.World;

public class CommonFunctions {
	public static void testWithFile(String path) throws 
			InvalidRoadModel, InvalidJointModel, InvalidWorldModel, InvalidLinkModel {
		GazeboSDFGeneration sdf_gen = new GazeboSDFGeneration();
		try {
			sdf_gen.initialize();
			if (sdf_gen.getDocumentRoot()!=null) {
				ResourceSet resourceSet = new ResourceSetImpl();

				// Register the appropriate resource factory to handle all file extensions.
				//
				resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
						.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());

				// Register the package to ensure it is available during loading.
				//
				resourceSet.getPackageRegistry().put(EnvironmentmodelPackage.eNS_URI, EnvironmentmodelPackage.eINSTANCE);
				File file = new File(path);
				if (file.isFile()) {
					URI uri = URI.createFileURI(file.getAbsolutePath());
					Resource resource = resourceSet.getResource(uri, true);
					World root = (World)resource.getContents().get(0);
					System.out.println("World loaded: "+root.getName());
					// Construct world
					sdf_gen.initialize();
					sdf_gen.serializeDefaultWorld(root);
					// Write XML out
					Transformer t = TransformerFactory.newInstance().newTransformer();
					t.setOutputProperty(OutputKeys.INDENT, "yes");
					
					t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
					Source source = new DOMSource(sdf_gen.getDocumentRoot());
				    Result result = new StreamResult(System.out);
				    t.transform(source, result);
				    // Validate XML
					sdf_gen.validateCurrentDocumentXSD();
				}else {
					fail("Abstract model is incorrectly placed");
				}

			}
			else {
				fail("Null document root");
			}
		} catch (ParserConfigurationException e) {
			fail("Parser configuration error: "+e);
		} catch (TransformerConfigurationException e) {
			fail("Transformer configuration error: "+e);
		} catch (TransformerFactoryConfigurationError e) {
			fail("Transformer configuration error: "+e);
		} catch (TransformerException e) {
			e.printStackTrace();
			fail("Transformer error: "+e);
		} catch (SAXException e) {
			e.printStackTrace();
			fail("XSD Validation error: "+e);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO error: "+e);
		} 
		System.out.println("-------------------------------");
	}
}
