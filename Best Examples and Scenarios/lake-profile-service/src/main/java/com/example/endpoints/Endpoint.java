package com.example.endpoints;

import java.net.SocketException;
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
import org.springframework.web.bind.annotation.RestController;

import com.example.domain.LakeProfile;
import com.example.repository.LakeProfileRepository;

@RestController
@RequestMapping("/lake-profile")
public class Endpoint {
	private int getLakeProfileRequestCount = 0;
	private int addLakeProfileRequestCount = 0;
	
    @Autowired
    LakeProfileRepository lakeProfileRepository;

    @GetMapping(value = "/{profileId}")
    public ResponseEntity<LakeProfile> getLakeProfile(@PathVariable Long profileId) throws SocketException, InterruptedException {
    	// Here we need to implement Spring Retry.....
    	// simulate a temporary socket exception caused by temporary server overload
    	if(getLakeProfileRequestCount < 2) {
    		getLakeProfileRequestCount++;
    		throw new SocketException();
    	}
    	
    	// simulate the timeout exception because delays lake profile client for 1s longer than 3s configured read timeout threashold
    	/*if(getLakeProfileRequestCount < 2) {
    		getLakeProfileRequestCount++;
    		Thread.sleep(4000);
    	}*/
    	
        Optional<LakeProfile> lakeProfile = lakeProfileRepository.findById(profileId);
        if (!lakeProfile.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        getLakeProfileRequestCount = 0;
        return new ResponseEntity<>(lakeProfile.get(), HttpStatus.OK);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LakeProfile> addLakeProfile(@RequestBody LakeProfile lakeProfile) throws InterruptedException, SocketException {
        
    	// simulate a temporary socket exception caused by temporary server overload
    	if(getLakeProfileRequestCount < 2) {
    		getLakeProfileRequestCount++;
    		throw new SocketException();
    	}
    	
    	Thread.sleep(2000); // This simulates real-world database latency
        
        LakeProfile savedProfile = lakeProfileRepository.save(lakeProfile);
        addLakeProfileRequestCount = 0;
        return new ResponseEntity<>(savedProfile, HttpStatus.ACCEPTED);
    }
}
