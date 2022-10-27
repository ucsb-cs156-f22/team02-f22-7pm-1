package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
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
@Api(description = "MenuItemReview")
@RequestMapping("/api/ucsbdates")
@RestController
@Slf4j

public class MenuItemReviewController extends ApiController {
    @Autowired
    MenuItemReviewRepository menuItemReviewRepository;

    @ApiOperation(value = "List all menu item reviews")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<MenuItemReview> allReviews() {
        Iterable<MenuItemReview> reviews = menuItemReviewRepository.findAll();
        return reviews;
    }


    @ApiOperation(value = "Get a single Review")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public MenuItemReview getById(
            @ApiParam("id") @RequestParam Long id) {
        MenuItemReview menuItemreview = menuItemReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

        return menuItemreview;
    }


    @ApiOperation(value = "Create a new item review")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public MenuItemReview postReview(
        @ApiParam("itemId") @RequestParam long itemId,
        @ApiParam("reviewerEmail") @RequestParam String reviewerEmail,
        @ApiParam("stars") @RequestParam int stars,
        @ApiParam("dateReviewed") @RequestParam LocalDateTime dateReviewed,
        @ApiParam("comments") @RequestParam String comments
        )
        {

        MenuItemReview review = new MenuItemReview();
        review.setItemId(itemId);
        review.setReviewerEmail(reviewerEmail);
        review.setStars(stars);
        review.setDateReviewed(dateReviewed);
        review.setComments(comments);


        MenuItemReview savedReview = menuItemReviewRepository.save(review);

        return savedReview;
    }



    @ApiOperation(value = "Delete a menu item review")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteReview(
            @ApiParam("code") @RequestParam long itemId) {
        MenuItemReview review = menuItemReviewRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, itemId));

        menuItemReviewRepository.delete(review);
        return genericMessage("Menu item review with id %s deleted".formatted(itemId));
    }



    @ApiOperation(value = "Update a single review")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public MenuItemReview updateReview(
            @ApiParam("itemId") @RequestParam long itemId,
            @RequestBody @Valid MenuItemReview incoming) {

        MenuItemReview review = menuItemReviewRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, itemId));

        
        review.setItemId(incoming.getItemId());
        review.setReviewerEmail(incoming.getReviewerEmail());
        review.setStars(incoming.getStars());
        review.setDateReviewed(incoming.getDateReviewed());
        review.setComments(incoming.getComments());


        menuItemReviewRepository.save(review);

        return review;
    }



}
