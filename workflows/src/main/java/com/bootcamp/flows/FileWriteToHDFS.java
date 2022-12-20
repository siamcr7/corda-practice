package com.bootcamp.flows;

//import org.apache.hadoop.conf.Configuration;
//import org.apache.hadoop.fs.FileSystem;
//import org.apache.hadoop.fs.Path;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

//import org.apache.commons.io.IOUtils;
//import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
//import org.apache.hadoop.fs.FSDataInputStream;
//import org.apache.hadoop.fs.FSDataOutputStream;
//import org.apache.hadoop.fs.FileSystem;
//import org.apache.hadoop.fs.Path;

public class FileWriteToHDFS {
    public static void main(String[] args) throws Exception {

//Source file in the local file system
        String localSrc = args[0];
//Destination file in HDFS
        String dst = args[1];

//Input stream for the file in local file system to be written to HDFS
        InputStream in = new BufferedInputStream(new FileInputStream(localSrc));

//Get configuration of Hadoop system
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://localhost:9000");
        System.out.println("Connecting to -- "+conf.get("fs.defaultFS"));

//Destination file in HDFS
        FileSystem fs = FileSystem.get(URI.create(dst), conf);
        OutputStream out = fs.create(new Path(dst));

//Copy file from local to HDFS
//        IOUtils.cop
//        IOUtils.copyBytes(in, out, 4096, true);
//        IOUtils.copy

        System.out.println(dst + " copied to HDFS");



    }
}
