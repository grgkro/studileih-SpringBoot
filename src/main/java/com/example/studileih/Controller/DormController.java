package com.example.studileih.Controller;

import com.example.studileih.Dto.CityEnum;
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
        // https://www.studentenwerk-muenchen.de/wohnen/wohnanlagen/
        // https://www.studierendenwerk-stuttgart.de/wohnen/wohnanlagen/
        // at the start we create some dorms into the database, but only if there are no entries yet!
        if (dormService.listAllDorms().isEmpty()) {

            Dorm alexanderstraße = new Dorm("Alexanderstraße",48.767485, 9.179693, CityEnum.Stuttgart.toString(), DormDistricts.StuttgartMitte.toString());
            Dorm annaHerrigelHaus = new Dorm("Anna-Herrigel-Haus",48.807598, 9.220796, CityEnum.Stuttgart.toString(), DormDistricts.StuttgartMitte.toString());
            Dorm birkenwaldstraße = new Dorm("Birkenwaldstraße",48.790106, 9.174672, CityEnum.Stuttgart.toString(), DormDistricts.StuttgartMitte.toString());
            Dorm bordinghaus = new Dorm("Bordinghaus",48.790607, 9.197915, CityEnum.Stuttgart.toString(), DormDistricts.StuttgartMitte.toString());
            Dorm brückenstraße = new Dorm("Brückenstraße",48.807679, 9.210581, CityEnum.Stuttgart.toString(), DormDistricts.StuttgartMitte.toString());
            Dorm heilmannstraße1 = new Dorm("Heilmannstraße 3-7",48.788257, 9.192228, CityEnum.Stuttgart.toString(), DormDistricts.StuttgartMitte.toString());
            Dorm heilmannstraße2 = new Dorm("Heilmannstraße 4A-4B",48.787754, 9.191977, CityEnum.Stuttgart.toString(), DormDistricts.StuttgartMitte.toString());
            Dorm inDerAu = new Dorm("In der Au",48.776335, 9.250241, CityEnum.Stuttgart.toString(), DormDistricts.StuttgartMitte.toString());
            Dorm johannesstraße = new Dorm("Johannesstraße",48.781312, 9.158844, CityEnum.Stuttgart.toString(), DormDistricts.StuttgartMitte.toString());
            Dorm kernerstraße = new Dorm("Kernerstraße",48.785247, 9.189944, CityEnum.Stuttgart.toString(), DormDistricts.StuttgartMitte.toString());
            Dorm landhausstraße = new Dorm("Landhausstraße",48.783838, 9.191096, CityEnum.Stuttgart.toString(), DormDistricts.StuttgartMitte.toString());
            Dorm maxKade = new Dorm("Max-Kade",48.780427, 9.169875, CityEnum.Stuttgart.toString(), DormDistricts.StuttgartMitte.toString());
            Dorm neckarstraße = new Dorm("Neckarstraße",48.787470, 9.193148, CityEnum.Stuttgart.toString(), DormDistricts.StuttgartMitte.toString());
            Dorm rieckestraße = new Dorm("Rieckestraße",48.789951, 9.198050, CityEnum.Stuttgart.toString(), DormDistricts.StuttgartMitte.toString());
            Dorm rosensteinstraße = new Dorm("Rosensteinstraße 1-3-5",48.793259, 9.190513, CityEnum.Stuttgart.toString(), DormDistricts.StuttgartMitte.toString());
            Dorm theodorHeuss = new Dorm("Theodor-Heuss",48.779592, 9.158887, CityEnum.Stuttgart.toString(), DormDistricts.StuttgartMitte.toString());
            Dorm wiederholdstraße = new Dorm("Wiederholdstraße",48.785276, 9.167170, CityEnum.Stuttgart.toString(), DormDistricts.StuttgartMitte.toString());
            Dorm wohnarealStuttgartRot = new Dorm("Wohnareal Stuttgart-Rot",48.833793, 9.185528, CityEnum.Stuttgart.toString(), DormDistricts.StuttgartMitte.toString());

            Dorm allmandring1 = new Dorm("Allmandring I",48.745091, 9.101267, CityEnum.Stuttgart.toString(), DormDistricts.StuttgartVaihingen.toString());
            Dorm allmandring2 = new Dorm("Allmandring II",48.744399, 9.097331, CityEnum.Stuttgart.toString(), DormDistricts.StuttgartVaihingen.toString());
            Dorm allmandring3 = new Dorm("Allmandring III",48.744202, 9.095966, CityEnum.Stuttgart.toString(), DormDistricts.StuttgartVaihingen.toString());
            Dorm allmandring4 = new Dorm("Allmandring IV",48.743459, 9.095129, CityEnum.Stuttgart.toString(), DormDistricts.StuttgartVaihingen.toString());
            Dorm bauhäusle = new Dorm("Bauhäusle",48.743527, 9.097635, CityEnum.Stuttgart.toString(), DormDistricts.StuttgartVaihingen.toString());
            Dorm filderbahnplatz = new Dorm("Filderbahnplatz",48.730925, 9.148441, CityEnum.Stuttgart.toString(), DormDistricts.StuttgartVaihingen.toString());
            Dorm straußäcker2 = new Dorm("Straußi II",48.742248, 9.102229, CityEnum.Stuttgart.toString(), DormDistricts.StuttgartVaihingen.toString());
            Dorm straußäcker3 = new Dorm("Straußi III",48.742253, 9.102226, CityEnum.Stuttgart.toString(), DormDistricts.StuttgartVaihingen.toString());

            Dorm boardinghausEsslingen = new Dorm("Boardinghaus Esslingen",48.741887, 9.289050, CityEnum.Esslingen.toString());
            Dorm fabrikstraße = new Dorm("Fabrikstraße",48.738291, 9.307852, CityEnum.Esslingen.toString());
            Dorm geschwisterScholl = new Dorm("Geschwister Scholl",48.747223, 9.325982, CityEnum.Esslingen.toString());
            Dorm goerdelerweg = new Dorm("Goerdelerweg",48.746854, 9.326301, CityEnum.Esslingen.toString());
            Dorm rossneckar1 = new Dorm("Rossneckar I",48.741743, 9.290082, CityEnum.Esslingen.toString());
            Dorm rossneckar2 = new Dorm("Rossneckar II",48.741894, 9.289018, CityEnum.Esslingen.toString());

            Dorm studentendorfGöppingen = new Dorm("Studentendorf Göppingen",48.695202, 9.674639, CityEnum.Göppingen.toString());

            Dorm oFDWohnturm = new Dorm("OFD Wohnturm",48.889811, 9.196151, CityEnum.Ludwigsburg.toString());
            Dorm studentendorfLudwigsburg = new Dorm("Studentendorf",48.911245, 9.179221, CityEnum.Ludwigsburg.toString());
            Dorm wohnhausDerFinanzen = new Dorm("Wohnhaus der Finanzen",48.910570, 9.178520, CityEnum.Ludwigsburg.toString());

            Dorm felsennelkenanger = new Dorm("Felsennelkenanger",48.209765, 11.564023, CityEnum.München.toString(), DormDistricts.MünchenNord.toString());

            dormService.addDorm(alexanderstraße);
            dormService.addDorm(annaHerrigelHaus);
            dormService.addDorm(birkenwaldstraße);
            dormService.addDorm(bordinghaus);
            dormService.addDorm(brückenstraße);
            dormService.addDorm(heilmannstraße1);
            dormService.addDorm(heilmannstraße2);
            dormService.addDorm(inDerAu);
            dormService.addDorm(johannesstraße);
            dormService.addDorm(kernerstraße);
            dormService.addDorm(landhausstraße);
            dormService.addDorm(maxKade);
            dormService.addDorm(neckarstraße);
            dormService.addDorm(rieckestraße);
            dormService.addDorm(rosensteinstraße);
            dormService.addDorm(theodorHeuss);
            dormService.addDorm(wiederholdstraße);
            dormService.addDorm(wohnarealStuttgartRot);

            dormService.addDorm(allmandring1);
            dormService.addDorm(allmandring2);
            dormService.addDorm(allmandring3);
            dormService.addDorm(allmandring4);
            dormService.addDorm(bauhäusle);
            dormService.addDorm(filderbahnplatz);
            dormService.addDorm(straußäcker2);
            dormService.addDorm(straußäcker3);

            dormService.addDorm(boardinghausEsslingen);
            dormService.addDorm(fabrikstraße);
            dormService.addDorm(geschwisterScholl);
            dormService.addDorm(goerdelerweg);
            dormService.addDorm(rossneckar1);
            dormService.addDorm(rossneckar2);

            dormService.addDorm(studentendorfGöppingen);

            dormService.addDorm(oFDWohnturm);
            dormService.addDorm(studentendorfLudwigsburg);
            dormService.addDorm(wohnhausDerFinanzen);

            dormService.addDorm(felsennelkenanger);
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