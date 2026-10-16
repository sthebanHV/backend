package com.hotel.service;

import java.util.UUID;
import org.springframework.stereotype.Service;

// Issues the digital key used at check-in.
@Service
public class AccessService {

  public String generateKey() {
    return UUID.randomUUID().toString();
  }
}
