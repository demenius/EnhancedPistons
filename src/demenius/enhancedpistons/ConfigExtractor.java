/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package demenius.enhancedpistons;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Braydon
 */
public class ConfigExtractor
{
    public static void main(String[] args)
    {
        try
        {
            extractFile("EnhancedPistonsConfig.yml");
        } catch(Exception ex)
        {
            System.out.println(ex.getMessage());
        }
    }

    private static void extractFile(String name) throws IOException
    {
        ClassLoader cl = ConfigExtractor.class.getClassLoader();
        File target = new File("EnhancedPistons" + File.separator + name);
        
        File dir = new File("EnhancedPistons");
        if(!dir.exists() || !dir.isDirectory())
        {
            dir.mkdir();
        }
        
        if (target.exists())
        {
            return;
        }

        FileOutputStream out = new FileOutputStream(target);
        InputStream in = cl.getResourceAsStream(name);

        byte[] buf = new byte[8 * 1024];
        int len;
        while ((len = in.read(buf)) != -1)
        {
            out.write(buf, 0, len);
        }
        out.close();
        in.close();
    }
}
