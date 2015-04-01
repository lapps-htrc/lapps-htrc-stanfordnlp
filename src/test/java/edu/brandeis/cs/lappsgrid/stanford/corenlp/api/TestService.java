package edu.brandeis.cs.lappsgrid.stanford.corenlp.api;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by Chunqi SHI (shicq@cs.brandeis.edu) on 3/5/14.
 */
public class TestService {


    protected HashMap<String,String> jsons = new HashMap<String,String>();

    public static String getResource(String name) throws IOException{
        java.io.InputStream in =  TestService.class.getClassLoader().getResourceAsStream(name);
        return IOUtils.toString(in);
    }

    @Before
    public void init() throws IOException {
        File jsonDir = FileUtils.toFile(this.getClass().getResource("/jsons"));
//        System.out.println(jsonDir);
        Collection<File> fils = FileUtils.listFiles(jsonDir, new String[]{"json"}, true);
//        System.out.println(fils);
        for(File jsonFil : fils){
//            System.out.println(jsonFil.getName());
            jsons.put(jsonFil.getName(), FileUtils.readFileToString(jsonFil, "UTF-8"));
        }
    }


    @Test
    public void test() {
//        System.out.println(payload);
//        Assert.assertEquals("", payload, data.getPayload());
//        Assert.assertEquals("", Types.JSON, data.getDiscriminator());
//        System.out.println(container.getText());
    }

}
