package org.st411ar.aem.doc.navigation;


import java.io.IOException;


public class App 
{
    public static void main( String[] args ) throws IOException {
        long startTime = System.currentTimeMillis();

        DocNavNode navigation = DocNavNode.buildNavigation();
        System.out.println(navigation);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println(duration/1000 + " seconds");
    }
}