package com.example.studileih.Controller;

import com.example.studileih.Dto.DormDto;
import com.example.studileih.Service.DormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;



@RestController
@CrossOrigin
public class DormController {

    @Autowired
    private DormService dormService;

    /**
     * @return: all products from the repository
     */
    @GetMapping("/dorms")
    public List<DormDto> getDormLocations() {
        return dormService.listAllDorms();
    }




}