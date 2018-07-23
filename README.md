# Image Search With Apache Nutch

This is a simple Nutch plugin for image search. The plugin looks for image tags on the web pages and retrieve the URLs of the relevant images according to a search query provided in the nutch-site.xml file. 
To add the plugin to Nutch, download the plugin and copy to Nutch_Home/src/plugin directory. Prior to building Nutch, do the following steps.
1.	 add the following line to Nutch_Home/src/plugin/build.xml file under <target name = “deploy”>
     
           <ant dir = “imagesearch” target = “deploy” />

2.	In the Nutch conf directory, find nutch-site.xml file and add the imagesearch plugin under <name>plugin.includes</name>. It should look something like this. 
     
          <property>
             <name>plugin.includes</name>
             <value>protocol-http|urlfilter-regex|parse-(html|tika|metatags)|urlnormalizer-(pass|regex|basic)|imagesearch</value>
          </property>

3.	In the nutch-site.xml file, you need to provide a query for the search. Add something like the following. 

          <property>
               <name>keywords</name>
               <value>keyword1,keyword2,keyword3</value>
          </property>

The code can be modified to search images for several categories at the same time. The text data that is used for searching images includes only image name, title and alt text etc. This could be modified to add headings text, paragraphs and other text data however this can significantly increase the noise ratio. 
