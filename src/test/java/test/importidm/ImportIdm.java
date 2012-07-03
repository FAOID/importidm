package test.importidm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.collect.manager.LogoManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.Logo;
import org.openforis.collect.persistence.LogoDao;
import org.openforis.collect.persistence.SurveyDao;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.persistence.jooq.DialectAwareJooqFactory;
import org.openforis.collect.persistence.xml.CollectIdmlBindingContext;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.xml.InvalidIdmlException;
import org.openforis.idm.metamodel.xml.SurveyUnmarshaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import static org.openforis.collect.persistence.jooq.tables.OfcLogo.OFC_LOGO;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/ImportIdm-context.xml" })
@TransactionConfiguration(defaultRollback = false)
/**
 * @author Wibowo, Eko
 */
@Transactional
public class ImportIdm {

	@Autowired
	protected SurveyDao surveyDao;

	@Autowired
	protected LogoManager logoManager;
	
	protected String idmFileName;
	
	@Autowired
	protected FactoryDao factoryDao;
	
	@Autowired
	protected RecordManager recordManager;
	
	/*
	//@Test
	public void testAddIds() throws TransformerException, ParserConfigurationException, IOException, SAXException {
		idmFileName = "MOFOR_WORKING.idnfi.idm.xml";
		
		Document doc = parseXmlFile();
		Assert.assertNotNull(doc);
		Element documentElement = doc.getDocumentElement();
		addIdsToLists(documentElement);
		addIdsToListOfElements(documentElement, "versioning", "version");
		//addIdsToListOfElements(documentElement, "spatialReferenceSystems", "spatialReferenceSystem");
		addIdsToListOfElements(documentElement, "units", "unit");
		addIdsToSchema(documentElement);
		String docToString = docToString(doc);
		System.out.println(docToString);
	}*/

	protected void addIdsToLists(Element documentElement) {
		Element codeListsEl = getChildNode(documentElement, "codeLists");
		Assert.assertNotNull(codeListsEl);
		List<Node> lists = getChildNodes(codeListsEl, "list");
		int listId = 1;
		for (Node list : lists) {
			addIdsToList((Element) list, listId++);
		}
	}
	
	protected void addIdsToListOfElements(Element documentEl, String rootNodeName, String childName) {
		Element codeListsEl = getChildNode(documentEl, rootNodeName);
		List<Node> items = getChildNodes(codeListsEl, childName);
		int currentId = 1;
		for (Node item : items) {
			addIdsToList((Element) item, currentId++);
		}
	}
	
	protected void addIdsToList(Element listEl, int listId) {
		listEl.setAttribute("id", Integer.toString(listId));
		addIdsToListHierarchy(listEl);
		Element itemsEl = getChildNode(listEl, "items");
		if (itemsEl != null ) {
			addIdsToChildrenListItems(itemsEl);
		}
	}
	
	protected void addIdsToListHierarchy(Element listEl) {
		Element hierarchyEl = getChildNode(listEl, "hierarchy");
		if ( hierarchyEl != null ) {
			List<Node> levels = getChildNodes(hierarchyEl, "level");
			int childId = 1;
			for (Node node : levels) {
				((Element) node).setAttribute("id", Integer.toString(childId++));
			}
		}
	}

	protected void addIdsToListItem(Element listEl, int id) {
		listEl.setAttribute("id", Integer.toString(id));
		addIdsToChildrenListItems(listEl);
	}

	protected void addIdsToChildrenListItems(Element itemsRootElEl) {
		List<Node> items = getChildNodes(itemsRootElEl, "item");
		if ( items != null ) {
			int childId = 1;
			for (Node node : items) {
				addIdsToListItem((Element) node, childId ++);
			}
		}
	}
	
