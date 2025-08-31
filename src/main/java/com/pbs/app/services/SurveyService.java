package com.pbs.app.services;

import com.pbs.app.dto.OptionRequest;
import com.pbs.app.dto.QuestionRequest;
import com.pbs.app.dto.SurveyRequest;
import com.pbs.app.models.*;
import com.pbs.app.models.SurveyUserAnswerId;
import com.pbs.app.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class SurveyService {

    private final SurveyRepository surveyRepo;
    private final SurveyQuestionRepository questionRepo;
    private final SurveyOptionRepository optionRepo;
    private final SurveyUserAnswerRepository answerRepo;

    public SurveyService(SurveyRepository surveyRepo,
                         SurveyQuestionRepository questionRepo,
                         SurveyOptionRepository optionRepo,
                         SurveyUserAnswerRepository answerRepo) {
        this.surveyRepo = surveyRepo;
        this.questionRepo = questionRepo;
        this.optionRepo = optionRepo;
        this.answerRepo = answerRepo;
    }

    public Survey createSurvey(SurveyRequest req) {
        Survey survey = new Survey();
        survey.setTitle(req.getTitle());
        survey.setDescription(req.getDescription());

        if (req.getQuestions() != null) {
            for (QuestionRequest qReq : req.getQuestions()) {
                SurveyQuestion q = new SurveyQuestion();
                q.setText(qReq.getText());
                q.setType(qReq.getType());
                q.setSurvey(survey);

                if (qReq.getSurveyOptions() != null) {
                    for (OptionRequest oReq : qReq.getSurveyOptions()) {
                        SurveyOption o = new SurveyOption();
                        o.setText(oReq.getText());
                        o.setQuestion(q);
                        q.getSurveyOptions().add(o);
                    }
                }

                survey.getQuestions().add(q);
            }
        }

        return surveyRepo.save(survey);
    }

    public Survey getSurvey(Long surveyId) {
        return surveyRepo.findById(surveyId)
            .orElseThrow(() -> new EntityNotFoundException("Survey not found: " + surveyId));
    }

    public List<Survey> getAllSurveys() {
        return surveyRepo.findAll();
    }

    public Survey updateSurvey(Long surveyId, Survey updated) {
        Survey existing = getSurvey(surveyId);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        return surveyRepo.save(existing);
    }

    public void deleteSurvey(Long surveyId) {
        if (!surveyRepo.existsById(surveyId)) {
            throw new EntityNotFoundException("Survey not found: " + surveyId);
        }
        surveyRepo.deleteById(surveyId);
    }

    public SurveyQuestion addQuestion(Long surveyId, SurveyQuestion question) {
        Survey survey = getSurvey(surveyId);
        question.setSurvey(survey);
        return questionRepo.save(question);
    }

    public SurveyQuestion getQuestion(Long questionId) {
        return questionRepo.findById(questionId)
            .orElseThrow(() -> new EntityNotFoundException("Question not found: " + questionId));
    }

    public List<SurveyQuestion> getQuestionsBySurvey(Long surveyId) {
        return questionRepo.findBySurveyId(surveyId);
    }

    public SurveyQuestion updateQuestion(Long questionId, SurveyQuestion updated) {
        SurveyQuestion existing = getQuestion(questionId);
        existing.setText(updated.getText());
        existing.setType(updated.getType());
        return questionRepo.save(existing);
    }

    public void deleteQuestion(Long questionId) {
        if (!questionRepo.existsById(questionId)) {
            throw new EntityNotFoundException("Question not found: " + questionId);
        }
        questionRepo.deleteById(questionId);
    }

    public SurveyOption addOption(Long questionId, SurveyOption option) {
        SurveyQuestion question = getQuestion(questionId);
        option.setQuestion(question);
        return optionRepo.save(option);
    }

    public SurveyOption getOption(Long optionId) {
        return optionRepo.findById(optionId)
            .orElseThrow(() -> new EntityNotFoundException("Option not found: " + optionId));
    }

    public List<SurveyOption> getOptionsByQuestion(Long questionId) {
        return optionRepo.findByQuestionId(questionId);
    }

    public SurveyOption updateOption(Long optionId, SurveyOption updated) {
        SurveyOption existing = getOption(optionId);
        existing.setText(updated.getText());
        return optionRepo.save(existing);
    }

    public void deleteOption(Long optionId) {
        if (!optionRepo.existsById(optionId)) {
            throw new EntityNotFoundException("Option not found: " + optionId);
        }
        optionRepo.deleteById(optionId);
    }


    public SurveyUserAnswer recordAnswer(Long userId, Long questionId, Long optionId) {
        questionRepo.findById(questionId)
            .orElseThrow(() -> new EntityNotFoundException("question not found: " + questionId));
        optionRepo.findById(optionId)
            .orElseThrow(() -> new EntityNotFoundException("pption not found: " + optionId));

        SurveyUserAnswer answer = SurveyUserAnswer.builder()
            .userId(userId)
            .questionId(questionId)
            .surveyOptionId(optionId)
            .answeredAt(Instant.now())
            .build();
        return answerRepo.save(answer);
    }

    public List<SurveyUserAnswer> getAnswersByUser(Long userId) {
        return answerRepo.findByUserId(userId);
    }

    public List<SurveyUserAnswer> getAnswersByQuestion(Long questionId) {
        return answerRepo.findByQuestionId(questionId);
    }

}

