package com.itermit.springtest02.model.entity;

import com.itermit.springtest02.model.EGender;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "profile")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 80)
    private String name;

    @Size(max = 80)
    private String surname;

    private EGender gender;

    @Size(max = 50)
    private String city;

    @Size(max = 150)
    private String address;

    @Size(max = 100)
    private String company;

    @Size(max = 10)
    private String mobile;

    @Size(max = 10)
    private String tele;

    @Size(max = 50)
    private String website;

    @Size(max = 50)
    private String date;

    @Size(max = 100)
    private String avatar;

}