	/*HashMap<String, Integer> hashPath = new HashMap<String, Integer>();
	protected void addIdsToSchema(Element docEl) {
		
		InputStream is;
		
		
		DialectAwareJooqFactory jf = factoryDao.getJooqFactory();
		try {
			is = ClassLoader.getSystemResource("MOFOR_WORKING.idnfi.idm.xml").openStream();
			CollectIdmlBindingContext idmlBindingContext = surveyDao.getBindingContext();
			SurveyUnmarshaller surveyUnmarshaller = idmlBindingContext.createSurveyUnmarshaller();
			CollectSurvey survey = (CollectSurvey) surveyUnmarshaller.unmarshal(is);
			Schema schema = survey.getSchema();
			Collection<NodeDefinition> definitions = schema.getAllDefinitions();
			for (NodeDefinition definition : definitions) {
				Record q = jf.select(OFC_SCHEMA_DEFINITION.ID).from(OFC_SCHEMA_DEFINITION).where(OFC_SCHEMA_DEFINITION.PATH.equal(definition.getPath())).fetchOne();
				hashPath.put(definition.getPath(), q.getValueAsInteger(OFC_SCHEMA_DEFINITION.ID));
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidIdmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Element schemaEl = getChildNode(docEl, "schema");
		List<Node> rootEntities = getChildNodes(schemaEl, "entity");
		for (Node rootEntityEl : rootEntities) {
			String rootPath = "/" + ((Element) rootEntityEl).getAttribute("name");
			addIdsToEntity((Element) rootEntityEl, hashPath.get(rootPath) , rootPath);
						
		}
		
		

	}
	
	protected void addIdsToEntity(Element entityEl, int currentId, String rootPath) {
		entityEl.setAttribute("id", ""+ currentId);
		NodeList childNodes = entityEl.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node tmp = childNodes.item(i);
			if (tmp.getNodeType() == Node.ELEMENT_NODE) {
				String nodeName = tmp.getNodeName();				
				if ( nodeName != null ) {
					if ( nodeName.equals("entity") ) {
						currentId = hashPath.get(rootPath + "/" + ((Element) tmp).getAttribute("name"));
						addIdsToEntity((Element) tmp,  currentId, rootPath + "/" + ((Element) tmp).getAttribute("name"));						
					} else if ( 
							nodeName.equals("boolean") ||
							nodeName.equals("code") || 
							nodeName.equals("coordinate") ||
							nodeName.equals("date") ||
							nodeName.equals("file") ||
							nodeName.equals("number") ||
							nodeName.equals("range") ||
							nodeName.equals("taxon") ||
							nodeName.equals("text") ||
							nodeName.equals("time") 
							) {
							
						addIdToAttribue((Element) tmp,  hashPath.get(rootPath + "/" + ((Element) tmp).getAttribute("name")));
					}
				}
			}
		}
	}
	
	protected void addIdToAttribue(Element attributeEl, int currentId) {
		
		attributeEl.setAttribute("id", "" + currentId);
	}*/

	protected Document parseXmlFile() throws ParserConfigurationException, IOException, SAXException{
		//get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		//Using factory get an instance of document builder
		DocumentBuilder db = dbf.newDocumentBuilder();
			//parse using builder to get DOM representation of the XML file
		URL idm = ClassLoader.getSystemResource(idmFileName);
		Assert.assertNotNull(idm);
		InputStream is = idm.openStream();
		Document dom = db.parse(is);
		return dom;
		
	}
	
