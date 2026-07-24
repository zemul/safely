package com.anbao.controllor;

import com.anbao.utils.FileUploadUtil;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;

/**
 * 视频存储与播放
 * 使用 S3 协议，兼容 AWS S3 / 阿里云 OSS / MinIO 等
 */
@Controller
public class VideoStream {

    private static final String BUCKET;
    private static final AmazonS3 s3Client;

    static {
        String endpoint = System.getenv("S3_ENDPOINT") != null
                ? System.getenv("S3_ENDPOINT") : "http://localhost:9000";
        String region = System.getenv("S3_REGION") != null
                ? System.getenv("S3_REGION") : "us-east-1";
        String accessKey = System.getenv("S3_ACCESS_KEY") != null
                ? System.getenv("S3_ACCESS_KEY") : "minioadmin";
        String secretKey = System.getenv("S3_SECRET_KEY") != null
                ? System.getenv("S3_SECRET_KEY") : "minioadmin";
        BUCKET = System.getenv("S3_BUCKET") != null
                ? System.getenv("S3_BUCKET") : "safely-videos";

        s3Client = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .withPathStyleAccessEnabled(true)  // MinIO 需要 path-style
                .withClientConfiguration(new ClientConfiguration().withSignerOverride("AWSS3V4SignerType"))
                .build();

        // 自动创建 bucket（如果不存在）
        if (!s3Client.doesBucketExistV2(BUCKET)) {
            s3Client.createBucket(BUCKET);
        }
    }

    /**
     * 视频播放（支持 Range 请求）
     */
    @RequestMapping("/video")
    public void playVideo(String path, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (path == null) return;

        if (!FileUploadUtil.isValidPath(path)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String key = "video/" + path;

        // 获取文件大小
        ObjectMetadata metadata;
        try {
            metadata = s3Client.getObjectMetadata(BUCKET, key);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        long fileLen = metadata.getContentLength();

        String range = req.getHeader("Range");
        OutputStream out = resp.getOutputStream();

        if (range == null) {
            // 完整下载
            String displayName = path.contains("/") ? path.substring(path.lastIndexOf("/") + 1) : path;
            resp.setHeader("Content-Disposition", "attachment; filename=" + displayName);
            resp.setContentType("application/octet-stream");
            resp.setContentLengthLong(fileLen);

            try (S3Object obj = s3Client.getObject(BUCKET, key);
                 InputStream in = obj.getObjectContent()) {
                copy(in, out);
            }
        } else {
            // Range 请求（视频拖拽播放）
            long start = Long.parseLong(range.substring(range.indexOf("=") + 1, range.indexOf("-")));
            long end = range.endsWith("-") ? fileLen - 1 : Long.parseLong(range.substring(range.indexOf("-") + 1));
            long count = end - start + 1;

            resp.setStatus(206);
            resp.setContentType("video/mp4");
            resp.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLen);
            resp.setContentLengthLong(count);
            resp.setHeader("Accept-Ranges", "bytes");

            GetObjectRequest getReq = new GetObjectRequest(BUCKET, key).withRange(start, end);
            try (S3Object obj = s3Client.getObject(getReq);
                 InputStream in = obj.getObjectContent()) {
                copy(in, out);
            }
        }
        out.flush();
    }

    /**
     * 上传视频
     */
    @RequestMapping("/uploadVideo")
    @ResponseBody
    public void uploadVideo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            ServletContext servletContext = request.getSession().getServletContext();
            String pathTemp = servletContext.getRealPath("temp");
            File tempDir = new File(pathTemp);
            if (!tempDir.exists()) tempDir.mkdirs();

            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setSizeThreshold(1024 * 1024 * 10);
            factory.setRepository(tempDir);

            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setHeaderEncoding("UTF-8");

            if (!upload.isMultipartContent(request)) return;

            List<FileItem> parseRequest = upload.parseRequest(request);
            if (parseRequest == null) return;

            for (FileItem item : parseRequest) {
                if (item.isFormField()) continue;

                String rawFileName = item.getName();
                String fileName = FileUploadUtil.sanitizeFileName(rawFileName);
                if (fileName == null || !FileUploadUtil.isAllowedExtension(fileName, FileUploadUtil.ALLOWED_VIDEO_EXTENSIONS)) {
                    item.delete();
                    continue;
                }
                if (!FileUploadUtil.isWithinSizeLimit(item.getSize(), FileUploadUtil.MAX_VIDEO_SIZE)) {
                    item.delete();
                    continue;
                }

                String key = "video/" + fileName;

                // 上传到 S3
                ObjectMetadata meta = new ObjectMetadata();
                meta.setContentLength(item.getSize());
                meta.setContentType("video/mp4");

                try (InputStream in = item.getInputStream()) {
                    s3Client.putObject(new PutObjectRequest(BUCKET, key, in, meta));
                }

                item.delete();
            }
        } catch (FileUploadException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
    }
}
