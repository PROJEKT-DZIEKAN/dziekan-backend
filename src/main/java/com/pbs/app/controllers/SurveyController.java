package com.pbs.app.controllers;

import com.pbs.app.dto.OptionResponse;
import com.pbs.app.dto.QuestionResponse;
import com.pbs.app.dto.SurveyRequest;
import com.pbs.app.dto.SurveyResponse;
import com.pbs.app.models.*;
import com.pbs.app.services.SurveyService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/surveys")
public class SurveyController {

    private final SurveyService surveyService;

    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @PostMapping
    public ResponseEntity<Survey> createSurvey(@RequestBody SurveyRequest surveyRequest) {
        Survey created = surveyService.createSurvey(surveyRequest);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<Survey>> getAllSurveys() {
        return ResponseEntity.ok(surveyService.getAllSurveys());
    }

    @GetMapping("/{surveyId}")
    public ResponseEntity<SurveyResponse> getSurvey(@PathVariable Long surveyId) {
        return ResponseEntity.ok(surveyService.getSurvey(surveyId));
    }

    @PutMapping("/{surveyId}")
    public ResponseEntity<SurveyResponse> updateSurvey(
            @PathVariable Long surveyId,
            @RequestBody SurveyRequest surveyRequest
    ) {
        Survey updated = surveyService.updateSurvey(surveyId, surveyRequest);

        SurveyResponse response = new SurveyResponse();
        response.setId(updated.getId());
        response.setTitle(updated.getTitle());
        response.setDescription(updated.getDescription());
        response.setCreatedAt(updated.getCreatedAt());

        List<QuestionResponse> questions = updated.getQuestions().stream().map(q -> {
            QuestionResponse qr = new QuestionResponse();
            qr.setId(q.getId());
            qr.setText(q.getText());
            qr.setType(q.getType());

            List<OptionResponse> options = q.getSurveyOptions().stream().map(o -> {
                OptionResponse or = new OptionResponse();
                or.setId(o.getId());
                or.setText(o.getText());
                return or;
            }).toList();

            qr.setSurveyOptions(options);
            return qr;
        }).toList();

        response.setQuestions(questions);

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{surveyId}")
    public ResponseEntity<Void> deleteSurvey(@PathVariable Long surveyId) {
        surveyService.deleteSurvey(surveyId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{surveyId}/questions")
    public ResponseEntity<List<SurveyQuestion>> getQuestionsBySurvey(@PathVariable Long surveyId) {
        return ResponseEntity.ok(surveyService.getQuestionsBySurvey(surveyId));
    }

    @GetMapping("/{surveyId}/questions/{questionId}")
    public ResponseEntity<SurveyQuestion> getQuestion(
            @PathVariable Long surveyId,
            @PathVariable Long questionId
    ) {
        return ResponseEntity.ok(surveyService.getQuestion(questionId));
    }

    @PutMapping("/{surveyId}/questions/{questionId}")
    public ResponseEntity<SurveyQuestion> updateQuestion(
            @PathVariable Long surveyId,
            @PathVariable Long questionId,
            @RequestBody SurveyQuestion question
    ) {
        return ResponseEntity.ok(surveyService.updateQuestion(questionId, question));
    }

    @DeleteMapping("/{surveyId}/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long surveyId,
            @PathVariable Long questionId
    ) {
        surveyService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{surveyId}/questions/{questionId}/options")
    public ResponseEntity<SurveyOption> addOption(
            @PathVariable Long surveyId,
            @PathVariable Long questionId,
            @RequestBody SurveyOption option
    ) {
        SurveyOption created = surveyService.addOption(questionId, option);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{surveyId}/questions/{questionId}/options")
    public ResponseEntity<List<SurveyOption>> getOptionsByQuestion(
            @PathVariable Long surveyId,
            @PathVariable Long questionId
    ) {
        return ResponseEntity.ok(surveyService.getOptionsByQuestion(questionId));
    }

    @GetMapping("/{surveyId}/questions/{questionId}/options/{optionId}")
    public ResponseEntity<SurveyOption> getOption(
            @PathVariable Long surveyId,
            @PathVariable Long questionId,
            @PathVariable Long optionId
    ) {
        return ResponseEntity.ok(surveyService.getOption(optionId));
    }

    @PutMapping("/{surveyId}/questions/{questionId}/options/{optionId}")
    public ResponseEntity<SurveyOption> updateOption(
            @PathVariable Long surveyId,
            @PathVariable Long questionId,
            @PathVariable Long optionId,
            @RequestBody SurveyOption option
    ) {
        return ResponseEntity.ok(surveyService.updateOption(optionId, option));
    }

    @DeleteMapping("/{surveyId}/questions/{questionId}/options/{optionId}")
    public ResponseEntity<Void> deleteOption(
            @PathVariable Long surveyId,
            @PathVariable Long questionId,
            @PathVariable Long optionId
    ) {
        surveyService.deleteOption(optionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{surveyId}/questions/{questionId}/options/{optionId}/answers")
    public ResponseEntity<SurveyUserAnswer> recordAnswer(
            @PathVariable Long surveyId,
            @PathVariable Long questionId,
            @PathVariable Long optionId,
            @RequestParam Long userId
    ) {
        SurveyUserAnswer answer = surveyService.recordAnswer(userId, questionId, optionId);
        return ResponseEntity.ok(answer);
    }

    @GetMapping("/{surveyId}/questions/{questionId}/answers")
    public ResponseEntity<List<SurveyUserAnswer>> getAnswersByQuestion(
            @PathVariable Long surveyId,
            @PathVariable Long questionId
    ) {
        return ResponseEntity.ok(surveyService.getAnswersByQuestion(questionId));
    }

    @GetMapping("/{surveyId}/answers")
    public ResponseEntity<List<SurveyUserAnswer>> getAnswersByUser(
            @PathVariable Long surveyId,
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(surveyService.getAnswersByUser(userId));
    }
}
