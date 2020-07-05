package com.example.studileih.Service;

import com.example.studileih.Dto.DormDto;
import com.example.studileih.Entity.Dorm;
import com.example.studileih.Entity.Dorm;
import com.example.studileih.Repository.DormRepository;
import com.example.studileih.Repository.DormRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DormService {

    @Autowired
    private DormRepository dormRepository;

    @Autowired
    private ModelMapper modelMapper;  //modelMapper konvertiert Entities in DTOs (modelMapper Dependency muss in pom.xml drin sein)

    public void saveOrUpdateDorm(Dorm dorm) {
        dormRepository.save(dorm);
    }

    public Optional<Dorm> getDormById(Long id) {
        return dormRepository.findById(id);
    }

    public void deleteDorm(Long id) {
        dormRepository.deleteById(id);
    }

    public List<DormDto> listAllDorms() {
        List<Dorm> dorms = new ArrayList<>();
        dormRepository.findAll().forEach(dorms::add);  // dorms::add ist gleich wie: dorms.add(dorm)
        return dorms.stream()                 // List<Product> muss zu List<ProductDto> konvertiert werden. Hier tun wir zuerst die List<Product> in einen Stream umwandeln
                .map(this::convertToDto)            // Dann jedes Product ausm Stream herausnehmen und zu ProductDto umwandeln
                .collect(Collectors.toList());      // und dann den neuen Stream als List<ProductDto> einsammeln.;
    }

    public boolean addDorm(Dorm dorm) {
        dormRepository.save(dorm);
        return true;
    }

    /**
     * Converts a Dorm to a DormDto. 
     * @param dorm
     * @return dormDto
     */
    private DormDto convertToDto(Dorm dorm) {
        DormDto dormDto = modelMapper.map(dorm, DormDto.class);
        return dormDto;
    }

}