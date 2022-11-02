package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.time.LocalDateTime;


@Api(description = "HelpRequest")
@RequestMapping("/api/helprequest")
@RestController
@Slf4j
public class HelpRequestController extends ApiController {

    @Autowired
    HelpRequestRepository helpRequestRepository;

    @ApiOperation(value = "Request Help")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<HelpRequest> allRequests() {
        Iterable<HelpRequest> request = helpRequestRepository.findAll();
        return request;
    }

    @ApiOperation(value = "Get a single help request")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public HelpRequest getById(
            @ApiParam("id") @RequestParam Long id) {
        HelpRequest helpRequested = helpRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, id));

        return helpRequested;
    }

    @ApiOperation(value = "Create a new help request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public HelpRequest postRequest(
        @ApiParam("requesterEmail") @RequestParam String requesterEmail,
        @ApiParam("teamId") @RequestParam String teamId,
        @ApiParam("tableOrBreakoutRoom") @RequestParam String tableOrBreakoutRoom,
        @ApiParam("requestTime") @RequestParam LocalDateTime requestTime,
        @ApiParam("explanation") @RequestParam String explanation,
        @ApiParam("solved") @RequestParam boolean solved
        )
        {

        HelpRequest requests = new HelpRequest();
        requests.setRequesterEmail(requesterEmail);
        requests.setTeamId(teamId);
        requests.setTableOrBreakoutRoom(tableOrBreakoutRoom);
        requests.setRequestTime(requestTime);
        requests.setExplanation(explanation);
        requests.setSolved(solved);

        HelpRequest savedRequests = helpRequestRepository.save(requests);

        return savedRequests;
    }

    @ApiOperation(value = "Delete a HelpRequest")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteRequest(
            @ApiParam("id") @RequestParam Long id) {
        HelpRequest helpRequested = helpRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, id));

        helpRequestRepository.delete(helpRequested);
        return genericMessage("HelpRequest with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public HelpRequest updateRequest(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid HelpRequest incoming) {

        HelpRequest helpRequested = helpRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, id));

        helpRequested.setRequesterEmail(incoming.getRequesterEmail());  
        helpRequested.setTeamId(incoming.getTeamId());
        helpRequested.setTableOrBreakoutRoom(incoming.getTableOrBreakoutRoom());
        helpRequested.setRequestTime(incoming.getRequestTime());
        helpRequested.setExplanation(incoming.getExplanation());
        helpRequested.setSolved(incoming.getSolved());

        helpRequestRepository.save(helpRequested);

        return helpRequested;
    }
}
