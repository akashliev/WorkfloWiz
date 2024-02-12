package utility;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.w3c.dom.Document;

import com.ibm.wsdl.PortTypeImpl;

/**
 * This class is a dynamic WS client that allows to invoke any web service, given its wsdl, operation name and 
 * argument list.
 * 
 * @author Andrey Kashlev
 *
 */
public class WSClient {

	public static Object callWebService(String wsdl, String serviceName, String opName, ArrayList<Object> wsArguments, ArrayList<Class> returnTypes)
			throws Exception {
		Object[] wsArgArray = wsArguments.toArray();
		Class[] returnTypesArr = returnTypes.toArray(new Class[returnTypes.size()]);

		Object result = null;

		try {
			WSDLFactory sf = WSDLFactory.newInstance();
			WSDLReader s = sf.newWSDLReader();
			Definition wsdlDefinition = s.readWSDL(null, wsdl);

			// To get the targetNamespace from WSDL
			Map obNAME = wsdlDefinition.getNamespaces();
			// iterate over the Map
			Iterator entries = obNAME.entrySet().iterator();
			String nsFromWsdl = null;
			while (entries.hasNext()) {
				Map.Entry entry = (Map.Entry) entries.next();
				if (entry.getKey().equals("ns"))
					nsFromWsdl = (String) entry.getValue();
			}

			// Then create a port type implementation instance which has the
			// information we need
			Object[] ob = wsdlDefinition.getPortTypes().values().toArray();
			PortTypeImpl pti = (PortTypeImpl) ob[0];

			// Now we can get the name of operations
			pti.getQName().getNamespaceURI();
			List list = pti.getOperations();
			Iterator operIter = list.iterator();

			RPCServiceClient serviceClient = null;
			// public class RPCServiceClient extends ServiceClient
			try {
				serviceClient = new RPCServiceClient();
			} catch (AxisFault e1) {
				e1.printStackTrace();
			}

			Options options = serviceClient.getOptions();
			// getOptions method of ServiceClient gets the basic client
			// configuration
			// this is empty, so we set it in the following
			EndpointReference targetEPR = new EndpointReference(wsdl);

			// Set WS-Addressing Action / SOAP Action string.
			options.setAction(opName);
			// Set WS-Addressing To endpoint.
			options.setTo(targetEPR);

			// Ready to create a call now
			// Need QName. Provide targetNamespace and operation name to be
			// invoked
			QName op1Name = new QName(nsFromWsdl, opName);

			Object[] response = serviceClient.invokeBlocking(op1Name, wsArgArray, returnTypesArr);
			result = response[0];
			
			//if output is a complex type, return its DOM representation:
			if(result instanceof OMElementImpl){
				String xmlString = ((OMElementImpl) response[0]).toString();
				return XMLParser.getDocument(xmlString);
//				Document xmlDom = XMLParser.getDocument(xmlString);
//				String prettyfiedXML = Utility.formatXML(xmlDom);
//				System.out.println("prettyfied:");
//				System.out.println(prettyfiedXML);
//				System.out.println("end of prettyfied");
			}
			//////////////////////////////////////////////////////////////

		} catch (Exception ex) {
			System.out.println(ex);
		}
		return result;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		//String wsdl = "http://172.30.12.125:8080/axis2/services/SquareOfSum2?wsdl";
		//String wsdl = "http://dmsg1.cs.wayne.edu:8080/axis2/services/SquareOfSum2?wsdl";
		String wsdl = "http://localhost:8080/axis2/services/SquareOfSum2?wsdl";
		String serviceName = "SquareOfSum2";
		String opName = "squareOfSum";

		ArrayList<Object> wsArguments = new ArrayList();
		boolean arg1 = true;
		boolean arg2 = true;
		wsArguments.add(arg1);
		wsArguments.add(arg2);

		ArrayList<Class> returnTypes = new ArrayList<Class>();
		returnTypes.add(Integer.class);

		Object result = callWebService(wsdl, serviceName, opName, wsArguments, returnTypes);
		if(result instanceof Integer)
			System.out.println("int result = " + result);

	}

}
