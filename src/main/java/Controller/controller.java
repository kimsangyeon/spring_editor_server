package Controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by kimsangyeon on 2018. 7. 28..
 */
@Controller
public class controller {
    static String UPLOAD_PATH_MEDIA = "/Users/kimsangyeon/workspace/spring_editor/web/WEB-INF/resources/media";
    static String UPLOAD_PATH_DOC = "/Users/kimsangyeon/workspace/spring_editor/web/WEB-INF/resources/doc";
    static String UPLOAD_PATH_ZIP = "/Users/kimsangyeon/workspace/spring_editor/web/WEB-INF/resources/doc/zip";
    static String UPLOAD_PATH_UNZIP = "/Users/kimsangyeon/workspace/spring_editor/web/WEB-INF/resources/doc/unzip";
    static String API_SERVER = "http://synapeditor.iptime.org:7419";

    private static final long BEGINING_POSITION_OF_XOR_SECTION = 4L;

    @RequestMapping(value = "/")
    public String test() {
        return "index";
    }

    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> uploadFile(@RequestParam("file") MultipartFile uploadfile, HttpServletRequest request) throws IOException {
        System.out.println("----uploadFile-----");

        String fileName = uploadfile.getOriginalFilename();

        // Root Directory.
        File uploadRootDir = new File(UPLOAD_PATH_MEDIA);
        // Create directory if it not exists.
        if (!uploadRootDir.exists()) {
            uploadRootDir.mkdirs();
        }

        byte[] bytes = uploadfile.getBytes();
        Path path = Paths.get(uploadRootDir + "/" + fileName);
        Files.write(path, bytes);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("uploadPath", "resources/media" + "/" + uploadfile.getOriginalFilename());

        System.out.println(map);

        return map;
    }

    @RequestMapping(value = "/importDoc", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> importDoc(@RequestParam("docFile") MultipartFile uploadfile, HttpServletRequest request) throws IOException {
        System.out.println("----importDoc-----");
        String fileName = uploadfile.getOriginalFilename();

        File uploadRootDir = new File(UPLOAD_PATH_DOC);
        // Create directory if it not exists.
        if (!uploadRootDir.exists()) {
            uploadRootDir.mkdirs();
        }

        byte[] bytes = uploadfile.getBytes();
        Path path = Paths.get(uploadRootDir + "/" + fileName);
        Files.write(path, bytes);

        File convFile = new File(uploadRootDir + "/" + fileName);

        MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
        bodyMap.add("file", new FileSystemResource(convFile));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(bodyMap, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<byte[]> response = restTemplate.exchange(API_SERVER + "/convertDocToPb",
                HttpMethod.POST, requestEntity, byte[].class);

        String pbFilePath = unzipFile(response.getBody());
        Integer[] serializedData = serializePbData(pbFilePath);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("serializedData", serializedData);

        return map;
    }

    public String unzipFile(byte[] bytes) throws IOException {
        String ZIP = UPLOAD_PATH_ZIP + "/document.word.pb.zip";
        String UNZIP = UPLOAD_PATH_UNZIP + "/document.word.pb";

        File zipDir = new File(UPLOAD_PATH_ZIP);
        // Create directory if it not exists.
        if (!zipDir.exists()) {
            zipDir.mkdirs();
        }

        Path path = Paths.get(ZIP);
        Files.write(path, bytes);

        unzip(ZIP, UPLOAD_PATH_UNZIP);

        return UNZIP;
    }

    private static void unzip(String zipFilePath, String destDir) {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if(!dir.exists()) dir.mkdirs();
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while(ze != null){
                String fileName = ze.getName();
                File newFile = new File(destDir + File.separator + fileName);
                System.out.println("Unzipping to "+newFile.getAbsolutePath());
                //create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Integer[] serializePbData(String pbFilePath) throws IOException {
        List<Integer> serializedData = new ArrayList<Integer>();
        FileInputStream fis = new FileInputStream(pbFilePath);

        Integer[] data = null;

        fis.skip(16);

        InflaterInputStream ifis = new InflaterInputStream(fis);

        byte[] buffer = new byte[1024];

        int len = -1;
        while ((len = ifis.read(buffer)) != -1) {
            for (int i = 0; i < len; i++) {
                serializedData.add(buffer[i] & 0xFF);
            }
        }

        data = serializedData.toArray(new Integer[serializedData.size()]);

        ifis.close();
        fis.close();

        return data;
    }
}
