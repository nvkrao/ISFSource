package com.isf.parsers;

import com.isf.webutils.InscriptWebLoader;
import isf.ptm.fileio.PTMIO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Created by IntelliJ IDEA.
 * User: 0223
 * Date: Aug 8, 2007
 * Time: 12:31:27 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class LazyParser {
    private static final int BUFSIZ = 2048;
    long fileSize;
    // static int width,height;


    //blic static final String CACHE_PATH = InscriptWebLoader.getInstance().getProperty("cacheDirectory");
    public static final boolean ZIP = Boolean.getBoolean(InscriptWebLoader.getInstance().getProperty("cacheZipped"));
    public static final int SEGMENT = Integer.parseInt(InscriptWebLoader.getInstance().getProperty("segment"));
 public static final String CACHE_PATH = InscriptWebLoader.getInstance().getProperty("cacheDirectory"); //"D://personal//images//cache";//

    protected String version;
    protected String type = "";

    public void setVersion(String s) {
        version = s;
    }


    public static LazyParser getPTMParser(InputStream in, String name, long size) throws Exception {
        in.mark(BUFSIZ);
        String version = PTMIO.getLine(in);
        String type = PTMIO.getLine(in);
        int width = Integer.parseInt(PTMIO.getLine(in));
        int height = Integer.parseInt(PTMIO.getLine(in));
        String[] sa;
        /* scale */
        sa = PTMIO.getLine(in).split(" ");
        float[] scale = new float[sa.length];
        for (int i = 0; i < sa.length; i++) {
            scale[i] = Float.parseFloat(sa[i]);

            // debug("Scale: " + Utils.asString(scale));

            /* bias */
        }
        sa = PTMIO.getLine(in).split(" ");
        int[] bias = new int[sa.length];
        for (int i = 0; i < sa.length; i++) {
            bias[i] = Integer.parseInt(sa[i]);
        }


        File dir = new File(CACHE_PATH, name);
        dir.mkdir();
        String sep = ",";


        StringBuffer temp = new StringBuffer();
        temp.append("    <PTM name=\"");
        temp.append(name);
        temp.append("\" width=\"");
        temp.append(width);
        temp.append("\" height=\"");
        temp.append(height);
        temp.append("\" scale=\"");
        for (int i = 0; i < scale.length; i++) {
            temp.append(scale[i] + sep);
        }
        temp.deleteCharAt(temp.length() - 1);
        temp.append("\" bias=\"");
        for (int i = 0; i < bias.length; i++) {
            temp.append(bias[i] + sep);
        }
        temp.deleteCharAt(temp.length() - 1);
        temp.append("\" size=\"");
        temp.append(size);
        temp.append("\" rows=\"");
        int rows = height / SEGMENT;
        int balance = height % SEGMENT;
        if (((float) balance) / SEGMENT > 0.3)
            rows += 1;
        rows = (rows == 0) ? 1 : rows;
        temp.append(rows);
        int cols = width / SEGMENT;
        balance = width % SEGMENT;
        if (((float) balance) / SEGMENT > 0.3)
            cols += 1;
        cols = (cols == 0) ? 1 : cols;
        temp.append("\" cols=\"");
        temp.append(cols);
        temp.append("\" segment=\"");
        temp.append(SEGMENT);
        temp.append("\" type=\"");
        temp.append(type);
        temp.append("\" version=\"");
        temp.append(version);
        temp.append("\" cached=\"no");
        temp.append("\"/>\n");
        try {
            FileOutputStream fs = new FileOutputStream(CACHE_PATH + File.separator + name + File.separator + name + ".cfg");
            fs.write(temp.toString().getBytes());
            fs.flush();
            fs.close();
        } catch (IOException ie) {
            ie.printStackTrace();
        }


        if (type.equals("PTM_FORMAT_LRGB")) {

            BLRGBParser jp = new BLRGBParser(in, name, width, height);
            jp.setVersion(version);

            return jp;

        } else if (type.equals("PTM_FORMAT_JPEG_LRGB")) {
            /* JPEGParser jp = new JPEGParser(in, name, width, height);
             jp.setVersion(version);

             return jp;*/
            throw new UnsupportedOperationException("Not Supported");

        } else if (type.equals("PTM_FORMAT_RGB")) {
            RGBParser jp = new RGBParser(in, name, width, height);
            jp.setVersion(version);

            return jp;

        } else {
            throw new Exception("Unsupported Type: " + type);
        }

    }


    public long getLength(File f) {
        int length = 0;
        File[] files = f.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory())
                length += getLength(files[i]);
            else
                length += files[i].length();
        }

        //logger.debug("Length of :"+ f.getName()+"::"+length);

        return length;
    }

    public abstract void cachePTM();

    public String getEncodeRate(String type, String fileName) {
        String str = InscriptWebLoader.getInstance().getProperty(fileName.toLowerCase());
        if (str == null || str.equalsIgnoreCase("")) {
          str= getWildCard(fileName);
        //  logger.debug(fileName+":: "+str+"-");
          str = str.equalsIgnoreCase("0")?InscriptWebLoader.getInstance().getProperty(type):str;
           // str = InscriptWebLoader.getInstance().getProperty(type);
        }
        return str;
    }
    
    private String getWildCard(String fileName)
    {
        Enumeration keys = wildcard_encode_rates.keys();
        String key="";
        String encode="0";
        while(keys.hasMoreElements())
        {
            key = (String)keys.nextElement();
            if(fileName.startsWith(key))
            {
                encode = (String) wildcard_encode_rates.get(key);
                break;
            }
                
        }
        return encode;
    }
    
    private static Hashtable wildcard_encode_rates;
    static {
        wildcard_encode_rates = new Hashtable();
        String wildcards = InscriptWebLoader.getInstance().getProperty("wild");
        String[] nums = wildcards.split(",");
        String[] vals ;
        for(int i = 0 ; i < nums.length; i++)
        {
            vals = InscriptWebLoader.getInstance().getProperty("wild"+nums[i]).split(",") ;
            for(int x = 1 ; x<vals.length;x++)
                wildcard_encode_rates.put(vals[x], vals[0]);
        }

        Enumeration keys = wildcard_encode_rates.keys();
        while(keys.hasMoreElements())
        {
            String key = (String)keys.nextElement();

               System.out.println(key+"--"+(String)wildcard_encode_rates.get(key));


        }

        
    }


}
