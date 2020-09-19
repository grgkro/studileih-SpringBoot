package com.example.studileih.Service;

import com.example.studileih.Dto.ProductDto;

import com.example.studileih.Dto.UserDto;
import com.example.studileih.Entity.Product;
import com.example.studileih.Entity.User;
import com.example.studileih.Repository.ProductRepository;

import org.json.simple.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ModelMapper modelMapper;

    public void saveOrUpdateProduct(Product product) {
        productRepository.save(product);
    }

    public ProductDto getProductById(Long id) {
        return convertToDto(productRepository.findById(id).get());
    }

    public Product getProductEntityById(Long id) {
        return productRepository.findById(id).get();
    }

    public List<ProductDto> getProductsByDorm(String dorm) {
        List<Product> products = productRepository.findAll();
        if (dorm != null) {
            products = products.stream().filter(product -> {
                if (product.getDorm() != null) {
                    if (product.getDorm().equals(dorm)) {
                        return true;
                    }
                }
                return false;
            }).collect(Collectors.toList());
        }
        return convertListToDtos(products);
    }


    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public List<ProductDto> listAllProducts() {
        try {
            List<Product> products = new ArrayList<>();
            productRepository.findAll().forEach(products::add);  // products::add ist gleich wie: products.add(product)
            return convertListToDtos(products);
        } catch (NullPointerException e) {
            System.out.println(e);
            return null;
        }

    }

    public Date transformStringToDate(String stringDate) {
        //transform LocalDate to date: https://beginnersbook.com/2017/10/java-convert-localdate-to-date/
        // 1. get default time zone TODO: Get the actual user time zone from angular!
        ZoneId defaultZoneId = ZoneId.systemDefault();
        Date date = null;
        LocalDate localStartDay = null;
        if (stringDate != null) {
            int positionDot = stringDate.indexOf(".");
            if (positionDot != -1) {
                stringDate = stringDate.substring(0, positionDot);  // if the user didnt change the default start date, the startdate will come in format 2123123123.23 or 2123123123.232 contain milliseconds cuts of the milliseconds (we only need seconds)
            }
            Instant instantStart = Instant.ofEpochSecond(Long.parseLong(stringDate));
            localStartDay = instantStart.atZone(ZoneId.systemDefault()).toLocalDate();   // we only need the Day of the year, not the hours + minutes -> localDate() instead of LocalDateTime()
            //2. local date + atStartOfDay() + default time zone + toInstant() = Date
            date = Date.from(localStartDay.atStartOfDay(defaultZoneId).toInstant());
        }
        return date;
    }

