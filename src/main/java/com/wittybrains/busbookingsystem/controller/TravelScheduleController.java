package com.wittybrains.busbookingsystem.controller;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.format.DateTimeParseException;

import com.wittybrains.busbookingsystem.dto.TravelScheduleDTO;
import com.wittybrains.busbookingsystem.model.Station;
import com.wittybrains.busbookingsystem.service.TravelScheduleService;

@RestController
@RequestMapping("/schedules")
public class TravelScheduleController {

    private static final Logger logger = LoggerFactory.getLogger(TravelScheduleController.class);

    @Autowired
    private TravelScheduleService travelScheduleService;

    @GetMapping("avalibility")
    public ResponseEntity<TravelScheduleResponseWrapper> getSchedules(
            @RequestParam(value = "sourceCode") String sourceCode,
            @RequestParam(value = "destinationCode") String destinationCode,
            @RequestParam("date") String date) {

        if (StringUtils.isEmpty(destinationCode.trim())) {
            TravelScheduleResponseWrapper response = new TravelScheduleResponseWrapper(
                    "Destination station code is null or empty", null);
            logger.warn(response.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (sourceCode == null || sourceCode.trim().isEmpty()) {
            TravelScheduleResponseWrapper response = new TravelScheduleResponseWrapper(
                    "Source station code is null or empty", null);
            logger.warn(response.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (sourceCode.equals(destinationCode)) {
            TravelScheduleResponseWrapper response = new TravelScheduleResponseWrapper(
                    "Source and destination station codes cannot be the same", null);
            logger.warn(response.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Station source = travelScheduleService.getStationByCode(sourceCode);
        Station destination = travelScheduleService.getStationByCode(destinationCode);

        try {
            LocalDate parsedDate = LocalDate.parse(date);
            List<TravelScheduleDTO> schedules = travelScheduleService.getAvailableSchedules(source, destination,
                    parsedDate);
            if (schedules.isEmpty()) {
                String message = "No bus is available between " + source.getName() + " and " + destination.getName()
                        + " on " + date;
                TravelScheduleResponseWrapper response = new TravelScheduleResponseWrapper(message, schedules);
                logger.info(response.getMessage());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            } else {
                TravelScheduleResponseWrapper response = new TravelScheduleResponseWrapper("", schedules);
                logger.info(response.getMessage());
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        } catch (DateTimeParseException ex) {
            TravelScheduleResponseWrapper response = new TravelScheduleResponseWrapper(
                    "Invalid date format. The correct format is ISO date format (yyyy-MM-dd)", null);
            logger.warn(response.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping
   
    public ResponseEntity<TravelScheduleResponseWrapper> createSchedule(
            @RequestBody TravelScheduleDTO travelScheduleDTO) throws ParseException {
        if (travelScheduleService.createSchedule(travelScheduleDTO)) {
            logger.info("Successfully created travel schedule: {}", travelScheduleDTO);
            TravelScheduleResponseWrapper response = new TravelScheduleResponseWrapper(
                    "Successfully created travel schedule", null);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } else {
            logger.error("Failed to create travel schedule: {}", travelScheduleDTO);
            TravelScheduleResponseWrapper response = new TravelScheduleResponseWrapper(
                    "Unable to create travel schedule", null);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
 }

