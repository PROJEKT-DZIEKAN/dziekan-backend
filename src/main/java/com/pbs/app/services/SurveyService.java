package com.pbs.app.services;

import com.pbs.app.dto.*;
import com.pbs.app.models.*;
import com.pbs.app.models.SurveyUserAnswerId;
import com.pbs.app.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SurveyService {

    private final SurveyRepository surveyRepo;
    private final SurveyQuestionRepository questionRepo;
    private final SurveyOptionRepository optionRepo;
    private final SurveyUserAnswerRepository answerRepo;
    private final UserCompletedSurveyRepository completedSurveyRepository;
    private final UserRepository userRepository;

    public SurveyService(SurveyRepository surveyRepo,
                         SurveyQuestionRepository questionRepo,
                         SurveyOptionRepository optionRepo,
                         SurveyUserAnswerRepository answerRepo,
                         UserCompletedSurveyRepository completedSurveyRepository,
                            UserRepository userRepository
    ) {
        this.surveyRepo = surveyRepo;
        this.questionRepo = questionRepo;
        this.optionRepo = optionRepo;
        this.answerRepo = answerRepo;
        this.completedSurveyRepository = completedSurveyRepository;
        this.userRepository = userRepository;
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
    public Survey getSingleSurveyEntity(Long surveyId) {
        return surveyRepo.findById(surveyId)
                .orElseThrow(() -> new EntityNotFoundException("Survey not found: " + surveyId));
    }

    public SurveyResponse getSurvey(Long surveyId) {
        Survey survey = surveyRepo.findById(surveyId)
                .orElseThrow(() -> new EntityNotFoundException("Survey not found: " + surveyId));

        SurveyResponse response = new SurveyResponse();
        response.setId(survey.getId());
        response.setTitle(survey.getTitle());
        response.setDescription(survey.getDescription());
        response.setCreatedAt(survey.getCreatedAt());

        List<QuestionResponse> questions = survey.getQuestions().stream().map(q -> {
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
        return response;
    }

    public List<Survey> getAllSurveys() {
        return surveyRepo.findAllWithQuestionsAndOptions();
    }

    public Survey updateSurvey(Long surveyId, SurveyRequest req) {

        Survey existing = surveyRepo.findById(surveyId)
                .orElseThrow(() -> new EntityNotFoundException("Survey not found: " + surveyId));

        existing.setTitle(req.getTitle());
        existing.setDescription(req.getDescription());
        if (req.getQuestions() != null) {
            List<SurveyQuestion> existingQuestions = new ArrayList<>(existing.getQuestions());
            
            for (int i = 0; i < req.getQuestions().size(); i++) {
                QuestionRequest qReq = req.getQuestions().get(i);
                SurveyQuestion question;
                if (i < existingQuestions.size()) {
                    question = existingQuestions.get(i);
                    question.setText(qReq.getText());
                    question.setType(qReq.getType());
                } else {
                    question = new SurveyQuestion();
                    question.setText(qReq.getText());
                    question.setType(qReq.getType());
                    question.setSurvey(existing);
                    existing.getQuestions().add(question);
                }
                if (qReq.getSurveyOptions() != null) {
                    List<SurveyOption> existingOptions = new ArrayList<>(question.getSurveyOptions());
                    
                    for (int j = 0; j < qReq.getSurveyOptions().size(); j++) {
                        OptionRequest oReq = qReq.getSurveyOptions().get(j);
                        SurveyOption option;

                        if (j < existingOptions.size()) {
                            option = existingOptions.get(j);
                            option.setText(oReq.getText());
                        } else {
                            option = new SurveyOption();
                            option.setText(oReq.getText());
                            option.setQuestion(question);
                            question.getSurveyOptions().add(option);
                        }
                    }
                    if (qReq.getSurveyOptions().size() < existingOptions.size()) {
                        for (int k = qReq.getSurveyOptions().size(); k < existingOptions.size(); k++) {
                            SurveyOption optionToRemove = existingOptions.get(k);
                            if (!isOptionReferencedByAnswers(optionToRemove.getId())) {
                                question.getSurveyOptions().remove(optionToRemove);
                            }
                        }
                    }
                }
            }

            if (req.getQuestions().size() < existingQuestions.size()) {
                for (int i = req.getQuestions().size(); i < existingQuestions.size(); i++) {
                    SurveyQuestion questionToRemove = existingQuestions.get(i);
                    if (!isQuestionReferencedByAnswers(questionToRemove.getId())) {
                        existing.getQuestions().remove(questionToRemove);
                    }
                }
            }
        }

        return surveyRepo.save(existing);
    }
    
    private boolean isOptionReferencedByAnswers(Long optionId) {
        return answerRepo.existsBySurveyOptionId(optionId);
    }
    
    private boolean isQuestionReferencedByAnswers(Long questionId) {
        return answerRepo.existsByQuestionId(questionId);
    }


    public void deleteSurvey(Long surveyId) {
        Survey existing = surveyRepo.findById(surveyId)
                .orElseThrow(() -> new EntityNotFoundException("Survey not found: " + surveyId));

        surveyRepo.delete(existing);
    }

    public void submitUserAnswers(Long userId, Long surveyId, List<AnswerRequest> answers) {
        List<SurveyUserAnswer> userAnswers = new ArrayList<>();

        for (AnswerRequest a : answers) {
            for (Long optionId : a.getSelectedOptionIds()) {
                SurveyUserAnswer answer = SurveyUserAnswer.builder()
                        .userId(userId)
                        .questionId(a.getQuestionId())
                        .surveyOptionId(optionId)
                        .answeredAt(Instant.now())
                        .build();
                userAnswers.add(answer);
            }
        }

        answerRepo.saveAll(userAnswers);
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

    public List<Survey> getAvailableSurveysForUser(Long userId) {
        return surveyRepo.findAvailableSurveysForUser(userId);
    }

    @Transactional
    public void markSurveyAsCompleted(Long userId, Long surveyId) {
        if (completedSurveyRepository.existsByUserIdAndSurveyId(userId, surveyId)) {
            return;
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Survey survey = surveyRepo.findById(surveyId)
            .orElseThrow(() -> new IllegalArgumentException("Survey not found"));

        UserCompletedSurvey completed = UserCompletedSurvey.builder()
            .user(user)
            .survey(survey)
            .build();

        completedSurveyRepository.save(completed);
    }

    public boolean isSurveyCompletedByUser(Long userId, Long surveyId) {
        return completedSurveyRepository.existsByUserIdAndSurveyId(userId, surveyId);
    }

}

