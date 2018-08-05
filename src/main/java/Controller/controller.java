package Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
/**
 * Created by kimsangyeon on 2018. 7. 28..
 */
@Controller
public class controller {

    @RequestMapping(value = "/")
    public String test(){
        return "index";
    }

    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> uploadFile(@RequestParam("file") MultipartFile uploadfile, HttpServletRequest request) throws IOException {
        System.out.println("----upload-----");
        String fileName = uploadfile.getOriginalFilename();

        System.out.println("fileName:" + fileName);

        // Root Directory.
        String uploadRootPath = "/Users/kimsangyeon/workspace/spring_editor/web/WEB-INF/resources/media";
        System.out.println("uploadRootPath: " +  uploadRootPath);
        File uploadRootDir = new File(uploadRootPath);
        // Create directory if it not exists.
        if (!uploadRootDir.exists()) {
            uploadRootDir.mkdirs();
        }

        byte[] bytes = uploadfile.getBytes();
        Path path = Paths.get(uploadRootDir + "/" + fileName);
        Files.write(path, bytes);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("uploadPath", "resources/media" + "/" + fileName);

        System.out.println(map);

        return map;
    }
}