	protected String docToString(Document doc) throws TransformerException {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(doc), new StreamResult(writer));
		//String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
		return writer.getBuffer().toString();
	}
	
	public static Element getChildNode(Element node, String childName) {
		Element child = null;
		if (node.hasChildNodes()) {
			NodeList childNodes = node.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node tmp = childNodes.item(i);
				if (tmp.getNodeType() == Node.ELEMENT_NODE) {
					String nodeName = tmp.getNodeName();
					if ( nodeName != null && nodeName.equals(childName)) {
						child = (Element) tmp;
						break;
					}
				}
			}
		}
		return child;
	}
	
	public static List<Node> getChildNodes(Node node, String localName) {
		NodeList list = node.getChildNodes();
		List<Node> newList = new ArrayList<Node>();
		for (int i = 0; i < list.getLength(); i++) {
			Node n = list.item(i);
			String nodeName = n.getLocalName() != null ? n.getLocalName() : n.getNodeName();
			if (nodeName.equalsIgnoreCase(localName)) {
				newList.add(n);
			}
		}

		return newList;
	}
	
	
	/*
	 * disabled. UpdateModel has all the functionality of importModel, with
	 * added feature of updating database structure into the newest IDM I'll
	 * keep the method here for historical purpose only public void
	 * testImportIdnfi() throws Exception { idmUpdater.importIdnfi("idnfi",
	 * ClassLoader.getSystemResource("idnfi.idm.xml")); }
	 */

	
	
	@Test
	public void testUpdateIdnfiIdm() throws IOException, InvalidIdmlException, SurveyImportException {
	
		InputStream is = ClassLoader.getSystemResource("MOFOR_WORKING.idnfi.idm.xml").openStream();
		CollectIdmlBindingContext idmlBindingContext = surveyDao.getBindingContext();
		SurveyUnmarshaller surveyUnmarshaller = idmlBindingContext.createSurveyUnmarshaller();
		CollectSurvey survey = (CollectSurvey) surveyUnmarshaller.unmarshal(is);
		survey.setName("idnfi");
		survey.setUri("http://www.openforis.org/idm/idnfi");		
		surveyDao.updateModel(survey);
	}

	//@Test
	public void updateGreenbookIdm() throws IOException, InvalidIdmlException,
			SurveyImportException {
		InputStream is = ClassLoader.getSystemResource("MOFOR_WORKING.greenbook.idm.xml").openStream();
		CollectIdmlBindingContext idmlBindingContext = surveyDao.getBindingContext();
		SurveyUnmarshaller surveyUnmarshaller = idmlBindingContext.createSurveyUnmarshaller();
		CollectSurvey survey = (CollectSurvey) surveyUnmarshaller.unmarshal(is);
		survey.setName("greenbook");
		survey.setUri("http://www.openforis.org/idm/greenbook");
		surveyDao.updateModel(survey);
	}
	
	//@Test
	public void testWat() throws IOException, InvalidIdmlException
	{
		InputStream is = ClassLoader.getSystemResource("MOFOR_WORKING.idnfi.idm.xml").openStream();
		CollectIdmlBindingContext idmlBindingContext = surveyDao.getBindingContext();
		SurveyUnmarshaller surveyUnmarshaller = idmlBindingContext.createSurveyUnmarshaller();
		CollectSurvey survey = (CollectSurvey) surveyUnmarshaller.unmarshal(is);
		Schema schema = survey.getSchema();
		String rootEntityName="cluster";
		EntityDefinition rootEntityDefinition = schema.getRootEntityDefinition(rootEntityName);
		String rootEntityDefinitionName = rootEntityDefinition.getName();
		int count = recordManager.getRecordCount(survey, rootEntityDefinitionName);
		System.out.println(count);
	
	}
	
	//@Test
	public void storeLogo() throws IOException
	{
		byte[] image = null;
		Logo logo = new Logo();
		logo.setPosition(0);
		URL imgFile = ClassLoader.getSystemResource("logo/specific_logo.jpg");
		File file = new File(imgFile.getPath());		
		image = getBytesFromFile(file);
		logo.setImage(image);		
		logoManager.storeLogo(logo);
	}
	
	// borrowed from http://www.exampledepot.com/egs/java.io/file2bytearray.html
	public static byte[] getBytesFromFile(File file) throws IOException {
	    InputStream is = new FileInputStream(file);

	    // Get the size of the file
	    long length = file.length();

	    // You cannot create an array using a long type.
	    // It needs to be an int type.
	    // Before converting to an int type, check
	    // to ensure that file is not larger than Integer.MAX_VALUE.
	    if (length > Integer.MAX_VALUE) {
	        // File is too large
	    }

	    // Create the byte array to hold the data
	    byte[] bytes = new byte[(int)length];

	    // Read in the bytes
	    int offset = 0;
	    int numRead = 0;
	    while (offset < bytes.length
	           && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	        offset += numRead;
	    }

	    // Ensure all the bytes have been read in
	    if (offset < bytes.length) {
	        throw new IOException("Could not completely read file "+file.getName());
	    }

	    // Close the input stream and return bytes
	    is.close();
	    return bytes;
	}


}
