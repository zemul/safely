package com.anbao.controllor;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@CrossOrigin(origins = {"*"}, maxAge = 3600)
public class VideoStream {
    public static String HDFSAddress="hdfs://monitor/video/";

    /**
     * 视频播放
     * @param path
     * @param req
     * @param resp
     * @throws IOException
     */
    @RequestMapping("/video")
    public void getSelectUser(String path, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println(path);
        if(path==null)
            return;
        String filename=HDFSAddress+path;
        Configuration config=new Configuration();
        config.addResource("/hadoop/core-site.xml");
        config.addResource("/hadoop/hdfs-site.xml");
        FileSystem fs = null;
        FSDataInputStream in=null;
        try {
            fs = FileSystem.get(URI.create(filename),config,"hadoop");
            in=fs.open(new Path(filename));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final long fileLen = fs.getFileStatus(new Path(filename)).getLen();
        String range=req.getHeader("Range");
        resp.setHeader("Content-type","video/mp4");
        OutputStream out=resp.getOutputStream();
        if(range==null)
        {
            filename=path.substring(path.lastIndexOf("/")+1);
            resp.setHeader("Content-Disposition", "attachment; filename="+filename);
            resp.setContentType("application/octet-stream");
            resp.setContentLength((int)fileLen);
            IOUtils.copyBytes(in, out, fileLen, false);
        }
        else
        {
            long start=Integer.valueOf(range.substring(range.indexOf("=")+1, range.indexOf("-")));
            long count=fileLen-start;
            long end;
            if(range.endsWith("-"))
                end=fileLen-1;
            else
                end=Integer.valueOf(range.substring(range.indexOf("-")+1));
            String ContentRange="bytes "+String.valueOf(start)+"-"+end+"/"+String.valueOf(fileLen);
            resp.setStatus(206);
            resp.setContentType("video/mpeg4");
            resp.setHeader("Content-Range",ContentRange);
            in.seek(start);
            try{
                IOUtils.copyBytes(in, out, count, false);
            }
            catch(Exception e)
            {
                throw e;
            }
        }
        in.close();
        in = null;
        out.close();
        out = null;
    }

    /**
     * 上传视频
     */
    @RequestMapping("/uploadVideo")
    public void uploadViedo( HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {

            //1、创建磁盘文件项工厂
            //作用：设置缓存文件的大小  设置临时文件存储的位置
            ServletContext servletContext = request.getSession().getServletContext();
            String path_temp = servletContext.getRealPath("temp");
            //DiskFileItemFactory factory = new DiskFileItemFactory(1024*1024, new File(path_temp));
            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setSizeThreshold(1024*1024*10);
            factory.setRepository(new File(path_temp));
            //2、创建文件上传的核心类
            ServletFileUpload upload = new ServletFileUpload(factory);
            //设置上传文件的名称的编码
            upload.setHeaderEncoding("UTF-8");
            //ServletFileUpload的API
            boolean multipartContent = upload.isMultipartContent(request);//判断表单是否是文件上传的表单
            if(multipartContent){
                //是文件上传的表单
                //***解析request获得文件项集合
                List<FileItem> parseRequest = upload.parseRequest(request);
                if(parseRequest!=null){
                    for(FileItem item : parseRequest){
                        //判断是不是一个普通表单项
                        boolean formField = item.isFormField();
                        if(formField){
                            //username=zhangsan
                            String fieldName = item.getFieldName();
                            String fieldValue = item.getString("UTF-8");//对普通表单项的内容进行编码
                            System.out.println(fieldName+"----"+fieldValue);

                            //当表单是enctype="multipart/form-data"时 request.getParameter相关的方法
                            //String parameter = request.getParameter("username");

                        }else{
                            //文件上传项
                            //文件的名
                            String fileName = item.getName();
                            //获得上传文件的内容
                            InputStream in = item.getInputStream();
                            System.out.println(fileName);

                            String filename="/video/"+fileName;
                            //20180802_12_00
                            Configuration config=new Configuration();
                            config.addResource("/hadoop/core-site.xml");
                            config.addResource("/hadoop/hdfs-site.xml");

                            FileSystem fs = FileSystem.get(config);
                            OutputStream out = fs.create(new Path(filename));
                            IOUtils.copyBytes(in, out, 1024*1024*10, true);
                            //删除临时文件
                            item.delete();

                        }
                    }
                }

            }else{
                //不是文件上传表单
                //使用原始的表单数据的获得方式 request.getParameter();
            }
        } catch (FileUploadException e) {
            e.printStackTrace();
        }


    }



}
