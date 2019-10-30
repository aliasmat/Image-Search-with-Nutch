package org.apache.nutch.imagesearch;

import java.util.*;
import java.net.*;
import java.io.*;
import org.apache.nutch.parse.HTMLMetaTags; 
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.ParseResult; 
import org.apache.nutch.parse.HtmlParseFilter; 
import org.apache.nutch.protocol.Content; 
import org.apache.nutch.util.NodeWalker; 
import org.apache.hadoop.conf.Configuration; 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.DocumentFragment; 
import org.w3c.dom.NamedNodeMap; 
import org.w3c.dom.Node; 
import org.w3c.dom.Attr;

public class ImageSearch implements HtmlParseFilter{
	public static final Log LOG = LogFactory.getLog(ImageSearch.class.getName());
	private Configuration conf;
        private String[] Keywords;
	  
	public ParseResult filter(Content content, ParseResult parseResult, HTMLMetaTags metaTags, DocumentFragment doc){
		
		Parse parse = parseResult.get(content.getUrl());
		URL base = null;
		try{
			base = new URL(content.getBaseUrl());
		}catch(MalformedURLException e){}


		//Search for Images and related metadata.
	    SearchImages(doc, base, parseResult);
		return parseResult;
	}
	 private void SearchImages(DocumentFragment doc, URL base, ParseResult parseResult){
			NodeWalker walker = new NodeWalker(doc);
			while(walker.hasNext()){
				Node currentNode = walker.nextNode();
				String nodeName = currentNode.getNodeName();
				short nodeType = currentNode.getNodeType();
				
				if("script".equalsIgnoreCase(nodeName))
					walker.skipChildren();
				if("style".equalsIgnoreCase(nodeName))
					walker.skipChildren();
				
				if("img".equalsIgnoreCase(nodeName) && nodeType == Node.ELEMENT_NODE){
					String imageUrl = "NoUrl"; 
					String altText = "NoAltText";
					String imageName = "NoImageName";  //default values set to avoid nullpointerException
					String titleText = "NoTitle";                        // in findMatches method
					boolean MatchFound = false;                   

					NamedNodeMap attributes = currentNode.getAttributes();
					
			    //Analyze the attributes values inside the img node. <img src="xxx" alt="myPic"> 
			      for(int i = 0; i < attributes.getLength(); i++){
			
						Attr attr = (Attr)attributes.item(i); 
						if("src".equalsIgnoreCase(attr.getName())){ 
							imageUrl = getImageUrl(base, attr);
							if(imageUrl != null){
							      imageName = getImageName(imageUrl);
					    }}
						else if("alt".equalsIgnoreCase(attr.getName())){
							      altText = attr.getValue().trim().toLowerCase();
					    }
					    else if("title".equalsIgnoreCase(attr.getName())){
							      titleText = attr.getValue().trim().toLowerCase();
				    }}
					
			      //Perform matching and other actions
				  MatchFound =  FindMatches(imageName, altText, titleText);
				        if(MatchFound){
						     imageUrl = getUrlNormalized(imageUrl);
				        	if(imageUrl.contains("thumb") || imageUrl.contains("Thumb")){
				        		writeFIlteredImage(imageUrl);
				        	}else
					              writeUrlToFile(imageUrl);
				        }else{
					           writeMisMatch("Name:  " + imageName);
				       }
			     }
		    }
	 }
	
	private boolean FindMatches(String imageName, String altText, String titleText){
		
		for(String keyword : Keywords){
			if(imageName.contains(keyword) || titleText.contains(keyword) || altText.contains(keyword)){
				return true;
			}
		}	
		return false;
	}
	
	private  String getUrlNormalized(String Url){
		try{
		    String NormUrl = Url.substring(0, Url.indexOf('?'));
		    return NormUrl;
		}catch(StringIndexOutOfBoundsException ex){
			return Url;
		}catch(Exception e){
			return Url;
		}
	}

 	private String getImageUrl(URL base, Attr attr){
		URL Source = null;
		String imageUrl = null;
		try{
			Source = new URL(base, attr.getValue());
			imageUrl = Source.toString();
			imageUrl = imageUrl.replaceAll("\\s", "%20");
		}catch(MalformedURLException ex){
		}
		return imageUrl;
	}
 	
	private String getImageName(String Url){
		
		int index = Url.lastIndexOf('/');
		String ImageName = Url.substring(index+1);
		return ImageName.toLowerCase();
	}

	private void writeUrlToFile(String Url){
	     String format = Url.substring(Url.length() - 3).toLowerCase();
	     if(!format.equals("ico")  && !format.equals("gif")){
	    	try(FileWriter fw = new FileWriter("/home/dev/RetreivedUrls.txt", true);
	  	               BufferedWriter bw = new BufferedWriter(fw);
	  	               PrintWriter out = new PrintWriter(bw)){
	  	                     out.println(Url);
	                   }
	  		    catch(IOException ex){
	            } 
	    	}
	    }
	
	private void writeFIlteredImage(String url){
		try(FileWriter fw = new FileWriter("/home/dev/filteredThumbnails.txt", true);
	               BufferedWriter bw = new BufferedWriter(fw);
	               PrintWriter out = new PrintWriter(bw)){
	                     out.println(url);
                }
		    catch(IOException ex){
            }
		}

	private void writeMisMatch(String text){
		try(FileWriter fw = new FileWriter("/home/dev/MismatchedImages.txt", true);
			    BufferedWriter bw = new BufferedWriter(fw);
			    PrintWriter out = new PrintWriter(bw)){
			    out.println(text);
		}
		catch(IOException ex){
    	}
	}

	public void setConf(Configuration conf) 
	{ 
	    this.conf = conf; 
	    Keywords = conf.getStrings("keywords");  //This will retrieve the search keywords from Nutch-site.xml
	  } 
	
	public Configuration getConf() 
	  { 
	    return this.conf; 
	  } 
}

