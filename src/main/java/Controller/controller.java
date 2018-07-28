package Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by kimsangyeon on 2018. 7. 28..
 */
@Controller
public class controller {

    @RequestMapping(value = "/")
    public String test(){
        return "index";
    }
}
