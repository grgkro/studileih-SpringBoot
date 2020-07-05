package com.example.studileih.Controller;

import com.example.studileih.Dto.DormDistricts;
import com.example.studileih.Dto.DormDto;
import com.example.studileih.Dto.ProductDto;
import com.example.studileih.Entity.Dorm;
import com.example.studileih.Entity.Product;
import com.example.studileih.Service.DormService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@CrossOrigin
public class DormController {

    @Autowired
    private DormService dormService;

    /**
     * Die Funktion wird direkt nach Start aufgerufen und speichert 1 Beispielwohnheim/Adresse/2 Pro in die DB -> Kann später auskommentiert/gelöscht werden
     */
    @PostConstruct
    public void createBaseDataset() {
        // at the start we create some dorms into the database, but only if there are no entries yet!
        if (dormService.listAllDorms().isEmpty()) {
            Dorm alexanderstraße = new Dorm("alexanderstraße",48.767485, 9.179693, DormDistricts.StuttgartMitte.toString());
            Dorm annaHerrigelHaus = new Dorm("anna-herrigel-haus",48.807598, 9.220796, DormDistricts.StuttgartMitte.toString());
            dormService.addDorm(alexanderstraße);
            dormService.addDorm(annaHerrigelHaus);
        }

    }



    /**
     * @return: all products from the repository
     */
    @GetMapping("/dorms")
    public List<DormDto> getDormLocations() {
        System.out.println(dormService.listAllDorms());
        return dormService.listAllDorms();
    }




}