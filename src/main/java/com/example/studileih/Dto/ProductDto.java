package com.example.studileih.Dto;

import com.example.studileih.Entity.Product;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;


@Data
@NoArgsConstructor
public class ProductDto {

    @Autowired
    private ModelMapper modelMapper;

    private static final SimpleDateFormat dateFormat
            = new SimpleDateFormat("yyyy-MM-dd HH:mm");

//    @NotNull
    private Long id;
    private Long userId;
    private String description;
    private String title;
    private double price = 0;
    private int views = 0; //How often was the product viewed?
    private boolean available = true; //is it available?
    private String createdAt;
    private String updatedAt;
    private ArrayList<String> picPaths;
    private MultipartFile[] imageFiles;
    private String category;

    private boolean isBeerOk;  // when creating a new product, the user can chose that instead of a price, the renter can also give one beer.
    private String startDay;
    private String endDay;
    private String pickUpTime;
    private String returnTime;
    private String dorm;
    private String city;

    public Date getCreatedAtConverted(String timezone) throws ParseException {
        dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
        return dateFormat.parse(this.createdAt);
    }

    public void setCreatedAt(Date createdAt, String timezone) {
        dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
        this.createdAt = dateFormat.format(createdAt);
    }

    public Date getUpdatedAtConverted(String timezone) throws ParseException {
        dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
        return dateFormat.parse(this.updatedAt);
    }

    public void setUpdatedAt(Date updatedAt, String timezone) {
        dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
        this.updatedAt = dateFormat.format(updatedAt);
    }

    public static SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

}
