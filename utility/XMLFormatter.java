package utility;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/**
 * This class is used to format xml nicely
 * 
 * @author Andrey Kashlev
 *
 */
public class XMLFormatter {

	 public static void serialize(Document doc, OutputStream out) throws Exception {
	        
	        TransformerFactory tfactory = TransformerFactory.newInstance();
	        Transformer serializer;
	        
	        try {
	            serializer = tfactory.newTransformer();
	            //Setup indenting to "pretty print"
	            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
	            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
	            
	            serializer.transform(new DOMSource(doc), new StreamResult(out));
	        } catch (TransformerException e) {
	            // this is fatal, just dump the stack and throw a runtime exception
	            e.printStackTrace();
	            
	            throw new RuntimeException(e);
	        }
	    }
	
	
	public static void main(String[] args) throws Exception {
		Document wfDoc = Utility.readFileAsDocument("testFiles/JUnit/builtin/Add.swl");
		
		 OutputStream output = new OutputStream()
		    {
		        private StringBuilder string = new StringBuilder();
		        @Override
		        public void write(int b) throws IOException {
		            this.string.append((char) b );
		        }

		        //Netbeans IDE automatically overrides this toString()
		        public String toString(){
		            return this.string.toString();
		        }
		    };
		    
		//output.toString(serialize(wfDoc));
		    serialize(wfDoc, output);
		    
		    String result = output.toString();
		    System.out.println("result: \n\n");
		    System.out.println(result);

	}

}
