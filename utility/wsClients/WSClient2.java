package utility.wsClients;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;

import com.ibm.wsdl.PortTypeImpl;

/**
 * This class is a dynamic WS client that allows to invoke any web service, given its wsdl, operation name and argument list.
 * 
 * @author Andrey Kashlev
 * 
 */
public class WSClient2 {

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
			Object[] opSetArgs = new Object[] { new Integer(2), new Integer(8) };
			Class[] returnTypes2 = new Class[] { Integer.class };
			// Need QName. Provide targetNamespace and operation name to be
			// invoked
			QName op1Name = new QName(nsFromWsdl, opName);

			Object[] response = serviceClient.invokeBlocking(op1Name, wsArgArray, returnTypesArr);
			result = response[0];

			// System.out.println("square of sum:  " + result);
		} catch (Exception ex) {
			System.out.println(ex);
		}
		return result;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String wsdl = null;
		wsdl = "http://localhost:8080/axis2/services/SquareL?wsdl";
		String serviceName = "SquareL";
		String opName = "square";

		ArrayList<Object> wsArguments = new ArrayList();
		Long arg1 = new Long("2147483642");
		wsArguments.add(arg1);

		ArrayList<Class> returnTypes = new ArrayList<Class>();
		returnTypes.add(Long.class);

		Object result = callWebService(wsdl, serviceName, opName, wsArguments, returnTypes);
		System.out.println("result type = " + result.getClass());
		System.out.println("result value = " + result);

		// IncrementI:
		System.out.println("\nIncrementI:");
		wsdl = "http://localhost:8080/axis2/services/IncrementI?wsdl";
		serviceName = "IncrementI";
		opName = "increment";

		wsArguments = new ArrayList();
		String arg2 = "2147483";
		wsArguments.add(arg2);

		returnTypes = new ArrayList<Class>();
		returnTypes.add(Integer.class);

		result = callWebService(wsdl, serviceName, opName, wsArguments, returnTypes);
		System.out.println("result type = " + result.getClass());
		System.out.println("result value = " + result);
		// short c = 3;
		// short d = ++c;
		// System.out.println("d  = " + d);

		// IncrementS:
		System.out.println("\nIncrementS:");
		wsdl = "http://localhost:8080/axis2/services/IncrementS?wsdl";
		serviceName = "IncrementS";
		opName = "increment";

		wsArguments = new ArrayList();
		String arg3 = "30000";
		wsArguments.add(arg3);

		returnTypes = new ArrayList<Class>();
		returnTypes.add(Short.class);

		result = callWebService(wsdl, serviceName, opName, wsArguments, returnTypes);
		System.out.println("result type = " + result.getClass());
		System.out.println("result value = " + result);

		// IncrementB:
		System.out.println("\nIncrementB:");
		wsdl = "http://localhost:8080/axis2/services/IncrementB?wsdl";
		serviceName = "IncrementB";
		opName = "increment";

		wsArguments = new ArrayList();
		String arg4 = "32";
		wsArguments.add(arg4);

		returnTypes = new ArrayList<Class>();
		returnTypes.add(Short.class);

		result = callWebService(wsdl, serviceName, opName, wsArguments, returnTypes);
		System.out.println("result type = " + result.getClass());
		System.out.println("result value = " + result);

		// IncrementF:
		System.out.println("\nIncrementF:");
		wsdl = "http://localhost:8080/axis2/services/IncrementF?wsdl";
		serviceName = "IncrementF";
		opName = "increment";

		wsArguments = new ArrayList();
		float arg5 = 17f;
		wsArguments.add(arg5);

		returnTypes = new ArrayList<Class>();
		returnTypes.add(Float.class);

		result = callWebService(wsdl, serviceName, opName, wsArguments, returnTypes);
		System.out.println("result type = " + result.getClass());
		System.out.println("result value = " + result);

		// IncrementD:
		System.out.println("\nIncrementD:");
		wsdl = "http://localhost:8080/axis2/services/IncrementD?wsdl";
		serviceName = "IncrementD";
		opName = "increment";

		wsArguments = new ArrayList();
		float arg6 = 12.5f;
		wsArguments.add(arg6);

		returnTypes = new ArrayList<Class>();
		returnTypes.add(Double.class);

		result = callWebService(wsdl, serviceName, opName, wsArguments, returnTypes);
		System.out.println("result type = " + result.getClass());
		System.out.println("result value = " + result);

		// IncrementStr:
		System.out.println("\nIncrementStr:");
		wsdl = "http://localhost:8080/axis2/services/IncrementStr?wsdl";
		serviceName = "IncrementStr";
		opName = "increment";

		wsArguments = new ArrayList();
		boolean arg7 = true;
		wsArguments.add(arg7);

		returnTypes = new ArrayList<Class>();
		returnTypes.add(String.class);

		result = callWebService(wsdl, serviceName, opName, wsArguments, returnTypes);
		System.out.println("result type = " + result.getClass());
		System.out.println("result value = " + result);

		// System.out.println("\nlet's try 3rd party calculator:");
		// wsdl = "http://soaptest.parasoft.com/calculator.wsdl";
		// serviceName = "Calculator";
		// opName = "Add";
		//
		// wsArguments = new ArrayList();
		// float arg8 = 45.5f;
		// float arg9 = 5.5f;
		// wsArguments.add(arg8);
		// wsArguments.add(arg9);
		//
		// returnTypes = new ArrayList<Class>();
		// returnTypes.add(String.class);
		//
		// result = callWebService(wsdl, serviceName, opName, wsArguments, returnTypes);
		// System.out.println("result type = " + result.getClass());
		// System.out.println("result value = " + result);

		// IncrementD:
		System.out.println("\nIncrementDTcheck:");
		wsdl = "http://localhost:8080/axis2/services/IncrementDTcheck?wsdl";
		serviceName = "IncrementDTcheck";
		opName = "increment";

		wsArguments = new ArrayList();
		int arg10 = 15;
		wsArguments.add(arg10);

		returnTypes = new ArrayList<Class>();
		returnTypes.add(Double.class);

		result = callWebService(wsdl, serviceName, opName, wsArguments, returnTypes);
		System.out.println("result type = " + result.getClass());
		System.out.println("result value = " + result);

		// produceDate:
		System.out.println("\nproduceDate:");
		wsdl = "http://localhost:8080/axis2/services/produceDate?wsdl";
		serviceName = "produceDate";
		opName = "produce";

		wsArguments = new ArrayList();

		returnTypes = new ArrayList<Class>();
		returnTypes.add(Calendar.class);

		result = callWebService(wsdl, serviceName, opName, wsArguments, returnTypes);
		System.out.println("result type = " + result.getClass());
		System.out.println("result value = " + ((Calendar) result).getTime());
		
		// IncrementStr input date:
		System.out.println("\nIncrementStr input date:");
		wsdl = "http://localhost:8080/axis2/services/IncrementStr?wsdl";
		serviceName = "IncrementStr";
		opName = "increment";

		wsArguments = new ArrayList();
		Calendar calendar = Calendar.getInstance();
		calendar.set(2012, 8, 24, 4, 30, 0);
		System.out.println("sending: " + calendar.toString());
		wsArguments.add(calendar.getTime());

		returnTypes = new ArrayList<Class>();
		returnTypes.add(String.class);

		result = callWebService(wsdl, serviceName, opName, wsArguments, returnTypes);
		System.out.println("result type = " + result.getClass());
		System.out.println("result value = " + result);
		
		
		//FAIL:
		//IncrementL for calendar as input:
		System.out.println("\nIncrementL:");
		wsdl = "http://localhost:8080/axis2/services/IncrementL?wsdl";
		serviceName = "IncrementL";
		opName = "increment";

		wsArguments = new ArrayList();
		Calendar arg11 = Calendar.getInstance();
		//wsArguments.add(arg11); FAIL
		wsArguments.add(arg11.getTime());
		System.out.println("sending: " + arg11);

		returnTypes = new ArrayList<Class>();
		returnTypes.add(Long.class);

		//result = callWebService(wsdl, serviceName, opName, wsArguments, returnTypes);
		System.out.println("result type = " + result.getClass());
		System.out.println("result value = " + result);
		
		
		System.out.println("\nAddPoly:");
		wsdl = "http://localhost:8080/axis2/services/AddPoly?wsdl";
		serviceName = "AddPoly";
		opName = "add";

		wsArguments = new ArrayList();
		long arg12 = 5;
		long arg13 = 7;
		
		wsArguments.add(arg12);
		wsArguments.add(arg13);

		returnTypes = new ArrayList<Class>();
		returnTypes.add(Long.class);

		result = callWebService(wsdl, serviceName, opName, wsArguments, returnTypes);
		System.out.println("result type = " + result.getClass());
		System.out.println("result value = " + result);
		
		System.out.println("\nAddPoly2:");
		wsdl = "http://localhost:8080/axis2/services/AddPoly2?wsdl";
		serviceName = "AddPoly2";
		opName = "add3";

		wsArguments = new ArrayList();
		long arg14 = 2147483647;
		long arg15 = 2147483647;
		
		wsArguments.add(arg14);
		wsArguments.add(arg15);

		returnTypes = new ArrayList<Class>();
		returnTypes.add(Long.class);

		result = callWebService(wsdl, serviceName, opName, wsArguments, returnTypes);
		System.out.println("result type = " + result.getClass());
		System.out.println("result value = " + result);
	}
}
