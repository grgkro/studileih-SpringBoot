package com.example.studileih.Dto;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Data
@NoArgsConstructor
public class UserDtoForEditing {
    private static final SimpleDateFormat dateFormat
            = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private int id;
    private String name;
    private String email;
    private String profilePic;
    private String createdAt;
    private String updatedAt;
    private Long dormId;
    private String city;
    private String token; //JWT login token of that user

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
}
