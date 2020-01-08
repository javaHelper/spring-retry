package com.example.endpoints;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.domain.LakeProfile;
import com.example.repository.LakeProfileRepository;

@RestController
@RequestMapping("/lake-profile")
public class Endpoint {

    @Autowired
    LakeProfileRepository lakeProfileRepository;

    @GetMapping(value = "/{profileId}")
    public ResponseEntity<LakeProfile> getLakeProfile(@PathVariable Long profileId) {
        Optional<LakeProfile> lakeProfile = lakeProfileRepository.findById(profileId);
        if (!lakeProfile.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(lakeProfile.get(), HttpStatus.OK);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LakeProfile> addLakeProfile(@RequestBody LakeProfile lakeProfile) throws InterruptedException {
        Thread.sleep(2000); // This simulates real-world database latency
        
        LakeProfile savedProfile = lakeProfileRepository.save(lakeProfile);
        return new ResponseEntity<>(savedProfile, HttpStatus.ACCEPTED);
    }
}
