package com.lamgnoah.hustoj.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lamgnoah.hustoj.domain.ContestProblemSubmitInfo;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

@Entity
@Table(name = "ranking_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankingUser extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;
  @ManyToOne
  @JoinColumn(name = "contest_id")
  @NotFound(action = NotFoundAction.IGNORE)
  private Contest contest;

  private Integer acceptCount = 0;

  private Integer submitCount = 0;

  @Column(name = "submission_info" , columnDefinition = "json default ('{}')")
  @Convert(converter = SubmissionInfoConverter.class)
  private Map<Long , ContestProblemSubmitInfo>  submissionInfo = new HashMap<>();

  private Double score = 0.0;

  private Long time = 0L;

  private Boolean ranked = true;

  private Integer rankingNumber;

  public void increaseAcceptCount() {
    this.acceptCount++;
  }

  public void increaseSubmitCount() {
    this.submitCount++;
  }

  public void addTime(Long milliseconds) {
    this.time += milliseconds;
  }

  public void addScore(Double score) {
    this.score += score;
  }

  @Slf4j
  static class SubmissionInfoConverter implements AttributeConverter<Map<Long , ContestProblemSubmitInfo> , String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<Long, ContestProblemSubmitInfo> submissionInfo) {
      String submissionInfoJson = null;
      try {
        submissionInfoJson = objectMapper.writeValueAsString(submissionInfo);
      } catch (final JsonProcessingException e) {
        log.error("JSON writing error", e);
      }
      return submissionInfoJson;
    }

    @Override
    public Map<Long, ContestProblemSubmitInfo> convertToEntityAttribute(String submissionInfoJson) {
      Map<Long, ContestProblemSubmitInfo> submissionInfo = null;
      try {
        submissionInfo = objectMapper.readValue(submissionInfoJson, new TypeReference<HashMap<Long, ContestProblemSubmitInfo>>() {});
      } catch (final IOException e) {
        log.error("JSON reading error", e);
      }
      return submissionInfo;
    }
  }
}
