package hu.bme.mit.inf.testing.description.simulator.environment.generator.handlers;

import java.io.File;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.EnvironmentmodelPackage;
import hu.bme.mit.inf.testing.description.simulator.environment.model.environmentmodel.World;

public class GenerateProgram {
	
	public static void main(String[] args) {
		GazeboSDFGeneration sdf_gen = new GazeboSDFGeneration();
		try {
			sdf_gen.initialize();
			ResourceSet resourceSet = new ResourceSetImpl();
			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
			resourceSet.getPackageRegistry().put(EnvironmentmodelPackage.eNS_URI, EnvironmentmodelPackage.eINSTANCE);
			File modelfolder = new File("./model/simple");
			for (File f: modelfolder.listFiles()) {
				URI uri = URI.createFileURI(f.getAbsolutePath());
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
				File outputworld = new File("./testworlds/"+root.getName()+".sdf"); 
			    Result result = new StreamResult(outputworld);
			    t.transform(source, result);
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidWorldModel e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidLinkModel e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidRoadModel e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidJointModel e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
