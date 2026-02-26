package com.example.expensestracker.model.entity;

import com.example.expensestracker.util.StringCryptoConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "token_blacklist")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenBlackList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, length = 1000)
    @Convert(converter = StringCryptoConverter.class)
    private String token;

    @Column(name = "expiry_date")
    private Date expiryDate;
}
