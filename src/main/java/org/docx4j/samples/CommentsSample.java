/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */


package org.docx4j.samples;

import java.io.File;
import java.math.BigInteger;
import java.util.Calendar;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.docx4j.convert.out.flatOpcXml.FlatOpcXmlCreator;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.model.table.TblFactory;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.contenttype.CTDefault;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.contenttype.ObjectFactory;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTAltChunk;
import org.docx4j.wml.Comments;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Comments.Comment;

/**
 * Creates a WordprocessingML document from scratch, and adds a comment.
 * 
 * Note that only w:commentReference is required; this example 
 * doesn't add w:commentRangeStart or w:commentRangeEnd 
 * 
          <w:p>
            <w:commentRangeStart w:id="0"/>
            <w:r>
              <w:t>hello</w:t>
            </w:r>
            <w:commentRangeEnd w:id="0"/>
            <w:r>
              <w:rPr>
                <w:rStyle w:val="CommentReference"/>
              </w:rPr>
              <w:commentReference w:id="0"/>
            </w:r>
          </w:p>
 *  
 * 
 * @author Jason Harrop
 * @version 1.0
 */
public class CommentsSample extends AbstractSample {

	   static org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();
	   
	   public static void main(String[] args) throws Exception {
		
		
		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
	    	inputfilepath = System.getProperty("user.dir") + "/Comments_out.docx";	    	
		}
		
		boolean save = 
			(inputfilepath == null ? false : true);
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();

		// Create and add a Comments Part
	      CommentsPart cp = new CommentsPart();
	      wordMLPackage.getMainDocumentPart().addTargetPart(cp);
	      	      
		// Part must have minimal contents
		Comments comments = factory.createComments();
		cp.setJaxbElement(comments);

		// Add a comment to the comments part
		java.math.BigInteger commentId = BigInteger.valueOf(0);
		Comment theComment = createComment(commentId, "fred", null,
				"my first comment");
		comments.getComment().add(theComment);
		
		// Add comment reference to document
		P paraToCommentOn = wordMLPackage.getMainDocumentPart().addParagraphOfText("here is some content");
		paraToCommentOn.getParagraphContent().add(createRunCommentReference(commentId));

		// ++, for next comment ...
		commentId = commentId.add(java.math.BigInteger.ONE);

		// Now save it
		if (save) {
			wordMLPackage.save(new java.io.File(inputfilepath) );
			System.out.println("Saved " + inputfilepath);
		} else {
		   	// Create a org.docx4j.wml.Package object
			FlatOpcXmlCreator worker = new FlatOpcXmlCreator(wordMLPackage);
			org.docx4j.xmlPackage.Package pkg = worker.get();
	    	
	    	// Now marshall it
			JAXBContext jc = Context.jcXmlPackage;
			Marshaller marshaller=jc.createMarshaller();
			
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			NamespacePrefixMapperUtils.setProperty(marshaller, 
					NamespacePrefixMapperUtils.getPrefixMapper());			
			System.out.println( "\n\n OUTPUT " );
			System.out.println( "====== \n\n " );	
			marshaller.marshal(pkg, System.out);				
			
		}
		
		System.out.println("Done.");
				
	}
	   

	    private static org.docx4j.wml.Comments.Comment createComment(java.math.BigInteger commentId,
	    		String author, Calendar date, String message) {

			org.docx4j.wml.Comments.Comment comment = factory.createCommentsComment();
			comment.setId( commentId );
			if (author!=null) {
				comment.setAuthor(author);
			}
			if (date!=null) {
//				String dateString = RFC3339_FORMAT.format(date.getTime()) ;	
//				comment.setDate(value)
				// TODO - at present this is XMLGregorianCalendar
			}
			org.docx4j.wml.P commentP = factory.createP();
			comment.getEGBlockLevelElts().add(commentP);
			org.docx4j.wml.R commentR = factory.createR();
			commentP.getParagraphContent().add(commentR);
			org.docx4j.wml.Text commentText = factory.createText();
			commentR.getRunContent().add(commentText);
			
			commentText.setValue(message);
	    	
	    	return comment;
	    }
	    
	    private static org.docx4j.wml.R createRunCommentReference(java.math.BigInteger commentId) {
	    	
			org.docx4j.wml.R run = factory.createR();
			org.docx4j.wml.R.CommentReference commentRef = factory.createRCommentReference();
			run.getRunContent().add(commentRef);
			commentRef.setId( commentId );	
			
			return run;
	    	
	    }

	
}