//    public Product updateProduct(Product newProduct, Long id) {
//        Product oldProduct = productRepository.findById(id).get();
//
//        if(oldProduct.isAvailable()){
//            oldProduct.setDescription(newProduct.getDescription());
//            oldProduct.setTitle(newProduct.getTitle());
//            oldProduct.setType(newProduct.getType());
//            oldProduct.setPrice(newProduct.getPrice());
//            oldProduct.setViews(newProduct.getViews());
//            oldProduct.setAvailable(newProduct.isAvailable());
//            oldProduct.setPicPaths(newProduct.getPicPaths());
//
//            productRepository.save(oldProduct);
//            System.out.println(oldProduct);
//            return oldProduct;
//        }
//
//        return null;
//    }

    private List<ProductDto> convertListToDtos(List<Product> products) {
        return products.stream()                 // List<Product> muss zu List<ProductDto> konvertiert werden. Hier tun wir zuerst die List<Product> in einen Stream umwandeln
                .map(this::convertToDto)            // Dann jedes Product ausm Stream herausnehmen und zu ProductDto umwandeln
                .collect(Collectors.toList());      // und dann den neuen Stream als List<ProductDto> einsammeln.
    }

    /**
     * Converts a Product to a ProductDto. The createdAt and updatedAt Dates are converted to simple Strings, because Date is Java specific and can't be send to Angular.
     *
     * @param product
     * @return productDto
     */
    private ProductDto convertToDto(Product product) {
        ProductDto productDto = modelMapper.map(product, ProductDto.class);
        // falls ein Product ein CreatedAt oder UpdatedAt Value hat, muss der zu einem String konvertiert werden. Falls nicht darf das Date nicht konvertiert werden, sonst gibts NullPointerExceptions.
        if (product.getCreatedAt() != null)
            productDto.setCreatedAt(product.getCreatedAt(), ZonedDateTime.now(ZoneId.systemDefault()).toString()); //konvertiert Date zu String -> einfachhaltshaber nimmts immer die Zeitzone des Servers (ZoneId.systemdefault), vielleicht irgendwann mal durch die Zeitzone des Nutzers ersetzen.
        if (product.getUpdatedAt() != null)
            productDto.setUpdatedAt(product.getUpdatedAt(), ZonedDateTime.now(ZoneId.systemDefault()).toString());
        return productDto;
    }

    public ResponseEntity addProduct(String description, String title, String category, Principal user, double price, boolean isBeerOk, String startDate, String endDate, String pickUpTime, String returnTime, MultipartFile[] imageFiles) {
        Date startDay = transformStringToDate(startDate);
        Date endDay = transformStringToDate(endDate);

        // first we get the user who added the product
        User productOwner = userService.getActiveUserByName(user.getName());
        Product product = null;
        if (productOwner.getDorm() != null) {  //the dorm == null when the user added a new dorm at registration and has no dorm yet. His products get added but not shown to other users until he has a dorm.
            // then we create the product
            product = new ProductBuilder().withTitle(title).withDescription(description).withCategory(category)
                    .withPrice(price).withIsBeerOk(isBeerOk).withStartDay(startDay).withEndDay(endDay).withUser(productOwner)
                    .withPickUpTime(pickUpTime).withReturnTime(returnTime).withAvailable(true).withDorm(productOwner.getDorm().getName()).withCity(productOwner.getDorm().getCity()).build();

        } else {
            product = new ProductBuilder().withTitle(title).withDescription(description).withCategory(category)
                    .withPrice(price).withIsBeerOk(isBeerOk).withStartDay(startDay).withEndDay(endDay).withUser(productOwner)
                    .withPickUpTime(pickUpTime).withReturnTime(returnTime).withAvailable(true).build();
        }

        // save the product
        saveOrUpdateProduct(product);
        // if there were product pics uploaded, we also save them
        saveImageFilesToProduct(imageFiles, product);
        //TODO: We need more checks here
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("response", "Produkt " + product.getTitle() + " erfolgreich angelegt.");
        jsonObject.put("productId", product.getId());
        return ResponseEntity.status(HttpStatus.OK).body(jsonObject);
    }

    public ResponseEntity editProduct(Long id, String description, String title, String category, double price, boolean isBeerOk, String startDate, String endDate, String pickUpTime, String returnTime, MultipartFile[] imageFiles) {
        Date startDay = transformStringToDate(startDate);
        Date endDay = transformStringToDate(endDate);

        if (id != null) {
            // get Product from DB and edit it
            Product product = productRepository.findById(id).get();
            product.setTitle(title);
            product.setDescription(description);
            if (category != null) {
                product.setCategory(category);
            }
            product.setPrice(price);
            product.setBeerOk(isBeerOk);
            product.setStartDay(startDay);
            product.setEndDay(endDay);
            product.setPickUpTime(pickUpTime);
            product.setReturnTime(returnTime);
            product.setAvailable(true);
            // save the product
            saveOrUpdateProduct(product);
            // if there were product pics uploaded, we also save them
            Product finalProduct = product;   // variable in Lambda Expression should be final, so we have to copy the product.
            saveImageFilesToProduct(imageFiles, finalProduct);
            //TODO: We need more checks here
            return ResponseEntity.status(HttpStatus.OK).body("Produkt erfolgreich editiert.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Zum Editieren muss eine Produkt Id angegben werden.");
        }


    }

    private ResponseEntity<String> saveImageFilesToProduct(MultipartFile[] imageFiles, Product product) {
        ResponseEntity responseEntity = ResponseEntity.status(HttpStatus.OK).body("Deine Produktfotos wurden gespeichert.");
        for (MultipartFile file : imageFiles) {
            ResponseEntity response = imageService.saveProductPic(file, product);
            if (response.getStatusCodeValue() != 200) {
                responseEntity = response;
            }
        }
        return responseEntity;
    }

    public ResponseEntity validateInput(Long productId,
                                        String description,
                                        String title,
                                        String category,
                                        Principal userDetails,
                                        double price,
                                        String startDate,
                                        String endDate,
                                        String pickUpTime,
                                        String returnTime,
                                        MultipartFile[] imageFiles) {

        String allowedRegex = "[a-zA-Z0-9.,_äöüÄÖÜß \\-+]*";
        if (startDate != null) {
            startDate = startDate.substring(0, startDate.indexOf("."));    //the date comes from the FE in format: "1598439940.666" or "1598439940.66", so we have to remove the "." and everything after.
        }

        if (productId != null) {
            if (!productRepository.existsById(productId)) {
                return new ResponseEntity("Unerwarteter Datenbankfehler: Das Produkt mit Id " + productId + " existiert nicht.", HttpStatus.BAD_REQUEST);
            }
        }
        if (userDetails != null) {
            if (userService.getActiveUserByName(userDetails.getName()) == null) {
                return new ResponseEntity("Unerwarteter Datenbankfehler (Principal existiert nicht).", HttpStatus.BAD_REQUEST);
            } else if (productId != null) {
                Boolean userOwnsProduct = false;
                for (Product product : userService.getActiveUserByName(userDetails.getName()).getProducts()) {
                    if (product.getId() == productId) userOwnsProduct = true;
                }
                if (!userOwnsProduct) {
                    return new ResponseEntity("Nur der Ersteller kann das Produkt bearbeiten.", HttpStatus.BAD_REQUEST);
                }
            }
        }
        if (description == null || description == "" || description.trim().isEmpty()) {
            return new ResponseEntity("Produktbeschreibung darf nicht leer sein.", HttpStatus.BAD_REQUEST);
        } else {
            if (!description.matches(allowedRegex)) {
                return new ResponseEntity("Ungültiges Zeichen in Produktbeschreibung eingegeben.", HttpStatus.BAD_REQUEST);
            }
        }
        if (title == null || title.isEmpty() || title.trim().isEmpty()) {
            return new ResponseEntity("Bitte einen knackigen Titel eingeben.", HttpStatus.BAD_REQUEST);
        } else {
            if (!title.matches(allowedRegex)) {
                return new ResponseEntity("Ungültiges Zeichen im knackigen Titel eingegeben.", HttpStatus.BAD_REQUEST);
            }
        }
        if (category != null) {
            if (category.isEmpty() || category.trim().isEmpty()) {
                return new ResponseEntity("Ungültiges Zeichen in Kategorie eingegeben.", HttpStatus.BAD_REQUEST);
            } else if (!category.matches(allowedRegex)) {
                return new ResponseEntity("Ungültiges Zeichen in Kategorie eingegeben.", HttpStatus.BAD_REQUEST);
            }
        }
        if (price < 0) {
            return new ResponseEntity("Preis darf nicht negativ sein.", HttpStatus.BAD_REQUEST);
        } else if (price > 100) {
            return new ResponseEntity("Preis darf nicht größer 100 € sein.", HttpStatus.BAD_REQUEST);
        }
        if (startDate != null) {
            Long yesterday = (System.currentTimeMillis() - 86400000) / 1000;
            if (Long.parseLong(startDate) < yesterday) {
                return new ResponseEntity("Anfangsdatum darf nicht in der Vergangenheit liegen.", HttpStatus.BAD_REQUEST);
            }
        }
        if (pickUpTime != null) {
            if (startDate == null) {
                return new ResponseEntity("Wenn bei \"Abholbar ab\" eine Uhrzeit eingegeben wird, muss auch ein Anfangsdatum eingegeben werden.", HttpStatus.BAD_REQUEST);
            } else {
                Long today = (System.currentTimeMillis()) / 1000;
                Long hh_mm_InMilliseconds = convertHoursMinutesInMilliseconds(pickUpTime);
                if ((Long.parseLong(startDate) + (hh_mm_InMilliseconds / 1000)) < today) {
                    return new ResponseEntity("\"Abholbar ab\" darf nicht in der Vergangenheit liegen.", HttpStatus.BAD_REQUEST);
                }
            }
        }
        if (endDate != null) {
            if (startDate == null) {
                return new ResponseEntity("Wenn ein Enddatum eingegeben wird, muss auch ein Anfangsdatum eingegeben werden.", HttpStatus.BAD_REQUEST);
            } else if (Long.parseLong(endDate) < Long.parseLong(startDate)) {
                //sollte nur auftreten können, wenn der Request vom Nutzer in Konsole manipuliert wird.
                return new ResponseEntity("Enddatum darf nicht vor Anfangsdatum liegen.", HttpStatus.BAD_REQUEST);
            }
            if (pickUpTime != null && returnTime != null) {
                if (Long.parseLong(endDate) == Long.parseLong(startDate) && convertHoursMinutesInMilliseconds(pickUpTime) > convertHoursMinutesInMilliseconds(returnTime)) {
                    return new ResponseEntity("Bei gleichem Anfang- und Enddatum darf die Rückgabezeit nicht vor der frühesten Abholzeit liegen.", HttpStatus.BAD_REQUEST);
                }
            }
        }
        if (imageFiles != null) {
            for (MultipartFile file : imageFiles) {
                if (!imageService.checkContentType(file)) {
                    return new ResponseEntity("Es können nur Bilddateien hochgeladen werden.", HttpStatus.BAD_REQUEST);
                }
            }
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    public Long convertHoursMinutesInMilliseconds(String time) {
        String[] split = time.split(":");
        Long hours = Long.parseLong(split[0]);
        Long mins = Long.parseLong(split[1]);
        return hours * 3_600_000 + mins * 60_000;
    }

}
