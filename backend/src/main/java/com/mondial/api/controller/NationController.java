package com.mondial.api.controller;

import com.mondial.api.dto.NationResponse;
import com.mondial.api.repository.NationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/nations")
public class NationController {

    private final NationRepository nationRepository;

    public NationController(NationRepository nationRepository) {
        this.nationRepository = nationRepository;
    }

    @GetMapping
    public ResponseEntity<List<NationResponse>> listNations() {
        List<NationResponse> nations = nationRepository.findAll()
                .stream()
                .map(n -> new NationResponse(n.getId(), n.getName(), n.getFlag()))
                .toList();
        return ResponseEntity.ok(nations);
    }
}

